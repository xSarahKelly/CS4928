package com.cafepos;


import com.cafepos.catalog.SimpleProduct;
import com.cafepos.common.Money;
import com.cafepos.order.LineItem;
import com.cafepos.order.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Week2Tests {
   public Week2Tests() {
   }

   @Test
   void money_add_and_multiply() {
      Money m = Money.of(2.5).multiply(2).add(Money.of(3.5));
      Assertions.assertEquals(Money.of(8.5), m);
   }

   @Test
   void order_totals() {
      SimpleProduct p1 = new SimpleProduct("A", "A", Money.of(2.5));
      SimpleProduct p2 = new SimpleProduct("B", "B", Money.of(3.5));
      Order o = new Order(1L);
      o.addItem(new LineItem(p1, 2));
      o.addItem(new LineItem(p2, 1));
      Assertions.assertEquals(Money.of(8.5), o.subtotal());
      Assertions.assertEquals(Money.of(0.85), o.taxAtPercent(10));
      Assertions.assertEquals(Money.of(9.35), o.totalWithTax(10));
   }
}
