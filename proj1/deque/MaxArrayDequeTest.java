package deque;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Comparator;

public class MaxArrayDequeTest {
    private MaxArrayDeque<Integer> deque;
    private MaxArrayDeque<Integer> reverseDeque;

    @Before
    public void setUp() {
        deque = new MaxArrayDeque<>(Comparator.naturalOrder());
        reverseDeque = new MaxArrayDeque<>(Comparator.reverseOrder());
    }

    @Test
    public void testEmptyMax() {
        assertNull(deque.max());
    }

    @Test
    public void testSingleElement() {
        deque.addFirst(5);
        assertEquals(5, deque.max().intValue());
    }

    @Test
    public void testMultipleElements() {
        deque.addLast(3);
        deque.addLast(5);
        deque.addLast(2);
        assertEquals(5, deque.max().intValue());
    }

    @Test
    public void testReverseComparator() {
        reverseDeque.addFirst(3);
        reverseDeque.addFirst(5);
        reverseDeque.addFirst(2); // 元素顺序：2 → 5 → 3
        assertEquals(2, reverseDeque.max().intValue()); // 最小值为2
    }

    @Test
    public void testMaxWithDifferentComparator() {
        deque.addLast(3);
        deque.addLast(5);
        deque.addLast(2);
        assertEquals(2, deque.max(Comparator.reverseOrder()).intValue());
    }

    @Test
    public void testAllSameElements() {
        deque.addLast(5);
        deque.addLast(5);
        deque.addLast(5);
        assertEquals(5, deque.max().intValue());
    }

    @Test
    public void testMixedAdditions() {
        deque.addFirst(10);
        deque.addLast(20);
        deque.addFirst(5); // 元素顺序：5 → 10 → 20
        assertEquals(20, deque.max().intValue());
    }
}
