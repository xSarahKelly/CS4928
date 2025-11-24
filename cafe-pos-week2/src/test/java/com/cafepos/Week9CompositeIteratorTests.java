package com.cafepos;

import com.cafepos.domain.common.Money;
import com.cafepos.menu.Menu;
import com.cafepos.menu.MenuComponent;
import com.cafepos.menu.MenuItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Week9CompositeIteratorTests {

    private Menu buildMenu() {
        Menu root = new Menu("CAFE MENU");
        Menu drinks  = new Menu("Drinks");
        Menu coffee  = new Menu("Coffee");
        Menu desserts = new Menu("Dessert");

        coffee.add(new MenuItem("Espresso", Money.of(2.50), true));
        coffee.add(new MenuItem("Latte (Large)", Money.of(3.90), true));
        drinks.add(coffee);

        desserts.add(new MenuItem("Cheesecake", Money.of(3.50), false));
        desserts.add(new MenuItem("Oat Cookie", Money.of(1.20), true));

        root.add(drinks);
        root.add(desserts);
        return root;

    }

    @Test
    void depth_first_traversal_order_correct() {
        Menu root = buildMenu();
        List<MenuComponent> comps = root.allItems();

        List<String> itemNames = comps.stream()
                .filter(mc -> mc instanceof MenuItem)
                .map(mc -> ((MenuItem) mc).name())
                .toList();

        assertEquals(
                List.of("Espresso", "Latte (Large)", "Cheesecake", "Oat Cookie"),
                itemNames
        );
    }

    @Test
    void vegetarianItems_returns_only_vegetarian_items() {
        Menu root = buildMenu();
        List<MenuItem> veg = root.vegetarianItems();

        Set<String> names = veg.stream().map(MenuItem::name).collect(Collectors.toSet());
        assertEquals(Set.of("Espresso", "Latte (Large)", "Oat Cookie"), names);
        assertTrue(veg.stream().allMatch(MenuItem::vegetarian));
    }
}
