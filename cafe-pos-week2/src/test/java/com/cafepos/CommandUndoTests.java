package com.cafepos;

import com.cafepos.command.*;
import com.cafepos.domain.*;
import com.cafepos.order.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandUndoTests {
    @Test void undo_removes_last_item() {
        Order order = new Order(OrderIds.next());
        OrderService svc = new OrderService(order);
        PosRemote remote = new PosRemote(2);
        remote.setSlot(0, new AddItemCommand(svc, "ESP+SHOT", 1));
        remote.setSlot(1, new AddItemCommand(svc, "LAT+L", 1));

        remote.press(0);
        remote.press(1);
        assertEquals(2, order.items().size());

        remote.undo();
        assertEquals(1, order.items().size());
    }
}
