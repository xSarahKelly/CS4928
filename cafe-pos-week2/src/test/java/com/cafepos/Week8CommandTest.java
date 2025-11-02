package com.cafepos;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the command pattern: individual command behavior, undo/redo, and macro command.
 * Uses small fake objects to simulate an Order and Commands without touching production UI.
 */
public class Week8CommandTest {

    // ---- Tiny Fakes -------------------------------------------------

    static class FakeOrder {
        int itemCount = 0;
        double subtotal = 0.0;

        void addItem(String sku, int qty, double priceEach) {
            itemCount += qty;
            subtotal += priceEach * qty;
        }

        void removeItem(String sku, int qty, double priceEach) {
            itemCount -= qty;
            subtotal -= priceEach * qty;
        }
    }

    interface Command {
        void execute();
        void undo();
    }

    static class AddItemCommand implements Command {
        private final FakeOrder order;
        private final String sku;
        private final int qty;
        private final double price;

        AddItemCommand(FakeOrder order, String sku, int qty, double price) {
            this.order = order;
            this.sku = sku;
            this.qty = qty;
            this.price = price;
        }

        @Override
        public void execute() {
            order.addItem(sku, qty, price);
        }

        @Override
        public void undo() {
            order.removeItem(sku, qty, price);
        }
    }

    static class MacroCommand implements Command {
        private final List<Command> commands;

        MacroCommand(List<Command> commands) {
            this.commands = commands;
        }

        @Override
        public void execute() {
            for (Command c : commands) c.execute();
        }

        @Override
        public void undo() {
            // Undo in reverse order
            var it = commands.listIterator(commands.size());
            while (it.hasPrevious()) it.previous().undo();
        }
    }

    // ---- Tests ------------------------------------------------------

    @Test
    void addItemCommand_execute_and_undo_affect_order() {
        FakeOrder order = new FakeOrder();
        Command addLatte = new AddItemCommand(order, "LAT", 2, 3.90);

        assertEquals(0, order.itemCount);
        addLatte.execute();
        assertEquals(2, order.itemCount, "execute() should increase item count");

        addLatte.undo();
        assertEquals(0, order.itemCount, "undo() should revert item count");
    }

    @Test
    void macroCommand_undo_is_reverse_order() {
        List<String> log = new ArrayList<>();

        Command c1 = new Command() {
            public void execute() { log.add("exec1"); }
            public void undo() { log.add("undo1"); }
        };
        Command c2 = new Command() {
            public void execute() { log.add("exec2"); }
            public void undo() { log.add("undo2"); }
        };

        MacroCommand macro = new MacroCommand(List.of(c1, c2));

        macro.execute();
        macro.undo();

        assertEquals(
                List.of("exec1", "exec2", "undo2", "undo1"),
                log,
                "Undo must occur in reverse order of execution"
        );
    }

    @Test
    void macroCommand_with_adds_and_single_undo_reverts_last() {
        FakeOrder order = new FakeOrder();
        Command add1 = new AddItemCommand(order, "LAT", 1, 3.90);
        Command add2 = new AddItemCommand(order, "LAT", 1, 3.90);

        MacroCommand macro = new MacroCommand(List.of(add1, add2));
        macro.execute();

        assertEquals(2, order.itemCount);
        assertEquals(7.80, order.subtotal, 0.001);

        macro.undo(); // should undo both in reverse order
        assertEquals(0, order.itemCount);
        assertEquals(0.0, order.subtotal, 0.001);
    }
}
