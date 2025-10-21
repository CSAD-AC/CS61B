package src;

import src.transport.*;

/**
 * 测试类 - 测试Transport抽象类及其子类
 */
public class TransportTest {
    public static void main(String[] args) {
        System.out.println("=== 交通工具信息展示 ===\n");

        // 创建包含3个Transport对象的数组
        Transport[] transports = new Transport[3];
        
        // 初始化数组中的对象
        transports[0] = new Car("丰田", 80.0, 5);
        transports[1] = new Train("京沪高铁", 120.0, 16);
        transports[2] = new Bus("101路", 40.0, 80);
        
        // 循环调用数组中每个元素的showTransportInfo()方法
        for (int i = 0; i < transports.length; i++) {
            transports[i].showTransportInfo();
            System.out.println(); // 添加空行分隔不同交通工具的信息
        }

    }
}