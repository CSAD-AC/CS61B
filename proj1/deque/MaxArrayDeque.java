package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{
//    private Comparator<T> comparator;
    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }



    public T max(){
        return max(comparator);
    }

    public T max(Comparator<T> c){
        if(isEmpty()){
            return null;
        }
        T max = get(0);
        for(int i = 1; i < size(); i++){
            if(c.compare(max, get(i)) < 0){
                max = get(i);
            }
        }
        return max;
    }

}
