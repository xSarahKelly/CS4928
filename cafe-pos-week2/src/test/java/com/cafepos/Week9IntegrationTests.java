package com.cafepos;

import com.cafepos.catalog.Product;
import com.cafepos.common.Money;
import com.cafepos.decorator.Priced;
import com.cafepos.factory.ProductFactory;
import com.cafepos.menu.Menu;
import com.cafepos.menu.MenuComponent;
import com.cafepos.menu.MenuItem;
import com.cafepos.state.OrderFSM;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Week9IntegrationTests {

    @Test
    void depth_first_collects_all_items_via_allItems() {
        Menu root = new Menu("ROOT");
        Menu a = new Menu("A");
        Menu b = new Menu("B");
        root.add(a); root.add(b);
        a.add(new MenuItem("x", Money.of(1.00), true));
        b.add(new MenuItem("y", Money.of(2.00), false));

        List<String> names = root.allItems().stream().map(MenuComponent::name).toList();
        assertTrue(names.contains("x"));
        assertTrue(names.contains("y"));
    }

    @Test
    void order_fsm_happy_path() {
        OrderFSM fsm = new OrderFSM();
        assertEquals("NEW", fsm.status());
        fsm.pay();
        assertEquals("PREPARING", fsm.status());
        fsm.markReady();
        assertEquals("READY", fsm.status());
        fsm.deliver();
        assertEquals("DELIVERED", fsm.status());
    }

    @Test
    void menu_name_to_factory_price_matches_money() {
        String selected = "ESP+SHOT";
        Money menuPrice = Money.of(3.30);

        ProductFactory factory = new ProductFactory();
        Product p = factory.create(selected);
        Money unit = (p instanceof Priced) ? ((Priced) p).price() : p.basePrice();

        assertEquals(menuPrice, unit);
    }

    @Test
    void integration_menu_name_factory_price_matches() {
        String selected = "ESP+SHOT";         // must match your ProductFactory recipe
        Money expected = Money.of(3.30);      // 2.50 + 0.80

        var p = new ProductFactory().create(selected);
        Money unit = (p instanceof com.cafepos.decorator.Priced pr) ? pr.price() : p.basePrice();

        assertEquals(expected, unit);
    }

}
