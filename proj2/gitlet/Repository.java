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
            System.exit(0);
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
    private static void getCurrentBranchLog(Commit startCommit) {
        Set<String> visited = new HashSet<>();
        // 优先队列按提交时间从新到旧排序
        PriorityQueue<Commit> queue = new PriorityQueue<>((c1, c2) ->
                c2.getTimestamp().compareTo(c1.getTimestamp())
        );
        queue.add(startCommit);
        visited.add(startCommit.getId());

        while (!queue.isEmpty()) {
            Commit commit = queue.poll();
            printCommit(commit);

            // 处理第一个父提交
            if (commit.getParent1ID() != null) {
                String parent1Id = commit.getParent1ID();
                if (!visited.contains(parent1Id)) {
                    Commit parent1 = readObject(join(OBJECTS_DIR, parent1Id), Commit.class);
                    queue.add(parent1);
                    visited.add(parent1Id);
                }
            }

            // 处理第二个父提交（合并提交）
            if (commit.getParent2ID() != null) {
                String parent2Id = commit.getParent2ID();
                if (!visited.contains(parent2Id)) {
                    Commit parent2 = readObject(join(OBJECTS_DIR, parent2Id), Commit.class);
                    queue.add(parent2);
                    visited.add(parent2Id);
                }
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
        Set<String> visited = new HashSet<>();
        PriorityQueue<Commit> queue = new PriorityQueue<>((c1, c2) ->
                c2.getTimestamp().compareTo(c1.getTimestamp())
        );

        // 添加所有分支的最新提交到队列
        for (File branchFile : HEADS_DIR.listFiles()) {
            String commitHash = readContentsAsString(branchFile);
            if (!visited.contains(commitHash)) {
                Commit commit = readObject(join(OBJECTS_DIR, commitHash), Commit.class);
                queue.add(commit);
                visited.add(commitHash);
            }
        }

        // 遍历所有可达提交
        while (!queue.isEmpty()) {
            Commit commit = queue.poll();
            printCommit(commit);

            // 处理父提交
            addParentToQueue(commit.getParent1ID(), queue, visited);
            addParentToQueue(commit.getParent2ID(), queue, visited);
        }
    }

    private static void addParentToQueue(String parentId, PriorityQueue<Commit> queue, Set<String> visited) {
        if (parentId != null && !visited.contains(parentId)) {
            Commit parent = readObject(join(OBJECTS_DIR, parentId), Commit.class);
            queue.add(parent);
            visited.add(parentId);
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
            System.exit(0);
        } else if(branchName.equals(getCurrentBranch())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Commit targetCommit = readObject(join(OBJECTS_DIR, readContentsAsString(branch)), Commit.class);
        reset(targetCommit.getId(), true);
        writeContents(HEAD, "ref: refs/heads/" + branchName);

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

    private static void checkUntrackedFiles(Commit targetCommit) {
        Commit currentCommit = getCurrentCommit(); // 获取当前 HEAD 提交
        Map<String, String> staging = readStagingArea();

        for (String fileName : plainFilenamesIn(CWD)) {
            boolean inTarget = targetCommit.getFileToBlobID().containsKey(fileName);
            boolean inStaging = staging.containsKey(fileName);
            boolean inWorking = join(CWD, fileName).exists();
            boolean inCurrent = currentCommit.getFileToBlobID().containsKey(fileName);

            // 规则1：完全未跟踪文件（不在任何提交或暂存区）
            if (!inTarget && !inStaging && !inCurrent && inWorking) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            // 规则2：工作区文件与当前提交不一致，且未暂存 → 可能丢失修改
            if (inCurrent && !inStaging && inWorking) {
                Blob currentBlob = readObject(join(OBJECTS_DIR, currentCommit.getFileToBlobID().get(fileName)), Blob.class);
                Blob wrokingBlob = new Blob(readContents(join(CWD, fileName)));
                if (!currentBlob.getID().equals(wrokingBlob.getID())) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            // 新增规则3：暂存区中的新增文件（未被任何提交跟踪）
            if (inStaging) {
                System.out.println("Cannot switch branches with untracked files in staging area.");
                System.exit(0);
            }
        }
    }
    public static void reset(String commitID, boolean isCheckOutBranch) {
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

        String currentBranch = getCurrentBranch();
        if (!isCheckOutBranch)
            writeContents(join(HEADS_DIR, currentBranch), commitID);

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
            System.exit(0);
        }

        // 情况2：当前分支是给定分支的祖先
            if (splitPoint.getId().equals(currentCommit.getId())) {
                checkout(branchName); // 复用checkout逻辑
                System.out.println("Current branch fast-forwarded.");
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
            // 两个分支都修改
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
        // ========== 7. 更新工作目录 ==========
        // 删除合并提交中不存在的文件
        for (File file : CWD.listFiles()) {
            String fileName = file.getName();
            if (!mergedFiles.containsKey(fileName)) {
                file.delete();
            }
        }

        // 写入合并后的文件内容
        for (Map.Entry<String, String> entry : mergedFiles.entrySet()) {
            String fileName = entry.getKey();
            String blobHash = entry.getValue();
            File file = join(CWD, fileName);

            if (blobHash != null) {
                Blob blob = readObject(join(OBJECTS_DIR, blobHash), Blob.class);
                writeContents(file, blob.getContent());
            } else {
                // 如果blobHash为null，表示文件应被删除
                if (file.exists()) {
                    file.delete();
                }
            }
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
            out.write("current<<<<<<< HEAD\n".getBytes(StandardCharsets.UTF_8));
            if (current != null) out.write(current);
            out.write("\n=======\n".getBytes(StandardCharsets.UTF_8));
            if (given != null) out.write(given);
            out.write("\n>>>>>>>given\n".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Conflict generation failed");
        }
        return out.toByteArray();
    }

    private static Commit findSplitPoint(Commit current, Commit given) {
        Set<String> visited = new HashSet<>();

        // 遍历 current 的祖先
        while (current != null) {
            visited.add(current.getId());
            current = getFirstParent(current); // 确保 getFirstParent 处理 null
        }

        // 遍历 given 的祖先，寻找第一个已访问的提交
        while (given != null) {
            if (visited.contains(given.getId())) {
                return given;
            }
            given = getFirstParent(given);
        }

        return null; // 无共同祖先（理论上不可能）
    }

    private static Commit getFirstParent(Commit commit) {
        if (commit == null || commit.getParent1ID() == null) {
            return null;
        }
        return readObject(join(OBJECTS_DIR, commit.getParent1ID()), Commit.class);
    }


}
