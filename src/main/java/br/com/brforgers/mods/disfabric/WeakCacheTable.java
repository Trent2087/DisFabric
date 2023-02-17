package br.com.brforgers.mods.disfabric;// Created 2022-17-06T11:00:09

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * A minimal generic implementation that allows for caching of decorated text.
 *
 * @param <K> Primary key.
 * @param <C> Cache key.
 * @param <V> Value.
 * @author Ampflower
 * @since 1.4.0
 **/
public class WeakCacheTable<K, C, V> {
    private static final Logger logger = LogUtils.getLogger();
    private final ReferenceQueue<C> queue = new ReferenceQueue<>();
    private final Map<K, WeakRow> backing = new ConcurrentHashMap<>();

    /**
     * Cleans up the backing hashmap of stale references.
     * <p>
     * Call this on a timer on any caches that may not be updated regularly.
     */
    // SuspiciousMethodCalls - It's practically guaranteed that the row's ours.
    @SuppressWarnings("SuspiciousMethodCalls")
    public void clean() {
        Reference<? extends C> reference;
        while ((reference = queue.poll()) != null) {
            if (reference instanceof WeakCacheTable<?, ?, ?>.WeakRow row) {
                backing.remove(row.k, reference);
            } else {
                logger.warn("Unexpected reference {}", reference);
            }
        }
    }

    /**
     * Gets or computes the cached value for the given input.
     *
     * @param k          The primary key. Must be deterministic to allow proper fetching.
     * @param c          The cache key. Must be deterministic to avoid recomputing.
     * @param biFunction The compute function to return a new cached value.
     * @return The cached or newly computed return.
     */
    public V computeIfMismatch(K k, C c, BiFunction<K, C, V> biFunction) {
        clean();

        if (k == null) {
            return biFunction.apply(null, c);
        }

        var r = backing.get(k);
        if (r == null || !c.equals(r.get())) {
            V v = biFunction.apply(k, c);
            backing.put(k, new WeakRow(k, c, v));
            return v;
        }
        return r.v;
    }

    /**
     * Storage row based off of a {@link WeakReference}.
     * <p>
     * Instanced, basing off the parameters {@link K}, {@link C} and {@link V}.
     */
    private class WeakRow extends WeakReference<C> {
        private final K k;
        private final V v;

        WeakRow(K k, C c, V v) {
            super(c, queue);
            this.k = k;
            this.v = v;
        }
    }
}
