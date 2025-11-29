package com.cafepos;

import com.cafepos.app.CheckoutService;
import com.cafepos.app.ReceiptFormatter;
import com.cafepos.app.events.*;
import com.cafepos.common.Money;
import com.cafepos.domain.LineItem;
import com.cafepos.domain.OrderRepository;
import com.cafepos.factory.ProductFactory;
import com.cafepos.infra.InMemoryOrderRepository;
import com.cafepos.infra.Wiring;
import com.cafepos.pricing.*;
import com.cafepos.ui.OrderController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Week 10 Tests - Layered Architecture, MVC, and EventBus
 */
public class Week10ArchitectureTests {

    // ═══════════════════════════════════════════════════════════════
    // DOMAIN LAYER TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Domain Layer - Order Entity")
    class OrderEntityTests {

        @Test
        @DisplayName("Order should store and return items")
        void orderStoresItems() {
            com.cafepos.domain.Order order = new com.cafepos.domain.Order(1001L);
            ProductFactory factory = new ProductFactory();
            
            order.addItem(new LineItem(factory.create("ESP"), 2));
            order.addItem(new LineItem(factory.create("LAT"), 1));
            
            assertEquals(2, order.items().size());
            assertEquals(1001L, order.id());
        }

        @Test
        @DisplayName("Order.removeLastItem should remove the last added item")
        void removeLastItemWorks() {
            com.cafepos.domain.Order order = new com.cafepos.domain.Order(1002L);
            ProductFactory factory = new ProductFactory();
            
            order.addItem(new LineItem(factory.create("ESP"), 1));
            order.addItem(new LineItem(factory.create("LAT"), 1));
            
            assertEquals(2, order.items().size());
            
            order.removeLastItem();
            
            assertEquals(1, order.items().size());
            assertEquals("Espresso", order.items().get(0).product().name());
        }

        @Test
        @DisplayName("Order.removeLastItem on empty order should be safe")
        void removeLastItemOnEmptyIsSafe() {
            com.cafepos.domain.Order order = new com.cafepos.domain.Order(1003L);
            assertDoesNotThrow(() -> order.removeLastItem());
            assertTrue(order.items().isEmpty());
        }

        @Test
        @DisplayName("Order subtotal should sum all line items")
        void subtotalCalculation() {
            com.cafepos.domain.Order order = new com.cafepos.domain.Order(1004L);
            ProductFactory factory = new ProductFactory();
            
            // ESP = 2.50, LAT = 3.20
            order.addItem(new LineItem(factory.create("ESP"), 2)); // 5.00
            order.addItem(new LineItem(factory.create("LAT"), 1)); // 3.20
            
            assertEquals(Money.of(8.20), order.subtotal());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // INFRASTRUCTURE LAYER TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Infrastructure Layer - Repository")
    class RepositoryTests {

        @Test
        @DisplayName("InMemoryOrderRepository should save and retrieve orders")
        void saveAndFindById() {
            OrderRepository repo = new InMemoryOrderRepository();
            com.cafepos.domain.Order order = new com.cafepos.domain.Order(2001L);
            
            repo.save(order);
            
            var found = repo.findById(2001L);
            assertTrue(found.isPresent());
            assertEquals(2001L, found.get().id());
        }

        @Test
        @DisplayName("Repository should return empty for non-existent order")
        void findByIdReturnsEmptyForMissing() {
            OrderRepository repo = new InMemoryOrderRepository();
            
            var found = repo.findById(9999L);
            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("Repository should update existing orders")
        void updateExistingOrder() {
            OrderRepository repo = new InMemoryOrderRepository();
            ProductFactory factory = new ProductFactory();
            
            com.cafepos.domain.Order order = new com.cafepos.domain.Order(2002L);
            repo.save(order);
            
            order.addItem(new LineItem(factory.create("ESP"), 1));
            repo.save(order);
            
            var retrieved = repo.findById(2002L).orElseThrow();
            assertEquals(1, retrieved.items().size());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // APPLICATION LAYER TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Application Layer - CheckoutService")
    class CheckoutServiceTests {

        @Test
        @DisplayName("CheckoutService should generate receipt without printing")
        void checkoutGeneratesReceipt() {
            OrderRepository repo = new InMemoryOrderRepository();
            PricingService pricing = new PricingService(
                new LoyaltyPercentDiscount(5),
                new FixedRateTaxPolicy(10)
            );
            CheckoutService checkout = new CheckoutService(repo, pricing);
            
            // Create and save order
            com.cafepos.domain.Order order = new com.cafepos.domain.Order(3001L);
            ProductFactory factory = new ProductFactory();
            order.addItem(new LineItem(factory.create("ESP"), 1)); // 2.50
            repo.save(order);
            
            String receipt = checkout.checkout(3001L, 10);
            
            assertNotNull(receipt);
            assertTrue(receipt.contains("Order #3001"));
            assertTrue(receipt.contains("Espresso"));
            assertTrue(receipt.contains("Total:"));
        }

        @Test
        @DisplayName("CheckoutService should throw for non-existent order")
        void checkoutThrowsForMissingOrder() {
            OrderRepository repo = new InMemoryOrderRepository();
            PricingService pricing = new PricingService(
                new NoDiscount(),
                new FixedRateTaxPolicy(10)
            );
            CheckoutService checkout = new CheckoutService(repo, pricing);
            
            assertThrows(Exception.class, () -> checkout.checkout(9999L, 10));
        }
    }

    @Nested
    @DisplayName("Application Layer - ReceiptFormatter")
    class ReceiptFormatterTests {

        @Test
        @DisplayName("ReceiptFormatter should format items correctly")
        void formatterOutputsCorrectStructure() {
            ProductFactory factory = new ProductFactory();
            List<LineItem> items = List.of(
                new LineItem(factory.create("ESP"), 1),
                new LineItem(factory.create("LAT"), 2)
            );
            
            PricingService pricing = new PricingService(
                new LoyaltyPercentDiscount(5),
                new FixedRateTaxPolicy(10)
            );
            
            Money subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(Money.zero(), Money::add);
            var pr = pricing.price(subtotal);
            
            ReceiptFormatter formatter = new ReceiptFormatter();
            String receipt = formatter.format(4001L, items, pr, 10);
            
            assertTrue(receipt.contains("Order #4001"));
            assertTrue(receipt.contains("Espresso"));
            assertTrue(receipt.contains("Latte"));
            assertTrue(receipt.contains("Subtotal:"));
            assertTrue(receipt.contains("Tax (10%):"));
            assertTrue(receipt.contains("Total:"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PRESENTATION LAYER TESTS (MVC)
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Presentation Layer - MVC Controller")
    class MVCControllerTests {

        @Test
        @DisplayName("OrderController should create orders via repository")
        void controllerCreatesOrder() {
            var components = Wiring.createDefault();
            var controller = new OrderController(components.repo(), components.checkout());
            
            long id = controller.createOrder(5001L);
            
            assertEquals(5001L, id);
            assertTrue(components.repo().findById(5001L).isPresent());
        }

        @Test
        @DisplayName("OrderController should add items to orders")
        void controllerAddsItems() {
            var components = Wiring.createDefault();
            var controller = new OrderController(components.repo(), components.checkout());
            
            controller.createOrder(5002L);
            controller.addItem(5002L, "ESP+SHOT", 1);
            
            var order = components.repo().findById(5002L).orElseThrow();
            assertEquals(1, order.items().size());
            assertTrue(order.items().get(0).product().name().contains("Extra Shot"));
        }

        @Test
        @DisplayName("OrderController.checkout should return receipt string")
        void controllerCheckoutReturnsReceipt() {
            var components = Wiring.createDefault();
            var controller = new OrderController(components.repo(), components.checkout());
            
            controller.createOrder(5003L);
            controller.addItem(5003L, "LAT+L", 2);
            
            String receipt = controller.checkout(5003L, 10);
            
            assertNotNull(receipt);
            assertTrue(receipt.contains("Latte (Large)"));
            assertTrue(receipt.contains("x2"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EVENT BUS TESTS (Components & Connectors)
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("EventBus - Publish/Subscribe")
    class EventBusTests {

        @Test
        @DisplayName("EventBus should deliver events to subscribers")
        void eventDelivery() {
            EventBus bus = new EventBus();
            List<Long> received = new ArrayList<>();
            
            bus.on(OrderCreated.class, e -> received.add(e.orderId()));
            
            bus.emit(new OrderCreated(6001L));
            bus.emit(new OrderCreated(6002L));
            
            assertEquals(2, received.size());
            assertTrue(received.contains(6001L));
            assertTrue(received.contains(6002L));
        }

        @Test
        @DisplayName("EventBus should support multiple handlers for same event type")
        void multipleHandlers() {
            EventBus bus = new EventBus();
            AtomicInteger counter = new AtomicInteger(0);
            
            bus.on(OrderPaid.class, e -> counter.incrementAndGet());
            bus.on(OrderPaid.class, e -> counter.incrementAndGet());
            bus.on(OrderPaid.class, e -> counter.incrementAndGet());
            
            bus.emit(new OrderPaid(6003L));
            
            assertEquals(3, counter.get());
        }

        @Test
        @DisplayName("EventBus should only deliver to matching event type handlers")
        void eventTypeIsolation() {
            EventBus bus = new EventBus();
            List<String> log = new ArrayList<>();
            
            bus.on(OrderCreated.class, e -> log.add("created"));
            bus.on(OrderPaid.class, e -> log.add("paid"));
            
            bus.emit(new OrderCreated(6004L));
            
            assertEquals(1, log.size());
            assertEquals("created", log.get(0));
        }

        @Test
        @DisplayName("EventBus should handle emit with no subscribers gracefully")
        void noSubscribersIsSafe() {
            EventBus bus = new EventBus();
            assertDoesNotThrow(() -> bus.emit(new OrderCreated(6005L)));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // WIRING / COMPOSITION ROOT TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Composition Root - Wiring")
    class WiringTests {

        @Test
        @DisplayName("Wiring.createDefault should return all components")
        void wiringProvidesAllComponents() {
            var components = Wiring.createDefault();
            
            assertNotNull(components.repo());
            assertNotNull(components.pricing());
            assertNotNull(components.checkout());
        }

        @Test
        @DisplayName("Components from Wiring should work together")
        void componentsIntegrate() {
            var components = Wiring.createDefault();
            
            com.cafepos.domain.Order order = new com.cafepos.domain.Order(7001L);
            ProductFactory factory = new ProductFactory();
            order.addItem(new LineItem(factory.create("ESP"), 1));
            
            components.repo().save(order);
            
            String receipt = components.checkout().checkout(7001L, 10);
            assertNotNull(receipt);
            assertTrue(receipt.contains("Espresso"));
        }
    }
}

