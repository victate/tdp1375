package br.unb.cic.tdp.td_16;

import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.proof.util.ListOfCycles;
import br.unb.cic.tdp.util.Triplet;
import com.google.common.primitives.Ints;
import lombok.SneakyThrows;
import lombok.val;

import java.util.Arrays;
import java.util.Stack;

public class LongestSequenceSearcher {

    @SneakyThrows
    public void search(final ListOfCycles spi,
                       final boolean[] parity,
                       final int[][] spiIndex,
                       final int maxSymbol,
                       final int[] pi,
                       final Stack<Cycle> moves,
                       final Stack<Cycle> longest) {
        if (longest.size() == 5)
            return;

        analyzeOrientedCycles(spi, parity, spiIndex, maxSymbol, pi, moves, longest);

        if (longest.size() == 5)
            return;

        analyzeOddCycles(spi, parity, spiIndex, maxSymbol, pi, moves, longest);
    }

    private void analyzeOddCycles(final ListOfCycles spi,
                                  final boolean[] parity,
                                  final int[][] spiIndex,
                                  final int maxSymbol,
                                  final int[] pi,
                                  final Stack<Cycle> moves,
                                  final Stack<Cycle> longest) {

        for (int i = 0; i < pi.length - 2; i++) {
            if (parity[pi[i]]) continue;
            for (int j = i + 1; j < pi.length - 1; j++) {
                if (parity[pi[j]]) continue;
                for (int k = j + 1; k < pi.length; k++) {
                    if (parity[pi[k]]) continue;

                    int a = pi[i], b = pi[j], c = pi[k];

                    // if it's the same cycle, skip it
                    if (spiIndex[a] == spiIndex[b] && spiIndex[b] == spiIndex[c])
                        continue;

                    val is_2Move = spiIndex[a] != spiIndex[b] &&
                            spiIndex[b] != spiIndex[c] &&
                            spiIndex[a] != spiIndex[c];
                    if (is_2Move)
                        continue;

                    final Triplet<ListOfCycles, ListOfCycles, Integer> triplet = simulate0MoveTwoCycles(spiIndex, a, b, c);

                    if (triplet.third != 2)
                        continue;

                    // == APPLY THE MOVE ===
                    spi.removeAll(triplet.first);
                    var numberOfTrivialCycles = 0;
                    spi.removeAll(triplet.first);

                    var current = triplet.second.head;
                    for (int l = 0; l < triplet.second.size; l++) {
                        val cycle = current.data;

                        if (cycle.length > 1) {
                            spi.add(cycle);
                        } else {
                            numberOfTrivialCycles++;
                        }
                        current = current.next;
                    }

                    updateIndex(spiIndex, parity, triplet.second);
                    // ==============================

                    moves.push(Cycle.create(a, b, c));

                    if (moves.size() > longest.size()) {
                        longest.clear();
                        longest.addAll(moves);
                    }

                    int[] newPi = applyTransposition(pi, a, b, c, pi.length - numberOfTrivialCycles, spiIndex);
                    search(spi, parity, spiIndex, maxSymbol, newPi, moves, longest);

                    // ==== ROLLBACK ====
                    current = triplet.second.head;
                    for (int l = 0; l < triplet.second.size; l++) {
                        val cycle = current.data;
                        if (cycle.length > 1) spi.remove(cycle);
                        current = current.next;
                    }
                    spi.addAll(triplet.first);
                    updateIndex(spiIndex, parity, triplet.first);
                    // ==============================

                    moves.pop();
                }
            }
        }
    }

    private void analyzeOrientedCycles(final ListOfCycles spi,
                                       final boolean[] parity,
                                       final int[][] spiIndex,
                                       final int maxSymbol,
                                       final int[] pi,
                                       final Stack<Cycle> moves,
                                       final Stack<Cycle> longest) {
        val piInverseIndex = getPiInverseIndex(pi, maxSymbol);

        val orientedCycles = getOrientedCycles(spi, piInverseIndex);

        var current = orientedCycles.head;
        for (int l = 0; l < orientedCycles.size; l++) {
            val cycle = current.data;

            val before = parity[cycle[0]] ? 1 : 0;

            for (var i = 0; i < cycle.length - 2; i++) {
                for (var j = i + 1; j < cycle.length - 1; j++) {
                    val ab_k = j - i;

                    if (before == 1 && (ab_k & 1) == 0) {
                        continue;
                    }

                    for (var k = j + 1; k < cycle.length; k++) {
                        val bc_k = k - j;

                        if (before == 1 && (bc_k & 1) == 0) {
                            continue;
                        }

                        val ca_k = (cycle.length - k) + i;

                        int a = cycle[i], b = cycle[j], c = cycle[k];

                        var after = ab_k & 1;
                        after += bc_k & 1;
                        after += ca_k & 1;

                        // check if it's applicable
                        if (after - before == 2 && areSymbolsInCyclicOrder(piInverseIndex, a, c, b)) {
                            final int[] symbols = startingBy(cycle, a);
                            val aCycle = new int[ca_k];
                            aCycle[0] = a;
                            System.arraycopy(symbols, ab_k + bc_k + 1, aCycle, 1, ca_k - 1);

                            val bCycle = new int[ab_k];
                            bCycle[0] = b;
                            System.arraycopy(symbols, 1, bCycle, 1, ab_k - 1);

                            val cCycle = new int[bc_k];
                            cCycle[0] = c;
                            System.arraycopy(symbols, ab_k + 1, cCycle, 1, bc_k - 1);

                            moves.push(Cycle.create(a, b, c));

                            // == APPLY THE MOVE ===
                            spi.remove(cycle);
                            var numberOfTrivialCycles = 0;
                            if (aCycle.length > 1) spi.add(aCycle);
                            else numberOfTrivialCycles++;
                            if (bCycle.length > 1) spi.add(bCycle);
                            else numberOfTrivialCycles++;
                            if (cCycle.length > 1) spi.add(cCycle);
                            else numberOfTrivialCycles++;
                            update(spiIndex, parity, aCycle, bCycle, cCycle);
                            // =======================

                            if (moves.size() > longest.size()) {
                                longest.clear();
                                longest.addAll(moves);
                            }

                            int[] newPi = applyTransposition(pi, a, b, c, pi.length - numberOfTrivialCycles, spiIndex);
                            search(spi, parity, spiIndex, maxSymbol, newPi, moves, longest);

                            moves.pop();

                            // ==== ROLLBACK ====
                            if (aCycle.length > 1) spi.remove(aCycle);
                            if (bCycle.length > 1) spi.remove(bCycle);
                            if (cCycle.length > 1) spi.remove(cCycle);
                            spi.add(cycle);
                            update(spiIndex, parity, cycle);
                            // ====================
                        }
                    }
                }
            }
            current = current.next;
        }
    }

    private static boolean areSymbolsInCyclicOrder(final int[] piInverseIndex, final int a, final int b, final int c) {
        return (piInverseIndex[a] < piInverseIndex[b] && piInverseIndex[b] < piInverseIndex[c]) ||
                (piInverseIndex[b] < piInverseIndex[c] && piInverseIndex[c] < piInverseIndex[a]) ||
                (piInverseIndex[c] < piInverseIndex[a] && piInverseIndex[a] < piInverseIndex[b]);
    }

    private static void update(final int[][] index, final boolean[] parity, final int[]... cycles) {
        for (int[] cycle : cycles) {
            val p = (cycle.length & 1) == 1;
            for (int k : cycle) {
                index[k] = cycle;
                parity[k] = p;
            }
        }
    }

    private static int[] getPiInverseIndex(final int[] pi, final int maxSymbol) {
        val piInverseIndex = new int[maxSymbol + 1];
        for (var i = 0; i < pi.length; i++) {
            piInverseIndex[pi[pi.length - i - 1]] = i;
        }
        return piInverseIndex;
    }

    private static ListOfCycles getOrientedCycles(final ListOfCycles spi, final int[] piInverseIndex) {
        val orientedCycles = new ListOfCycles();
        var current = spi.head;
        for (int i = 0; i < spi.size; i++) {
            final int[] cycle = current.data;
            if (!areSymbolsInCyclicOrder(piInverseIndex, cycle))
                orientedCycles.add(cycle);
            current = current.next;
        }
        return orientedCycles;
    }

    private static void updateIndex(final int[][] index, final boolean[] parity, final ListOfCycles cycles) {
        for (var current = cycles.head; current != null; current = current.next) {
            val cycle = current.data;
            updateIndex(index, parity, cycle);
        }
    }

    private static void updateIndex(final int[][] index, final boolean[] parity, final int[]... cycles) {
        for (int[] cycle : cycles) {
            val p = (cycle.length & 1) == 1;
            for (int k : cycle) {
                index[k] = cycle;
                parity[k] = p;
            }
        }
    }

    private static boolean areSymbolsInCyclicOrder(final int[] index, int[] symbols) {
        boolean leap = false;
        for (int i = 0; i < symbols.length; i++) {
            int nextIndex = i + 1;
            if (nextIndex >= symbols.length)
                nextIndex = (i + 1) % symbols.length;
            if (index[symbols[i]] > index[symbols[nextIndex]]) {
                if (!leap) {
                    leap = true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    private static Triplet<ListOfCycles, ListOfCycles, Integer> simulate0MoveTwoCycles(final int[][] spiIndex,
                                                                                       final int a,
                                                                                       final int b,
                                                                                       final int c) {
        int numberOfEvenCycles = 0;
        int a_, b_, c_;
        if (spiIndex[a] == spiIndex[c]) {
            a_ = a;
            b_ = c;
            c_ = b;
            numberOfEvenCycles += spiIndex[a].length & 1;
            numberOfEvenCycles += spiIndex[b].length & 1;
        } else if (spiIndex[a] == spiIndex[b]) {
            a_ = b;
            b_ = a;
            c_ = c;
            numberOfEvenCycles += spiIndex[a].length & 1;
            numberOfEvenCycles += spiIndex[c].length & 1;
        } else {
            // spi.getCycle(b) == spi.getCycle(c)
            a_ = c;
            b_ = b;
            c_ = a;
            numberOfEvenCycles += spiIndex[a].length & 1;
            numberOfEvenCycles += spiIndex[c].length & 1;
        }

        val index = cycleIndex(spiIndex[c_]);
        val cImage = image(index, spiIndex[c_], c_);
        val abCycle = startingBy(spiIndex[a_], a_);
        val cCycle = startingBy(spiIndex[c_], cImage);

        val abCycleIndex = cycleIndex(abCycle);

        val ba_k = getK(abCycleIndex, abCycle, b_, a_);
        val newaCycle = new int[1 + ba_k - 1];
        newaCycle[0] = a_;
        val ab_k = getK(abCycleIndex, abCycle, a_, b_);
        System.arraycopy(abCycle, ab_k + 1, newaCycle, 1, ba_k - 1);

        val newbCycle = new int[1 + cCycle.length + (ab_k - 1)];
        newbCycle[0] = b_;
        System.arraycopy(cCycle, 0, newbCycle, 1, cCycle.length);
        System.arraycopy(abCycle, 1, newbCycle, 1 + cCycle.length, ab_k - 1);

        var newNumberOfEvenCycles = 0;
        newNumberOfEvenCycles += newaCycle.length & 1;
        newNumberOfEvenCycles += newbCycle.length & 1;

        val oldCycles = new ListOfCycles();
        oldCycles.add(spiIndex[a]);
        if (!oldCycles.contains(spiIndex[b]))
            oldCycles.add(spiIndex[b]);
        if (!oldCycles.contains(spiIndex[c]))
            oldCycles.add(spiIndex[c]);

        val newCycles = new ListOfCycles();
        newCycles.add(newaCycle);
        newCycles.add(newbCycle);

        return new Triplet<>(oldCycles, newCycles, newNumberOfEvenCycles - numberOfEvenCycles);
    }

    private static int image(int[] index, int[] cycle, int a) {
        return cycle[(index[a] + 1) % cycle.length];
    }

    private static int[] cycleIndex(int[] cycle) {
        val index = new int[Ints.max(cycle) + 1];

        for (int i = 0; i < cycle.length; i++) {
            index[cycle[i]] = i;
        }

        return index;
    }

    private static int getK(int[] cycleIndex, int[] cycle, int a, int b) {
        val aIndex = cycleIndex[a];
        val bIndex = cycleIndex[b];

        if (bIndex >= aIndex)
            return bIndex - aIndex;

        return (cycle.length - aIndex) + bIndex;
    }

    private static int[] startingBy(int[] symbols, int a) {
        if (symbols[0] == a)
            return symbols;

        val result = new int[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] == a) {
                System.arraycopy(symbols, i, result, 0, symbols.length - i);
                System.arraycopy(symbols, 0, result, symbols.length - i, i);
                break;
            }
        }

        return result;
    }

    private static int[] applyTransposition(final int[] pi,
                                            final int a,
                                            final int b,
                                            final int c,
                                            int numberOfSymbols, int[][] spiIndex) {
        val indexes = new int[3];
        Arrays.fill(indexes, -1);

        for (var i = 0; i < pi.length; i++) {
            if (pi[i] == a)
                indexes[0] = i;
            if (pi[i] == b)
                indexes[1] = i;
            if (pi[i] == c)
                indexes[2] = i;

            if (indexes[0] != -1 && indexes[1] != -1 && indexes[2] != -1)
                break;
        }

        // sort indexes - this is CPU efficient
        if (indexes[0] > indexes[2]) {
            val temp = indexes[0];
            indexes[0] = indexes[2];
            indexes[2] = temp;
        }

        if (indexes[0] > indexes[1]) {
            val temp = indexes[0];
            indexes[0] = indexes[1];
            indexes[1] = temp;
        }

        if (indexes[1] > indexes[2]) {
            val temp = indexes[1];
            indexes[1] = indexes[2];
            indexes[2] = temp;
        }

        val result = new int[numberOfSymbols];

        int counter = 0;
        for (int i = 0; i < indexes[0]; i++) {
            if (spiIndex[pi[i]].length == 1) continue;
            result[counter] = pi[i];
            counter++;
        }

        for (int i = 0; i < indexes[2] - indexes[1]; i++) {
            if (spiIndex[pi[indexes[1] + i]].length == 1) continue;
            result[counter] = pi[indexes[1] + i];
            counter++;
        }

        for (int i = 0; i < indexes[1] - indexes[0]; i++) {
            if (spiIndex[pi[indexes[0] + i]].length == 1) continue;
            result[counter] = pi[indexes[0] + i];
            counter++;
        }

        for (int i = 0; i < pi.length - indexes[2]; i++) {
            if (spiIndex[pi[indexes[2] + i]].length == 1) continue;
            result[counter] = pi[indexes[2] + i];
            counter++;
        }

        return result;
    }
}