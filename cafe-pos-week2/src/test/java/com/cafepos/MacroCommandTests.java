package com.cafepos;

import com.cafepos.command.*;
import com.cafepos.order.*;
import com.cafepos.domain.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MacroCommandTests {
    @Test void macro_runs_all_and_undo_rolls_back() {
        Order order = new Order(OrderIds.next());
        OrderService svc = new OrderService(order);

        Command a = new AddItemCommand(svc, "ESP", 1);
        Command b = new AddItemCommand(svc, "LAT+L", 1);
        Command macro = new MacroCommand(a, b);

        macro.execute();
        assertEquals(2, order.items().size());

        macro.undo();
        assertEquals(0, order.items().size());
    }
}
