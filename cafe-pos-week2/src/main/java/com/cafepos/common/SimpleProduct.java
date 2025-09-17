package com.cafepos.common;

import java.util.Objects;

public final class SimpleProduct implements Product {
    private final String id;
    private final String name;
    private final Money basePrice;
    public SimpleProduct(String id, String name, Money basePrice) {
        if (id == null || id.isBlank()){
            throw new IllegalArgumentException("id required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name required");
        }
        this.basePrice = Objects.requireNonNull(basePrice, "basePrice required");
        this.id = id;
        this.name = name;
    }
    @Override public String id() { return id; }
    @Override public String name() { return name; }
    @Override public Money basePrice() { return basePrice; }
}