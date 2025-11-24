package com.cafepos.application;

import com.cafepos.catalog.Product;
import com.cafepos.payment.PaymentStrategy;
import com.cafepos.domain.*;
import com.cafepos.domain.common.Money;
import com.cafepos.domain.factory.ProductFactory;
import com.cafepos.domain.pricing.PricingService;
import com.cafepos.domain.pricing.PricingService.PricingResult;

public final class CheckoutService {
    private final ProductFactory factory;
    private final PricingService pricing;
    private final ReceiptFormatter printer;
    private final int taxPercent;
    private final PaymentStrategy payment; // Step 4: reuse Week-3 strategies (Cash, Card, Wallet)
    private final OrderRepository orders;



    public CheckoutService(ProductFactory factory, PricingService pricing, ReceiptFormatter printer, int taxPercent) {
        this(factory, pricing, printer, null, taxPercent);
    }

    public CheckoutService(OrderRepository orders, PricingService pricing) {
        this.orders = orders;
        this.pricing = pricing;
        this.factory = null;
        this.printer = null;
        this.payment = null;
        this.taxPercent = 0;
    }
    

    // New ctor with PaymentStrategy injection.
    public CheckoutService(ProductFactory factory, PricingService pricing,
                           ReceiptFormatter printer, PaymentStrategy payment, int taxPercent) {
        this.factory = factory;
        this.pricing = pricing;
        this.printer = printer;
        this.payment = payment;
        this.taxPercent = taxPercent;
        this.orders = null;
    }
/*
    public String checkout(String recipe, int qty) {
        Product product = factory.create(recipe);
        if (qty <= 0) qty = 1;

        Money unit;
        try {
            if (product instanceof com.cafepos.decorator.Priced p) {
                unit = p.price();
            } else {
                unit = product.basePrice();
            }
        } catch (Exception e) {
            unit = product.basePrice();
        }

        Money subtotal = unit.multiply(qty);
        var result = pricing.price(subtotal);


        String receipt = printer.format(recipe, qty, result, taxPercent);

        if (payment != null) {
            Order order = new Order(System.currentTimeMillis());
            order.addItem(new LineItem(product, qty));
            payment.pay(order);
        }

        return receipt;
    }*/
    
    public String checkout(long orderId, int taxPercent) {
        Order order = orders.findById(orderId).orElseThrow();
        var pr = pricing.price(order.subtotal());
        return new ReceiptFormatter().format(orderId, order.items(), pr, taxPercent);
    } 
}