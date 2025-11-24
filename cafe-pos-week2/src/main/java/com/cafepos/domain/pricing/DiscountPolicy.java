package com.cafepos.domain.pricing;

import com.cafepos.domain.common.Money;

public interface DiscountPolicy {
    Money discountOf(Money subtotal);
}