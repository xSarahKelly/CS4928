package com.cafepos;

import com.cafepos.infra.Wiring;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckoutServiceIntegrationTests {
    @Test void receipt_contains_lines() {
        var c = Wiring.createDefault();
        var repo = c.repo();
        var checkout = c.checkout();

        long id = 5001L;
        var controller = new com.cafepos.ui.OrderController(repo, checkout);
        controller.createOrder(id);
        controller.addItem(id, "ESP+SHOT+OAT", 1);
        controller.addItem(id, "LAT+L", 2);

        String receipt = controller.checkout(id, 10);
        assertTrue(receipt.startsWith("Order #5001"));
        assertTrue(receipt.contains("Espresso + Extra Shot + Oat Milk"));
        assertTrue(receipt.contains("Latte (Large)"));
        assertTrue(receipt.contains("Subtotal:"));
        assertTrue(receipt.contains("Total:"));
    }
}
