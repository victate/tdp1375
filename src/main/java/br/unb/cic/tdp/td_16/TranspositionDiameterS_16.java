package br.unb.cic.tdp.td_16;

import br.unb.cic.tdp.Silvaetal;
import br.unb.cic.tdp.base.Configuration.Signature;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.util.Move;
import br.unb.cic.tdp.util.Pair;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static br.unb.cic.tdp.base.CommonOperations.areSymbolsInCyclicOrder;
import static br.unb.cic.tdp.base.CommonOperations.cycleIndex;
import static br.unb.cic.tdp.base.Configuration.getCanonicalSignature;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;

public class TranspositionDiameterS_16 {

    private static AtomicLong visited = new AtomicLong(0);
    public static Cache<Signature, Boolean> VISITED_PERMUTATIONS;
    private static FileWriter fileWriter;
    private static BufferedWriter writer;

    static {
        val maxSize = (long) (((Runtime.getRuntime().maxMemory() - (4 * 1024 * 1024 * 1024)) * 0.85) / 120);
        System.out.println("cache max size=" + maxSize);

        VISITED_PERMUTATIONS = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        try {
            fileWriter = new FileWriter("C:\\Users\\I556732\\Temp\\uniciclicas-16.txt");
        } catch (IOException e) {
            // empty
        }
        writer = new BufferedWriter(fileWriter, 1024);
    }

    static final int[][] _10_8 = new int[][]{
            {-1, -2, 0, 0, -2, -2, -2, -2, -2, -2, -2},
            {-1, -2, 0, -2, 0, -2, -2, -2, -2, -2, -2},
            {-1, -2, 0, -2, -2, 0, -2, -2, -2, -2, -2},
            {-1, -2, 0, -2, -2, -2, 0, -2, -2, -2, -2},
            {-1, -2, 0, -2, -2, -2, -2, 0, -2, -2, -2},
            {-1, -2, 0, -2, -2, -2, -2, -2, 0, -2, -2},
            {-1, -2, 0, -2, -2, -2, -2, -2, -2, 0, -2},
            {-1, -2, 0, -2, -2, -2, -2, -2, -2, -2, 0},
            {-1, -2, -2, 0, 0, -2, -2, -2, -2, -2, -2},
            {-1, -2, -2, 0, -2, 0, -2, -2, -2, -2, -2},
            {-1, -2, -2, 0, -2, -2, 0, -2, -2, -2, -2},
            {-1, -2, -2, 0, -2, -2, -2, 0, -2, -2, -2},
            {-1, -2, -2, 0, -2, -2, -2, -2, 0, -2, -2},
            {-1, -2, -2, 0, -2, -2, -2, -2, -2, 0, -2},
            {-1, -2, -2, 0, -2, -2, -2, -2, -2, -2, 0},
            {-1, -2, -2, -2, 0, 0, -2, -2, -2, -2, -2},
            {-1, -2, -2, -2, 0, -2, 0, -2, -2, -2, -2},
            {-1, -2, -2, -2, 0, -2, -2, 0, -2, -2, -2},
            {-1, -2, -2, -2, 0, -2, -2, -2, 0, -2, -2},
            {-1, -2, -2, -2, 0, -2, -2, -2, -2, 0, -2},
            {-1, -2, -2, -2, 0, -2, -2, -2, -2, -2, 0},
            {-1, -2, -2, -2, -2, 0, 0, -2, -2, -2, -2},
            {-1, -2, -2, -2, -2, 0, -2, 0, -2, -2, -2},
            {-1, -2, -2, -2, -2, 0, -2, -2, 0, -2, -2},
            {-1, -2, -2, -2, -2, 0, -2, -2, -2, 0, -2},
            {-1, -2, -2, -2, -2, 0, -2, -2, -2, -2, 0},
            {-1, -2, -2, -2, -2, -2, 0, 0, -2, -2, -2},
            {-1, -2, -2, -2, -2, -2, 0, -2, 0, -2, -2},
            {-1, -2, -2, -2, -2, -2, 0, -2, -2, 0, -2},
            {-1, -2, -2, -2, -2, -2, 0, -2, -2, -2, 0},
            {-1, -2, -2, -2, -2, -2, -2, 0, 0, -2, -2},
            {-1, -2, -2, -2, -2, -2, -2, 0, -2, 0, -2},
            {-1, -2, -2, -2, -2, -2, -2, 0, -2, -2, 0},
            {-1, -2, -2, -2, -2, -2, -2, -2, 0, 0, -2},
            {-1, -2, -2, -2, -2, -2, -2, -2, 0, -2, 0},
            {-1, -2, -2, -2, -2, -2, -2, -2, -2, 0, 0}};

    public static final Move _10_8_SEQS = new Move(-1, new Move[0], null);

    private static final HeuristicSorting heuristicSorting = new HeuristicSorting();

    private static final Silvaetal silvaetal = new Silvaetal();

    static {
        try {
            toTrie(_10_8, _10_8_SEQS);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    public static void toTrie(final int[][] seqs, Move root) {
        val root_ = root;
        for (int[] seq : seqs) {
            root = root_;
            for (int j = 1; j < seq.length; j++) {
                val move = seq[j];
                if (Arrays.stream(root.children).noneMatch(m -> m.mu == move)) {
                    if (root.children.length == 0) {
                        root.children = new Move[1];
                        root.children[0] = new Move(move, new Move[0], root);
                    } else {
                        val children = new Move[2];
                        children[0] = root.children[0];
                        children[1] = new Move(move, new Move[0], root);
                        root.children = children;
                    }
                }
                root = Arrays.stream(root.children).filter(m -> m.mu == move).findFirst().get();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        val pi = Cycle.create("(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16)");
        val spi = new MulticyclePermutation("(0)(1)(2)(3)(4)(5)(6)(7)(8)(9)(10)(11)(12)(13)(14)(15)(16)");

        val formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        val date = new Date(System.currentTimeMillis());
        System.out.println("Time=" + formatter.format(date));

        System.out.println("Start searching...");
        searchDistance(spi, pi, _10_8_SEQS);
    }

    private static void searchDistance(final MulticyclePermutation spi, final Cycle pi, final Move rootMove) {
        if (rootMove.mu == -1) {
            Arrays.stream(rootMove.children).forEach(child -> searchDistance(spi, pi, child));
            return;
        }

        final Stream<Cycle> stream = rootMove.mu == -2 ? generateAllMinus2Moves(spi, pi) : generateAll0Moves(spi, pi);

        stream.map(move -> new Pair<>(computeProduct(spi, move.getInverse()),
                        computeProduct(move, pi).asNCycle()))
                .parallel()
                .forEach(pair -> {
                    if (pair.getFirst().size() == 1 && rootMove.getHeight() == 1) {
                        if (visited.incrementAndGet() % 1_000_000 == 0) {
                            val formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                            val date = new Date(System.currentTimeMillis());
                            System.out.println("visited=" + visited.get() + ", time=" + formatter.format(date));
                        }

                        val signature = getCanonicalSignature(spi, pi);
                        try {
                            VISITED_PERMUTATIONS.get(signature, () -> {
                                var distance = silvaetal.sort(pair.getSecond()).getSecond().size();
                                if (distance >= rootMove.pathToRoot().size() - 1) {
                                    distance = heuristicSorting.sort(pair.getSecond()).getSecond().size();
                                    if (distance >= rootMove.pathToRoot().size() - 1) {
                                        String line = "pi=" + pair.getSecond() + ", spi=" + pair.getFirst() + "-" + (rootMove.pathToRoot().size() - 1) + ", calculated distance=" + distance;
                                        System.out.println(line);
                                        try {
                                            writer.write(line + "\n");
                                        } catch (IOException e) {
                                            // empty
                                        }
                                    }
                                }
                                return Boolean.TRUE;
                            });
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    Arrays.stream(rootMove.children)
                            .forEach(childMove -> searchDistance(pair.getFirst(), pair.getSecond(), childMove));
                });
    }

    public static Stream<Cycle> generateAllMinus2Moves(final MulticyclePermutation spi, final Cycle pi) {
        val ci = cycleIndex(spi, pi);
        return IntStream.range(0, pi.size() - 2).boxed()
                .flatMap(i -> IntStream.range(i + 1, pi.size() - 1).boxed()
                        .flatMap(j -> IntStream.range(j + 1, pi.size()).boxed()
                                .map(k -> {
                                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);
                                    val is_2Move = ci[a] != ci[b] && ci[b] != ci[c] && ci[a] != ci[c];
                                    val move = Cycle.create(a, b, c);
                                    if (is_2Move)
                                        return move;
                                    return null;
                                }))).filter(Objects::nonNull);
    }

    public static Stream<Cycle> generateAll0Moves(final MulticyclePermutation spi, final Cycle pi) {
        val ci = cycleIndex(spi, pi);
        return IntStream.range(0, pi.size() - 2).boxed()
                .flatMap(i -> IntStream.range(i + 1, pi.size() - 1).boxed()
                        .flatMap(j -> IntStream.range(j + 1, pi.size()).boxed()
                                .map(k -> {
                                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);
                                    val isMinus2Move = ci[a] != ci[b] && ci[b] != ci[c] && ci[a] != ci[c];
                                    val move = Cycle.create(a, b, c);

                                    if (!isMinus2Move) {
                                        val sameCycle = ci[a] == ci[b] && ci[b] == ci[c] && ci[a] == ci[c];

                                        if (sameCycle && !areSymbolsInCyclicOrder(ci[a], a, b, c)) {
                                            // 0-move
                                            return move;
                                        }
                                    }

                                    return null;
                                }))).filter(Objects::nonNull);
    }
}
