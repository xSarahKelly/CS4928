package com.cafepos;

import com.cafepos.catalog.SimpleProduct;
import com.cafepos.common.Money;
import com.cafepos.domain.LineItem;
import com.cafepos.domain.Order;
import com.cafepos.order.KitchenDisplay;
import com.cafepos.order.DeliveryDesk;
import com.cafepos.order.CustomerNotifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class Week4Tests {
 
    @Test
    void observers_notified_on_item_add() {
        var p = new SimpleProduct("A", "A", Money.of(5));
        var order = new Order(42);
        order.addItem(new LineItem(p, 1));

        List<String> events = new ArrayList<>();
        order.register((o, e) -> events.add(e));
        order.addItem(new LineItem(p, 1));
assertTrue(events.contains("itemAdded"), "Observer should be notified of itemAdded");
    }

    @Test
void kitchen_display_shows_item_added() {
    var p = new SimpleProduct("A", "A", Money.of(5));
    var order = new Order(42);

    String out = captureStdout(() -> {
        var kd = new KitchenDisplay();
        order.register(kd);
        order.addItem(new LineItem(p, 1));
    });

    assertTrue(out.contains("[Kitchen] order #42: A item(s) added"), 
               "KitchenDisplay should show itemAdded message");
}


    @Test
    void delivery_desk_shows_ready() {
        var p = new SimpleProduct("A", "A", Money.of(5));
        var order = new Order(42);
        order.addItem(new LineItem(p, 1));

        String out = captureStdout(() -> {
            var dd = new DeliveryDesk();
            order.register(dd);
            order.markReady();
        });
        assertTrue(out.contains("[Delivery] order #42 is ready for delivery"), "DeliveryDesk should show ready message");
    }

    @Test
    void customer_notifier_shows_updates() {
        var p = new SimpleProduct("A", "A", Money.of(5));
        var order = new Order(42);
        order.addItem(new LineItem(p, 1));

        String out = captureStdout(() -> {
            var cn = new CustomerNotifier();
            order.register(cn);
            order.addItem(new LineItem(p, 1));
            order.markReady();
        });
        assertTrue(out.contains("[Customer] Dear Customer, your order #42 has been updated: itemAdded"), "CustomerNotifier should show itemAdded message");
        assertTrue(out.contains("[Customer] Dear Customer, your order #42 has been updated: ready"), "CustomerNotifier should show ready message");
    }

    private static String captureStdout(Runnable action) {
        var originalOut = System.out;
        var baos = new ByteArrayOutputStream();
        var ps = new PrintStream(baos);
        System.setOut(ps);
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString();
    }

}
