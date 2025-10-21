package src.transport;

/**
 * 公交车类 - 继承自Transport抽象类，实现Movable接口
 */
public class Bus extends Transport implements Movable {
    private String route;   // 公交线路
    private int capacity;   // 载客量

    /**
     * 构造方法
     * @param route 公交线路
     * @param speed 行驶速度
     * @param capacity 载客量
     */
    public Bus(String route, double speed, int capacity) {
        super("公交车", speed);  // 调用父类构造方法
        this.route = route;
        this.capacity = capacity;
    }

    /**
     * 实现抽象方法 - 展示公交车的完整信息
     */
    @Override
    public void showTransportInfo() {
        System.out.println("=== 交通工具信息 ===");
        System.out.println("类型: " + type);
        System.out.println("线路: " + route);
        System.out.println("速度: " + speed + " km/h");
        System.out.println("载客量: " + capacity + " 人");
    }

    /**
     * 实现Movable接口方法 - 展示公交车移动信息
     */
    @Override
    public void moveInfo() {
        System.out.println("公交车：在公交专用道行驶，停靠指定站点");
    }

    /**
     * 获取公交线路
     * @return 公交线路
     */
    public String getRoute() {
        return route;
    }

    /**
     * 设置公交线路
     * @param route 公交线路
     */
    public void setRoute(String route) {
        this.route = route;
    }

    /**
     * 获取载客量
     * @return 载客量
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 设置载客量
     * @param capacity 载客量
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}