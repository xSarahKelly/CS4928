package com.cafepos.catalog;

import java.util.Objects;

import com.cafepos.common.Money;

public final class SimpleProduct implements Product {
    private final String id;
    private final String name;
    private final Money basePrice;
    public SimpleProduct(String id, String name, Money basePrice) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (basePrice == null) throw new IllegalArgumentException("basePrice required");
        // Money already enforces non-negative
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
    }
    @Override public String id() { return id; }
    @Override public String name() { return name; }
    @Override public Money basePrice() { return basePrice; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleProduct)) return false;
        SimpleProduct that = (SimpleProduct) o;
        return id.equals(that.id) && name.equals(that.name) && basePrice.equals(that.basePrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, basePrice);
    }

    @Override
    public String toString() {
        return id + ":" + name + "@" + basePrice;
    }

}