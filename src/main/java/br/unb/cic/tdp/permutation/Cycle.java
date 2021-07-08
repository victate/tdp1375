package br.unb.cic.tdp.permutation;

import cern.colt.list.ByteArrayList;
import org.apache.commons.lang.ArrayUtils;

import java.util.*;

import static br.unb.cic.tdp.base.CommonOperations.areSymbolsInCyclicOrder;
import static br.unb.cic.tdp.base.CommonOperations.mod;

public class Cycle implements Permutation, Comparable<Cycle> {
    private byte[] symbols;
    private byte[] symbolIndexes;
    private byte minSymbol = -1;
    private byte maxSymbol = -1;
    private Cycle inverse;

    private Cycle(final byte... symbols) {
        this.symbols = symbols;
        updateInternalState();
    }

    public static Cycle create(final String cycle) {
        final var strSymbols = cycle.replace("(", "").replace(")", "").split(",|\\s");
        final var symbols = new byte[strSymbols.length];
        for (var i = 0; i < strSymbols.length; i++) {
            final var strSymbol = strSymbols[i];
            symbols[i] = Byte.parseByte(strSymbol);
        }
        return create(symbols);
    }

    public static Cycle create(final ByteArrayList lSymbols) {
        final var symbols = new byte[lSymbols.size()];
        System.arraycopy(lSymbols.elements(), 0, symbols, 0, lSymbols.size());
        return create(symbols);
    }

    public static Cycle create(final byte... symbols) {
        return new Cycle(symbols);
    }

    public byte[] getSymbols() {
        return symbols;
    }

    private void updateInternalState() {
        for (byte symbol : symbols) {
            if (minSymbol == -1 || symbol < minSymbol) {
                minSymbol = symbol;
            }
            if (symbol > maxSymbol) {
                maxSymbol = symbol;
            }
        }

        symbolIndexes = new byte[maxSymbol + 1];

        Arrays.fill(symbolIndexes, (byte) -1);

        for (var i = 0; i < symbols.length; i++) {
            symbolIndexes[symbols[i]] = (byte) i;
        }
    }

    public byte getMaxSymbol() {
        return maxSymbol;
    }

    public byte getMinSymbol() {
        return minSymbol;
    }

    @Override
    public String toString() {
        return defaultStringRepresentation();
    }

    @Override
    public Cycle getInverse() {
        if (inverse == null) {
            final var symbolsCopy = new byte[this.symbols.length];
            System.arraycopy(this.symbols, 0, symbolsCopy, 0, this.symbols.length);
            ArrayUtils.reverse(symbolsCopy);
            inverse = Cycle.create(symbolsCopy);
        }
        return inverse;
    }

    public int getNorm() {
        return this.size() - 1;
    }

    private String defaultStringRepresentation() {
        final var _default = this.startingBy(minSymbol);

        final var representation = new StringBuilder().append("(");

        for (var i = 0; ; i++) {
            representation.append(_default.symbols[i]);
            if (i == _default.symbols.length - 1)
                break;
            representation.append(" ");
        }
        representation.append(")");

        return representation.toString();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(startingBy(getMinSymbol()).getSymbols());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final var other = (Cycle) obj;

        if (size() != other.size()) {
            return false;
        }

        return Arrays.equals(startingBy(getMinSymbol()).getSymbols(),
                ((Cycle) obj).startingBy(((Cycle) obj).getMinSymbol()).getSymbols());
    }

    public boolean isEven() {
        return this.size() % 2 == 1;
    }

    public byte image(final byte a) {
        return symbols[(symbolIndexes[a] + 1) % symbols.length];
    }

    public byte pow(final byte a, final int power) {
        return symbols[mod(symbolIndexes[a] + power, symbols.length)];
    }

    public int getK(final byte a, final byte b) {
        final var aIndex = indexOf(a);
        final var bIndex = indexOf(b);

        if (bIndex >= aIndex)
            return bIndex - aIndex;

        return (symbols.length - aIndex) + bIndex;
    }

    public Cycle startingBy(final byte symbol) {
        if (this.symbols[0] == symbol) {
            return this;
        }

        final var index = indexOf(symbol);
        final var symbols = new byte[this.symbols.length];
        System.arraycopy(this.symbols, index, symbols, 0, symbols.length - index);
        System.arraycopy(this.symbols, 0, symbols, symbols.length - index, index);

        return Cycle.create(symbols);
    }

    @Override
    public int getNumberOfEvenCycles() {
        return size() % 2;
    }

    @Override
    public int compareTo(final Cycle o) {
        return this.defaultStringRepresentation().compareTo(o.defaultStringRepresentation());
    }

    public byte get(final int i) {
        return symbols[i];
    }

    public int indexOf(final byte symbol) {
        return symbolIndexes[symbol];
    }

    public boolean contains(final byte symbol) {
        return symbol <= symbolIndexes.length - 1 && symbolIndexes[symbol] != -1;
    }

    @Override
    public int size() {
        return symbols.length;
    }

    @Override
    public Cycle asNCycle() {
        return this;
    }

    public boolean isLong() {
        return this.size() > 3;
    }

    public byte[] getSymbolIndexes() {
        return symbolIndexes;
    }
}