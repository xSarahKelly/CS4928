package com.cafepos.domain.pricing;

import com.cafepos.domain.common.Money;

public interface TaxPolicy {
    Money taxOn(Money amount);
}