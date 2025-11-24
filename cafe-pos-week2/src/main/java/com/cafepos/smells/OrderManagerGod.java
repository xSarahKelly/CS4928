package com.cafepos.smells;
import com.cafepos.domain.common.Money;
import com.cafepos.domain.factory.ProductFactory;
import com.cafepos.catalog.Product;


//God Class (Bloater) (does too much)
public class OrderManagerGod {


    //Long method and too many responsibilities (creation, pricing, discount, tax, payment I/O, printing)
    public static String process(String recipe, int qty, String paymentType, String discountCode, boolean printReceipt) {

        final int taxPercent = 10; 
        // Feature Envy / Shotgun Surgery: creation logic here so product rule changes require edita
        ProductFactory factory = new ProductFactory();
        Product product = factory.create(recipe);

        // Duplicated Logic: price resolution & exception fallback scattered inline.
        Money unitPrice;
        try {

            var priced = product instanceof com.cafepos.decorator.Priced p ? p.price() : product.basePrice();
            unitPrice = priced;
        } catch (Exception e) {
            unitPrice = product.basePrice();
        }
        if (qty <= 0) qty = 1;

        // Duplicated Logic: Money/BigDecimal math scattered inline.
        Money subtotal = unitPrice.multiply(qty);

        // Primitive Obsession: string protocol for discountCode and magic numbers for rates.
        // Feature Envy / Shotgun Surgery: discount rules embedded inline so adding a rule requires editing
        Money discount = Money.zero();

        if (discountCode != null) { // Primitive Obsession (string presence controls logic)
            if (discountCode.equalsIgnoreCase("LOYAL5")) {// Primitive Obsession
                // Duplicated Logic: repeated percentage maths, rounding policy implicit.
                // Primitive Obsession: magic numbers 5 and 100
                discount = Money.of(subtotal.asBigDecimal()
                        .multiply(java.math.BigDecimal.valueOf(5))
                        .divide(java.math.BigDecimal.valueOf(100)));
            } else if (discountCode.equalsIgnoreCase("COUPON1")) { // Primitive Obsession String
                discount = Money.of(1.00); //Primitive Obsession magic number
            } else if (discountCode.equalsIgnoreCase("NONE")) {
                discount = Money.zero();
            } else {
                discount = Money.zero();
            }

        }

        // Duplicated Logic: manual subtraction
        Money discounted = Money.of(subtotal.asBigDecimal().subtract(discount.asBigDecimal()));
        if (discounted.asBigDecimal().signum() < 0) discounted = Money.zero();


        // Feature Envy / Shotgun Surgery: tax rule embedded inline so any rule change requires edits
        // Primitive Obsession: TAX_PERCENT as primitive and magic number 100
        // Duplicated Logic: repeated percentage maths and implicit rounding
        var tax = Money.of(discounted.asBigDecimal()
                .multiply(java.math.BigDecimal.valueOf(taxPercent))
                .divide(java.math.BigDecimal.valueOf(100)));

        var total = discounted.add(tax); // Duplicated Logic: repeated Money composition.

        // Feature Envy / Shotgun Surgery: payment handling via string protocol and if/else chain (should be Strategy)
        if (paymentType != null) { // Primitive Obsession string
            if (paymentType.equalsIgnoreCase("CASH")) {
                System.out.println("[Cash] Customer paid " + total + " EUR");
            } else if (paymentType.equalsIgnoreCase("CARD")) {
                System.out.println("[Card] Customer paid " + total + " EUR with card ****1234");
            } else if (paymentType.equalsIgnoreCase("WALLET")) {
                System.out.println("[Wallet] Customer paid " + total + " EUR via wallet user-wallet-789");
            } else {
                System.out.println("[UnknownPayment] " + total);
            }
        }

        // Long Method & Duplicated Logic: mixes UI formatting with pricing, repeated values, should be extracted.
        StringBuilder receipt = new StringBuilder();
        receipt.append("Order (").append(recipe).append(") x").append(qty).append("\n");
                receipt.append("Subtotal: ").append(subtotal).append("\n");
        if (discount.asBigDecimal().signum() > 0) {
            receipt.append("Discount: -").append(discount).append("\n");
        }
        receipt.append("Tax (").append(taxPercent).append("%): ").append(tax).append("\n");
                receipt.append("Total: ").append(total);
        String out = receipt.toString();
        if (printReceipt) {
            // Long Method / Mixed Concerns: I/O side effect inside pricing flow
            System.out.println(out);
        }
        return out;
    }
}