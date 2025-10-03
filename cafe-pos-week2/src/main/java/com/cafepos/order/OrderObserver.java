package com.cafepos.order;

public interface OrderObserver {
void updated(Order order, String eventType);
}