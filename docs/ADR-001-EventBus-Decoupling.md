# ADR-001: EventBus for UI-Application Decoupling

## Context

As the Café POS system evolved from Week 8 to Week 10, we needed a mechanism for the Presentation layer (UI) to react to Application layer events (e.g., order creation, payment completion) without creating tight coupling between components.

### Problem Statement
- UI controllers need to update views when domain events occur
- Direct method calls from Application services to UI create bidirectional dependencies
- Testing becomes difficult when components are tightly coupled
- Future features (notifications, analytics, logging) would require invasive changes

### Requirements
1. Loose coupling between Application and Presentation layers
2. Multiple subscribers for the same event type
3. Easy to add new event handlers without modifying existing code
4. Simple in-process solution (no external dependencies needed at current scale)

---

## Decision

We chose to implement a **lightweight in-process EventBus** as a connector between components.

### Implementation
```java
// com/cafepos/app/events/EventBus.java
public final class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> handlers = new HashMap<>();
    
    public <T> void on(Class<T> type, Consumer<T> h) {
        handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(h);
    }
    
    public <T> void emit(T event) {
        var list = handlers.getOrDefault(event.getClass(), List.of());
        for (var h : list) ((Consumer<T>) h).accept(event);
    }
}
```

### Event Types (Sealed Interface)
```java
public sealed interface OrderEvent permits OrderCreated, OrderPaid {}
public record OrderCreated(long orderId) implements OrderEvent {}
public record OrderPaid(long orderId) implements OrderEvent {}
```

---

## Alternatives Considered

### 1. Direct Method Calls
- **Pros:** Simple, no abstraction overhead, compile-time safety
- **Cons:** Tight coupling, UI changes require Application changes, hard to add new subscribers
- **Rejected:** Violates layered architecture principles

### 2. Observer Pattern (Domain-level)
- **Pros:** Already familiar from Week 5 (OrderObserver)
- **Cons:** Per-entity observers don't scale well for cross-cutting concerns; requires passing observer references through layers
- **Rejected:** Not suitable for layer-to-layer communication

### 3. External Message Queue (e.g., RabbitMQ, Kafka)
- **Pros:** Scalable, persistent, supports distributed systems
- **Cons:** Overkill for current single-process monolith; adds operational complexity
- **Deferred:** Good candidate for future partitioning 

### 4. Reactive Streams (Project Reactor, RxJava)
- **Pros:** Powerful backpressure handling, rich operators
- **Cons:** Learning curve, adds dependency, unnecessary complexity for simple pub/sub
- **Rejected:** Over-engineering for current requirements

---

## Consequences

### Positive
- **Decoupling:** Application services emit events without knowing who listens
- **Extensibility:** New handlers (logging, notifications) can subscribe without changing emitters
- **Testability:** EventBus can be mocked; handlers tested in isolation
- **Single Responsibility:** Each handler does one thing

### Negative
- **Indirection:** Harder to trace event flow than direct calls (mitigated by clear naming)
- **Runtime Errors:** Subscriber type mismatches caught at runtime, not compile time
- **In-Process Only:** Events are lost if process crashes before handling 

### Neutral
- **No Persistence:** Events are fire-and-forget (can add event store later if needed)
- **Synchronous:** Handlers run on emitter's thread

---

## Code Location

| Component | Package/Class |
|-----------|---------------|
| EventBus | `com.cafepos.app.events.EventBus` |
| Events | `com.cafepos.app.events.OrderEvent`, `OrderCreated`, `OrderPaid` |
| Demo | `com.cafepos.ui.EventWiringDemo` |
| Wiring | `com.cafepos.infra.Wiring` (composition root) |

---

## Future Considerations

If the system is partitioned into microservices:
1. Replace in-process EventBus with message broker (Kafka/RabbitMQ)
2. Events become async with guaranteed delivery
3. Event schema versioning becomes critical
4. Consider event sourcing for audit trail

