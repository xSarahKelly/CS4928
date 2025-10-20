package com.cafepos;

import com.cafepos.catalog.Product;
import com.cafepos.catalog.SimpleProduct;
import com.cafepos.common.Money;
import com.cafepos.decorator.ExtraShot;
import com.cafepos.decorator.OatMilk;
import com.cafepos.decorator.Priced;
import com.cafepos.decorator.SizeLarge;
import com.cafepos.factory.ProductFactory;
import com.cafepos.order.LineItem;
import com.cafepos.order.Order;
import com.cafepos.order.OrderIds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class Week5Tests {
    @Test void decorator_single_addon() {
        Product espresso = new SimpleProduct("P-ESP", "Espresso",
                Money.of(2.50));
        Product withShot = new ExtraShot(espresso);
        assertEquals("Espresso + Extra Shot", withShot.name());
// if using Priced interface:
        assertEquals(Money.of(3.30), ((Priced) withShot).price());
    }
    @Test
    void decorator_stacks() {
        Product espresso = new SimpleProduct("P-ESP", "Espresso",
                Money.of(2.50));
        Product decorated = new SizeLarge(new OatMilk(new
                ExtraShot(espresso)));
        assertEquals("Espresso + Extra Shot + Oat Milk (Large)",
                decorated.name());
        assertEquals(Money.of(4.50), ((Priced) decorated).price());
    }
    @Test void factory_parses_recipe() {
        ProductFactory f = new ProductFactory();
        Product p = f.create("ESP+SHOT+OAT");
        assertTrue(p.name().contains("Espresso") &&
                p.name().contains("Oat Milk"));
    }
    @Test void order_uses_decorated_price() {
        Product espresso = new SimpleProduct("P-ESP", "Espresso",
                Money.of(2.50));
        Product withShot = new ExtraShot(espresso); // 3.30
        Order o = new Order(1);
        o.addItem(new LineItem(withShot, 2));
        assertEquals(Money.of(6.60), o.subtotal());
    }

    //activity
    @Test
    void factoryAndManualBuildShouldBeEquivalent() {
        // 1Build drink via factory
        Product viaFactory = new ProductFactory().create("ESP+SHOT+OAT+L");

        // 2️Build same drink manually via decorators
        Product viaManual = new SizeLarge(
                new OatMilk(
                        new ExtraShot(
                                new SimpleProduct("P-ESP", "Espresso", Money.of(2.50))
                        )
                )
        );

        // 3️Compare names
        assertEquals(viaManual.name(), viaFactory.name(),
                "Factory and manually built drink names should match");

        // 4️If using Priced interface, compare unit prices
        if (viaFactory instanceof Priced f && viaManual instanceof Priced m) {
            assertEquals(f.price(), m.price(),
                    "Factory and manually built drink prices should match");
        }

        // 5️Compare order subtotals and totals
        Order orderFactory = new Order(OrderIds.next());
        orderFactory.addItem(new LineItem(viaFactory, 1));

        Order orderManual = new Order(OrderIds.next());
        orderManual.addItem(new LineItem(viaManual, 1));

        assertEquals(orderManual.subtotal(), orderFactory.subtotal(),
                "Subtotals should match for factory and manual drinks");
        assertEquals(orderManual.totalWithTax(10), orderFactory.totalWithTax(10),
                "Totals with tax should match for factory and manual drinks");
    }
}
