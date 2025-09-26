package com.cafepos.catalog;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryCatalog implements Catalog {
private final Map<String, Product> byId = new HashMap<>();

public void add(Product p) {
if (p == null) {
    throw new IllegalArgumentException("product required");
} else {
    this.byId.put(p.id(), p);
}
}

public Optional<Product> findById(String id) {
return Optional.ofNullable((Product)this.byId.get(id));
}
}
