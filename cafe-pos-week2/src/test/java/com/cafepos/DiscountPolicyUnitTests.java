package com.cafepos;

import com.cafepos.domain.common.Money;
import com.cafepos.domain.pricing.FixedCouponDiscount;
import com.cafepos.domain.pricing.LoyaltyPercentDiscount;
import com.cafepos.domain.pricing.NoDiscount;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DiscountPolicyUnitTests {
    @Test void no_discount_returns_zero() {
        assertEquals(Money.zero(), new NoDiscount().discountOf(Money.of(10.00)));
    }
    @Test void loyalty5_is_5_percent() {
        assertEquals(Money.of(0.50), new LoyaltyPercentDiscount(5).discountOf(Money.of(10.00)));
    }
    @Test void coupon1_caps_at_subtotal() {
        var p = new FixedCouponDiscount(Money.of(1.00));
        assertEquals(Money.of(1.00), p.discountOf(Money.of(3.30)));
        assertEquals(Money.of(0.80), p.discountOf(Money.of(0.80)));
    }
}
