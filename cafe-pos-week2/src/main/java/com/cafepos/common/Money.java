package com.cafepos.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money implements Comparable<Money> {
    private final BigDecimal amount;

    public static Money of(double value) {
        throw new UnsupportedOperationException("todo");
    }

    public static Money zero() {
        throw new UnsupportedOperationException("todo");
    }

    private Money(BigDecimal a) {
        if (a == null) throw new IllegalArgumentException("amount required");
        this.amount = a.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        throw new UnsupportedOperationException("todo");
    }

    public Money multiply(int qty) {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public int compareTo(Money o) {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public String toString() {
        return "Money(" + amount + ")";
    }
}
