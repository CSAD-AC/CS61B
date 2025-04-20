package testing;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class DisjointSetsTest {
    @Test
    public void testDisjointSets() {
        DisjointSets sets = new DisjointSets(5);
        sets.connected(0, 1);
        sets.connected(1, 2);
        sets.connected(2, 3);
        assertTrue(sets.isConnected(0, 3));
        assertFalse(sets.isConnected(0, 4));
    }
    @Test
    public void testDisjointSets2() {
        DisjointSets sets = new DisjointSets(100);
        for (int i = 0; i < 33; i++) {
            sets.connected(i, i + 1);
        }
        for (int i = 0; i < 33; i++) {
            assertTrue(sets.isConnected(i, i + 1));
        }

        assertTrue(sets.isConnected(0, 33));
        assertFalse(sets.isConnected(0, 34));
    }

    @Test
    public void testInitialization() {
        DisjointSets ds = new DisjointSets(5);
        // 初始时，每个元素都是自己的根，且高度为 1（存储为 -1）
        // 不同元素初始时不连通
        assertFalse(ds.isConnected(0, 1));
        assertFalse(ds.isConnected(2, 4));
    }

    @Test
    public void testBasicUnionAndFind() {
        DisjointSets ds = new DisjointSets(5);

        // 连接 0 和 1
        ds.connected(0, 1);
        assertTrue(ds.isConnected(0, 1));
        assertFalse(ds.isConnected(0, 2));

        // 连接 2 和 3
        ds.connected(2, 3);
        assertTrue(ds.isConnected(2, 3));
        assertFalse(ds.isConnected(1, 3));

        // 连接 1 和 3（此时 0-1 和 2-3 合并）
        ds.connected(1, 3);
        assertTrue(ds.isConnected(0, 2)); // 0 和 2 现在应该连通
        assertTrue(ds.isConnected(1, 3));
    }

    @Test
    public void testUnionByRank() {
        DisjointSets ds = new DisjointSets(6);

        // 连接 0-1, 2-3, 4-5（形成 3 棵树，每棵高度为 2）
        ds.connected(0, 1);
        ds.connected(2, 3);
        ds.connected(4, 5);

        // 连接 1 和 3（合并两棵高度为 2 的树，新树高度为 3）
        ds.connected(1, 3);
        assertTrue(ds.isConnected(0, 2));
        assertTrue(ds.isConnected(1, 3));

        // 连接 3 和 5（合并高度 3 和 2 的树，新树高度仍为 3）
        ds.connected(3, 5);
        assertTrue(ds.isConnected(0, 4)); // 0 和 4 现在应该连通
        assertTrue(ds.isConnected(2, 5));
    }

    @Test
    public void testOutOfBounds() {
        DisjointSets ds = new DisjointSets(3);

        // 超出范围的连接应该被忽略
        ds.connected(0, 5);
        ds.connected(-1, 2);

        // 超出范围的查询应该返回 false
        assertFalse(ds.isConnected(0, 5));
        assertFalse(ds.isConnected(-1, 2));
    }

}
