package com.cafepos.application.events;

public record OrderCreated(long orderId) implements OrderEvent { }
