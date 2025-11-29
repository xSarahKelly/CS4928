package com.cafepos;

import com.cafepos.app.events.EventBus;
import com.cafepos.app.events.OrderCreated;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EventBusTests {
    @Test void handler_receives_event() {
        EventBus bus = new EventBus();
        final int[] count = {0};
        bus.on(OrderCreated.class, e -> count[0]++);
        bus.emit(new OrderCreated(1L));
        assertEquals(1, count[0]);
    }
}
