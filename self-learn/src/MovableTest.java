package src;

import src.transport.*;

/**
 * 测试类 - 测试Movable接口及其实现类
 */
public class MovableTest {
    public static void main(String[] args) {
        // 创建Movable对象数组
        Movable[] movables = new Movable[3];
        
        // 初始化数组中的对象
        movables[0] = new Car("丰田", 80.0, 5);
        movables[1] = new Train("京沪高铁", 120.0, 16);
        movables[2] = new Bus("101路", 40.0, 80);
        
        // 循环调用数组中每个元素的moveInfo()方法
        for (int i = 0; i < movables.length; i++) {
            movables[i].moveInfo();
        }

    }
}