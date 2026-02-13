package com.example.dsa2_ca1.model;

public class UnionFind {
    private final int[] parent;
    private final int[] rank;

    public UnionFind(int size) {

        // initialise parent array to size of image
        parent = new int[size];
        rank = new int[size];

        // make each element its own parent (disjoint set)
        // sets the rank for each set to 0
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int p) {
        if (parent[p] != p) parent[p] = find(parent[p]); // updates pointer to root node
        return parent[p];
    }

    public void union(int p, int q) {

        // find root of both sets
        int rootP = find(p);
        int rootQ = find(q);

        // if same set, do nothing
        if (rootP == rootQ) return;

        // attach smaller tree to larger tree using ranks
        if (rank[rootP] < rank[rootQ]) {
            parent[rootP] = rootQ;
        } else if (rank[rootP] > rank[rootQ]) {
            parent[rootQ] = rootP;
        } else {
            parent[rootQ] = rootP;
            rank[rootP]++;
        }
    }

    // check if two elements are in the same set
    public boolean connected(int p, int q) {
        return find(p) == find(q);
    }
}
