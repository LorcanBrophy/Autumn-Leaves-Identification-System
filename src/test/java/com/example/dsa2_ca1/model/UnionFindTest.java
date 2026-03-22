package com.example.dsa2_ca1.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UnionFindTest {
    private UnionFind unionFind;

    @BeforeEach
    void setup() {
        unionFind = new UnionFind(5);
    }

    @Test
    void testInitialParents() {
        for (int i = 0; i < 5; i++) {
            assertEquals(i, unionFind.find(i));
        }
    }

    @Test
    void testUnion() {
        unionFind.union(0, 1);
        unionFind.union(2, 3);

        //  0  2  4
        //  |  |
        //  1  3

        assertEquals(unionFind.find(0), unionFind.find(1));
        assertEquals(unionFind.find(2), unionFind.find(3));

        assertNotEquals(unionFind.find(0), unionFind.find(2));

        unionFind.union(0, 2);

        //    0      4
        //  / | \
        // 1  2  3

        assertEquals(unionFind.find(1), unionFind.find(3));

        // test union on same set

        int root0Before = unionFind.find(0);
        int root1Before = unionFind.find(1);

        unionFind.union(0, 1);

        int root0After = unionFind.find(0);
        int root1After = unionFind.find(1);

        assertEquals(root0Before, root0After);
        assertEquals(root1Before, root1After);
    }

    @Test
    void testRanks() {
        // 0-1 : 0 has rank 1 now
        unionFind.union(0, 1);

        assertEquals(1, unionFind.getRank(0)); // 0 has rank 1
        assertEquals(0, unionFind.getRank(1)); // 1 has rank 0

        // 0 has rank 1
        unionFind.union(0, 2);

        assertEquals(1, unionFind.getRank(0));

        // 2 has rank 1
        unionFind.union(3, 0);

        assertEquals(1, unionFind.getRank(0));
    }

}
