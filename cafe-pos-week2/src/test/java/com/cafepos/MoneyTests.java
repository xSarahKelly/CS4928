package com.cafepos;

import com.cafepos.common.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyTests {
    @Test
    void addAndMultiply() {
        Money a = Money.of(2.00);
        Money b = Money.of(3.00);
        assertEquals(Money.of(5.00), a.add(b));
        assertEquals(Money.of(6.00), a.multiply(3));
    }

    @Test
    void negativeNotAllowed() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Money.of(-1.0));
        assertTrue(ex.getMessage().toLowerCase().contains("amount"));
    }
}
