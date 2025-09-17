package com.cafepos.domain;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility for generating unique order IDs.
 * Keeps it simple with an incrementing counter.
 */
public final class OrderIds {
    private static final AtomicLong COUNTER = new AtomicLong(1000);

    private OrderIds() {
        // utility class, prevent instantiation
    }

    /**
     * Returns the next unique order id.
     */
    public static long next() {
        return COUNTER.incrementAndGet();
    }
}
