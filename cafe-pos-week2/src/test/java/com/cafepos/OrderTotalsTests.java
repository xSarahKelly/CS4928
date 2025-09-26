package com.cafepos;

import com.cafepos.catalog.SimpleProduct;
import com.cafepos.common.Money;
import com.cafepos.order.LineItem;
import com.cafepos.order.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTotalsTests {
    @Test
    void subtotalTaxTotal() {
        SimpleProduct p1 = new SimpleProduct("1", "Coffee", Money.of(2.50));
        SimpleProduct p2 = new SimpleProduct("2", "Tea",    Money.of(1.75));

        Order order = new Order(42);
        order.addItem(new LineItem(p1, 2)); // 2 * 2.50 = 5.00
        order.addItem(new LineItem(p2, 3)); // 3 * 1.75 = 5.25

        Money expectedSubtotal = p1.basePrice().multiply(2).add(p2.basePrice().multiply(3));
        assertEquals(expectedSubtotal, order.subtotal());

        Money tax = order.taxAtPercent(10); // 10% of subtotal
        Money total = order.totalWithTax(10);
        assertEquals(expectedSubtotal.add(tax), total);
    }
}
