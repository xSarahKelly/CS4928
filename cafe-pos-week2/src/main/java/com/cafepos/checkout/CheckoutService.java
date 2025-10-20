package com.cafepos.checkout;

import com.cafepos.catalog.Product;
import com.cafepos.common.Money;
import com.cafepos.factory.ProductFactory;
import com.cafepos.pricing.PricingService;
import com.cafepos.pricing.ReceiptPrinter;

public final class CheckoutService {
    private final ProductFactory factory;
    private final PricingService pricing;
    private final ReceiptPrinter printer;
    private final int taxPercent;

    public CheckoutService(ProductFactory factory, PricingService pricing, ReceiptPrinter printer, int taxPercent) {
        this.factory = factory; this.pricing = pricing; this.printer = printer; this.taxPercent = taxPercent;
    }

    public String checkout(String recipe, int qty) {
        Product product = factory.create(recipe);
        if (qty <= 0) qty = 1;

        Money unit;
        try {
            if (product instanceof com.cafepos.decorator.Priced) {
                unit = ((com.cafepos.decorator.Priced) product).price();
            } else { unit = product.basePrice(); }
        } catch (Exception e) { unit = product.basePrice(); }

        Money subtotal = unit.multiply(qty);
        var pr = pricing.price(subtotal);
        return printer.format(recipe, qty, pr, taxPercent);
    }
}
