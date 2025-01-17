package br.unb.cic.tdp.base;

import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.permutation.PermutationGroups;
import br.unb.cic.tdp.util.Pair;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import lombok.val;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static br.unb.cic.tdp.BaseAlgorithm.isOutOfInterval;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;

public class CommonOperations implements Serializable {

    public static final Cycle[] CANONICAL_PI;

    static {
        CANONICAL_PI = new Cycle[2000];
        for (var i = 1; i < 2000; i++) {
            val pi = new int[i];
            for (var j = 0; j < i; j++) {
                pi[j] = j;
            }
            CANONICAL_PI[i] = Cycle.create(pi);
        }
    }

    /**
     * Assumes \sigma=(0,1,2,...,n).
     */
    public static Cycle simplify(Cycle pi) {
        FloatArrayList _pi = new FloatArrayList();
        for (int i = 0; i < pi.getSymbols().length; i++) {
            _pi.add(pi.getSymbols()[i]);
        }

        IntArrayList sigma = new IntArrayList();
        for (int i = 0; i < _pi.size(); i++) {
            sigma.add(i);
        }

        MulticyclePermutation sigmaPiInverse = computeProduct(Cycle.create(sigma), pi.getInverse());

        Cycle bigCycle;
        while ((bigCycle = sigmaPiInverse.stream().filter(c -> c.size() > 3).findFirst().orElse(null)) != null) {
            int leftMostSymbol = leftMostSymbol(bigCycle, pi);
            float newSymbol = _pi.get(_pi.indexOf(leftMostSymbol) - 1) + 0.001F;
            _pi.beforeInsert(_pi.indexOf(bigCycle.pow(leftMostSymbol, -2)), newSymbol);

            FloatArrayList piCopy = new FloatArrayList(Arrays.copyOf(_pi.elements(), _pi.size()));
            piCopy.sort();

            IntArrayList newPi = new IntArrayList();
            for (int i = 0; i < piCopy.size(); i++) {
                newPi.add(piCopy.indexOf(_pi.get(i)));
            }

            sigma = new IntArrayList();
            for (int i = 0; i < newPi.size(); i++) {
                sigma.add(i);
            }

            sigmaPiInverse = computeProduct(Cycle.create(sigma), Cycle.create(newPi).getInverse());

            _pi = new FloatArrayList();
            for (int i = 0; i < newPi.size(); i++) {
                _pi.add(newPi.get(i));
            }
            pi = Cycle.create(newPi);
        }

        return pi.startingBy(0);
    }

    private static int leftMostSymbol(Cycle bigCycle, Cycle pi) {
        for (int i = 1; i < pi.size(); i++)
            if (bigCycle.contains(pi.get(i)))
                return pi.get(i);
        return -1;
    }

    public static Cycle optimizedApplyTransposition(final Cycle pi, final Cycle move) {
        val a = move.get(0);
        val b = move.get(1);
        val c = move.get(2);

        val indexes = new int[3];
        for (var i = 0; i < pi.size(); i++) {
            if (pi.get(i) == a)
                indexes[0] = i;
            if (pi.get(i) == b)
                indexes[1] = i;
            if (pi.get(i) == c)
                indexes[2] = i;
        }

        Arrays.sort(indexes);

        val result = new int[pi.size()];
        System.arraycopy(pi.getSymbols(), 0, result, 0, indexes[0]);
        System.arraycopy(pi.getSymbols(), indexes[1], result, indexes[0], indexes[2] - indexes[1]);
        System.arraycopy(pi.getSymbols(), indexes[0], result, indexes[0] + (indexes[2] - indexes[1]), indexes[1] - indexes[0]);
        System.arraycopy(pi.getSymbols(), indexes[2], result, indexes[2], pi.size() - indexes[2]);

        return Cycle.create(result);
    }

    public static int mod(int a, int b) {
        int r = a % b;
        if (r < 0)
            r += b;
        return r;
    }

    /**
     * Creates an array where the cycles in <code>bigGamma</code> can be accessed by the symbols of <code>pi</code>
     * (being the indexes of the resulting array).
     */
    public static Cycle[] cycleIndex(final Collection<Cycle> bigGamma, final Cycle pi) {
        return cyclesIndex(List.of(bigGamma), pi);
    }

    public static Cycle[] cyclesIndex(final List<Collection<Cycle>> components, final Cycle pi) {
        val index = new Cycle[pi.getMaxSymbol() + 1];

        components.forEach(component -> {
            for (val cycle : component) {
                for (final int symbol : cycle.getSymbols()) {
                    index[symbol] = cycle;
                }
            }
        });

        return index;
    }

    public static boolean areSymbolsInCyclicOrder(final Cycle cycle, int... symbols) {
        val symbolIndexes = cycle.getSymbolIndexes();

        boolean leap = false;
        for (int i = 0; i < symbols.length; i++) {
            if (symbolIndexes[symbols[i]] > symbolIndexes[symbols[(i + 1) % symbols.length]]) {
                if (!leap) {
                    leap = true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Find a sorting sequence whose approximation ratio is at most <code>maxRatio</code>.
     */
    public static List<Cycle> searchForSortingSeq(final Cycle pi, final MulticyclePermutation bigGamma, final Stack<Cycle> moves,
                                                  final int initialNumberOfEvenCycles, final float maxRatio) {
        val numberOfEvenCycles = bigGamma.getNumberOfEvenCycles();
        val lowerBound = (pi.size() - numberOfEvenCycles) / 2;
        val minAchievableRatio = (float) (moves.size() + lowerBound) / (float) ((pi.size() - initialNumberOfEvenCycles) / 2);

        // Do not allow it to exceed the max ratio
        if (minAchievableRatio <= maxRatio) {
            val delta = (numberOfEvenCycles - initialNumberOfEvenCycles);
            val instantRatio = delta > 0
                    ? (float) (moves.size() * 2) / (numberOfEvenCycles - initialNumberOfEvenCycles)
                    : 0;
            if (moves.size() >= 8 && instantRatio <= maxRatio) {
                return moves;
            } else {
                final Iterator<Pair<Cycle, Integer>> iterator;
                iterator = generateAll0And2Moves(bigGamma, pi).iterator();
                while (iterator.hasNext()) {
                    val pair = iterator.next();
                    val move = pair.getFirst();

                    val _bigGamma = PermutationGroups.computeProduct(bigGamma, move.getInverse());
                    moves.push(move);
                    val sorting = searchForSortingSeq(optimizedApplyTransposition(pi, move),
                            _bigGamma, moves, initialNumberOfEvenCycles, maxRatio);
                    if (!sorting.isEmpty()) {
                        return moves;
                    }
                    moves.pop();
                }
            }
        }

        return Collections.emptyList();
    }

    public static Stream<Pair<Cycle, Integer>> generateAll0_2Moves(final MulticyclePermutation spi, final Cycle pi) {
        val ci = cycleIndex(spi, pi);
        return IntStream.range(0, pi.size() - 2).boxed()
                .flatMap(i -> IntStream.range(i + 1, pi.size() - 1).boxed()
                .flatMap(j -> IntStream.range(j + 1, pi.size()).boxed()
                .map(k -> {
                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);
                    val is_2Move = ci[a] != ci[b] && ci[b] != ci[c] && ci[a] != ci[c];
                    val move = Cycle.create(a, b, c);
                    if (is_2Move)
                        return new Pair<>(move, -2);

                    val spi_ = computeProduct(true, pi.getMaxSymbol() + 1, spi, move.getInverse());
                    val delta = spi_.size() - spi.size();

                    if (delta == 0)
                        return new Pair<>(move, delta);

                    return null;
                }))).filter(Objects::nonNull);
    }

    public static Stream<Pair<Cycle, Integer>> generateAll2Moves(final MulticyclePermutation spi, final Cycle pi) {
        val ci = cycleIndex(spi, pi);
        val numberOfEvenCycles = spi.getNumberOfEvenCycles();
        return IntStream.range(0, pi.size() - 2).boxed()
                .filter(i -> ci[pi.get(i)].size() > 1)
                .flatMap(i -> IntStream.range(i + 1, pi.size() - 1).boxed()
                        .filter(j -> ci[pi.get(j)].size() > 1).flatMap(j -> IntStream.range(j + 1, pi.size()).boxed()
                                .filter(k -> ci[pi.get(k)].size() > 1)
                                .filter(k -> {
                                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);
                                    val is_2Move = ci[a] != ci[b] && ci[b] != ci[c] && ci[a] != ci[c];
                                    // skip (-2)-moves
                                    return !is_2Move;
                                }).map(k -> {
                                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);

                                    if ((ci[a] == ci[b] && ci[b] == ci[c] && ci[a] == ci[c] && ci[a].isEven()) // If all symbols belong to the same cycle and the cycle is even - can be a 2-move from an oriented cycle
                                            || ((ci[a] == ci[b] && !ci[a].isEven() && !ci[c].isEven()) || // If the symbols belong to two cycles, they must be odd in order to exist a 2-move
                                            (ci[a] == ci[c] && !ci[a].isEven() && !ci[b].isEven()))) {
                                        val move = Cycle.create(a, b, c);
                                        val spi_ = computeProduct(true, pi.getMaxSymbol() + 1, spi, move.getInverse());
                                        val delta = spi_.getNumberOfEvenCycles() - numberOfEvenCycles;

                                        if (delta > 0)
                                            return new Pair<>(move, delta);
                                    }

                                    return null;
                                }))).filter(Objects::nonNull);
    }

    public static Stream<Pair<Cycle, Integer>> generateAll0And2Moves(final MulticyclePermutation spi, final Cycle pi) {
        val ci = cycleIndex(spi, pi);
        val numberOfEvenCycles = spi.getNumberOfEvenCycles();
        return IntStream.range(0, pi.size() - 2).boxed()
                .filter(i -> ci[pi.get(i)].size() > 1)
                .flatMap(i -> IntStream.range(i + 1, pi.size() - 1).boxed()
                        .filter(j -> ci[pi.get(j)].size() > 1).flatMap(j -> IntStream.range(j + 1, pi.size()).boxed()
                                .filter(k -> ci[pi.get(k)].size() > 1)
                                .filter(k -> {
                                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);
                                    val is_2Move = ci[a] != ci[b] && ci[b] != ci[c] && ci[a] != ci[c];
                                    // skip (-2)-moves
                                    return !is_2Move;
                                }).map(k -> {
                                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);

                                    val move = Cycle.create(a, b, c);
                                    val spi_ = computeProduct(true, pi.getMaxSymbol() + 1, spi, move.getInverse());
                                    val delta = spi_.getNumberOfEvenCycles() - numberOfEvenCycles;

                                    if (delta >= 0)
                                        return new Pair<>(move, delta);

                                    return null;
                                }))).filter(Objects::nonNull);
    }

    public static List<Cycle> generateAll2MovesFromOrientedCycles(final Collection<Cycle> spi, final Cycle pi) {
        val _2moves = new ArrayList<Cycle>();

        for (val cycle : spi.stream().filter(c -> isOriented(pi, c))
                .collect(Collectors.toList())) {
            val before = cycle.isEven() ? 1 : 0;
            for (var i = 0; i < cycle.size() - 2; i++) {
                for (var j = i + 1; j < cycle.size() - 1; j++) {
                    for (var k = j + 1; k < cycle.size(); k++) {
                        val a = cycle.get(i);
                        val b = cycle.get(j);
                        val c = cycle.get(k);
                        if (areSymbolsInCyclicOrder(pi, a, b, c)) {
                            var after = cycle.getK(a, b) % 2 == 1 ? 1 : 0;
                            after += cycle.getK(b, c) % 2 == 1 ? 1 : 0;
                            after += cycle.getK(c, a) % 2 == 1 ? 1 : 0;
                            if (after - before == 2)
                                _2moves.add(Cycle.create(a, b, c));
                        }
                    }
                }
            }
        }

        return _2moves;
    }

    /**
     * Search for a 2-move given by an oriented cycle.
     */
    public static Optional<Cycle> searchFor2MoveFromOrientedCycle(final Collection<Cycle> spi, final Cycle pi) {
        return generateAll2MovesFromOrientedCycles(spi, pi).stream().findFirst();
    }

    /**
     * Tells if a <code>cycle</code> is oriented or not having <code>pi</code> as reference.
     */
    public static boolean isOriented(final Cycle pi, final Cycle cycle) {
        return !areSymbolsInCyclicOrder(pi.getInverse(), cycle.getSymbols());
    }

    // optimize
    public static List<Collection<Cycle>> getComponents(final Collection<Cycle> spi, final Cycle pi) {
        val cycleIndex = cycleIndex(spi, pi);
        val piInverse = pi.getInverse();

        final List<Collection<Cycle>> components = new ArrayList<>(); // small components

        Set<Cycle> nonVisitedCycles = spi.stream().filter(c -> c.size() > 1).collect(Collectors.toSet());

        while (!nonVisitedCycles.isEmpty()) {
            val queue = new LinkedList<Cycle>();
            queue.add(nonVisitedCycles.stream().findAny().get());

            final Set<Cycle> component = new HashSet<>();
            while (!queue.isEmpty()) {
                val cycle = queue.remove();
                if (cycle.size() == 1)
                    continue;

                component.add(cycle);

                for (int i = 0; i < cycle.size(); i++) {
                    val symbol = cycle.get(i);
                    val aPos = piInverse.indexOf(symbol);
                    val bPos = piInverse.indexOf(cycle.image(symbol));

                    var nextPos = (aPos + 1) % piInverse.size();

                    while (nextPos != bPos) {
                        val _cycle = cycleIndex[piInverse.get(nextPos)];
                        if (_cycle != null) {
                            for (int j = 0; j < _cycle.size(); j++) {
                                val pos = piInverse.indexOf(_cycle.get(j));
                                if (isOutOfInterval(pos, aPos, bPos) && (!component.contains(cycleIndex[(_cycle.get(j))]))) {
                                    component.add(cycleIndex[(_cycle.get(j))]);
                                    queue.add(cycleIndex[(_cycle.get(j))]);
                                    break;
                                }
                            }
                        }

                        nextPos = (nextPos + 1) % piInverse.size();
                    }
                }
            }

            nonVisitedCycles.removeAll(component);
            components.add(new ArrayList<>(component));
            component.clear();
        }

        return components;
    }

    public static Set<Integer> getOpenGates(final Collection<Cycle> spi, final Cycle pi) {
        val openGates = new HashSet<Integer>();

        for (Cycle epsilon : spi) {
            if (epsilon.size() == 1) continue;

            for (int i = 0; i < epsilon.size(); i++) {
                int a = epsilon.get(i);
                int b = epsilon.image(a);

                var intersecting = false;
                var orientedTriple = false;

                openGate: for (Cycle gamma : spi) {
                    if (gamma.size() == 1) continue;

                    if (epsilon != gamma) {
                        for (int j = 0; j < gamma.size(); j++) {
                            int c = gamma.get(j);
                            int d = gamma.image(c);

                            if (areSymbolsInCyclicOrder(pi.getInverse(), a, c, b, d)) {
                                intersecting = true;
                                break openGate;
                            }
                        }
                    } else {
                        for (int j = 0; j < epsilon.size(); j++) {
                            int c = epsilon.get(j);
                            if (c != a && c != b) {
                                if (areSymbolsInCyclicOrder(pi, a, b, c)) {
                                    orientedTriple = true;
                                    break openGate;
                                }
                            }
                        }
                    }
                }

                if (!intersecting && !orientedTriple) {
                    openGates.add(a);
                }
            }
        }

        return openGates;
    }

    public static boolean is11_8(MulticyclePermutation spi, Cycle pi, final List<Cycle> moves) {
        val before = spi.getNumberOfEvenCycles();
        for (val move : moves) {
            pi = optimizedApplyTransposition(pi, move);
            spi = PermutationGroups.computeProduct(spi, move.getInverse());
        }
        val after = spi.getNumberOfEvenCycles();
        val ratio = (float) moves.size() / ((after - before) / 2);
        return ratio >= 1 && ratio <= ((float) 11 / 8);
    }
}