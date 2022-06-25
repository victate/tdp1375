package br.unb.cic.tdp.permutation;

import lombok.val;

import java.io.Serializable;

public interface Permutation extends Serializable {

    Permutation getInverse();

    int getNumberOfEvenCycles();

    int size();

    Cycle asNCycle();

    default MulticyclePermutation conjugateBy(final Permutation conjugator) {
        return PermutationGroups.computeProduct(false, conjugator, this, conjugator.getInverse());
    }

    default int[] toOneLine() {
        val n = getMaxSymbol();
        val result = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            result[i] = contains(i) ? image(i) : i;
        }
        return result;
    }

    boolean contains(int i);

    int image(int a);

    int getMaxSymbol();
}
