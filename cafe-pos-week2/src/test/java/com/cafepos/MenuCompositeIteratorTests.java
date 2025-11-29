package com.cafepos;

import com.cafepos.menu.*;
import com.cafepos.common.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class MenuCompositeIteratorTests {
    @Test void depth_first_includes_leaves() {
        Menu root = new Menu("ROOT");
        Menu a = new Menu("A");
        Menu b = new Menu("B");
        root.add(a); root.add(b);
        a.add(new MenuItem("x", Money.of(1.0), true));
        b.add(new MenuItem("y", Money.of(2.0), false));

        List<String> names = root.allItems().stream().map(MenuComponent::name).toList();
        assertTrue(names.contains("x"));
        assertTrue(names.contains("y"));
    }

    @Test void vegetarian_filter_only_veg() {
        Menu root = new Menu("ROOT");
        root.add(new MenuItem("x", Money.of(1.0), true));
        root.add(new MenuItem("y", Money.of(2.0), false));
        var veg = root.vegetarianItems();
        assertEquals(1, veg.size());
        assertEquals("x", veg.get(0).name());
    }
}
