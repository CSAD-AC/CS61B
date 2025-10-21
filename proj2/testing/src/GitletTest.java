package testing.src;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class GitletTest {
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
    void testInit() throws IOException, InterruptedException {
        CommandResult result = runGitletCommand("init");
        assertEquals(0, result.exitCode, "init 命令应该成功执行");

        Path gitletDir = tempDir.resolve(".gitlet");
        assertTrue(Files.exists(gitletDir), "应该创建 .gitlet 目录");
        assertTrue(Files.exists(gitletDir.resolve("objects")), "应该创建 objects 目录");
        assertTrue(Files.exists(gitletDir.resolve("HEAD")), "应该创建 HEAD 文件");
    }

    @Test
    void testInitAlreadyExists() throws IOException, InterruptedException {
        runGitletCommand("init");
        CommandResult result = runGitletCommand("init");
        assertNotEquals(0, result.exitCode, "重复 init 应该失败");
        assertTrue(result.output.contains("已经存在于当前文件夹"), "应该提示系统已存在");
    }

    @Test
    void testAddAndCommit() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");

        CommandResult addResult = runGitletCommand("add", "test.txt");
        assertEquals(0, addResult.exitCode, "add 命令应该成功执行");

        CommandResult commitResult = runGitletCommand("commit", "First commit");
        assertEquals(0, commitResult.exitCode, "commit 命令应该成功执行");

        CommandResult logResult = runGitletCommand("log");
        assertTrue(logResult.output.contains("First commit"), "提交信息应该正确");
    }

    @Test
    void testAddFileNotExist() throws IOException, InterruptedException {
        runGitletCommand("init");
        CommandResult result = runGitletCommand("add", "nonexistent.txt");
        assertNotEquals(0, result.exitCode, "添加不存在的文件应该失败");

        assertTrue(result.output.contains("文件不存在"), "应该提示文件不存在");
    }

    @Test
    void testCommitEmptyStaging() throws IOException, InterruptedException {
        runGitletCommand("init");
        CommandResult result = runGitletCommand("commit", "Empty commit");
        assertNotEquals(0, result.exitCode, "空暂存区提交应该失败");
        assertTrue(result.output.contains("提交没有新的内容"), "应该提示没有更改");
    }

    @Test
    void testCommitEmptyMessage() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");

        CommandResult result = runGitletCommand("commit", "");
        assertNotEquals(0, result.exitCode, "空消息提交应该失败");
        assertTrue(result.output.contains("请输入提交信息"), "应该提示输入提交信息");
    }

    @Test
    void testRm() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Add file");

        CommandResult rmResult = runGitletCommand("rm", "test.txt");
        assertEquals(0, rmResult.exitCode, "rm 命令应该成功执行");

        CommandResult commitResult = runGitletCommand("commit", "Remove file");
        assertEquals(0, commitResult.exitCode, "提交删除应该成功");
    }

    @Test
    void testRmNotTracked() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");

        CommandResult result = runGitletCommand("rm", "test.txt");
        assertNotEquals(0, result.exitCode, "删除未跟踪文件应该失败");
        assertTrue(result.output.contains("文件未被追踪"), "应该提示无删除理由");
    }

    @Test
    void testLog() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "First commit");

        CommandResult logResult = runGitletCommand("log");
        assertEquals(0, logResult.exitCode, "log 命令应该成功执行");
        assertTrue(logResult.output.contains("First commit"), "应该显示提交信息");
        assertFalse(logResult.output.contains("initial commit"), "不应该显示初始提交");
    }

    @Test
    void testGlobalLog() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "First commit");

        CommandResult result = runGitletCommand("global-log");
        assertEquals(0, result.exitCode, "global-log 命令应该成功执行");
        assertTrue(result.output.contains("First commit"), "应该显示提交信息");
    }

    @Test
    void testFind() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Important commit");

        CommandResult result = runGitletCommand("find", "Important commit");
        assertEquals(0, result.exitCode, "find 命令应该成功执行");
        assertTrue(result.output.contains("commit"), "应该找到对应的提交ID");
    }

    @Test
    void testFindNoCommit() throws IOException, InterruptedException {
        runGitletCommand("init");
        CommandResult result = runGitletCommand("find", "Nonexistent message");
        assertNotEquals(0, result.exitCode, "查找不存在的消息应该失败");
        assertTrue(result.output.contains("没有找到包含该信息的提交"), "应该提示未找到提交");
    }

    @Test
    void testStatus() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");

        CommandResult result = runGitletCommand("status");
        assertEquals(0, result.exitCode, "status 命令应该成功执行");
        assertTrue(result.output.contains("test.txt"), "应该显示暂存文件");
        assertTrue(result.output.contains("master"), "应该显示分支信息");
    }

    @Test
    void testCheckoutFile() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Version 1");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Version 1");

        createFile("test.txt", "Version 2");

        CommandResult result = runGitletCommand("checkout", "--", "test.txt");
        assertEquals(0, result.exitCode, "checkout 文件应该成功执行");
        assertEquals("Version 1", readFile("test.txt"), "文件应该恢复为版本1");
    }

    @Test
    void testCheckoutFileFromCommit() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Version 1");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Version 1");

        // 获取提交ID
        CommandResult logResult = runGitletCommand("log");
        String commitId = extractCommitId(logResult.output);

        createFile("test.txt", "Version 2");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Version 2");

        CommandResult result = runGitletCommand("checkout", commitId, "--", "test.txt");
        assertEquals(0, result.exitCode, "从指定提交checkout应该成功");
        assertEquals("Version 1", readFile("test.txt"), "文件应该恢复为版本1");
    }

    @Test
    void testBranchAndCheckout() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Hello World");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "First commit");

        CommandResult branchResult = runGitletCommand("branch", "new-branch");
        assertEquals(0, branchResult.exitCode, "创建分支应该成功");

        CommandResult checkoutResult = runGitletCommand("checkout", "new-branch");
        assertEquals(0, checkoutResult.exitCode, "切换分支应该成功");

        CommandResult statusResult = runGitletCommand("status");
        assertTrue(statusResult.output.contains("new-branch"), "应该显示新分支");
    }

    @Test
    void testRmBranch() throws IOException, InterruptedException {
        runGitletCommand("init");
        runGitletCommand("branch", "new-branch");

        CommandResult result = runGitletCommand("rm-branch", "new-branch");
        assertEquals(0, result.exitCode, "删除分支应该成功");
    }

    @Test
    void testRmCurrentBranch() throws IOException, InterruptedException {
        runGitletCommand("init");
        CommandResult result = runGitletCommand("rm-branch", "master");
        assertNotEquals(0, result.exitCode, "删除当前分支应该失败");
        assertTrue(result.output.contains("无法删除当前所在分支"), "应该提示不能删除当前分支");
    }

    @Test
    void testReset() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Version 1");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Version 1");

        createFile("test.txt", "Version 2");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Version 2");

        // 获取第一个提交的ID
        CommandResult logResult = runGitletCommand("log");
        System.out.println("Log output: " + logResult.output);
        String firstCommitId = extractFirstCommitId(logResult.output);
        System.out.println("First commit ID: " + firstCommitId);
        CommandResult resetResult = runGitletCommand("reset", firstCommitId);
        assertEquals(0, resetResult.exitCode, "reset 命令应该成功执行");

        assertEquals("Version 1", readFile("test.txt"), "文件应该重置为版本1");
    }

    @Test
    void testMerge() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("test.txt", "Base content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Base commit");

        runGitletCommand("branch", "feature");
        runGitletCommand("checkout", "feature");

        createFile("test.txt", "Feature content");
        runGitletCommand("add", "test.txt");
        runGitletCommand("commit", "Feature commit");

        runGitletCommand("checkout", "master");
        CommandResult mergeResult = runGitletCommand("merge", "feature");

        // 合并可能成功或产生冲突
        assertTrue(mergeResult.exitCode == 0 || mergeResult.output.contains("conflict"),
                "合并应该成功或报告冲突");
    }

    @Test
    void testMergeConflict() throws IOException, InterruptedException {
        runGitletCommand("init");
        createFile("conflict.txt", "Base");
        runGitletCommand("add", "conflict.txt");
        runGitletCommand("commit", "Base commit");

        runGitletCommand("branch", "feature");

        // 在master分支修改
        createFile("conflict.txt", "Master version");
        runGitletCommand("add", "conflict.txt");
        runGitletCommand("commit", "Master commit");

        // 在feature分支修改
        runGitletCommand("checkout", "feature");
        createFile("conflict.txt", "Feature version");
        runGitletCommand("add", "conflict.txt");
        runGitletCommand("commit", "Feature commit");

        runGitletCommand("checkout", "master");
        CommandResult result = runGitletCommand("merge", "feature");

        // 应该检测到冲突
        assertTrue(result.output.contains("出现合并冲突，请手动调整冲突文件"),
                "应该报告合并冲突");
    }

    // 辅助方法：从log输出中提取第一个（最旧的）提交ID
    private String extractFirstCommitId(String logOutput) {
        // log是倒序输出的，所以第一个（最旧的）提交在最后面
        String[] lines = logOutput.split("\n");
        // 从后往前找第一个commit行
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i];
            if (line.startsWith("commit ")) {
                return line.substring("commit ".length()).trim();
            }
        }
        return "unknown";
    }

    // 辅助方法：从log输出中提取第一个（最新的）提交ID
    private String extractCommitId(String logOutput) {
        // log是倒序输出的，所以第一个提交在最前面
        String[] lines = logOutput.split("\n");
        for (String line : lines) {
            if (line.startsWith("commit ")) {
                return line.substring("commit ".length()).trim();
            }
        }
        return "unknown";
    }

    // 辅助方法：提取最后一个提交ID
    private String extractLastCommitId(String logOutput) {
        String[] lines = logOutput.split("\n");
        String lastCommitId = "unknown";
        for (String line : lines) {
            if (line.contains("commit")) {
                String[] parts = line.split(" ");
                if (parts.length > 1) {
                    lastCommitId = parts[1].trim();
                }
            }
        }
        return lastCommitId;
    }
}