# ADR-002: Layered Architecture with Dependency Inversion


## Context

Starting from Week 2, the Café POS system grew organically with patterns (Factory, Decorator, Strategy, Observer). By Week 10, we needed to formalize the structure to:
- Separate concerns clearly
- Make dependencies explicit
- Enable independent testing of each layer
- Prepare seams for future service extraction

---

## Decision

We adopted a **Four-Layer Architecture** with dependencies pointing inward:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION (UI)                            │
│  OrderController, ConsoleView, Week*Demo classes                │
└───────────────────────────────┬─────────────────────────────────┘
                                │ depends on
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION (Use Cases)                       │
│  CheckoutService, ReceiptFormatter, EventBus                    │
└───────────────────────────────┬─────────────────────────────────┘
                                │ depends on
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DOMAIN (Core Model)                           │
│  Order, LineItem, Money, OrderRepository (interface)            │
└───────────────────────────────▲─────────────────────────────────┘
                                │ implements
┌───────────────────────────────┴─────────────────────────────────┐
│                    INFRASTRUCTURE (Adapters)                     │
│  InMemoryOrderRepository, LegacyPrinterAdapter, Wiring          │
└─────────────────────────────────────────────────────────────────┘
```

### Key Principle: Dependency Inversion
- Domain defines interfaces (e.g., `OrderRepository`)
- Infrastructure implements them (e.g., `InMemoryOrderRepository`)
- Application depends on domain interfaces, not concrete implementations
- Presentation orchestrates but doesn't contain business logic

---

## Package Structure

```
com.cafepos/
├── ui/                  # Presentation Layer
│   ├── OrderController.java
│   ├── ConsoleView.java
│   └── EventWiringDemo.java
├── demo/                # Presentation Layer (demos)
│   ├── Week8Demo_Commands.java
│   ├── Week8Demo_Adapter.java
│   ├── Week9Demo_Menu.java
│   ├── Week9Demo_State.java
│   └── Week10Demo_MVC.java
├── app/                 # Application Layer
│   ├── CheckoutService.java
│   ├── ReceiptFormatter.java
│   └── events/
│       ├── EventBus.java
│       ├── OrderEvent.java
│       ├── OrderCreated.java
│       └── OrderPaid.java
├── domain/              # Domain Layer
│   ├── Order.java
│   ├── LineItem.java
│   └── OrderRepository.java
├── common/              # Domain Layer (shared)
│   └── Money.java
├── infra/               # Infrastructure Layer
│   ├── InMemoryOrderRepository.java
│   └── Wiring.java
├── printing/            # Infrastructure Layer (adapters)
│   ├── Printer.java
│   └── LegacyPrinterAdapter.java
└── ...                  # Other domain/pattern packages
```

---

## Alternatives Considered

### 1. Flat Package Structure
- **Pros:** Simple, no navigation overhead
- **Cons:** No enforced boundaries, easy to create tangled dependencies
- **Rejected:** Doesn't scale; violates separation of concerns

### 2. Hexagonal Architecture (Ports & Adapters)
- **Pros:** Cleaner port/adapter distinction, better for complex domains
- **Cons:** More packages, steeper learning curve
- **Partially Adopted:** We use the concept (repository as port, InMemoryRepo as adapter) without full hexagonal package structure

### 3. Microservices from Start
- **Pros:** Independent deployment, clear boundaries
- **Cons:** Massive overhead for a POS prototype; network complexity
- **Deferred:** We identified seams (Payments, Notifications) for future extraction

---

## Consequences

### Positive
- **Testability:** Each layer can be tested with mocks for dependencies
- **Maintainability:** Changes in UI don't affect domain; infrastructure can be swapped
- **Onboarding:** New developers understand where code belongs
- **Evolution:** Clear seams for future partitioning

### Negative
- **Indirection:** More classes and packages than a simple script
- **Boilerplate:** Interfaces for single implementations (justified by testability)

### Neutral
- **Learning Curve:** Team must understand layer rules (enforced by code review)

---

## Composition Root

All wiring happens in `com.cafepos.infra.Wiring`:

```java
public final class Wiring {
    public static record Components(
        OrderRepository repo, 
        PricingService pricing, 
        CheckoutService checkout
    ) {}

    public static Components createDefault() {
        OrderRepository repo = new InMemoryOrderRepository();
        PricingService pricing = new PricingService(
            new LoyaltyPercentDiscount(5), 
            new FixedRateTaxPolicy(10)
        );
        CheckoutService checkout = new CheckoutService(repo, pricing);
        return new Components(repo, pricing, checkout);
    }
}
```

This keeps object creation centralized and makes dependencies explicit.

