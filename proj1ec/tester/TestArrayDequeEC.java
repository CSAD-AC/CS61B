package tester;
import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.Deque;
import student.StudentArrayDeque;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TestArrayDequeEC {
    @Test
    public void randomTest() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
        List<String> operationLog = new ArrayList<>(); // 记录操作步骤

        for (int i = 0; i < 1000; i++) {
            double op = StdRandom.uniform();
            if (op < 0.5) { // 添加操作
                int randomNumber = StdRandom.uniform(1000);
                if (StdRandom.bernoulli(0.5)) {
                    sad.addFirst(randomNumber);
                    ads.addFirst(randomNumber);
                    operationLog.add("AddFirst " + randomNumber);
                } else {
                    sad.addLast(randomNumber);
                    ads.addLast(randomNumber);
                    operationLog.add("AddLast " + randomNumber);
                }
            } else { // 删除操作（仅当队列非空时）
                if (!sad.isEmpty() && !ads.isEmpty()) {
                    if (StdRandom.bernoulli(0.5)) {
                        Integer adResult = ads.removeFirst();
                        Integer sadResult = sad.removeFirst();
                        operationLog.add("RemoveFirst");
                        assertEquals(
                            buildErrorMessage("RemoveFirst 返回值不一致", ads, sad, operationLog),
                            adResult, sadResult
                        );
                    } else {
                        Integer adResult = ads.removeLast();
                        Integer sadResult = sad.removeLast();
                        operationLog.add("RemoveLast");
                        assertEquals(
                            buildErrorMessage("RemoveLast 返回值不一致", ads, sad, operationLog),
                            adResult, sadResult
                        );
                    }
                }
            }

            // 验证大小
            assertEquals(
                buildErrorMessage("大小不一致", ads, sad, operationLog),
                ads.size(), sad.size()
            );

            // 验证元素
            for (int j = 0; j < ads.size(); j++) {
                assertEquals(
                    buildErrorMessage("索引 " + j + " 处不一致", ads, sad, operationLog),
                    ads.get(j), sad.get(j)
                );
            }
        }

        // 最终验证
        assertEquals(
            buildErrorMessage("最终大小不一致", ads, sad, operationLog),
            ads.size(), sad.size()
        );

        for (int i = 0; i < ads.size(); i++) {
            assertEquals(
                buildErrorMessage("最终索引 " + i + " 处不一致", ads, sad, operationLog),
                ads.get(i), sad.get(i)
            );
        }
    }

    /**
     * 构建包含操作日志和队列打印信息的错误消息
     */
    private String buildErrorMessage(
        String baseMessage,
        Deque<Integer> ads,
        Deque<Integer> sad,
        List<String> operationLog
    ) {
        return baseMessage + ":\n" +
            "Student Deque: " + capturePrintDeque(sad) + "\n" +
            "Solution Deque: " + capturePrintDeque(ads) + "\n" +
            "操作日志:\n" + String.join("\n", operationLog);
    }

    /**
     * 捕获 Deque 的 printDeque() 输出
     */
    private String capturePrintDeque(Deque<?> deque) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        deque.printDeque();

        System.setOut(originalOut);
        return outputStream.toString().trim();
    }
}
