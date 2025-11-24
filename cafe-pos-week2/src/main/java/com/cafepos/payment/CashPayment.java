package com.cafepos.payment;

import com.cafepos.domain.Order;
import com.cafepos.domain.common.Money;

public final class CashPayment implements PaymentStrategy {
    private final Money amountGiven;
    private final boolean hasAmountGiven;

    
    public CashPayment() {
        this.amountGiven = Money.of(0);
        this.hasAmountGiven = false;
    }

   
    public CashPayment(double amountGiven) {
        this.amountGiven = Money.of(amountGiven);
        this.hasAmountGiven = true;
    }

    @Override
    public void pay(Order order) {
        Money total = order.totalWithTax(10);

        if (!hasAmountGiven) {
            // Original simple print behavior
            System.out.println("[Cash] Customer paid " + total + " EUR");
            return;
        }

        // New behavior: handle change
        System.out.println("[Cash] Customer gave " + amountGiven + " EUR");

        if (amountGiven.isLessThan(total)) {
            System.out.println("Insufficient cash provided. Payment failed.");
        } else {
            Money change = amountGiven.minus(total);
            System.out.println("Payment accepted.");
            System.out.println("Change returned: " + change + " EUR");
        }
    }
}
