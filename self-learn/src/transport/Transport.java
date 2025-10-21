package src.transport;

/**
 * 抽象类Transport - 表示交通工具
 */
public abstract class Transport {
    // 成员变量
    protected String type;   // 交通工具类型
    protected double speed;  // 行驶速度（km/h）

    /**
     * 构造方法 - 初始化工具类型与行驶速度
     * @param t 交通工具类型
     * @param s 行驶速度
     */
    public Transport(String t, double s) {
        this.type = t;
        this.speed = s;
    }

    /**
     * 抽象方法 - 展示交通工具的完整信息
     * @return 交通工具的完整信息字符串
     */
    public abstract void showTransportInfo();

    /**
     * 获取交通工具类型
     * @return 交通工具类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置交通工具类型
     * @param type 交通工具类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取行驶速度
     * @return 行驶速度
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * 设置行驶速度
     * @param speed 行驶速度
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }
}