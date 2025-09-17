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
// convenience factory from BigDecimal (used internally / by domain)
public static Money of(BigDecimal value) {
    return new Money(value);
}
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    private Money(BigDecimal a) {
        if (a == null) throw new IllegalArgumentException("amount required");
        BigDecimal scaled = a.setScale(2, RoundingMode.HALF_UP);
        if (scaled.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("money amount must be >= 0");
        this.amount = scaled;}

    public BigDecimal asBigDecimal() {
        return amount;
    }

    public Money add(Money other) {
        if (other == null) throw new IllegalArgumentException("other required");
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int qty) {
        if (qty < 0) throw new IllegalArgumentException("qty must be >= 0");
        return new Money(this.amount.multiply(BigDecimal.valueOf(qty)));
   }
   public Money multiply(BigDecimal multiplier) {
    if (multiplier == null) throw new IllegalArgumentException("multiplier required");
    return new Money(this.amount.multiply(multiplier));
}
public Money percentage(int percent) {
        if (percent < 0) throw new IllegalArgumentException("percent must be >= 0");
        BigDecimal pct = BigDecimal.valueOf(percent)
                            .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return multiply(pct);
    }
    @Override
    public int compareTo(Money o) {
        return this.amount.compareTo(o.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return this.amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        // rely on BigDecimal's hash
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}

