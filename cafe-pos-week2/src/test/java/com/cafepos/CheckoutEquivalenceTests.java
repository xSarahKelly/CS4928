// src/test/java/com/cafepos/CheckoutEquivalenceTests.java
package com.cafepos;

import com.cafepos.checkout.CheckoutService;
import com.cafepos.factory.ProductFactory;
import com.cafepos.pricing.*;
import com.cafepos.common.Money;
import com.cafepos.smells.OrderManagerGod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckoutEquivalenceTests {
    private CheckoutService flow(DiscountPolicy d, int tax) {
        return new CheckoutService(
                new ProductFactory(),
                new PricingService(d, new FixedRateTaxPolicy(tax)),
                new ReceiptPrinter(),
                tax
        );
    }

    @Test void loyal5_matches() {
        String oldR = OrderManagerGod.process("LAT+L", 2, "CARD", "LOYAL5", false);
        String newR = flow(new LoyaltyPercentDiscount(5), 10).checkout("LAT+L", 2);
        assertEquals(oldR, newR);
    }

    @Test void coupon1_matches() {
        String oldR = OrderManagerGod.process("ESP+SHOT", 0, "WALLET", "COUPON1", false);
        String newR = flow(new FixedCouponDiscount(Money.of(1.00)), 10).checkout("ESP+SHOT", 0);
        assertEquals(oldR, newR);
    }

    @Test void none_matches() {
        String oldR = OrderManagerGod.process("ESP+SHOT+OAT", 1, "CASH", "NONE", false);
        String newR = flow(new NoDiscount(), 10).checkout("ESP+SHOT+OAT", 1);
        assertEquals(oldR, newR);
    }
}
