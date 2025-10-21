package src.vector;

/**
 * 四维整数向量类 - 实现VectorComputable接口
 */
public class Vector implements VectorComputable {
    private int a, b, c, d;  // 四个分量
    public Vector(int a, int b, int c, int d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    /**
     * 向量相加
     * @param v 另一个向量
     * @return 相加后的新向量
     */
    @Override
    public Vector add(Vector v) {
        return new Vector(this.a + v.a, this.b + v.b, this.c + v.c, this.d + v.d);
    }

    /**
     * 向量相减
     * @param v 另一个向量
     * @return 相减后的新向量
     */
    @Override
    public Vector minus(Vector v) {
        return new Vector(this.a - v.a, this.b - v.b, this.c - v.c, this.d - v.d);
    }

    /**
     * 向量点乘（对应分量相乘）
     * @param v 另一个向量
     * @return 点乘后的新向量
     */
    @Override
    public Vector elementwiseProduct(Vector v) {
        return new Vector(this.a * v.a, this.b * v.b, this.c * v.c, this.d * v.d);
    }

    /**
     * 向量内积（点乘结果各分量的和）
     * @param v 另一个向量
     * @return 内积结果
     */
    @Override
    public int innerProduct(Vector v) {
        Vector product = this.elementwiseProduct(v);
        return product.a + product.b + product.c + product.d;
    }

    /**
     * 计算向量的模
     * @return 向量的模（保留两位小数）
     */
    @Override
    public double norm() {
        double sumOfSquares = a * a + b * b + c * c + d * d;
        return Math.round(Math.sqrt(sumOfSquares) * 100.0) / 100.0;
    }

    /**
     * 比较当前向量与参数向量的模
     * @param v 另一个向量
     * @return 模大返回1，模小返回-1，相等返回0
     */
    @Override
    public int compare(Vector v) {
        double thisNorm = this.norm();
        double otherNorm = v.norm();
        
        if (thisNorm > otherNorm) {
            return 1;
        } else if (thisNorm < otherNorm) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 获取向量的第一个分量
     * @return 第一个分量
     */
    public int getA() {
        return a;
    }

    /**
     * 获取向量的第二个分量
     * @return 第二个分量
     */
    public int getB() {
        return b;
    }

    /**
     * 获取向量的第三个分量
     * @return 第三个分量
     */
    public int getC() {
        return c;
    }

    /**
     * 获取向量的第四个分量
     * @return 第四个分量
     */
    public int getD() {
        return d;
    }

    /**
     * 重写toString方法，格式化向量显示
     * @return 格式化的向量字符串
     */
    @Override
    public String toString() {
        return "[" + a + ", " + b + ", " + c + ", " + d + "]";
    }
}