package ru.izebit.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResultCacheImpl<T> implements ResultCache<T> {
    private final Cache<Integer, Optional<T>> cache =
            Caffeine.newBuilder()
                    .maximumSize(1_000_000)
                    .build();

    private static Integer getKey(int level, int index) {
        return level * 10 + (index > level / 2 ? level - index : index);
    }

    public Optional<T> getCachedResult(int level, int index) {
        Integer key = getKey(level, index);
        return cache.get(key, k -> Optional.<T>empty());
    }

    @Override
    public boolean putCachedResult(int level, int index, T result) {
        Integer key = getKey(level, index);
        if (cache.getIfPresent(key) == null) {
            cache.put(key, Optional.of(result));
            return true;
        } else
            return false;
    }
}
