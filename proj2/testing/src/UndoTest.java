package testing.src;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class UndoTest {
    private static String classPath;
    private Path tempDir;

    @BeforeAll
    static void compileProject() throws IOException, InterruptedException {
        ProcessBuilder compilePb = new ProcessBuilder("javac", "-encoding", "UTF-8", "-d", "out", "gitlet/*.java");
        Process compileProcess = compilePb.start();
        int exitCode = compileProcess.waitFor();
        assertEquals(0, exitCode, "项目编译失败");
        classPath = "out";
    }

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
    }

    private CommandResult runGitletCommand(String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-Dfile.encoding=UTF-8");
        command.add("-Dgitlet.cwd=" + tempDir.toString());
        command.add("-cp");
        command.add(classPath);
        command.add("gitlet.Main");
        Collections.addAll(command, args);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        return new CommandResult(exitCode, output);
    }

    private static class CommandResult {
        public final int exitCode;
        public final String output;

        public CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }

    // 辅助方法：创建测试文件
    private void createFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }

    // 辅助方法：读取文件内容
    private String readFile(String fileName) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
    }

    // 辅助方法：文件是否存在
    private boolean fileExists(String fileName) {
        return Files.exists(tempDir.resolve(fileName));
    }

    @Test
    void testUndoWithoutOperation() throws IOException, InterruptedException {
        runGitletCommand("init");
        
        CommandResult result = runGitletCommand("undo");
        assertNotEquals(0, result.exitCode, "没有操作时undo应该失败");
        assertTrue(result.output.contains("没有可撤销的操作"), "应该提示没有可撤销的操作");
    }

    @Test
    void testUndoAdd() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");

        // 添加文件
        CommandResult addResult = runGitletCommand("add", "test.txt");
        assertEquals(0, addResult.exitCode, "add 命令应该成功执行");

        // 检查添加前的状态
        CommandResult statusBefore = runGitletCommand("status");
        assertTrue(statusBefore.output.contains("test.txt"), "添加后文件应该在暂存区中");

        // 撤销添加操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销add操作"), "应该提示已撤销add操作");

        // 检查状态，文件应该不再暂存
        CommandResult statusAfter = runGitletCommand("status");
        assertFalse(statusAfter.output.contains("test.txt"), "撤销后文件不应该在暂存区中");
    }

    @Test
    void testUndoAddDot() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test1.txt", "Hello World 1");
        createFile("test2.txt", "Hello World 2");
        Files.createDirectories(tempDir.resolve("subdir"));
        createFile("subdir/test3.txt", "Hello World 3");

        // 添加所有文件
        CommandResult addResult = runGitletCommand("add", ".");
        assertEquals(0, addResult.exitCode, "add . 命令应该成功执行");

        // 检查添加后的状态
        CommandResult statusBefore = runGitletCommand("status");
        assertTrue(statusBefore.output.contains("test1.txt"), "添加后test1.txt应该在暂存区中");
        assertTrue(statusBefore.output.contains("test2.txt"), "添加后test2.txt应该在暂存区中");
        assertTrue(statusBefore.output.contains("subdir/test3.txt"), "添加后subdir/test3.txt应该在暂存区中");

        // 撤销添加操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销add操作"), "应该提示已撤销add操作");

        // 检查状态，文件应该不再暂存
        CommandResult statusAfter = runGitletCommand("status");
        assertFalse(statusAfter.output.contains("test1.txt"), "撤销后test1.txt不应该在暂存区中");
        assertFalse(statusAfter.output.contains("test2.txt"), "撤销后test2.txt不应该在暂存区中");
        assertFalse(statusAfter.output.contains("subdir/test3.txt"), "撤销后subdir/test3.txt不应该在暂存区中");
    }

    @Test
    void testUndoCommit() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");
        
        // 提交前检查暂存区
        CommandResult statusBeforeCommit = runGitletCommand("status");
        assertTrue(statusBeforeCommit.output.contains("test.txt"), "提交前文件应该在暂存区中");

        // 提交
        CommandResult commitResult = runGitletCommand("commit", "First commit");
        assertEquals(0, commitResult.exitCode, "commit 命令应该成功执行");
        
        // 提交后检查暂存区应该为空
        CommandResult statusAfterCommit = runGitletCommand("status");
        assertFalse(statusAfterCommit.output.contains("test.txt"), "提交后暂存区应该为空");

        // 撤销提交
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销commit操作"), "应该提示已撤销commit操作");

        // 检查日志，应该没有"First commit"
        CommandResult logResult = runGitletCommand("log");
        assertFalse(logResult.output.contains("First commit"), "提交信息不应该存在");
        
        // 检查暂存区状态是否恢复
        CommandResult statusAfterUndo = runGitletCommand("status");
        assertTrue(statusAfterUndo.output.contains("test.txt"), "撤销提交后文件应该回到暂存区");
    }

    @Test
    void testUndoRm() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Add file");

        // 删除文件前检查文件存在
        assertTrue(fileExists("test.txt"), "删除前文件应该存在");
        String originalContent = readFile("test.txt");

        // 删除文件
        CommandResult rmResult = runGitletCommand("rm", "test.txt");
        assertEquals(0, rmResult.exitCode, "rm 命令应该成功执行");

        // 删除后检查文件不存在
        assertFalse(fileExists("test.txt"), "删除后文件不应该存在");

        // 撤销删除操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销rm操作"), "应该提示已撤销rm操作");

        // 检查状态，文件应该重新回到暂存区
        CommandResult statusResult = runGitletCommand("status");
        assertTrue(statusResult.output.contains("test.txt"), "撤销删除后文件应该回到暂存区");
        
        // 检查文件是否恢复到工作目录
        assertTrue(fileExists("test.txt"), "撤销删除后文件应该恢复到工作目录");
        assertEquals(originalContent, readFile("test.txt"), "撤销删除后文件内容应该与原来一致");
    }

    @Test
    void testUndoBranch() throws IOException, InterruptedException {
        runGitletCommand("init");

        // 创建分支前检查
        CommandResult statusBefore = runGitletCommand("status");
        assertFalse(statusBefore.output.contains("new-branch"), "创建前new-branch分支不应该存在");

        // 创建分支
        CommandResult branchResult = runGitletCommand("branch", "new-branch");
        assertEquals(0, branchResult.exitCode, "branch 命令应该成功执行");

        // 创建后检查
        CommandResult statusAfter = runGitletCommand("status");
        assertTrue(statusAfter.output.contains("new-branch"), "创建后new-branch分支应该存在");

        // 撤销创建分支操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销branch操作"), "应该提示已撤销branch操作");

        // 检查分支是否存在
        Path branchFile = tempDir.resolve(".gitlet").resolve("refs").resolve("heads").resolve("new-branch");
        assertFalse(Files.exists(branchFile), "分支文件应该被删除");
        
        // 检查状态
        CommandResult statusAfterUndo = runGitletCommand("status");
        assertFalse(statusAfterUndo.output.contains("new-branch"), "撤销后new-branch分支不应该存在");
    }

    @Test
    void testUndoRmBranch() throws IOException, InterruptedException {
        runGitletCommand("init");
        CommandResult BranchResult = runGitletCommand("branch", "to-delete");
        assertEquals(0, BranchResult.exitCode, "branch 命令应该成功执行");
        
        // 删除前检查分支存在
        CommandResult statusBefore = runGitletCommand("status");
        assertTrue(statusBefore.output.contains("to-delete"), "删除前to-delete分支应该存在");

        // 删除分支
        CommandResult rmBranchResult = runGitletCommand("rm-branch", "to-delete");
        assertEquals(0, rmBranchResult.exitCode, "rm-branch 命令应该成功执行");

        // 删除后检查分支不存在
        CommandResult statusAfter = runGitletCommand("status");
        assertFalse(statusAfter.output.contains("to-delete"), "删除后to-delete分支不应该存在");

        // 撤销删除分支操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销rm-branch操作"), "应该提示已撤销rm-branch操作");

        // 检查分支是否恢复
        Path branchFile = tempDir.resolve(".gitlet").resolve("refs").resolve("heads").resolve("to-delete");
        assertTrue(Files.exists(branchFile), "分支文件应该被恢复");
        
        // 检查状态
        CommandResult statusAfterUndo = runGitletCommand("status");
        assertTrue(statusAfterUndo.output.contains("to-delete"), "撤销后to-delete分支应该存在");
    }

    @Test
    void testUndoCheckout() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "First commit");
        runGitletCommand("branch", "new-branch");

        // 切换前检查分支
        CommandResult statusBefore = runGitletCommand("status");
        assertTrue(statusBefore.output.contains("*master"), "切换前应该在master分支");

        // 切换分支
        CommandResult checkoutResult = runGitletCommand("checkout", "new-branch");
        assertEquals(0, checkoutResult.exitCode, "checkout 命令应该成功执行");

        // 切换后检查分支
        CommandResult statusAfter = runGitletCommand("status");
        assertTrue(statusAfter.output.contains("*new-branch"), "切换后应该在new-branch分支");

        // 撤销切换分支操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销checkout操作"), "应该提示已撤销checkout操作");

        // 检查当前分支
        CommandResult statusAfterUndo = runGitletCommand("status");
        assertTrue(statusAfterUndo.output.contains("*master"), "撤销后应该回到master分支");
    }

    @Test
    void testUndoCheckoutFile() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Version 1");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "First commit");
        
        // 修改文件
        createFile("test.txt", "Version 2");
        
        // 检出文件前检查内容
        assertEquals("Version 2", readFile("test.txt"), "检出前文件应该是Version 2");

        // 检出文件
        CommandResult checkoutResult = runGitletCommand("checkout", "--", "test.txt");
        assertEquals(0, checkoutResult.exitCode, "checkout -- <文件名> 命令应该成功执行");
        
        // 检查检出后的状态
        assertEquals("Version 1", readFile("test.txt"), "检出后文件应该是Version 1");

        // 撤销检出操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销checkout操作"), "应该提示已撤销checkout操作");
        
        // 文件内容应该恢复到检出前的状态（Version 2）
        assertEquals("Version 2", readFile("test.txt"), "撤销后文件应该恢复到检出前的状态");
    }

    @Test
    void testUndoCheckoutFileFromCommit() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Version 1");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "First commit");
        
        String firstCommitId = getFirstCommitId();
        
        // 修改文件并提交
        createFile("test.txt", "Version 2");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Second commit");
        
        // 检出文件前检查内容
        assertEquals("Version 2", readFile("test.txt"), "检出前文件应该是Version 2");

        // 从第一个提交检出文件
        CommandResult checkoutResult = runGitletCommand("checkout", firstCommitId, "--", "test.txt");
        assertEquals(0, checkoutResult.exitCode, "checkout <提交ID> -- <文件名> 命令应该成功执行");
        
        // 检查检出后的状态
        assertEquals("Version 1", readFile("test.txt"), "检出后文件应该是Version 1");

        // 撤销检出操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销checkout操作"), "应该提示已撤销checkout操作");
        
        // 文件内容应该恢复到检出前的状态（Version 2）
        assertEquals("Version 2", readFile("test.txt"), "撤销后文件应该恢复到检出前的状态");
    }

    @Test
    void testUndoReset() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test1.txt", "Version 1");
        runGitletCommand("add", "test1.txt");
        runGitletCommand("commit", "First commit");
        
        String firstCommitId = getFirstCommitId();
        
        createFile("test2.txt", "Version 2");
        runGitletCommand("add", "test2.txt");
        runGitletCommand("commit", "Second commit");

        // 重置前检查文件存在
        assertTrue(fileExists("test1.txt"), "重置前test1.txt应该存在");
        assertTrue(fileExists("test2.txt"), "重置前test2.txt应该存在");

        // 重置到第一个提交
        CommandResult resetResult = runGitletCommand("reset", firstCommitId);
        assertEquals(0, resetResult.exitCode, "reset 命令应该成功执行");

        // 重置后检查文件
        assertTrue(fileExists("test1.txt"), "重置后test1.txt应该存在");
        assertFalse(fileExists("test2.txt"), "重置后test2.txt不应该存在");

        // 撤销重置操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销reset操作"), "应该提示已撤销reset操作");

        // 检查文件是否存在
        assertTrue(fileExists("test1.txt"), "撤销后test1.txt应该存在");
        assertTrue(fileExists("test2.txt"), "撤销后test2.txt应该存在");
    }

    @Test
    void testUndoMerge() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Base content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Base commit");

        // 创建并切换到新分支
        runGitletCommand("branch", "feature");
        runGitletCommand("checkout", "feature");

        // 在feature分支修改文件
        createFile("test.txt", "Feature content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Feature commit");

        // 切换回master分支
        runGitletCommand("checkout", "master");
        
        // 合并前检查内容
        assertEquals("Base content", readFile("test.txt"), "合并前master分支文件应该是Base content");

        // 合并feature分支
        CommandResult mergeResult = runGitletCommand("merge", "feature");
        assertEquals(0, mergeResult.exitCode, "merge 命令应该成功执行");
        
        // 检查合并后的状态
        assertEquals("Feature content", readFile("test.txt"), "合并后文件应该是Feature content");

        // 撤销合并操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        System.out.println(undoResult.output);
        assertTrue(undoResult.output.contains("已撤销merge操作"), "应该提示已撤销merge操作");
        
        // 文件内容应该恢复到合并前
        assertEquals("Base content", readFile("test.txt"), "撤销后文件应该恢复到Base content");
        
        // 检查分支应该回到master
        CommandResult statusResult = runGitletCommand("status");
        assertTrue(statusResult.output.contains("*master"), "撤销后应该在master分支");
    }
    
    @Test
    void testUndoMergeWithUncommittedFiles() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Base content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Base commit");
        runGitletCommand("branch", "feature");

        // 添加文件到暂存区但不提交
        createFile("uncommitted.txt", "Uncommitted file");
        runGitletCommand("add", "uncommitted.txt");

        // 尝试合并应该失败
        CommandResult mergeResult = runGitletCommand("merge", "feature");
        assertNotEquals(0, mergeResult.exitCode, "当有未提交文件时merge应该失败");
        assertTrue(mergeResult.output.contains("缓存区存在未提交的文件"), "应该提示缓存区存在未提交的文件");

        // undo应该提示没有可撤销的操作，因为merge没有成功执行
        CommandResult undoResult = runGitletCommand("undo");
        assertNotEquals(0, undoResult.exitCode, "上一步命令执行中止，操作无法撤销");
        assertTrue(undoResult.output.contains("不支持撤销该操作"), "应该提示不支持撤销该操作");
    }

    @Test
    void testUndoMergeWithNonExistentBranch() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Base content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Base commit");

        // 尝试合并不存在的分支应该失败
        CommandResult mergeResult = runGitletCommand("merge", "nonexistent");
        assertNotEquals(0, mergeResult.exitCode, "当分支不存在时merge应该失败");
        assertTrue(mergeResult.output.contains("指定合并分支不存在"), "应该提示指定合并分支不存在");

        // undo应该提示没有可撤销的操作，因为merge没有成功执行
        CommandResult undoResult = runGitletCommand("undo");
        assertNotEquals(0, undoResult.exitCode, "上一步命令执行中止，操作无法撤销");
        assertTrue(undoResult.output.contains("不支持撤销该操作"), "应该提示不支持撤销该操作");
    }

    @Test
    void testUndoMergeWithSelf() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Base content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Base commit");

        // 尝试与自身合并应该失败
        CommandResult mergeResult = runGitletCommand("merge", "master");
        assertNotEquals(0, mergeResult.exitCode, "与自身合并应该失败");
        assertTrue(mergeResult.output.contains("分支无法与自身合并"), "应该提示分支无法与自身合并");

        // undo应该提示没有可撤销的操作，因为merge没有成功执行
        CommandResult undoResult = runGitletCommand("undo");
        System.out.println(undoResult.output);
        assertNotEquals(0, undoResult.exitCode, "没有操作时undo应该失败");
        assertTrue(undoResult.output.contains("不支持撤销该操作"), "应该提示没有可撤销的操作");
    }

    @Test
    void testUndoMergeNoNeed() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Base content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Base commit");

        // 创建feature分支（当前分支的祖先）
        runGitletCommand("branch", "feature");
        
        // 在master上添加新提交，使master成为feature的后代
        createFile("new.txt", "New file");
        runGitletCommand("add", "new.txt");
        runGitletCommand("commit", "New commit");

        // 合并master分支（feature的祖先）
        CommandResult mergeResult = runGitletCommand("merge", "feature");
        assertEquals(0, mergeResult.exitCode, "merge命令应该成功执行");
        System.out.println(mergeResult.output);
        assertTrue(mergeResult.output.contains("指定的分支是当前分支的祖先"), "应该提示无需合并");

        // 撤销合并操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销merge操作"), "应该提示已撤销merge操作");
    }

    @Test
    void testUndoMergeConflict() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Base content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Base commit");

        // 创建并切换到新分支
        runGitletCommand("branch", "feature");
        runGitletCommand("checkout", "feature");

        // 在feature分支修改文件
        createFile("test.txt", "Feature content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Feature commit");

        // 切换回master分支
        runGitletCommand("checkout", "master");
        
        // 在master分支修改同一个文件，造成冲突
        createFile("test.txt", "Master content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Master commit");

        // 合并feature分支，应该产生冲突
        CommandResult mergeResult = runGitletCommand("merge", "feature");
        assertEquals(0, mergeResult.exitCode, "merge命令应该成功执行");
        assertTrue(mergeResult.output.contains("出现合并冲突，请手动调整冲突文件"), "应该提示出现合并冲突");

        // 检查冲突文件是否存在
        assertTrue(fileExists("test.txt"), "冲突文件应该存在");

        // 撤销合并操作
        CommandResult undoResult = runGitletCommand("undo");
        assertEquals(0, undoResult.exitCode, "undo 命令应该成功执行");
        assertTrue(undoResult.output.contains("已撤销merge操作"), "应该提示已撤销merge操作");
        
        // 文件内容应该恢复到合并前（master分支的内容）
        assertEquals("Master content", readFile("test.txt"), "撤销后文件应该恢复到master分支的内容");
    }

    // 辅助方法：获取第一个提交ID
    private String getFirstCommitId() throws IOException, InterruptedException {
        CommandResult logResult = runGitletCommand("log");
        String[] lines = logResult.output.split("\n");
        // 从后往前找第一个commit行
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i];
            if (line.startsWith("commit ")) {
                return line.substring("commit ".length()).trim();
            }
        }
        return "unknown";
    }
}