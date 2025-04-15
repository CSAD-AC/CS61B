package tester;
import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import student.StudentArrayDeque;


import java.util.ArrayList;
import java.util.List;

public class TestArrayDequeEC {
    @Test
    public void randomTest() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
        List<String> operationLog = new ArrayList<>(); // 新增：记录操作步骤

        for (int i = 0; i < 1000; i++) {
            double op = StdRandom.uniform();
            if (op < 0.5) { // 添加操作
                int randomNumber = StdRandom.uniform(1000);
                if (StdRandom.bernoulli(0.5)) {
                    sad.addFirst(randomNumber);
                    ads.addFirst(randomNumber);
                    operationLog.add("AddFirst " + randomNumber); // 记录操作
                } else {
                    sad.addLast(randomNumber);
                    ads.addLast(randomNumber);
                    operationLog.add("AddLast " + randomNumber); // 记录操作
                }
            } else { // 删除操作（仅当队列非空时）
                if (!sad.isEmpty() && !ads.isEmpty()) {
                    if (StdRandom.bernoulli(0.5)) {
                        Integer adResult = ads.removeFirst();
                        Integer sadResult = sad.removeFirst();
                        operationLog.add("RemoveFirst"); // 记录操作
                        assertEquals(
                            "RemoveFirst 返回值不一致:\n" + String.join("\n", operationLog),
                            adResult, sadResult
                        );
                    } else {
                        Integer adResult = ads.removeLast();
                        Integer sadResult = sad.removeLast();
                        operationLog.add("RemoveLast"); // 记录操作
                        assertEquals(
                            "RemoveLast 返回值不一致:\n" + String.join("\n", operationLog),
                            adResult, sadResult
                        );
                    }
                }
            }

            // 验证大小
            assertEquals(
                "大小不一致:\n" + String.join("\n", operationLog),
                ads.size(), sad.size()
            );

            // 验证元素
            for (int j = 0; j < ads.size(); j++) {
                assertEquals(
                    "索引 " + j + " 处不一致:\n" + String.join("\n", operationLog),
                    ads.get(j), sad.get(j)
                );
            }
        }

        // 最终验证
        assertEquals(
            "最终大小不一致:\n" + String.join("\n", operationLog),
            ads.size(), sad.size()
        );

        for (int i = 0; i < ads.size(); i++) {
            assertEquals(
                "最终索引 " + i + " 处不一致:\n" + String.join("\n", operationLog),
                ads.get(i), sad.get(i)
            );
        }
    }

}
