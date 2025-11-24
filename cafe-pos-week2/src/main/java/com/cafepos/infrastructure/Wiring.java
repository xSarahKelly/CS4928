package com.cafepos.infrastructure;

import com.cafepos.application.CheckoutService;
import com.cafepos.domain.*;
import com.cafepos.domain.pricing.*;

public final class Wiring {
    public static record Components(OrderRepository repo, PricingService pricing, CheckoutService checkout) {}

    public static Components createDefault() {
        OrderRepository repo = new InMemoryOrderRepository();
        PricingService pricing = new PricingService(new LoyaltyPercentDiscount(5), new FixedRateTaxPolicy(10));
        CheckoutService checkout = new CheckoutService(repo, pricing);
        return new Components(repo, pricing, checkout);
    }
}
