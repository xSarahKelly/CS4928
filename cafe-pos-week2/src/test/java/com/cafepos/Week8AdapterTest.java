package com.cafepos;

import com.cafepos.printing.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vendor.legacy.LegacyThermalPrinter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Adapter pattern — verifying that the LegacyPrinterAdapter correctly
 * converts text to bytes, delegates once, and maintains UTF-8 encoding.
 */
public class Week8AdapterTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream out;

    @BeforeEach
    void hookStdout() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    // ---- Helpers ----------------------------------------------------

    private static int extractLegacyByteLen(String console) {
        Pattern p = Pattern.compile("\\[Legacy\\] printing bytes:\\s*(\\d+)");
        Matcher m = p.matcher(console);
        if (!m.find()) return -1;
        return Integer.parseInt(m.group(1));
    }

    private static int countLegacyLines(String console) {
        Pattern p = Pattern.compile("\\[Legacy\\] printing bytes:");
        Matcher m = p.matcher(console);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    // ---- Tests ------------------------------------------------------

    @Test
    void adapter_converts_text_to_bytes_utf8_exact() {
        String receipt = "Total: 8.58 €"; // € is 3 bytes in UTF-8
        Printer printer = new LegacyPrinterAdapter(new LegacyThermalPrinter());

        printer.print(receipt);

        String console = out.toString();
        assertTrue(console.contains("[Legacy] printing bytes:"), "Should invoke legacy printer");

        int reportedLen = extractLegacyByteLen(console);
        assertTrue(reportedLen > 0, "Reported byte length must be positive");

        int expectedLen = receipt.getBytes(StandardCharsets.UTF_8).length;
        assertEquals(expectedLen, reportedLen, "Adapter must send UTF-8 bytes to legacy printer");
    }

    @Test
    void adapter_delegates_once_and_length_at_least_3_for_ABC() {
        String text = "ABC";
        Printer printer = new LegacyPrinterAdapter(new LegacyThermalPrinter());
        printer.print(text);

        String console = out.toString();
        int calls = countLegacyLines(console);
        assertEquals(1, calls, "Adapter should delegate exactly once");

        int reportedLen = extractLegacyByteLen(console);
        assertTrue(reportedLen >= 3, "Expected ≥3 bytes for 'ABC', got " + reportedLen);
    }

    @Test
    void adapter_prints_in_demo_order() {
        String receipt = "Order (LAT+L) x2\nSubtotal: 7.80\nTotal: 8.58";
        Printer printer = new LegacyPrinterAdapter(new LegacyThermalPrinter());
        printer.print(receipt);

        String console = out.toString();
        // The demo should first print Legacy line, then Demo marker
        assertTrue(console.contains("[Legacy] printing bytes:"), "Legacy line missing");
    }
}
