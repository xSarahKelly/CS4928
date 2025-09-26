package com.cafepos.order;

import com.cafepos.common.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class Order {
   private final long id;
   private final List<LineItem> items = new ArrayList();

   public Order(long id) {
      this.id = id;
   }

   public long id() {
      return this.id;
   }

   public List<LineItem> items() {
      return List.copyOf(this.items);
   }

   public void addItem(LineItem li) {
      if (li == null) {
         throw new IllegalArgumentException("line item required");
      } else {
         this.items.add(li);
      }
   }

   public Money subtotal() {
      return (Money)this.items.stream().map(LineItem::lineTotal).reduce(Money.zero(), Money::add);
   }

   public Money taxAtPercent(int percent) {
      if (percent < 0) {
         throw new IllegalArgumentException("percent >= 0");
      } else {
         BigDecimal tax = this.subtotal().asBigDecimal().multiply(BigDecimal.valueOf((long)percent)).divide(BigDecimal.valueOf(100L));
         return Money.of(tax);
      }
   }

   public Money totalWithTax(int percent) {
      return this.subtotal().add(this.taxAtPercent(percent));
   }
}
