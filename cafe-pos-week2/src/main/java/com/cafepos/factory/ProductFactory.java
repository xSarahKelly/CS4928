package com.cafepos.factory;
import com.cafepos.catalog.*;
import com.cafepos.common.Money;
import com.cafepos.decorator.*;
public final class ProductFactory {

    public Product create(String recipe) {
        if (recipe == null || recipe.isBlank())
            throw new IllegalArgumentException("recipe required");

        String[] raw = recipe.split("\\+"); // literal '+'
        String[] parts = java.util.Arrays.stream(raw)
                .map(String::trim)
                .filter(s -> !s.isEmpty())    //drop empty tokens
                .map(String::toUpperCase)
                .toArray(String[]::new);

        Product p = switch (parts[0]) {
            case "ESP" -> new SimpleProduct("P-ESP", "Espresso", Money.of(2.50));
            case "LAT" -> new SimpleProduct("P-LAT", "Latte", Money.of(3.20));
            case "CAP" -> new SimpleProduct("P-CAP", "Cappuccino", Money.of(3.00));
            default -> throw new
                    IllegalArgumentException("Unknown base: " + parts[0]);
        };

        for (int i = 1; i < parts.length; i++) {
            p = switch (parts[i]) {
                case "SHOT" -> new ExtraShot(p);
                case "OAT" -> new OatMilk(p);
                case "SYP" -> new Syrup(p);
                case "L" -> new SizeLarge(p);
                default -> throw new
                        IllegalArgumentException("Unknown addon: " + parts[i]);
            };
        }
        return p;
    }
}