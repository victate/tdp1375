package br.unb.cic.tdp.permutation;

import cc.redberry.core.utils.BitArray;
import cern.colt.list.IntArrayList;
import lombok.val;

import java.io.Serializable;
import java.util.Arrays;

public class PermutationGroups implements Serializable {

    public static MulticyclePermutation computeProduct(final Permutation... permutations) {
        return computeProduct(true, permutations);
    }

    public static MulticyclePermutation computeProduct(final boolean include1Cycle, final Permutation... p) {
        var n = 0;
        for (val p1 : p) {
            if (p1 instanceof Cycle) {
                n = Math.max(p1.getMaxSymbol(), n);
            } else {
                for (val c : ((MulticyclePermutation) p1)) {
                    n = Math.max(c.getMaxSymbol(), n);
                }
            }
        }
        return computeProduct(include1Cycle, n + 1, p);
    }

    public static MulticyclePermutation computeProduct(final boolean include1Cycle, final int n, final Permutation... permutations) {
        val functions = new int[permutations.length][n];

        // initializing
        for (var i = 0; i < permutations.length; i++)
            Arrays.fill(functions[i], -1);

        for (var i = 0; i < permutations.length; i++) {
            if (permutations[i] instanceof Cycle) {
                val cycle = (Cycle) permutations[i];
                val symbols = cycle.getSymbols();
                for (var j = 0; j < symbols.length; j++) {
                    functions[i][symbols[j]] = cycle.image(symbols[j]);
                }
            } else {
                for (val cycle : ((MulticyclePermutation) permutations[i])) {
                    val symbols = cycle.getSymbols();
                    for (var j = 0; j < symbols.length; j++) {
                        functions[i][symbols[j]] = cycle.image(symbols[j]);
                    }
                }
            }
        }

        val result = new MulticyclePermutation();

        val cycle = new IntArrayList();
        val seen = new BitArray(n);
        var counter = 0;
        while (counter < n) {
            var start = seen.nextZeroBit(0);

            var image = start;
            for (var i = functions.length - 1; i >= 0; i--) {
                image = functions[i][image] == -1 ? image : functions[i][image];
            }

            if (image == start) {
                ++counter;
                seen.set(start);
                if (include1Cycle)
                    result.add(Cycle.create(start));
                continue;
            }
            while (!seen.get(start)) {
                seen.set(start);
                ++counter;
                cycle.add(start);

                image = start;
                for (var i = functions.length - 1; i >= 0; i--) {
                    image = functions[i][image] == -1 ? image : functions[i][image];
                }

                start = image;
            }

            result.add(Cycle.create(Arrays.copyOfRange(cycle.elements(), 0, cycle.size())));
            cycle.clear();
        }

        return result;
    }

    public static MulticyclePermutation fromOneLine(final int[] permutation, final int n) {
        val function = new int[n + 1];

        for (var i = 0; i <= n; i++)
            function[i] = i;

        System.arraycopy(permutation, 0, function, 0, permutation.length);

        val result = new MulticyclePermutation();

        val cycle = new IntArrayList();
        val bitArray = new BitArray(n);
        var counter = 0;
        while (counter < n) {
            var start = bitArray.nextZeroBit(0);

            var image = start;
            for (var i = function.length - 1; i >= 0; i--) {
                image = function[image] == -1 ? image : function[image];
            }

            if (image == start) {
                ++counter;
                bitArray.set(start);
                continue;
            }

            while (!bitArray.get(start)) {
                bitArray.set(start);
                ++counter;
                cycle.add(start);

                image = start;
                for (var i = function.length - 1; i >= 0; i--) {
                    image = function[image] == -1 ? image : function[image];
                }

                start = image;
            }

            cycle.trimToSize();
            result.add(Cycle.create(cycle));
            cycle.clear();
        }

        return result;
    }
}
