package com.cafepos.checkout;

import com.cafepos.factory.ProductFactory;
import com.cafepos.order.LineItem;
import com.cafepos.order.Order;
import com.cafepos.payment.PaymentStrategy;

public final class PaymentAdapter {
    private final PaymentStrategy strategy;
    private final ProductFactory factory;

    public PaymentAdapter(PaymentStrategy strategy, ProductFactory factory) {
        this.strategy = strategy;
        this.factory = factory;
    }

    public void pay(String recipe, int qty) {
        int q = (qty <= 0) ? 1 : qty;
        Order order = new Order(System.currentTimeMillis());
        order.addItem(new LineItem(factory.create(recipe), q));
        strategy.pay(order);
    }
}
