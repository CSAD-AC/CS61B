package src.transport;

/**
 * 火车类 - 继承自Transport抽象类，实现Movable接口
 */
public class Train extends Transport implements Movable {
    private int carriages;  // 车厢数
    private String line;     // 线路

    /**
     * 构造方法
     * @param line 线路
     * @param speed 行驶速度
     * @param carriages 车厢数
     */
    public Train(String line, double speed, int carriages) {
        super("火车", speed);  // 调用父类构造方法
        this.line = line;
        this.carriages = carriages;
    }

    /**
     * 实现抽象方法 - 展示火车的完整信息
     */
    @Override
    public void showTransportInfo() {
        System.out.println("=== 交通工具信息 ===");
        System.out.println("类型: " + type);
        System.out.println("线路: " + line);
        System.out.println("速度: " + speed + " km/h");
        System.out.println("车厢数: " + carriages);
    }

    /**
     * 实现Movable接口方法 - 展示火车移动信息
     */
    @Override
    public void moveInfo() {
        System.out.println("火车：在铁轨行驶，按调度计划运行");
    }

    /**
     * 获取线路
     * @return 线路
     */
    public String getLine() {
        return line;
    }

    /**
     * 设置线路
     * @param line 线路
     */
    public void setLine(String line) {
        this.line = line;
    }

    /**
     * 获取车厢数
     * @return 车厢数
     */
    public int getCarriages() {
        return carriages;
    }

    /**
     * 设置车厢数
     * @param carriages 车厢数
     */
    public void setCarriages(int carriages) {
        this.carriages = carriages;
    }
}