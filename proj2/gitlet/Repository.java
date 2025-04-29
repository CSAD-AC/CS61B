package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author 逐辰
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File STAGING_AREA = join(GITLET_DIR, "index");


    public static final File HEAD = join(GITLET_DIR, "HEAD");
    static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
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
        return readObject(join(OBJECTS_DIR, commitHash), Commit.class);
    }
    //获取当前分支
    private static String getCurrentBranch() {
        return readContentsAsString(HEAD).substring("ref: refs/heads/".length()).trim();
    }
    //保存blob
    private static void saveBlobIfNotExists(Blob blob) {
        File blobFile = join(OBJECTS_DIR, blob.getID());
        if (!blobFile.exists()) {
            writeObject(blobFile, blob);
        }
    }
    public static void checkInGitlet() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
    public static void add(String fileName) {
        // 1. 检查是否在 Gitlet 目录中
        checkInGitlet();

        // 2. 检查文件是否存在
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        // 3. 读取当前提交和暂存区
        Commit currentCommit = getCurrentCommit();
        Map<String, String> stagingArea = readStagingArea();

        // 4. 生成当前文件的 Blob
        Blob newBlob = new Blob(readContents(file));
        String newBlobHash = newBlob.getID();

        // 5. 检查是否与当前提交的文件内容相同
        String currentBlobHash = currentCommit.getFileToBlobID().get(fileName);
        if (newBlobHash.equals(currentBlobHash)) {
            // 内容相同 → 从暂存区移除（如果存在）
            stagingArea.remove(fileName);
            saveStagingArea(stagingArea);
            return;
        }

        // 6. 暂存文件（覆盖旧记录）
        stagingArea.put(fileName, newBlobHash);
        saveStagingArea(stagingArea);

        // 7. 保存 Blob 到对象库（如果不存在）
        saveBlobIfNotExists(newBlob);
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

        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Map<String, String> stagingArea = readStagingArea();
        if (stagingArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // 创建新提交（继承当前提交的文件映射，并用暂存区覆盖）
        Commit currentCommit = getCurrentCommit();
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

        Commit newCommit = new Commit(
                currentCommit.getId(), // parent1
                null,                 // parent2
                new Date(),
                message,
                newFileToBlob
        );

        // 保存提交并更新分支
        saveCommit(newCommit);
        clearStagingArea();

    }
    public static void rm(String fileName) {
        checkInGitlet();

        Map<String, String> stagingArea = readStagingArea();
        Commit currentCommit = getCurrentCommit();

        if (!stagingArea.containsKey(fileName) && !currentCommit.getFileToBlobID().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        // 标记为删除
        stagingArea.put(fileName, null);
        saveStagingArea(stagingArea);
        File file = join(CWD, fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    private static void getCurrentBranchLog(Commit currentCommit) {
        while (currentCommit != null) {
            printCommit(currentCommit);

            if (currentCommit.getParent1ID() != null) {
                Commit currentCommitParent = readObject(join(OBJECTS_DIR, currentCommit.getParent1ID()), Commit.class);
                currentCommit = currentCommitParent;
            } else {
                currentCommit = null;
            }
        }
    }
    private static void printCommit(Commit currentCommit) {
        System.out.println("===");
        System.out.println("commit " + currentCommit.getId());
        System.out.println("Date: " + currentCommit.getTimestamp());
        System.out.println(currentCommit.getMessage());
        if (currentCommit.getParent2ID() != null) {
            System.out.println("Merge: " + currentCommit.getParent1ID().substring(0, 7) + " " + currentCommit.getParent2ID().substring(0, 7));
        }
        System.out.println();
    }
    public static void log() {
        checkInGitlet();

        Commit currentCommit = getCurrentCommit();
        getCurrentBranchLog(currentCommit);

    }
    public static void globalLog() {
        checkInGitlet();

        for(File headFile : HEADS_DIR.listFiles()){
            Commit currentCommit = readObject(join(OBJECTS_DIR, readContentsAsString(headFile)), Commit.class);
            getCurrentBranchLog(currentCommit);
        }
    }

    public static void find(String message){
        checkInGitlet();

        for(File headFile : HEADS_DIR.listFiles()){
            Commit currentCommit = readObject(join(OBJECTS_DIR, readContentsAsString(headFile)), Commit.class);
            while (currentCommit != null) {
                if(currentCommit.getMessage().contains(message))
                    printCommit(currentCommit);

                if (currentCommit.getParent1ID() != null) {
                    Commit currentCommitParent = readObject(join(OBJECTS_DIR, currentCommit.getParent1ID()), Commit.class);
                    currentCommit = currentCommitParent;
                } else {
                    currentCommit = null;
                }
            }
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
        File branch= join(HEADS_DIR, branchName);
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
        } else if(branchName.equals(getCurrentBranch())) {
            System.out.println("No need to checkout the current branch.");
        }
        writeContents(HEAD, "ref: refs/heads/" + branchName);
        Commit currentCommit = getCurrentCommit();
        reset(currentCommit.getId());

    }
    public static void checkout(String commitId, String fileName) {
        checkInGitlet();
        Commit currentCommit = getCurrentCommit();
        if(commitId.equals("--")) {
            getFileInCommit(fileName, currentCommit);
        } else {
            for (File headFile : HEADS_DIR.listFiles()) {
                currentCommit = readObject(join(OBJECTS_DIR, readContentsAsString(headFile)), Commit.class);
                while(currentCommit != null) {
                    if (currentCommit.getId().equals(commitId)) {
                        getFileInCommit(fileName, currentCommit);
                        System.exit(0);
                    }
                    if (currentCommit.getParent1ID() != null) {
                        currentCommit = readObject(join(OBJECTS_DIR, currentCommit.getParent1ID()), Commit.class);
                    } else {
                        currentCommit = null;
                    }
                }
            }
            System.out.println("No commit with that id exists.");
        }
    }

    private static void getFileInCommit(String fileName, Commit currentCommit) {
        if (!currentCommit.getFileToBlobID().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            String blobID = currentCommit.getFileToBlobID().get(fileName);
            Blob blob = readObject(join(OBJECTS_DIR, blobID), Blob.class);
            File file = join(CWD, fileName);
            writeContents(file, blob.getContent());
        }
    }

    public static void branch(String branchName) {
        checkInGitlet();
        if (join(HEADS_DIR, branchName).exists()) {
            System.out.println("A branch with that name already exists.");
        } else {
            writeContents(join(HEADS_DIR, branchName), getCurrentCommit().getId());
        }
    }

    public static void rmBranch(String branchName) {
        checkInGitlet();
        if (!join(HEADS_DIR, branchName).exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if(branchName.equals(getCurrentBranch())) {
            System.out.println("Cannot remove the current branch.");
        } else {
            join(HEADS_DIR, branchName).delete();
        }

    }

    public static void nbLog() {
        checkInGitlet();

        Commit currentCommit = getCurrentCommit();
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currentCommit.getId());
            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println("File: " + currentCommit.getFileToBlobID().keySet());
            System.out.println(currentCommit.getMessage());
            if (currentCommit.getParent2ID() != null) {
                System.out.println("Merge: " + currentCommit.getParent1ID().substring(0, 7) + " " + currentCommit.getParent2ID().substring(0, 7));
            }
            System.out.println();
            if (currentCommit.getParent1ID() != null) {
                Commit currentCommitParent = readObject(join(OBJECTS_DIR, currentCommit.getParent1ID()), Commit.class);
                currentCommit = currentCommitParent;
            } else {
                currentCommit = null;
            }
        }
    }
    private static void checkUntrackedFiles(Commit split, Commit current, Commit given) {
        for (String fileName : plainFilenamesIn(CWD)) {
            File file = join(CWD, fileName);
            // 文件未被跟踪的条件：
            // 1. 不在当前提交中
            // 2. 不在暂存区中
            boolean untracked = !current.getFileToBlobID().containsKey(fileName)
                    && !readStagingArea().containsKey(fileName);

            if (untracked) {
                // 检查是否会被覆盖
                String givenBlobHash = given.getFileToBlobID().get(fileName);
                String splitBlobHash = split.getFileToBlobID().get(fileName);

                if (givenBlobHash != null && !Objects.equals(givenBlobHash, splitBlobHash)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }
    private static void checkUntrackedFiles(Commit targetCommit) {
        Map<String, String> staging = readStagingArea();
        for (String fileName : plainFilenamesIn(CWD)) {
            boolean inCommit = targetCommit.getFileToBlobID().containsKey(fileName);
            boolean inStaging = staging.containsKey(fileName);
            boolean inWorking = join(CWD, fileName).exists();

            // 规则1：完全未跟踪文件
            if (!inCommit && !inStaging && inWorking) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            // 规则2：可能被覆盖的修改
            if (inCommit && !inStaging && inWorking) {
                Blob commitBlob = readObject(join(OBJECTS_DIR, targetCommit.getFileToBlobID().get(fileName)),  Blob.class);
                byte[] workingContent = readContents(join(CWD, fileName));
                if (!commitBlob.getContent().equals(workingContent)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }
    public static void reset(String commitID) {
        checkInGitlet();

        File commitFile = join(OBJECTS_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = readObject(commitFile, Commit.class);

        // 检查工作目录中是否有未跟踪的文件
        checkUntrackedFiles(commit);

        // 清空暂存区
        clearStagingArea();
        // 更新工作目录
        // 删除工作目录中多余的文件
        for (File file : CWD.listFiles()) {
            if (!commit.getFileToBlobID().containsKey(file.getName())) {
                file.delete();
            }
        }
        // 更新目标提交中存在的文件
        for (Map.Entry<String, String> entry : commit.getFileToBlobID().entrySet()) {
            String fileName = entry.getKey();
            File file = join(CWD, fileName);
            Blob blob = readObject(join(OBJECTS_DIR, entry.getValue()), Blob.class);
            writeContents(file, blob.getContent());

        }
        //  更新当前分支节点
        writeContents(join(HEADS_DIR, getCurrentBranch()), commit.getId());
    }

    public static void merge(String branchName) {
        // 1. 检查合并条件
        checkInGitlet();
        Map<String, String> stagingArea = readStagingArea();
        if (!stagingArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!join(HEADS_DIR, branchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(getCurrentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        // 2. 获取当前分支和被合并分支的最新提交
        Commit currentCommit = getCurrentCommit();
        Commit givenCommit = readObject(join(OBJECTS_DIR,readContentsAsString(join(HEADS_DIR, branchName))), Commit.class);

        // 3. 查找共同祖先（Split Point）
        Commit splitPoint = findSplitPoint(currentCommit, givenCommit);

        // 情况1：给定分支是当前分支的祖先
        if (splitPoint.getId().equals(givenCommit.getId())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        // 情况2：快进合并
            if (splitPoint.getId().equals(currentCommit.getId())) {
                checkout(branchName); // 复用checkout逻辑
                System.out.println("Current branch fast-forwarded.");
                return;
            }

        // ========== 4. 检查未跟踪文件冲突 ==========
        checkUntrackedFiles(splitPoint, currentCommit, givenCommit);

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
            byte[] baseContent = getBlobContent(baseBlobHash);
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

            // 规则5：新增文件冲突(分割点存在) → 生成冲突标记
            hasConflict = true;
            byte[] conflictContent = generateConflictContent(
                    currentContent,
                    givenContent
            );
            Blob conflictBlob = new Blob(conflictContent);
            saveBlob(conflictBlob);
            mergedFiles.put(fileName, conflictBlob.getID());
        }

        // ========== 6. 创建合并提交 ==========
        // 检查是否有实际变更（避免空提交）
        boolean hasRealChanges = !mergedFiles.equals(currentCommit.getFileToBlobID())
                || !mergedFiles.equals(givenCommit.getFileToBlobID());
        if (!hasRealChanges) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
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
            System.out.println("Encountered a merge conflict.");
        }
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
            out.write("\n=======\n".getBytes(StandardCharsets.UTF_8));
            if (given != null) out.write(given);
            out.write("\n>>>>>>>\n".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Conflict generation failed");
        }
        return out.toByteArray();
    }

    private static Commit findSplitPoint(Commit current, Commit given) {
        while (current != null) {
            Commit givenCommit = given;

            while (givenCommit != null) {
                if (current.getId().equals(givenCommit.getId())) {
                    return current;
                }
                givenCommit = readObject(join(OBJECTS_DIR, given.getParent1ID()), Commit.class);
            }
            current = readObject(join(OBJECTS_DIR, current.getParent1ID()), Commit.class);
        }
        return null;
    }


}
