package com.cafepos;
import com.cafepos.smells.OrderManagerGod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class DiscountPolicyCharacterizationTests {

    @Test void loyal5_has_discount_and_total_8_15() {
        String r = OrderManagerGod.process("LAT+L", 2, "CARD", "LOYAL5", false);
        assertTrue(r.contains("Discount: -"));
        assertTrue(r.contains("Total: 8.15"));
    }
    @Test void coupon1_has_discount_1_euro_total_2_53() {
        String r = OrderManagerGod.process("ESP+SHOT", 0, "WALLET", "COUPON1", false);
        assertTrue(r.contains("Discount: -1.00"));
        assertTrue(r.contains("Total: 2.53"));
    }
    @Test void unknown_code_has_no_discount_line() {
        String r = OrderManagerGod.process("LAT+L", 1, "CARD", "UNKNOWN", false);
        assertFalse(r.contains("Discount: -"));
    }
}
