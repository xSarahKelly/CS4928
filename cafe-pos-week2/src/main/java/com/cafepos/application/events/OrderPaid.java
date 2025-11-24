package com.cafepos.application.events;

public record OrderPaid(long orderId) implements OrderEvent { }
