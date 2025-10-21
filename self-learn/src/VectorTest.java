package src;

import src.vector.*;

/**
 * 测试类 - 测试Vector类及VectorComputable接口
 */
public class VectorTest {
    public static void main(String[] args) {
        // 创建两个测试向量
        Vector v1 = new Vector(3, 5, 7, 9);
        Vector v2 = new Vector(1, 2, 4, 6);
        
        System.out.println("向量1: " + v1);
        System.out.println("向量2: " + v2);
        
        // 测试向量相加
        System.out.println("\n=== 向量相加 ===");
        Vector sum = v1.add(v2);
        System.out.println(v1 + " + " + v2 + " = " + sum);
        
        // 测试向量相减
        System.out.println("\n=== 向量相减 ===");
        Vector difference = v1.minus(v2);
        System.out.println(v1 + " - " + v2 + " = " + difference);
        
        // 测试向量点乘
        System.out.println("\n=== 向量点乘 ===");
        Vector product = v1.elementwiseProduct(v2);
        System.out.println(v1 + " 点乘 " + v2 + " = " + product);
        
        // 测试向量内积
        System.out.println("\n=== 向量内积 ===");
        int innerProduct = v1.innerProduct(v2);
        System.out.println(v1 + " 与 " + v2 + " 的内积 = " + innerProduct);
        
        // 测试向量的模
        System.out.println("\n=== 向量的模 ===");
        double norm1 = v1.norm();
        double norm2 = v2.norm();
        System.out.println(v1 + " 的模 = " + norm1);
        System.out.println(v2 + " 的模 = " + norm2);
        
        // 测试向量模的比较
        System.out.println("\n=== 向量模的比较 ===");
        int comparison = v1.compare(v2);
        if (comparison == 1) {
            System.out.println(v1 + " 的模大于 " + v2 + " 的模");
        } else if (comparison == -1) {
            System.out.println(v1 + " 的模小于 " + v2 + " 的模");
        } else {
            System.out.println(v1 + " 的模等于 " + v2 + " 的模");
        }
        
        // 额外测试：比较相等模长的向量
        System.out.println("\n=== 额外测试 ===");
        Vector v3 = new Vector(2, 3, 4, 5);
        Vector v4 = new Vector(5, 4, 3, 2);
        System.out.println("向量3: " + v3 + " 模 = " + v3.norm());
        System.out.println("向量4: " + v4 + " 模 = " + v4.norm());
        int comparison2 = v3.compare(v4);
        if (comparison2 == 1) {
            System.out.println(v3 + " 的模大于 " + v4 + " 的模");
        } else if (comparison2 == -1) {
            System.out.println(v3 + " 的模小于 " + v4 + " 的模");
        } else {
            System.out.println(v3 + " 的模等于 " + v4 + " 的模");
        }
    }
}