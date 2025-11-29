package com.cafepos;

import com.cafepos.printing.*;
import vendor.legacy.LegacyThermalPrinter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdapterTests {
    static class FakeLegacyPrinter extends LegacyThermalPrinter {
        int lastLen = -1;
        @Override public void legacyPrint(byte[] payload) { lastLen = payload.length; }
    }

    @Test void adapter_converts_text_to_bytes() {
        FakeLegacyPrinter fake = new FakeLegacyPrinter();
        Printer p = new LegacyPrinterAdapter(fake);
        p.print("ABC");
        assertTrue(fake.lastLen >= 3);
    }
}
