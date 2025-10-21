package src.vector;

/**
 * 向量可计算接口 - 定义四维向量的运算方法
 */
public interface VectorComputable {

    Vector add(Vector v);

    Vector minus(Vector v);

    Vector elementwiseProduct(Vector v);

    int innerProduct(Vector v);

    double norm();

    int compare(Vector v);
}