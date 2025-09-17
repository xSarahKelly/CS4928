package com.cafepos.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BigDecimal amount;

    public static Money of(double value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    private Money(BigDecimal a) {
        if (a == null) throw new IllegalArgumentException("amount required");
        if (a.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("amount must be >= 0");
        this.amount = a.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal asBigDecimal() {
        return amount;
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other required");
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int qty) {
        if (qty < 0) throw new IllegalArgumentException("qty must be >= 0");
        return new Money(this.amount.multiply(BigDecimal.valueOf(qty)));
    }

    @Override
    public int compareTo(Money o) {
        return this.amount.compareTo(o.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money m = (Money) o;
        return amount.compareTo(m.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}

