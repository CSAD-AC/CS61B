package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        AList<Integer> N = new AList<>();
        AList<Double> time = new AList<>();
        AList<Integer> ops = new AList<>();
        N.addLast(1000);
        N.addLast(2000);
        N.addLast(4000);
        N.addLast(8000);
        N.addLast(16000);
        N.addLast(32000);
        N.addLast(64000);
        N.addLast(128000);
        for(int i = 0; i < N.size(); i++){
            SLList<Integer> alist = new SLList<>();
            int N_i = N.get(i);
            for(int j = 0; j < N_i; j++){
                alist.addLast(j);
            }

            ops.addLast(10000);
            Stopwatch sw = new Stopwatch();
            for(int j=0; j<10000; j++){
                alist.addLast(j);
            }
            time.addLast(sw.elapsedTime());
        }
        printTimingTable(N, time, ops);
    }

}
