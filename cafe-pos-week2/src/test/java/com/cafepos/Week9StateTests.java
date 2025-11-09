package com.cafepos;

import com.cafepos.state.OrderFSM;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Week9StateTests {
    //NEW→pay→PREPARING→markReady→READY→deliver→DELIVERED

    @Test
    void legal_path_NEW_to_DELIVERED() {
        OrderFSM fsm = new OrderFSM();

        assertEquals("NEW", fsm.status());

        fsm.pay();
        assertEquals("PREPARING", fsm.status());

        fsm.markReady();
        assertEquals("READY", fsm.status());

        fsm.deliver();
        assertEquals("DELIVERED", fsm.status());
    }

    @Test
    void illegal_PREPARE_before_PAY() {
        OrderFSM fsm = new OrderFSM();
        assertEquals("NEW", fsm.status());
        fsm.prepare();

        // State unchanged
        assertEquals("NEW", fsm.status());
    }

    @Test
    void illegal_DELIVER_before_READY() {
        OrderFSM fsm = new OrderFSM();
        fsm.pay();

        fsm.deliver();

        // Still preparing
        assertEquals("PREPARING", fsm.status());
    }

}


