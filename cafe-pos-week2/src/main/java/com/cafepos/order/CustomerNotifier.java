package com.cafepos.order;

import com.cafepos.domain.Order;

public final class CustomerNotifier implements OrderObserver {
    @Override
    public void updated(Order order, String eventType) {
        System.out.println("[Customer] Dear Customer, your order #" + order.id() + " has been updated: " + eventType);
    }
}
