package com.cafepos.demo;

import com.cafepos.catalog.Catalog;
import com.cafepos.catalog.InMemoryCatalog;
import com.cafepos.catalog.SimpleProduct;
import com.cafepos.common.Money;
import com.cafepos.order.CustomerNotifier;
import com.cafepos.order.DeliveryDesk;
import com.cafepos.order.KitchenDisplay;
import com.cafepos.order.LineItem;
import com.cafepos.order.Order;
import com.cafepos.order.OrderIds;
import com.cafepos.payment.CashPayment;

public final class Week4Demo {
public static void main(String[] args) {
Catalog catalog = new InMemoryCatalog();
catalog.add(new SimpleProduct("P-ESP", "Espresso",
Money.of(2.50)));
Order order = new Order(OrderIds.next());
order.register(new KitchenDisplay());
order.register(new DeliveryDesk());
order.register(new CustomerNotifier());
order.addItem(new LineItem(catalog.findById("P-ESP").orElseThrow(), 1));
order.pay(new CashPayment());
order.markReady();
}
}