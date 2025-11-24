package com.cafepos;
import com.cafepos.catalog.SimpleProduct;
import com.cafepos.domain.LineItem;
import com.cafepos.domain.Order;
import com.cafepos.domain.common.Money;
import com.cafepos.payment.CardPayment;
import com.cafepos.payment.CashPayment;
import com.cafepos.payment.PaymentStrategy;
import com.cafepos.payment.WalletPayment;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
public class Week3Tests {
    @Test
    void payment_strategy_called() {
        var p = new SimpleProduct("A", "A", Money.of(5));
        var order = new Order(42);
        order.addItem(new LineItem(p, 1));

        final boolean[] called = { false };
        PaymentStrategy fake = o -> called[0] = true;
        order.pay(fake);
        assertTrue(called[0], "Payment strategy should be called");
    }
    @Test
    void cash_payment_prints_total() {
        Order o = sampleOrder();
        String out = captureStdout(() -> o.pay(new CashPayment()));
        assertTrue(out.contains("[Cash]"), "Cash prefix missing");
        assertTrue(out.contains("9.35"), "Total with tax (10%) should appear");
    }

    @Test
    void card_payment_masks_last_four() {
        Order o = sampleOrder();
        String out = captureStdout(() -> o.pay(new CardPayment("1234567812341234")));
        assertTrue(out.contains("[Card]"), "Card prefix missing");
        assertTrue(out.contains("9.35"), "Total with tax (10%) should appear");
        assertTrue(out.contains("****1234"), "Card should be masked to last 4");
    }

    @Test
    void wallet_payment_includes_wallet_id() {
        Order o = sampleOrder();
        String out = captureStdout(() -> o.pay(new WalletPayment("alice-wallet-01")));
        assertTrue(out.contains("[Wallet]"), "Wallet prefix missing");
        assertTrue(out.contains("9.35"), "Total with tax (10%) should appear");
        assertTrue(out.contains("alice-wallet-01"), "Wallet ID should be printed");
    }

    private static Order sampleOrder() {
        var espresso = new SimpleProduct("P-ESP", "Espresso", Money.of(2.50));
        var cookie   = new SimpleProduct("P-CCK", "Chocolate Cookie", Money.of(3.50));
        var o = new Order(1001);
        o.addItem(new LineItem(espresso, 2)); // 2 * 2.50 = 5.00
        o.addItem(new LineItem(cookie, 1));   // + 3.50 = 8.50; +10% = 9.35
        return o;
    }
    private static String captureStdout(Runnable r) {
        PrintStream orig = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf));
        try { r.run(); } finally { System.setOut(orig); }
        return buf.toString();
    }
}
