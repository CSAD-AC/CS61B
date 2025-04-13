package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {

    @Test
    public void testThreeAddtwoRemove() {
        BuggyAList<Integer> b = new BuggyAList<>();
        AListNoResizing<Integer> e = new AListNoResizing<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                e.addLast(randVal);
                b.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                assertEquals(e.size(), b.size());
            } else if(operationNumber == 2) {
                if(e.size() > 0)
                assertEquals(e.getLast(), b.getLast());
            } else if(operationNumber == 3) {
                if(e.size() > 0)
                assertEquals(e.removeLast(), b.removeLast());
            }
        }

    }
}
