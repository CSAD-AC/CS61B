package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class ArrayDequeTest {

    private ArrayDeque<Integer> deque = new ArrayDeque<Integer>();

    @Test
    public void randomTest(){
        for (int i = 0; i < 10000; i++) {
            int op = StdRandom.uniform(0, 4);
            if (op == 0) {
                deque.addFirst(i);
            } else if (op == 1) {
                deque.addLast(i);
            } else if (op == 2) {
                deque.removeFirst();
            } else if (op == 3) {
                deque.removeLast();
            }
        }
    }
    // --- addFirst测试 ---
    @Test
    public void testAddFirst() {
        deque.addFirst(1);
        assertEquals(1, deque.size());
        assertEquals(1, (int) deque.get(0));
        deque.removeLast();
        // 测试扩容
        for (int i = 0; i < 8; i++) {
            deque.addFirst(i);
        }
        deque.addFirst(8); // 触发扩容到12
        assertEquals(9, deque.size());
        assertEquals(8, (int) deque.get(0)); // 新元素在最前面
    }

    // --- addLast测试 ---
    @Test
    public void testAddLast() {
        deque.addLast(1);
        assertEquals(1, deque.size());
        assertEquals(1, (int) deque.get(0));
        deque.removeLast();

        // 测试扩容
        for (int i = 0; i < 8; i++) {
            deque.addLast(i);
        }
        deque.addLast(8); // 触发扩容到12
        assertEquals(9, deque.size());
        assertEquals(8, (int) deque.get(8)); // 新元素在最后面
    }

    // --- removeFirst测试 ---
    @Test
    public void testRemoveFirst() {
        assertNull(deque.removeFirst()); // 空队列返回null

        deque.addFirst(1);
        assertEquals(1, (int) deque.removeFirst());
        assertEquals(0, deque.size());

        // 测试多个元素
        deque.addFirst(2);
        deque.addFirst(1);
        assertEquals(1, (int) deque.removeFirst());
        assertEquals(2, (int) deque.removeFirst());
        assertEquals(0, deque.size());
    }

    // --- removeLast测试 ---
    @Test
    public void testRemoveLast() {
        assertNull(deque.removeLast()); // 空队列返回null

        deque.addLast(1);
        assertEquals(1, (int) deque.removeLast());
        assertEquals(0, deque.size());

        // 测试多个元素
        deque.addLast(1);
        deque.addLast(2);
        assertEquals(2, (int) deque.removeLast());
        assertEquals(1, (int) deque.removeLast());
        assertEquals(0, deque.size());
    }

    // --- isEmpty测试 ---
    @Test
    public void testIsEmpty() {
        assertTrue(deque.isEmpty());
        deque.addFirst(1);
        assertFalse(deque.isEmpty());
        deque.removeFirst();
        assertTrue(deque.isEmpty());
    }

    // --- size测试 ---
    @Test
    public void testSize() {
        assertEquals(0, deque.size());
        deque.addFirst(1);
        assertEquals(1, deque.size());
        deque.addLast(2);
        assertEquals(2, deque.size());
        deque.removeFirst();
        assertEquals(1, deque.size());
    }

    // --- get越界测试 ---
    @Test
    public void testGetOutOfBounds() {
        deque.addFirst(1);
        assertNull(deque.get(1)); // 索引1越界
    }
    @Test
    public void removeToResize(){
        for (int i = 0; i < 1000; i++) {
            deque.addFirst(i);
        }
        for (int i = 0; i < 1000; i++) {
            deque.removeFirst();
        }
        assertEquals(0, deque.size());
    }
    // --- iterator测试 ---
    @Test
    public void testIterator() {
        deque.addFirst(1);
        deque.addLast(2);
        deque.addFirst(3);
        deque.addLast(4);
        deque.addFirst(5);

        Iterator<Integer> iterator = deque.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(5, (int) iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(3, (int) iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(1, (int) iterator.next());
    }

    // --- equals测试 ---
    @Test
    public void testEquals() {
        deque.addFirst(1);
        deque.addLast(2);
        deque.addFirst(3);

        assertFalse(deque==new ArrayDeque<Integer>());
        assertFalse(deque.equals(new LinkedListDeque<Integer>()));
        assertFalse(deque==new ArrayDeque<Integer>());
        ArrayDeque<Integer> other = new ArrayDeque<Integer>();
        other.addFirst(1);
        other.addLast(2);
        assertFalse(deque==other);
        other.addFirst(3);
        assertTrue("内容应该相等",deque.equals(other));
        assertFalse("地址不应该相等",deque == other);
    }
}
