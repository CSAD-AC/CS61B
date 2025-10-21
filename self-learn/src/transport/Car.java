package src.transport;

/**
 * 汽车类 - 继承自Transport抽象类，实现Movable接口
 */
public class Car extends Transport implements Movable {
    private String brand;  // 汽车品牌
    private int seats;     // 座位数

    /**
     * 构造方法
     * @param brand 汽车品牌
     * @param speed 行驶速度
     * @param seats 座位数
     */
    public Car(String brand, double speed, int seats) {
        super("汽车", speed);  // 调用父类构造方法
        this.brand = brand;
        this.seats = seats;
    }

    /**
     * 实现抽象方法 - 展示汽车的完整信息
     */
    @Override
    public void showTransportInfo() {
        System.out.println("=== 交通工具信息 ===");
        System.out.println("类型: " + type);
        System.out.println("品牌: " + brand);
        System.out.println("速度: " + speed + " km/h");
        System.out.println("座位数: " + seats);
    }

    /**
     * 实现Movable接口方法 - 展示汽车移动信息
     */
    @Override
    public void moveInfo() {
        System.out.println("汽车：在公路行驶，需遵守交通信号灯");
    }

    /**
     * 获取汽车品牌
     * @return 汽车品牌
     */
    public String getBrand() {
        return brand;
    }

    /**
     * 设置汽车品牌
     * @param brand 汽车品牌
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * 获取座位数
     * @return 座位数
     */
    public int getSeats() {
        return seats;
    }

    /**
     * 设置座位数
     * @param seats 座位数
     */
    public void setSeats(int seats) {
        this.seats = seats;
    }
}