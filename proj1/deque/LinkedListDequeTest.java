package deque;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class LinkedListDequeTest {
    private LinkedListDeque<String> deque = new LinkedListDeque<String>();

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {


        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();

		assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
		lld1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

		lld1.addLast("middle");
		assertEquals(2, lld1.size());

		lld1.addLast("back");
		assertEquals(3, lld1.size());

		System.out.println("Printing out deque: ");
		lld1.printDeque();

    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {


        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
		// should be empty
		assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

		lld1.addFirst(10);
		// should not be empty
		assertFalse("lld1 should contain 1 item", lld1.isEmpty());

		lld1.removeFirst();
		// should be empty
		assertTrue("lld1 should be empty after removal", lld1.isEmpty());

    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {


        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);

    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {


        LinkedListDeque<String>  lld1 = new LinkedListDeque<String>();
        LinkedListDeque<Double>  lld2 = new LinkedListDeque<Double>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();

    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {


        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());


    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }

    }
    @Test
    /* 验证添加首个元素到头部时的空检查和size更新 */
    public void addFirstEmptyTest() {
        deque.addFirst("A");
        assertEquals(1, deque.size());
        assertFalse(deque.isEmpty());
        assertEquals("A", deque.get(0));
    }

    @Test
    /* 验证多次addFirst的顺序和size */
    public void multipleAddFirstOrderTest() {
        deque.addFirst("B");
        deque.addFirst("A");
        assertEquals("A", deque.get(0));
        assertEquals("B", deque.get(1));
        assertEquals(2, deque.size());
    }

    @Test
    /* 验证索引越界时get返回null */
    public void getOutOfBoundsTest() {
        assertNull(deque.get(-1));
        assertNull(deque.get(0));
        deque.addFirst("A");
        assertNull(deque.get(1));
    }

    @Test
    /* 验证空队列removeLast返回null */
    public void removeLastEmptyTest() {
        assertNull(deque.removeLast());
        assertEquals(0, deque.size());
    }

    @Test
    /* 验证复杂添加/删除顺序 */
    public void complexAddRemoveTest() {
        deque.addFirst("A");
        deque.addLast("B");
        deque.addFirst("C");
        assertEquals("C", deque.removeFirst());
        assertEquals("B", deque.removeLast());
        assertFalse(deque.isEmpty());
    }

    @Test
    /* 验证get方法 */
    public void getTest() {
        deque.addFirst("A");
        deque.addLast("B");
        assertEquals("A", deque.get(0));
        assertEquals("B", deque.get(1));
    }
    @Test
    /* 验证get方法和getRecursive方法相同 */
    public void getAndGetRecursiveTest() {
        deque.addFirst("A");
        deque.addLast("B");
        assertEquals(deque.get(0), deque.getRecursive(0));
        assertEquals(deque.get(1), deque.getRecursive(1));
        assertEquals(deque.get(2), deque.getRecursive(2));
    }

    @Test
    /* 验证iterator方法 */
    public void iteratorTest() {
        deque.addFirst("A");
        deque.addLast("B");
        Iterator<String> iterator = deque.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("B", iterator.next());
    }

    @Test
    /* 验证equals方法 */
    public void equalsTest() {
        deque.addFirst("A");
        deque.addLast("B");
        assertTrue(deque.equals(deque));
        assertFalse(deque.equals(new ArrayDeque<>()));
        assertFalse(deque.equals(new LinkedListDeque<>()));

        LinkedListDeque<String> linkedListDeque = new LinkedListDeque<>();
        linkedListDeque.addFirst("A");
        assertFalse(deque.equals(linkedListDeque));
        linkedListDeque.addLast("B");
        assertTrue(deque.equals(linkedListDeque));
    }

}
