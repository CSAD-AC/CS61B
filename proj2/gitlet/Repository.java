package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import static gitlet.OperationHistory.*;

import static gitlet.Utils.*;
/** Represents a gitlet repository.
 *  @author 逐辰
 */
public class Repository {
    public static final File CWD = new File(System.getProperty("gitlet.cwd", System.getProperty("user.dir")));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File STAGING_AREA = join(GITLET_DIR, "index");
    public static final File IGNORE = join(CWD, ".ignore");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File OPERATION_HISTORY_FILE = join(GITLET_DIR, "operation_history");
    
    private static OperationHistory operationHistory = OperationHistory.load();
    
    /**
     * 读取并解析.ignore文件中的忽略规则
     * @return 包含所有忽略路径的Set集合
     */
    private static Set<String> readIgnoreFile() {
        Set<String> ignorePaths = new HashSet<>();
        if (IGNORE.exists()) {
            try {
                List<String> lines = Files.readAllLines(IGNORE.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        ignorePaths.add(line);
                    }
                }
            } catch (IOException e) {
                // 如果读取文件时出错，返回空的忽略列表
                System.err.println("警告：无法读取.ignore文件");
            }
        }
        return ignorePaths;
    }

    /**
     * 检查文件路径是否应该被忽略
     * @param filePath 要检查的文件路径
     * @param ignorePaths 忽略路径集合
     * @return 如果应该被忽略返回true，否则返回false
     */
    private static boolean shouldBeIgnored(String filePath, Set<String> ignorePaths) {
        // 检查是否明确匹配忽略列表中的路径
        if (ignorePaths.contains(filePath)) {
            return true;
        }
        
        // 检查是否匹配忽略列表中的目录（前缀匹配）
        for (String ignorePath : ignorePaths) {
            // 如果ignorePath以/结尾，只匹配目录
            if (ignorePath.endsWith("/")) {
                String dirPath = ignorePath.substring(0, ignorePath.length() - 1);
                // 精确匹配目录或者在目录下的文件/子目录
                if (filePath.equals(dirPath) || filePath.startsWith(dirPath + "/")) {
                    return true;
                }
            } else {
                // 对于不以/结尾的规则，精确匹配文件或目录，或者在指定目录下的内容
                if (filePath.equals(ignorePath) || filePath.startsWith(ignorePath + "/")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("一个Gitlet版本管理系统已经存在于当前文件夹");
            operationHistory.invalidOperation();
            System.exit(1);
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();

        writeObject(STAGING_AREA, new HashMap<String, String>());
        try {HEAD.createNewFile();} catch (IOException e) {throw new RuntimeException(e);}
        try {join(HEADS_DIR, "master").createNewFile();} catch (IOException e) {throw new RuntimeException(e);}
        Commit initial = new Commit(
                null,
                null,
                new Date(0L),
                "initial commit",
                new HashMap<>()
        );
        File initialCommitFile = join(OBJECTS_DIR, initial.getId());
        writeObject(initialCommitFile, initial);
        writeContents(HEAD, "ref: refs/heads/master\n");
        writeContents(join(HEADS_DIR, "master"), initial.getId());

        // 创建默认的.ignore文件，忽略.git、gitlet、gitlet目录
        writeContents(IGNORE, ".git/\n.gitlet/\ngitlet/");

    }
    // 读取暂存区
    //@SuppressWarnings("unchecked")
    private static Map<String, String> readStagingArea() {
        if (!STAGING_AREA.exists()) {
            return new HashMap<>();
        }
        return readObject(STAGING_AREA, HashMap.class);
    }

    // 保存暂存区
    private static void saveStagingArea(Map<String, String> stagingArea) {
        writeObject(STAGING_AREA, (Serializable) stagingArea);
    }
    //获取最新提交
    private static Commit getCurrentCommit() {
        String head = readContentsAsString(HEAD);
        String branchName = head.substring("ref: refs/heads/".length()).trim();
        String commitHash = readContentsAsString(join(HEADS_DIR, branchName));
        return readCommitFromObjects(commitHash);
    }
    //获取当前分支
    private static String getCurrentBranch() {
        return readContentsAsString(HEAD).substring("ref: refs/heads/".length()).trim();
    }
    public static void checkInGitlet() {
        if (!GITLET_DIR.exists()) {
            System.out.println("当前文件夹下Gitlet未初始化");
            operationHistory.invalidOperation();
            System.exit(1);
        }
    }
    public static void ignore(String fileName) {
        Set<String> ignorePaths = readIgnoreFile();
        if(fileName == null) {
            System.out.println("忽略规则如下");
            for(String ignore : ignorePaths) {
                System.out.println("- " + ignore);
            }
        } else {
            if(shouldBeIgnored(fileName, ignorePaths)) {
                System.out.println("文件 "+ fileName + "应该被忽略");
            } else {
                System.out.println("文件 "+ fileName + "不应该被忽略");
            }
        }
    }
    public static String Normalization(String fileName) {
        if (fileName.startsWith("./")) {
            return fileName.substring(2);
        }
        return fileName;
    }
    public static void add(String fileName) {
        // 检查是否在 Gitlet 目录中
        checkInGitlet();
        Map<String, String> stagingBefore = readStagingArea();

        // 规范化文件名（去除./前缀）
        String normalizedFileName = Normalization(fileName);

        // 检查是否明确指定目录（以/结尾）
        boolean explicitDirectory = normalizedFileName.endsWith("/");
        if (explicitDirectory) {
            normalizedFileName = normalizedFileName.substring(0, normalizedFileName.length() - 1);
        }

        // 特殊处理 "." 情况
        if (normalizedFileName.equals(".")) {
            normalizedFileName = "";
        }
        
        // 读取当前提交和暂存区
        Commit currentCommit = getCurrentCommit();
        Map<String, String> stagingArea = readStagingArea();
        
        // 读取忽略规则
        Set<String> ignorePaths = readIgnoreFile();

        // 处理文件或目录
        if (explicitDirectory) {
            // 明确指定目录，只添加目录中的文件
            File dir = join(CWD, normalizedFileName);
            if (dir.isDirectory() && dir.exists()) {
                addDirectory(dir, normalizedFileName, currentCommit, stagingArea, ignorePaths);
            } else {
                System.out.println("指定路径不是目录");
                operationHistory.invalidOperation();
                System.exit(1);
            }
        } else {
            // 优先处理文件（即使存在同名目录）
            File file = join(CWD, normalizedFileName);
            if (file.isFile() && file.exists()) {
                addSingleFile(file, normalizedFileName, currentCommit, stagingArea);
            } else if (file.isDirectory() && file.exists()) {
                // 只有当同名文件不存在时才添加目录
                addDirectory(file, normalizedFileName, currentCommit, stagingArea, ignorePaths);
            } else {
                System.out.println("文件不存在");
                operationHistory.invalidOperation();
                System.exit(1);
            }
        }
        
        // 保存暂存区
        saveStagingArea(stagingArea);
        
        // 记录操作历史，保存操作前的暂存区状态
        Map<String, Object> params = new HashMap<>();
        params.put("fileName", fileName);
        operationHistory.recordOperation(OperationHistory.OperationType.ADD, params, stagingBefore);
    }

    private static void addFileOrDirectory(File file, String filePath, Commit currentCommit,
                                           Map<String, String> stagingArea, Set<String> ignorePaths) {
        // 检查文件/目录是否应该被忽略
        // 对于根目录（filePath为空）不进行忽略检查
        if (!filePath.isEmpty() && shouldBeIgnored(filePath, ignorePaths)) {
            return; // 如果应该被忽略，直接返回
        }

        if (file.isFile()) {
            // 处理单个文件
            addSingleFile(file, filePath, currentCommit, stagingArea);
        } else if (file.isDirectory()) {
            // 使用addDirectory方法处理目录
            addDirectory(file, filePath, currentCommit, stagingArea, ignorePaths);
        }
    }

    /**
     * 递归添加目录中的所有文件
     */
    private static void addDirectory(File dir, String dirPath, Commit currentCommit,
                                    Map<String, String> stagingArea, Set<String> ignorePaths) {
        // 检查目录是否应该被忽略
        if (!dirPath.isEmpty() && shouldBeIgnored(dirPath, ignorePaths)) {
            return;
        }
        
        // 递归处理目录中的所有文件
        File[] files = dir.listFiles();
        if (files != null) {
            for (File subFile : files) {
                // 跳过.gitlet目录
                if (subFile.getName().equals(".gitlet") || subFile.getName().equals(".git")) {
                    continue;
                }
                // 构建子文件路径
                String subFilePath = dirPath.isEmpty() ? subFile.getName() : dirPath + "/" + subFile.getName();
                addFileOrDirectory(subFile, subFilePath, currentCommit, stagingArea, ignorePaths);
            }
        }
    }

    private static void addSingleFile(File file, String filePath, Commit currentCommit, Map<String, String> stagingArea) {
        // 生成当前文件的 Blob
        Blob newBlob = new Blob(readContents(file));
        String newBlobHash = newBlob.getID();

        // 检查是否与当前提交的文件内容相同
        String currentBlobHash = currentCommit.getFileToBlobID().get(filePath);
        if (newBlobHash.equals(currentBlobHash)) {
            // 内容相同 → 从暂存区移除（如果存在）
            stagingArea.remove(filePath);
            return;
        }

        // 内容不同 → 暂存文件（覆盖旧记录）
        stagingArea.put(filePath, newBlobHash);

        // 保存 Blob 到对象库（如果不存在）
        saveBlobIfNotExists(newBlob);
    }
    
    private static void saveBlobIfNotExists(Blob blob) {
        File blobFile = join(OBJECTS_DIR, blob.getID());
        if (!blobFile.exists()) {
            writeObject(blobFile, blob);
        }
    }
    
    private static void clearStagingArea() {
        writeObject(STAGING_AREA, new HashMap<>());
    }

    private static void saveCommit(Commit newCommit) {
        File commitFile = join(OBJECTS_DIR, newCommit.getId());
        writeObject(commitFile, newCommit);
        // 仅当 HEAD 指向分支时才更新分支引用
        String head = readContentsAsString(HEAD);
        if (head.startsWith("ref: ")) {
            String branchName = getCurrentBranch();
            writeContents(join(HEADS_DIR, branchName), newCommit.getId());
        }
    }
    public static void commit(String message) {
        checkInGitlet();
        // 获取操作前的缓存区
        Map<String, String> stagingBefore = readStagingArea();
        if (message.equals("")) {
            System.out.println("请输入提交信息");
            operationHistory.invalidOperation();
            System.exit(1);
        }
        Map<String, String> stagingArea = readStagingArea();
        if (stagingArea.isEmpty()) {
            System.out.println("提交没有新的内容");
            operationHistory.invalidOperation();
            System.exit(1);
        }

        // 创建新提交（继承当前提交的文件映射，并用暂存区覆盖）
        Commit currentCommit = getCurrentCommit();
        Commit newCommit = getNewCommit(message, currentCommit, stagingArea);

        // 保存提交并更新分支
        saveCommit(newCommit);
        clearStagingArea();
        
        // 记录操作历史，保存操作前的暂存区状态和提交信息
        Map<String, Object> params = new HashMap<>();
        params.put("message", message);
        params.put("commitId", newCommit.getId());
        Map<String, Object> commitData = new HashMap<>();
        commitData.put("staging", stagingBefore);
        commitData.put("commit", currentCommit);
        operationHistory.recordOperation(OperationHistory.OperationType.COMMIT, params, commitData);
    }

    private static Commit getNewCommit(String message, Commit currentCommit, Map<String, String> stagingArea) {
        Map<String, String> newFileToBlob = new HashMap<>(currentCommit.getFileToBlobID());

        // 处理暂存区的删除和更新
        for (Map.Entry<String, String> entry : stagingArea.entrySet()) {
            String fileName = entry.getKey();
            String blobHash = entry.getValue();
            if (blobHash == null) {
                newFileToBlob.remove(fileName); // 删除文件
            } else {
                newFileToBlob.put(fileName, blobHash); // 更新或新增
            }
        }

        return new Commit(
                currentCommit.getId(), // parent1
                null,                 // parent2
                new Date(),
                message,
                newFileToBlob
        );
    }

    public static void rm(String fileName) {
        checkInGitlet();

        Map<String, String> stagingArea = readStagingArea();
        Commit currentCommit = getCurrentCommit();

        // 规范化文件名（去除./前缀）
        String normalizedFileName = Normalization(fileName);

        File file = join(CWD, normalizedFileName);
        if (!file.exists()) {
            System.out.println("文件不存在");
            operationHistory.invalidOperation();
            System.exit(1);
        }
        
        // 检查目录中是否有被跟踪的文件
        boolean hasTrackedFiles = false;
        if (file.isDirectory()) {
            for (String filePath : currentCommit.getFileToBlobID().keySet()) {
                if (filePath.startsWith(normalizedFileName + "/")) {
                    hasTrackedFiles = true;
                    break;
                }
            }
            if (!hasTrackedFiles) {
                for (String filePath : stagingArea.keySet()) {
                    if (filePath.startsWith(normalizedFileName + "/")) {
                        hasTrackedFiles = true;
                        break;
                    }
                }
            }
        } else {
            hasTrackedFiles = stagingArea.containsKey(normalizedFileName) || currentCommit.getFileToBlobID().containsKey(normalizedFileName);
        }
        
        if (!hasTrackedFiles) {
            System.out.println(file.getName() + "文件未被追踪");
            operationHistory.invalidOperation();
            System.exit(1);
        }
        
        // 保存被删除文件的内容，用于撤销操作
        Map<String, byte[]> deletedFilesContent = new HashMap<>();
        collectFileContent(normalizedFileName, currentCommit, deletedFilesContent);
        
        removeFileOrDirectory(normalizedFileName, stagingArea, currentCommit);
        saveStagingArea(stagingArea);
        
        // 记录操作历史，保存操作前的暂存区状态和被删除的文件内容
        Map<String, Object> params = new HashMap<>();
        params.put("fileName", fileName);
        Map<String, Object> rmData = new HashMap<>();
        rmData.put("staging", readStagingArea());
        rmData.put("deletedFilesContent", deletedFilesContent);
        operationHistory.recordOperation(OperationHistory.OperationType.RM, params, rmData);
    }

    /**
     * 收集文件内容，用于撤销删除操作
     * @param fileName 文件或目录名
     * @param currentCommit 当前提交
     * @param fileContents 用于存储文件内容的Map
     */
    private static void collectFileContent(String fileName, Commit currentCommit, Map<String, byte[]> fileContents) {
        File file = join(CWD, fileName);
        
        if (file.isDirectory()) {
            // 处理目录
            if (fileName.equals(".gitlet") || fileName.equals(".git")) {
                return; // 不处理版本控制目录
            }
            
            File[] files = file.listFiles();
            if (files != null) {
                // 递归收集目录中的所有文件内容
                for (File subFile : files) {
                    String subFilePath = fileName + "/" + subFile.getName();
                    collectFileContent(subFilePath, currentCommit, fileContents);
                }
            }
        } else {
            // 处理单个文件
            // 从当前提交中获取文件内容
            String blobId = currentCommit.getFileToBlobID().get(fileName);
            if (blobId != null) {
                Blob blob = readObject(join(OBJECTS_DIR, blobId), Blob.class);
                fileContents.put(fileName, (byte[]) blob.getContent());
            }
        }
    }
    
    /**
     * 递归删除文件或目录
     * @param fileName 文件或目录名
     * @param stagingArea 暂存区
     * @param currentCommit 当前提交
     */
    private static void removeFileOrDirectory(String fileName, Map<String, String> stagingArea, Commit currentCommit) {
        File file = join(CWD, fileName);
        
        if (file.isDirectory()) {
            // 处理目录
            if (fileName.equals(".gitlet") || fileName.equals(".git")) {
                return; // 不删除版本控制目录
            }
            
            File[] files = file.listFiles();
            if (files != null) {
                // 递归删除目录中的所有文件和子目录
                for (File subFile : files) {
                    String subFilePath = fileName + "/" + subFile.getName();
                    removeFileOrDirectory(subFilePath, stagingArea, currentCommit);
                }
            }
        } else {
            // 处理单个文件
            // 标记为删除
            stagingArea.put(fileName, null);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private static void printCommit(Commit currentCommit) {
        System.out.println("===");
        System.out.println("commit " + currentCommit.getId());

        if (currentCommit.getParent2ID() != null) {
            String parent1Prefix = currentCommit.getParent1ID().substring(0, 7);
            String parent2Prefix = currentCommit.getParent2ID().substring(0, 7);
            System.out.println("Merge: " + parent1Prefix + " " + parent2Prefix);
        }
        System.out.println("Date: " + currentCommit.getTimestamp());
        System.out.println(currentCommit.getMessage());
        // 【新增/优化】打印文件列表
        if (!currentCommit.getFileToBlobID().isEmpty()) {
            System.out.println("Files Tracked:");
            for (String fileName : currentCommit.getFileToBlobID().keySet()) {
                System.out.println("  - " + fileName);
            }
        }

        System.out.println();
    }
    public static void log() {
        checkInGitlet();

        Commit currentCommit = getCurrentCommit();
        while (currentCommit != null) {
            if(currentCommit.getTimestamp().equals(new Date(0L))) {
                break;
            }
            System.out.println("===");
            System.out.println("commit " + currentCommit.getId());

            // 打印 Merge 信息（如果存在）
            if (currentCommit.getParent2ID() != null) {
                String parent1Prefix = currentCommit.getParent1ID().substring(0, 7);
                String parent2Prefix = currentCommit.getParent2ID().substring(0, 7);
                System.out.println("Merge: " + parent1Prefix + " " + parent2Prefix);
            }

            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println(currentCommit.getMessage());

            // 【新增/优化】打印文件列表（清晰展示该提交包含的文件）
            if (!currentCommit.getFileToBlobID().isEmpty()) {
                System.out.println("Files Tracked:");
                for (String fileName : currentCommit.getFileToBlobID().keySet()) {
                    System.out.println("  - " + fileName);
                }
            }

            System.out.println();

            // 仅沿着 Parent 1 向上遍历
            if (currentCommit.getParent1ID() != null) {
                currentCommit = readCommitFromObjects(currentCommit.getParent1ID());
            } else {
                currentCommit = null;
            }
        }
    }
    public static void globalLog() {
        checkInGitlet();
        File[] objects = OBJECTS_DIR.listFiles();

        if (objects == null) return;

        for (File objectFile : objects) {
            // 仅处理 Commit 对象（通过尝试反序列化）
            try {
                // 尝试读取对象并检查是否为 Commit 实例
                Commit obj = readCommitFromObjects(objectFile.getName());
                if (obj != null) {
                    // 打印 Commit 信息（复用 printCommit 逻辑）
                    printCommit(obj);
                }
            } catch (Exception e) {
                // 忽略非 Commit 对象或读取错误
            }
        }
    }


    public static void find(String message){
        checkInGitlet();

        Set<String> printedCommits = new HashSet<>();
         boolean found = false;
        
        // 遍历所有对象文件查找提交
        File[] objects = OBJECTS_DIR.listFiles();
        if (objects != null) {
            for (File objectFile : objects) {
                try {
                    Commit commit = readObject(objectFile, Commit.class);
                    if (commit != null && 
                        commit.getMessage().contains(message) && 
                        !printedCommits.contains(commit.getId())) {
                        printCommit(commit);
                        printedCommits.add(commit.getId());
                        found = true;
                    }
                } catch (Exception e) {
                    // 不是提交对象，跳过
                    continue;
                }
            }
        }
        
        if (!found) {
            System.out.println("没有找到包含该信息的提交。");
            System.exit(1);
        }
    }


    public static void status() {
        checkInGitlet();

        // 打印分支
        System.out.println("=== Branches ===");
        for (File headFile : HEADS_DIR.listFiles()) {
            String branchName = headFile.getName();
            String head = getCurrentBranch();

            if (head.equals(branchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(" " + branchName);
            }

        }
        System.out.println();
        //  打印暂存区
        System.out.println("=== Staged Files ===");
        for (Map.Entry<String, String> entry : readStagingArea().entrySet()) {
            if (entry.getValue() != null) {
                String fileName = entry.getKey();
                System.out.println(fileName);
            }
        }
        System.out.println();
        // 打印从暂存中删除的文件
        System.out.println("=== Removed Files ===");
        for (Map.Entry<String, String> entry : readStagingArea().entrySet()) {
            String fileName = entry.getKey();
            if (entry.getValue() == null) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    public static void checkout(String branchName) {
        checkInGitlet();
        String previousBranch = getCurrentBranch();
        File branch= join(HEADS_DIR, branchName);

        if (!branch.exists()) {
            System.out.println("分支不存在");
            operationHistory.invalidOperation();
            System.exit(1);
        } else if(branchName.equals(getCurrentBranch())) {
            System.out.println("已在对应分支");
            operationHistory.invalidOperation();
            System.exit(0);
        }

        Commit targetCommit = readCommitFromObjects(readContentsAsString(branch));
        reset(targetCommit.getId(), true);
        writeContents(HEAD, "ref: refs/heads/" + branchName);
        
        // 记录操作历史，保存操作前的分支状态
        Map<String, Object> params = new HashMap<>();
        params.put("branchName", branchName);
        params.put("previousBranch", previousBranch);
        operationHistory.recordOperation(OperationHistory.OperationType.CHECKOUT, params, null);

    }
    public static void checkout(String commitId, String fileName) {
        checkInGitlet();
        
        // 收集操作前的文件内容用于撤销
        Map<String, byte[]> fileContentsBefore = new HashMap<>();
        String normalizedFileName = Normalization(fileName);
        boolean explicitDirectory = normalizedFileName.endsWith("/");
        if (explicitDirectory) {
            normalizedFileName = normalizedFileName.substring(0, normalizedFileName.length() - 1);
        }
        
        if (explicitDirectory) {
            // 收集目录中所有文件的内容
            collectDirectoryFileContents(normalizedFileName, fileContentsBefore);
        } else {
            // 收集单个文件的内容
            File file = join(CWD, normalizedFileName);
            if (file.exists() && file.isFile()) {
                try {
                    fileContentsBefore.put(normalizedFileName, readContents(file));
                } catch (Exception e) {
                    // 文件读取失败，记录空内容
                    fileContentsBefore.put(normalizedFileName, new byte[0]);
                }
            }
        }
        
        Commit currentCommit = getCurrentCommit();
        if(commitId.equals("--")) {
            getFileOrDirectoryInCommit(fileName, currentCommit);
        } else {
            for (File headFile : HEADS_DIR.listFiles()) {
                currentCommit = readCommitFromObjects(readContentsAsString(headFile));
                while(currentCommit != null) {
                    if (currentCommit.getId().startsWith(commitId)) {
                        getFileOrDirectoryInCommit(fileName, currentCommit);
                        // 记录操作历史
                        Map<String, Object> params = new HashMap<>();
                        params.put("commitId", commitId);
                        params.put("fileName", fileName);
                        operationHistory.recordOperation(OperationHistory.OperationType.CHECKOUT, params, fileContentsBefore);
                        return;
                    }
                    if (currentCommit.getParent1ID() != null) {
                        currentCommit = readCommitFromObjects(currentCommit.getParent1ID());
                    } else {
                        currentCommit = null;
                    }
                }
            }
            System.out.println("对应提交不存在");
        }
        
        // 记录操作历史
        Map<String, Object> params = new HashMap<>();
        params.put("commitId", commitId);
        params.put("fileName", fileName);
        operationHistory.recordOperation(OperationHistory.OperationType.CHECKOUT, params, fileContentsBefore);
    }
    
    /**
     * 收集目录中所有文件的内容
     * @param dirName 目录名
     * @param fileContents 文件内容映射
     */
    private static void collectDirectoryFileContents(String dirName, Map<String, byte[]> fileContents) {
        File dir = join(CWD, dirName);
        if (dir.exists() && dir.isDirectory()) {
            collectDirectoryFiles(dir, dirName, fileContents);
        }
    }
    
    /**
     * 递归收集目录中所有文件的内容
     * @param dir 目录文件
     * @param dirPath 目录路径
     * @param fileContents 文件内容映射
     */
    private static void collectDirectoryFiles(File dir, String dirPath, Map<String, byte[]> fileContents) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(".gitlet") || file.getName().equals(".git")) {
                    continue;
                }
                
                String filePath = dirPath.isEmpty() ? file.getName() : dirPath + "/" + file.getName();
                if (file.isDirectory()) {
                    collectDirectoryFiles(file, filePath, fileContents);
                } else {
                    try {
                        fileContents.put(filePath, readContents(file));
                    } catch (Exception e) {
                        // 文件读取失败，记录空内容
                        fileContents.put(filePath, new byte[0]);
                    }
                }
            }
        }
    }
    
    /**
     * 从提交中检出文件或目录
     * @param fileName 文件或目录名
     * @param currentCommit 当前提交
     */
    private static void getFileOrDirectoryInCommit(String fileName, Commit currentCommit) {
        // 规范化文件名（去除./前缀）
        String normalizedFileName = Normalization(fileName);

        // 检查是否明确指定目录（以/结尾）
        boolean explicitDirectory = normalizedFileName.endsWith("/");
        if (explicitDirectory) {
            normalizedFileName = normalizedFileName.substring(0, normalizedFileName.length() - 1);
        }
        
        // 如果明确指定目录，直接处理目录
        if (explicitDirectory) {
            checkoutDirectory(normalizedFileName, currentCommit);
        } else {
            // 优先匹配文件，然后匹配目录
            if (currentCommit.getFileToBlobID().containsKey(normalizedFileName)) {
                // 找到匹配的文件
                byte[] content = getFileContentFromCommit(currentCommit, normalizedFileName);
                writeFileToWorkDir(normalizedFileName, content);
            } else {
                // 没有找到匹配的文件，尝试匹配目录
                checkoutDirectory(normalizedFileName, currentCommit);
            }
        }
    }
    
    /**
     * 从提交中检出目录
     * @param dirName 目录名
     * @param currentCommit 当前提交
     */
    private static void checkoutDirectory(String dirName, Commit currentCommit) {
        boolean found = false;
        for (Map.Entry<String, String> entry : currentCommit.getFileToBlobID().entrySet()) {
            String filePath = entry.getKey();
            if (filePath.equals(dirName) || filePath.startsWith(dirName + "/")) {
                found = true;
                byte[] content = getFileContentFromCommit(currentCommit, filePath);
                writeFileToWorkDir(filePath, content);
            }
        }
        
        if (!found) {
            System.out.println("对应提交内不存在指定文件或目录");
            operationHistory.invalidOperation();
            System.exit(1);
        }
    }

    public static void branch(String branchName) {
        checkInGitlet();
        if (join(HEADS_DIR, branchName).exists()) {
            System.out.println("分支已存在");
        } else {
            writeContents(join(HEADS_DIR, branchName), getCurrentCommit().getId());
            
            // 记录操作历史，保存操作前的分支状态
            Map<String, Object> params = new HashMap<>();
            params.put("branchName", branchName);
            operationHistory.recordOperation(OperationHistory.OperationType.BRANCH, params, null);
        }
    }

    public static void rmBranch(String branchName) {
        checkInGitlet();
        File branchFile = join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("分支不存在");
            operationHistory.invalidOperation();
            System.exit(1);
        } else if(branchName.equals(getCurrentBranch())) {
            System.out.println("无法删除当前所在分支");
            operationHistory.invalidOperation();
            System.exit(1);
        } else {
            // 记录操作历史，保存被删除分支的引用
            Map<String, Object> params = new HashMap<>();
            params.put("branchName", branchName);
            String branchRef = readContentsAsString(branchFile);
            operationHistory.recordOperation(OperationHistory.OperationType.RM_BRANCH, params, branchRef);
            
            branchFile.delete();
            
            System.exit(0);
        }

    }

    /**
     * 检查工作目录中是否存在会与目标提交冲突的未跟踪文件或未暂存修改
     * 核心原则：只在目标提交会覆盖工作目录内容时报错
     */
    private static void checkUntrackedFiles(Commit targetCommit) {
        Commit currentCommit = getCurrentCommit();
        Map<String, String> staging = readStagingArea();

        // 收集工作目录中的所有文件（包括子目录）
        Set<String> workingFiles = new HashSet<>();
        collectWorkingFiles(CWD, "", workingFiles);
        // 读取 .gitignore 规则
        Set<String> ignorePaths = readIgnoreFile();

        boolean hasConflict = false;
        List<String> conflictFiles = new ArrayList<>();

        for (String filePath : workingFiles) {
            // 跳过忽略的文件
            if (shouldBeIgnored(filePath, ignorePaths)) {
                continue;
            }
            // 特殊处理：跳过.ignore文件本身
            if (filePath.equals(".ignore")) {
                continue;
            }
            
            boolean inTarget = targetCommit.getFileToBlobID().containsKey(filePath);
            boolean inStaging = staging.containsKey(filePath) && staging.get(filePath) != null;
            boolean inCurrent = currentCommit.getFileToBlobID().containsKey(filePath);

            // 判断是否为未跟踪文件：不在当前提交且不在暂存区
            boolean isUntracked = !inCurrent && !inStaging;

            if (isUntracked) {
                if (inTarget) {
                    // 高危冲突：会被覆盖
                    hasConflict = true;
                    conflictFiles.add("未跟踪文件 '" + filePath + "' 将被目标提交覆盖。");
                } else {
                    // 中危风险：会被静默删除
                    hasConflict = true;
                    conflictFiles.add("未跟踪文件 '" + filePath + "' 将被删除，因为它不在目标提交中。");
                }
            } else {
                // 对于已跟踪文件，检查未暂存的修改是否会被目标提交覆盖
                if (inCurrent && !inStaging) {
                    try {
                        // 获取当前提交中的文件内容
                        String currentBlobHash = currentCommit.getFileToBlobID().get(filePath);
                        Blob currentBlob = readObject(join(OBJECTS_DIR, currentBlobHash), Blob.class);

                        // 获取工作目录中的文件内容
                        File workingFile = join(CWD, filePath);
                        Blob workingBlob = new Blob(readContents(workingFile));

                        // 检查工作目录是否与当前提交不同
                        boolean workingDiffersFromCurrent = !currentBlob.getID().equals(workingBlob.getID());

                        if (workingDiffersFromCurrent && inTarget) {
                            // 获取目标提交中的文件内容
                            String targetBlobHash = targetCommit.getFileToBlobID().get(filePath);
                            Blob targetBlob = readObject(join(OBJECTS_DIR, targetBlobHash), Blob.class);

                            // 检查目标提交是否与工作目录不同
                            boolean targetDiffersFromWorking = !targetBlob.getID().equals(workingBlob.getID());

                            if (targetDiffersFromWorking) {
                                hasConflict = true;
                                conflictFiles.add(filePath + " (未暂存的本地修改会被目标提交覆盖)");
                            }
                        }
                    } catch (Exception e) {
                        // 文件读取失败，按安全原则处理
                        hasConflict = true;
                        conflictFiles.add(filePath + " (文件访问错误，无法验证安全性)");
                    }
                }
            }
        }

        if (hasConflict) {
            System.out.println("❌ 存在冲突文件，无法执行reset操作：");
            for (String conflict : conflictFiles) {
                System.out.println("   - " + conflict);
            }
            System.out.println("\n💡 解决方法：");
            System.out.println("   1. 提交您的修改：gitlet commit");
            System.out.println("   2. 移动或删除冲突文件");
            System.out.println("   3. 添加未跟踪文件到暂存区：gitlet add <文件>");
            operationHistory.invalidOperation();
            System.exit(1);
        }
    }

    /**
     * 递归收集工作目录中的所有文件
     * @param dir 当前目录
     * @param prefix 路径前缀
     * @param files 收集到的文件集合
     */
    private static void collectWorkingFiles(File dir, String prefix, Set<String> files) {
        File[] dirFiles = dir.listFiles();
        if (dirFiles != null) {
            for (File file : dirFiles) {
                // 跳过版本控制目录
                if (file.getName().equals(".gitlet") || file.getName().equals(".git")) {
                    continue;
                }

                String filePath = prefix.isEmpty() ? file.getName() : prefix + "/" + file.getName();

                if (file.isFile()) {
                    files.add(filePath);
                } else if (file.isDirectory()) {
                    collectWorkingFiles(file, filePath, files);
                }
            }
        }
    }
    public static void reset(String commitID, boolean isCheckOutBranch) {
        checkInGitlet();
        // 记录缓存区和当前commit
        Map<String, String> stagingBefore = readStagingArea();
        Commit commitBefore = getCurrentCommit();

        // 验证提交ID是否存在
        File commitFile = join(OBJECTS_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("❌ 错误：不存在该提交ID。");
            operationHistory.invalidOperation();
            System.exit(1);
        }

        Commit targetCommit = readCommitFromObjects(commitID);

        // 检查工作目录状态（使用改进后的检测逻辑）
        checkUntrackedFiles(targetCommit);

        // 清空暂存区
        clearStagingArea();

        // 递归删除目标提交中不存在的文件
        deleteFilesNotInCommit(targetCommit, CWD, "");

        // 更新为目标提交中的文件内容
        for (Map.Entry<String, String> entry : targetCommit.getFileToBlobID().entrySet()) {
            String filePath = entry.getKey();
            String blobHash = entry.getValue();
            
            // 写入文件内容
            byte[] content = getFileContentFromCommit(targetCommit, filePath);
            writeFileToWorkDir(filePath, content);
        }

        // 更新分支引用（如果不是checkout操作）
        if (!isCheckOutBranch) {
            String currentBranch = getCurrentBranch();
            writeContents(join(HEADS_DIR, currentBranch), commitID);
            System.out.println("✅ 已成功重置到提交 " + commitID.substring(0, 7));
            
            // 记录操作历史，保存操作前的暂存区状态和提交信息
            Map<String, Object> params = new HashMap<>();
            String branchRef = readContentsAsString(join(HEADS_DIR, currentBranch));
            Map<String, Object> resetData = new HashMap<>();
            resetData.put("staging", stagingBefore);
            resetData.put("commit", commitBefore);
            resetData.put("branchRef", branchRef);
            resetData.put("branchName", currentBranch);
            operationHistory.recordOperation(OperationHistory.OperationType.RESET, params, resetData);
        }
    }

    /**
     * 递归删除目标提交中不存在的文件
     */
    private static void deleteFilesNotInCommit(Commit commit, File dir, String prefix) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.getName().equals(".gitlet") || file.getName().equals(".git")
                    || file.getName().equals(".ignore") || file.getName().equals("gitlet")) {
                continue;
            }

            String relativePath = prefix.isEmpty() ? file.getName() : prefix + "/" + file.getName();

            if (file.isDirectory()) {
                deleteFilesNotInCommit(commit, file, relativePath);
                // 删除可能已为空的目录
                if (file.list().length == 0) {
                    file.delete();
                }
            } else {
                // 如果文件不在目标提交中，则删除
                if (!commit.getFileToBlobID().containsKey(relativePath)) {
                    file.delete();
                }
            }
        }
    }

    /**
     * 确保目录存在，如果不存在则创建
     */
    private static void ensureDirectoryExists(File dir) {
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void merge(String branchName) {
        // 1. 检查合并条件
        checkInGitlet();
        Map<String, String> stagingArea = readStagingArea();
        if (!stagingArea.isEmpty()) {
            System.out.println("缓存区存在未提交的文件");
            operationHistory.invalidOperation();
            System.exit(1);
        }
        if (!join(HEADS_DIR, branchName).exists()) {
            System.out.println("指定合并分支不存在");
            operationHistory.invalidOperation();
            System.exit(1);
        }
        if (branchName.equals(getCurrentBranch())) {
            System.out.println("分支无法与自身合并");
            operationHistory.invalidOperation();
            System.exit(1);
        }
        // 2. 获取当前分支和被合并分支的最新提交
        Commit currentCommit = getCurrentCommit();
        Commit givenCommit = readCommitFromObjects(readContentsAsString(join(HEADS_DIR, branchName)));

        // 3. 查找共同祖先（Split Point）
        Commit splitPoint = findSplitPoint(currentCommit, givenCommit);

        // 记录操作前的状态，用于撤销
        Commit commitBefore = getCurrentCommit();
        Map<String, String> stagingBefore = readStagingArea();
        
        // 情况1：给定分支是当前分支的祖先
        if (splitPoint.getId().equals(givenCommit.getId())) {
            System.out.println("指定的分支是当前分支的祖先， 无需合并");
            
            // 记录操作历史
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> mergeData = new HashMap<>();
            mergeData.put("type", "no-need"); // 无需合并
            operationHistory.recordOperation(OperationHistory.OperationType.MERGE, params, mergeData);
            
            System.exit(0);
        }

        // 情况2：当前分支是给定分支的祖先
        if (splitPoint.getId().equals(currentCommit.getId())) {
            // 快进合并，直接检出给定分支的提交
            String givenCommitId = givenCommit.getId();
            reset(givenCommitId, false);
            writeContents(HEAD, "ref: refs/heads/" + getCurrentBranch());
            System.out.println("快进合并完成");
            
            // 记录操作历史
            Map<String, Object> params = new HashMap<>();
            params.put("branchName", branchName);
            Map<String, Object> mergeData = new HashMap<>();
            mergeData.put("commit", commitBefore);
            mergeData.put("staging", stagingBefore);
            mergeData.put("type", "fast-forward"); // 快进合并
            operationHistory.recordOperation(OperationHistory.OperationType.MERGE, params, mergeData);
            
            System.exit(0);
        }

        // ========== 5. 执行三方合并 ==========
        Map<String, String> mergedFiles = new HashMap<>();
        boolean hasConflict = false;

        // 获取所有涉及的文件（包括可能被删除的文件）
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(splitPoint.getFileToBlobID().keySet());
        allFiles.addAll(currentCommit.getFileToBlobID().keySet());
        allFiles.addAll(givenCommit.getFileToBlobID().keySet());

        for (String fileName : allFiles) {
            String baseBlobHash = splitPoint.getFileToBlobID().get(fileName);
            String currentBlobHash = currentCommit.getFileToBlobID().get(fileName);
            String givenBlobHash = givenCommit.getFileToBlobID().get(fileName);

            // 获取文件内容（可能为null表示文件被删除）
            byte[] currentContent = getBlobContent(currentBlobHash);
            byte[] givenContent = getBlobContent(givenBlobHash);

            // === 合并规则判断 ===
            // 规则1：双方未修改 → 保持当前版本
            if (Objects.equals(currentBlobHash, givenBlobHash)) {
                mergedFiles.put(fileName, currentBlobHash);
                continue;
            }

            // 规则2：仅在当前分支修改 → 保留当前版本
            if (Objects.equals(baseBlobHash, givenBlobHash)) {
                mergedFiles.put(fileName, currentBlobHash);
                continue;
            }

            // 规则3：仅在给定分支修改 → 采用给定版本
            if (Objects.equals(baseBlobHash, currentBlobHash)) {
                mergedFiles.put(fileName, givenBlobHash);
                continue;
            }
            
            // 规则4：新增文件（分割点不存在）
            if (baseBlobHash == null) {
                if (currentBlobHash == null) {
                    mergedFiles.put(fileName, givenBlobHash);  // 仅在给定分支存在
                } else if (givenBlobHash == null) {
                    mergedFiles.put(fileName, currentBlobHash); // 仅在当前分支存在
                } else {
                    // 冲突：双方都新增了同名但内容不同的文件
                    hasConflict = true;
                    byte[] conflictContent = generateConflictContent(currentContent, givenContent);
                    Blob conflictBlob = new Blob(conflictContent);
                    saveBlob(conflictBlob);
                    mergedFiles.put(fileName, conflictBlob.getID());
                }
                continue;
            }

            // 规则5：都修改了文件 → 生成冲突标记
            hasConflict = true;
            byte[] conflictContent = generateConflictContent(currentContent, givenContent);
            Blob conflictBlob = new Blob(conflictContent);
            saveBlob(conflictBlob);
            mergedFiles.put(fileName, conflictBlob.getID());
        }

        // ========== 6. 创建合并提交 ==========
        // 检查是否有实际变更（避免空提交）
        boolean hasRealChanges = !mergedFiles.equals(currentCommit.getFileToBlobID())
                || !mergedFiles.equals(givenCommit.getFileToBlobID());
        if (!hasRealChanges) {
            System.out.println("合并提交没有新的改变，合并终止");
            operationHistory.invalidOperation();
            System.exit(1);
        }

        Commit mergeCommit = new Commit(
                currentCommit.getId(),      // 第一父提交
                givenCommit.getId(),       // 第二父提交
                new Date(),
                "Merged " + branchName + " into " + getCurrentBranch() + ".",
                mergedFiles
        );

        // 保存提交并更新分支
        saveCommit(mergeCommit);
        clearStagingArea();

        // 输出冲突提示
        if (hasConflict) {
            System.out.println("出现合并冲突，请手动调整冲突文件");
        }
        
        // ========== 7. 更新工作目录 ==========
        updateWorkingDirectory(mergedFiles);
        
        // 记录操作历史
        Map<String, Object> params = new HashMap<>();
        params.put("branchName", branchName);
        Map<String, Object> mergeData = new HashMap<>();
        mergeData.put("commit", commitBefore);
        mergeData.put("staging", stagingBefore);
        mergeData.put("type", "normal"); // 普通合并
        operationHistory.recordOperation(OperationHistory.OperationType.MERGE, params, mergeData);
    }
    
    /**
     * 更新工作目录以匹配合并结果
     * @param mergedFiles 合并后的文件映射
     */
    private static void updateWorkingDirectory(Map<String, String> mergedFiles) {
        // 删除工作目录中未被跟踪的文件
        deleteUntrackedFiles(CWD, mergedFiles.keySet());

        // 写入合并后的文件内容
        for (Map.Entry<String, String> entry : mergedFiles.entrySet()) {
            String fileName = entry.getKey();
            String blobHash = entry.getValue();

            if (blobHash != null) {
                byte[] content = getBlobContent(blobHash);
                writeFileToWorkDir(fileName, content);
            } else {
                // 如果blobHash为null，表示文件应被删除
                File file = join(CWD, fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    /**
     * 递归删除工作目录中未被跟踪的文件和目录
     * @param dir 要检查的目录
     * @param trackedPaths 被跟踪文件的路径集合（相对路径）
     */
    private static void deleteUntrackedFiles(File dir, Set<String> trackedPaths) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            // 跳过.gitlet目录
            if (file.getName().equals(".gitlet") || file.getName().equals(".git")
                    || file.getName().equals("gitlet") || file.getName().equals("ignore")) {
                continue;
            }

            // 获取相对于CWD的路径
            String relativePath = getRelativePath(CWD, file);

            if (file.isDirectory()) {
                // 检查目录是否包含任何被跟踪的文件
                if (!isDirectoryTracked(relativePath, trackedPaths)) {
                    // 目录不包含任何被跟踪的文件，删除整个目录
                    deleteDirectory(file);
                } else {
                    // 目录包含被跟踪的文件，递归检查子目录
                    deleteUntrackedFiles(file, trackedPaths);

                    // 检查删除后目录是否为空，如果是则删除
                    if (file.exists() && file.isDirectory() &&
                            (file.list() == null || file.list().length == 0)) {
                        file.delete();
                    }
                }
            } else {
                // 检查文件是否被跟踪
                if (!trackedPaths.contains(relativePath)) {
                    file.delete();
                }
            }
        }
    }

    /**
     * 检查目录是否包含被跟踪的文件
     * @param dirPath 目录路径
     * @param trackedPaths 被跟踪文件的路径集合
     * @return 如果目录包含被跟踪的文件返回true，否则返回false
     */
    private static boolean isDirectoryTracked(String dirPath, Set<String> trackedPaths) {
        // 确保目录路径以"/"结尾，以便正确匹配子路径
        String prefix = dirPath.endsWith("/") ? dirPath : dirPath + "/";

        for (String path : trackedPaths) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 递归删除目录及其所有内容
     * @param dir 要删除的目录
     */
    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    /**
     * 获取文件相对于基目录的相对路径
     * @param base 基目录
     * @param file 文件
     * @return 相对路径
     */
    private static String getRelativePath(File base, File file) {
        String basePath = base.getAbsolutePath();
        String filePath = file.getAbsolutePath();

        if (filePath.startsWith(basePath)) {
            String relative = filePath.substring(basePath.length());
            // 移除开头的路径分隔符
            if (relative.startsWith(File.separator)) {
                relative = relative.substring(1);
            }
            // 统一使用"/"作为路径分隔符
            return relative.replace(File.separator, "/");
        }
        return filePath;
    }

    private static byte[] getBlobContent(String blobHash) {
        if (blobHash == null) return null;
        Blob blob = readObject(join(OBJECTS_DIR, blobHash), Blob.class);
        return (byte[]) blob.getContent();
    }

    private static void saveBlob(Blob blob) {
        if (blob == null) return;

        File blobFile = join(OBJECTS_DIR, blob.getID());
        if (!blobFile.exists()) {
            writeObject(blobFile, blob);
        }
    }
    private static byte[] generateConflictContent(byte[] current, byte[] given) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write("<<<<<<< HEAD\n".getBytes(StandardCharsets.UTF_8));
            if (current != null) out.write(current);
            out.write("=======\n".getBytes(StandardCharsets.UTF_8));
            if (given != null) out.write(given);
            out.write(">>>>>>>\n".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("冲突文件生成失败");
        }
        return out.toByteArray();
    }

    private static Commit findSplitPoint(Commit current, Commit given) {
        if (current == null || given == null) return null;

        Set<String> visitedCurrent = new HashSet<>();
        Queue<Commit> queue = new LinkedList<>();

        // 遍历当前提交的所有祖先
        queue.add(current);
        while (!queue.isEmpty()) {
            Commit commit = queue.poll();
            if (commit == null) continue;

            if (!visitedCurrent.contains(commit.getId())) {
                visitedCurrent.add(commit.getId());
                // 将第一个父提交加入队列
                if (commit.getParent1ID() != null) {
                    Commit parent1 = readCommitFromObjects(commit.getParent1ID());
                    queue.add(parent1);
                }
                // 将第二个父提交加入队列（如果存在）
                if (commit.getParent2ID() != null) {
                    Commit parent2 = readCommitFromObjects(commit.getParent2ID());
                    queue.add(parent2);
                }
            }
        }

        // 遍历给定提交的所有祖先，寻找第一个在visitedCurrent中出现的提交
        Queue<Commit> givenQueue = new LinkedList<>();
        Set<String> visitedGiven = new HashSet<>();
        givenQueue.add(given);

        while (!givenQueue.isEmpty()) {
            Commit commit = givenQueue.poll();
            if (commit == null) continue;

            if (visitedCurrent.contains(commit.getId())) {
                return commit; // 找到最低共同祖先
            }

            if (!visitedGiven.contains(commit.getId())) {
                visitedGiven.add(commit.getId());
                if (commit.getParent1ID() != null) {
                    Commit parent1 = readCommitFromObjects(commit.getParent1ID());
                    givenQueue.add(parent1);
                }
                if (commit.getParent2ID() != null) {
                    Commit parent2 = readCommitFromObjects(commit.getParent2ID());
                    givenQueue.add(parent2);
                }
            }
        }
        return null; // 无共同祖先（理论上不应发生）
    }

    /**
     * 从对象库中读取提交
     * @param commitId 提交ID
     * @return 提交对象
     */
    private static Commit readCommitFromObjects(String commitId) {
        return readObject(join(OBJECTS_DIR, commitId), Commit.class);
    }
    
    /**
     * 从提交中获取文件内容
     * @param commit 提交对象
     * @param filePath 文件路径
     * @return 文件内容
     */
    private static byte[] getFileContentFromCommit(Commit commit, String filePath) {
        String blobID = commit.getFileToBlobID().get(filePath);
        if (blobID == null) {
            return null;
        }
        Blob blob = readObject(join(OBJECTS_DIR, blobID), Blob.class);
        return (byte[]) blob.getContent();
    }
    
    /**
     * 写入文件内容到工作目录
     * @param filePath 文件路径
     * @param content 文件内容
     */
    private static void writeFileToWorkDir(String filePath, byte[] content) {
        File file = join(CWD, filePath);
        ensureDirectoryExists(file.getParentFile());
        writeContents(file, content);
    }
    
        public static void undo() {
        checkInGitlet();
        
        OperationHistory.OperationRecord lastOperation = operationHistory.getLastOperation();
        if (lastOperation == null) {
            System.out.println("没有可撤销的操作");
            System.exit(1);
        }
        
        OperationHistory.OperationType type = lastOperation.getType();
        Map<String, Object> params = lastOperation.getParameters();
        Object data = lastOperation.getData();
        
        switch (type) {
            case ADD:
                // 撤销add操作，需要重新构建暂存区
                undoAdd(params, data);
                break;
            case COMMIT:
                // 撤销commit操作，回退到上一个提交
                undoCommit(params, data);
                break;
            case RM:
                // 撤销rm操作，恢复文件
                undoRm(params, data);
                break;
            case BRANCH:
                // 撤销branch操作，删除创建的分支
                undoBranch(params, data);
                break;
            case RM_BRANCH:
                // 撤销rm-branch操作，恢复删除的分支
                undoRmBranch(params, data);
                break;
            case CHECKOUT:
                // 撤销checkout操作，切换回原来的分支
                undoCheckout(params, data);
                break;
            case RESET:
                // 撤销reset操作，恢复到操作前的状态
                undoReset(params, data);
                break;
            case MERGE:
                // 撤销merge操作，回退到合并前的状态
                undoMerge(params, data);
                break;
            default:
                System.out.println("不支持撤销该操作");
                System.exit(1);
        }
        
        // 移除已撤销的操作记录
        operationHistory.removeLastOperation();
    }
    
    private static void undoAdd(Map<String, Object> params, Object data) {
        // 恢复操作前的暂存区状态
        if (data instanceof Map) {
            saveStagingArea((Map<String, String>) data);
            System.out.println("已撤销add操作");
        } else {
            System.out.println("无法撤销add操作：状态数据损坏");
            System.exit(1);
        }
    }
    
    private static void undoCommit(Map<String, Object> params, Object data) {
        // 恢复操作前的提交和暂存区状态
        if (data instanceof Map) {
            Map<String, Object> commitData = (Map<String, Object>) data;
            Commit commitBefore = (Commit) commitData.get("commit");
            Map<String, String> stagingBefore = (Map<String, String>) commitData.get("staging");
            
            // 恢复分支引用到之前的提交
            String currentBranch = getCurrentBranch();
            writeContents(join(HEADS_DIR, currentBranch), commitBefore.getId());
            
            // 恢复暂存区
            saveStagingArea(stagingBefore);
            
            System.out.println("已撤销commit操作");
        } else {
            System.out.println("无法撤销commit操作：状态数据损坏");
            System.exit(1);
        }
    }
    
    private static void undoRm(Map<String, Object> params, Object data) {
        // 恢复操作前的暂存区状态和文件内容
        if (data instanceof Map) {
            Map<String, Object> rmData = (Map<String, Object>) data;
            Map<String, String> stagingBefore = (Map<String, String>) rmData.get("staging");
            Map<String, byte[]> deletedFilesContent = (Map<String, byte[]>) rmData.get("deletedFilesContent");
            
            // 恢复暂存区
            saveStagingArea(stagingBefore);
            
            // 恢复被删除的文件到工作目录
            if (deletedFilesContent != null) {
                for (Map.Entry<String, byte[]> entry : deletedFilesContent.entrySet()) {
                    String fileName = entry.getKey();
                    byte[] content = entry.getValue();
                    writeFileToWorkDir(fileName, content);
                }
            }
            
            System.out.println("已撤销rm操作");
        } else {
            System.out.println("无法撤销rm操作：状态数据损坏");
            System.exit(1);
        }
    }
    
    private static void undoBranch(Map<String, Object> params, Object data) {
        String branchName = (String) params.get("branchName");
        File branchFile = join(HEADS_DIR, branchName);
        if (branchFile.exists()) {
            branchFile.delete();
            System.out.println("已撤销branch操作");
        } else {
            System.out.println("分支不存在，无法撤销");
            System.exit(1);
        }
    }
    
    private static void undoRmBranch(Map<String, Object> params, Object data) {
        // 恢复被删除的分支
        String branchName = (String) params.get("branchName");
        if (data instanceof String) {
            String branchRef = (String) data;
            writeContents(join(HEADS_DIR, branchName), branchRef);
            System.out.println("已撤销rm-branch操作");
        } else {
            System.out.println("无法撤销rm-branch操作：状态数据损坏");
            System.exit(1);
        }
    }
    
    private static void undoCheckout(Map<String, Object> params, Object data) {
        String branchName = (String) params.get("branchName");
        String fileName = (String) params.get("fileName");
        
        // 区分分支checkout和文件checkout
        if (branchName != null) {
            // 分支checkout撤销
            String previousBranch = (String) params.get("previousBranch");
            if (previousBranch != null) {
                writeContents(HEAD, "ref: refs/heads/" + previousBranch);
                System.out.println("已撤销checkout操作");
            } else {
                System.out.println("无法撤销checkout操作：状态数据损坏");
                System.exit(1);
            }
        } else if (fileName != null) {
            // 文件checkout撤销
            if (data instanceof Map) {
                Map<String, byte[]> fileContents = (Map<String, byte[]>) data;
                // 恢复文件内容
                for (Map.Entry<String, byte[]> entry : fileContents.entrySet()) {
                    String filePath = entry.getKey();
                    byte[] content = entry.getValue();
                    writeFileToWorkDir(filePath, content);
                }
                System.out.println("已撤销checkout操作");
            } else {
                System.out.println("无法撤销checkout操作：状态数据损坏");
                System.exit(1);
            }
        } else {
            System.out.println("无法撤销checkout操作：状态数据损坏");
            System.exit(1);
        }
    }
    
    private static void undoReset(Map<String, Object> params, Object data) {
        // 恢复reset操作前的状态
        if (data instanceof Map) {
            Map<String, Object> resetData = (Map<String, Object>) data;
            Map<String, String> stagingBefore = (Map<String, String>) resetData.get("staging");
            String branchRef = (String) resetData.get("branchRef");
            String branchName = (String) resetData.get("branchName");
            Commit commitBefore = (Commit) resetData.get("commit");
            
            // 恢复分支引用
            writeContents(join(HEADS_DIR, branchName), branchRef);
            
            // 恢复暂存区
            saveStagingArea(stagingBefore);
            
            // 恢复工作区
            updateWorkingDirectory(commitBefore.getFileToBlobID());
            
            System.out.println("已撤销reset操作");
        } else {
            System.out.println("无法撤销reset操作：状态数据损坏");
            System.exit(1);
        }
    }
    
    private static void undoMerge(Map<String, Object> params, Object data) {
        // 撤销merge操作，回退到合并前的状态
        if (data instanceof Map) {
            Map<String, Object> mergeData = (Map<String, Object>) data;
            Commit commitBefore = (Commit) mergeData.get("commit");
            Map<String, String> stagingBefore = (Map<String, String>) mergeData.get("staging");
            String mergeType = (String) mergeData.get("type"); // 合并类型："fast-forward", "no-need", "normal"
            if ("no-need".equals(mergeType)) {
                // 无需合并的情况，不需要做任何操作
                System.out.println("已撤销merge操作");
                return;
            }
            
            // 恢复分支引用到之前的提交
            String currentBranch = getCurrentBranch();
            writeContents(join(HEADS_DIR, currentBranch), commitBefore.getId());
            
            // 恢复暂存区
            saveStagingArea(stagingBefore);
            
            // 快进合并和普通合并都需要更新工作目录
            updateWorkingDirectory(commitBefore.getFileToBlobID());

            System.out.println("已撤销merge操作");
        } else {
            System.out.println("无法撤销merge操作：状态数据损坏");
            System.exit(1);
        }
    }
    
    public static void help() {
        System.out.println("Gitlet 版本控制系统 - 可用命令:");
        System.out.println();
        System.out.println("init");
        System.out.println("  初始化 Gitlet 版本库");
        System.out.println("  用法: gitlet init");
        System.out.println();
        System.out.println("add <文件名>");
        System.out.println("  添加文件或目录到暂存区");
        System.out.println("  用法: gitlet add <文件名>");
        System.out.println("  示例: gitlet add . (添加当前目录下所有文件)");
        System.out.println("  注意: 使用 <目录名>/ 形式明确指定添加目录");
        System.out.println();
        System.out.println("commit <提交信息>");
        System.out.println("  提交暂存区的更改");
        System.out.println("  用法: gitlet commit <提交信息>");
        System.out.println();
        System.out.println("rm <文件名>");
        System.out.println("  从版本库中删除文件或目录");
        System.out.println("  用法: gitlet rm <文件名>");
        System.out.println();
        System.out.println("log");
        System.out.println("  显示当前分支的提交历史");
        System.out.println("  用法: gitlet log");
        System.out.println();
        System.out.println("global-log");
        System.out.println("  显示所有提交的历史记录");
        System.out.println("  用法: gitlet global-log");
        System.out.println();
        System.out.println("find <提交信息>");
        System.out.println("  根据提交信息查找提交");
        System.out.println("  用法: gitlet find <提交信息>");
        System.out.println();
        System.out.println("status");
        System.out.println("  显示当前仓库状态");
        System.out.println("  用法: gitlet status");
        System.out.println();
        System.out.println("checkout <分支名>");
        System.out.println("  切换到指定分支");
        System.out.println("  用法: gitlet checkout <分支名>");
        System.out.println();
        System.out.println("checkout <提交ID> -- <文件名>");
        System.out.println("  从指定提交中检出文件或目录");
        System.out.println("  用法: gitlet checkout <提交ID> -- <文件名>");
        System.out.println();
        System.out.println("checkout -- <文件名>");
        System.out.println("  从当前提交中检出文件或目录");
        System.out.println("  用法: gitlet checkout -- <文件名>");
        System.out.println();
        System.out.println("branch <分支名>");
        System.out.println("  创建新分支");
        System.out.println("  用法: gitlet branch <分支名>");
        System.out.println();
        System.out.println("rm-branch <分支名>");
        System.out.println("  删除指定分支");
        System.out.println("  用法: gitlet rm-branch <分支名>");
        System.out.println();
        System.out.println("reset <提交ID>");
        System.out.println("  重置到指定提交");
        System.out.println("  用法: gitlet reset <提交ID>");
        System.out.println();
        System.out.println("merge <分支名>");
        System.out.println("  将指定分支合并到当前分支");
        System.out.println("  用法: gitlet merge <分支名>");
        System.out.println();
        System.out.println("ignore [<文件名>]");
        System.out.println("  显示忽略规则列表或检查特定文件是否被忽略");
        System.out.println("  用法: gitlet ignore 或 gitlet ignore <文件名>");
        System.out.println();
        System.out.println("--help");
        System.out.println("  显示此帮助信息");
        System.out.println("  用法: gitlet --help");
        System.out.println();
        System.out.println("undo");
        System.out.println("  撤销上一步操作");
        System.out.println("  用法: gitlet undo");
    }
}
