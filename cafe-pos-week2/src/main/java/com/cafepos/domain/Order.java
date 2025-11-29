package com.cafepos.domain;

import com.cafepos.common.Money;
import com.cafepos.order.OrderObserver;
import com.cafepos.payment.PaymentStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class Order {
   private final long id;
   private final List<LineItem> items = new ArrayList<>();
   private final List<OrderObserver> observers = new ArrayList<>();

   public void register(OrderObserver o) {
      if (o == null)
         throw new IllegalArgumentException("observer required");
      if (!this.observers.contains(o)) {
         this.observers.add(o);
      }
   }

   public void unregister(OrderObserver o) {
      if (o == null)
         throw new IllegalArgumentException("observer required");
      this.observers.remove(o);
   }

   private void notifyObservers(Order order, String eventType) {
      for (OrderObserver o : this.observers) {
         o.updated(order, eventType);
      }
   }

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
         notifyObservers(this, "itemAdded");
      }
   }

   public void removeLastItem() {
      if (!items.isEmpty()) {
         items.remove(items.size() - 1);
         notifyObservers(this, "itemRemoved");
      }
   }

   public void pay(PaymentStrategy strategy) {
      if (strategy == null)
         throw new IllegalArgumentException("strategy required");
      strategy.pay(this);
      notifyObservers(this, "paid");
   }

   public void markReady() {
      notifyObservers(this, "ready");
   }

   public Money subtotal() {
      return (Money) this.items.stream().map(LineItem::lineTotal).reduce(Money.zero(), Money::add);
   }

   public Money taxAtPercent(int percent) {
      if (percent < 0) {
         throw new IllegalArgumentException("percent >= 0");
      } else {
         BigDecimal tax = this.subtotal().asBigDecimal().multiply(BigDecimal.valueOf((long) percent))
               .divide(BigDecimal.valueOf(100L));
         return Money.of(tax);
      }
   }

   public Money totalWithTax(int percent) {
      return this.subtotal().add(this.taxAtPercent(percent));
   }

}
