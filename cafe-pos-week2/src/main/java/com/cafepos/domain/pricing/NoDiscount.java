package com.cafepos.domain.pricing;

import com.cafepos.domain.common.Money;

public final class NoDiscount implements DiscountPolicy {
    @Override
    public Money discountOf(Money subtotal) {
        return Money.zero();
    }
}
