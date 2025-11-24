package com.cafepos.checkout;

import com.cafepos.catalog.Product;
import com.cafepos.common.Money;
import com.cafepos.domain.LineItem;
import com.cafepos.domain.Order;
import com.cafepos.factory.ProductFactory;
import com.cafepos.payment.PaymentStrategy;
import com.cafepos.pricing.PricingService;

public final class CheckoutService {
    private final ProductFactory factory;
    private final PricingService pricing;
    private final ReceiptPrinter printer;
    private final int taxPercent;
    private final PaymentStrategy payment; // Step 4: reuse Week-3 strategies (Cash, Card, Wallet)


    public CheckoutService(ProductFactory factory, PricingService pricing, ReceiptPrinter printer, int taxPercent) {
        this(factory, pricing, printer, null, taxPercent);
    }
    

    // New ctor with PaymentStrategy injection.
    public CheckoutService(ProductFactory factory, PricingService pricing,
                           ReceiptPrinter printer, PaymentStrategy payment, int taxPercent) {
        this.factory = factory;
        this.pricing = pricing;
        this.printer = printer;
        this.payment = payment;
        this.taxPercent = taxPercent;
    }

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
    }
}