package ru.izebit.services;

import lombok.Setter;

import java.util.Optional;

public class SuperServiceImpl implements SuperService {

    @Setter
    private ResultCache<Double> cache;

    @Override
    public Double getHumanEdgeWeight(int level, int index) {
        if (index > level || index < 0 || level < 0) return null;

        if (level == 0) return 0.0;
        System.out.println(String.format("%d %d", level, index));

        Double result = 0.0;
        Optional<Double> cachedResult = cache.getCachedResult(level - 1, index - 1);
        if (cachedResult.isPresent())
            result = cachedResult.get() / 2 + 25;
        else {
            Double intermediateResult = getHumanEdgeWeight(level - 1, index - 1);
            if (intermediateResult != null) result = intermediateResult / 2 + 25;
        }

        cachedResult = cache.getCachedResult(level - 1, index);
        if (cachedResult.isPresent())
            result += cachedResult.get() / 2 + 25;
        else {
            Double intermediateResult = getHumanEdgeWeight(level - 1, index);
            if (intermediateResult != null) result += intermediateResult / 2 + 25;
        }

        cache.putCachedResult(level, index, result);

        return result;
    }
}
