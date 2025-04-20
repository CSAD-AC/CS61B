package testing;

public class DisjointSets {
    private int[] items;
    private int MaxSize;

    public DisjointSets(int MaxSize) {
        this.MaxSize = MaxSize;
        items = new int[MaxSize];
        for(int i = 0; i < MaxSize; i++) {
            items[i] = -1;
        }
    }

    public void connected(int p, int q) {
        if(p >= MaxSize || q >= MaxSize || p < 0 || q < 0) return;
        int num1 = p;
        while(items[num1] >= 0) {
            num1 = items[num1];
        }

        int num2 = q;
        while(items[num2] >= 0) {
            num2 = items[num2];
        }

        if(num1 == num2) return;

        if(items[num1] < items[num2])
            items[num2] = num1;
        else if(items[num1] > items[num2])
            items[num1] = num2;
        else {
            items[num2] = num1;
            items[num1]--;
        }
    }

    public boolean isConnected(int p, int q) {
        if(p >= MaxSize || q >= MaxSize || p < 0 || q < 0) return false;
        while(items[p] >= 0) {
            p = items[p];
        }
        while(items[q] >= 0) {
            q = items[q];
        }
        return p == q;
    }
}
