package br.unb.cic.tdp.proof.util;

import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Move {
    public Move parent;
    public final int mu;
    public Move[] children;
    private List<Integer> pathToRoot;
    private String pathToRootStr;

    public Move(int mu, Move[] children, Move parent) {
        this.mu = mu;
        this.children = children;
        this.parent = parent;
        pathToRoot();
    }


    public int getHeight() {
        return maxDepth(this);
    }

    private int maxDepth(final Move move) {
        if (move.children.length == 0)
            return 1;

        int lDepth = maxDepth(move.children[0]);
        int rDepth = move.children.length == 1 ? 1 : maxDepth(move.children[1]);

        if (lDepth > rDepth)
            return (lDepth + 1);
        else
            return (rDepth + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move m = (Move) o;
        return mu == m.mu;
    }

    public List<Integer> pathToRoot() {
        if (pathToRoot == null) {
            pathToRoot = new ArrayList<>();
            var current = this;
            while (current != null) {
                pathToRoot.add(current.mu);
                current = current.parent;
            }
        }
        return pathToRoot;
    }

    public String pathToRootStr() {
        if (pathToRootStr == null) {
            val _pathToRoot = new ArrayList<>(pathToRoot());
            Collections.sort(_pathToRoot);
            var current = this;
            pathToRootStr = _pathToRoot.stream().map(i -> Integer.toString(i)).collect(Collectors.joining());
        }
        return pathToRootStr;
    }

    @Override
    public String toString() {
        return Integer.toString(mu);
    }
}