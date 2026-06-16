package com.ochoscar.operations.similarity;

import dev.brachtendorf.jimagehash.hash.Hash;

import java.util.ArrayList;
import java.util.List;

public record ExtendedFingerprint(List<Hash> hashes, double duration) {

    public double similarity(ExtendedFingerprint other) {
        ExtendedFingerprint shorter, longer;
        if (this.duration <= other.duration) {
            shorter = this;
            longer = other;
        } else {
            shorter = other;
            longer = this;
        }

        int L = longer.hashes.size();
        if (shorter.hashes.isEmpty() || L == 0) return 0.0;

        double longInterval = longer.duration / L;
        int windowSize = Math.max((int) Math.round(shorter.duration / longInterval), 3);
        windowSize = Math.min(windowSize, L);

        List<Hash> shortSample = resample(shorter.hashes, windowSize);

        double bestScore = 0.0;
        int positions = L - windowSize + 1;

        for (int offset = 0; offset < positions; offset++) {
            List<Hash> longWindow = longer.hashes.subList(offset, offset + windowSize);
            double score = alignedSimilarity(shortSample, longWindow);
            if (score > bestScore) {
                bestScore = score;
            }
        }

        return bestScore;
    }

    private static List<Hash> resample(List<Hash> source, int targetSize) {
        if (targetSize >= source.size()) return source;

        List<Hash> result = new ArrayList<>(targetSize);
        for (int i = 0; i < targetSize; i++) {
            int idx = (int) Math.round((double) i * (source.size() - 1) / (targetSize - 1));
            result.add(source.get(idx));
        }
        return result;
    }

    private static double alignedSimilarity(List<Hash> a, List<Hash> b) {
        int size = Math.min(a.size(), b.size());
        if (size == 0) return 0.0;

        double total = 0.0;
        for (int i = 0; i < size; i++) {
            Hash h1 = a.get(i);
            Hash h2 = b.get(i);
            total += 1.0 - (h1.hammingDistance(h2) / (double) h1.getBitResolution());
        }
        return total / size;
    }
}
