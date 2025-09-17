package com.cafepos.domain;

import com.cafepos.common.Money;
import java.util.ArrayList;
import java.util.List;
//an aggregate root: holds LineItems, exposes totals only
public final class Order {
    private final long id;
    private final List<LineItem> items = new ArrayList<>();

    public Order(long id) { this.id = id; }

    public void addItem(LineItem li) {
        if (li == null) throw new IllegalArgumentException("line item required");
        if (li.quantity() <= 0) throw new IllegalArgumentException("quantity must be > 0");
        items.add(li);
    }

    public List<LineItem> items() {
        return List.copyOf(items);
    }

    public Money subtotal() {
        return items.stream()
                    .map(LineItem::lineTotal)
                    .reduce(Money.zero(), Money::add);
    }

    public Money taxAtPercent(int percent) {
        if (percent < 0) throw new IllegalArgumentException("percent must be >= 0");
        return subtotal().percentage(percent);
    }

    public Money totalWithTax(int percent) {
        return subtotal().add(taxAtPercent(percent));
    }

    public long id() { return id; }
}
