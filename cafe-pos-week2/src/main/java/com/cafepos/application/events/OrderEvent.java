package com.cafepos.application.events;

public sealed interface OrderEvent permits OrderCreated, OrderPaid { }
