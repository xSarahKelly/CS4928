package com.cafepos.domain.pricing;
import com.cafepos.domain.common.Money;

public final class FixedRateTaxPolicy implements TaxPolicy {
    private final int percent;

    public FixedRateTaxPolicy(int percent) {
        if (percent < 0) throw new IllegalArgumentException("percent");
        this.percent = percent;
    }

    public int percent() { return percent; }

    @Override public Money taxOn(Money amount) {
        var t = amount.asBigDecimal()
                .multiply(java.math.BigDecimal.valueOf(percent))
                .divide(java.math.BigDecimal.valueOf(100));
        return Money.of(t);
    }
}
