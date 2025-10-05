package com.cafepos.order;

public final class KitchenDisplay implements OrderObserver {
@Override
public void updated(Order order, String eventType) {
    if ("itemAdded".equals(eventType)) {
        var items = order.items(); 
        if (items != null && !items.isEmpty()) {
            var last = items.get(items.size() - 1);
        String name = last.product().name();
            System.out.println("[Kitchen] order #" + order.id() + ": " + name + " item(s) added");
        } else {
            System.out.println("[Kitchen] order #" + order.id() + ": item(s) added");
        }
    } else if ("paid".equals(eventType)) {
        System.out.println("[Kitchen] order #" + order.id() + ": payment received");
    }
}
}