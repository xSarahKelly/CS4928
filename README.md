# Café POS & Delivery System

A comprehensive Point of Sale system demonstrating design patterns, layered architecture, and clean code principles.

---

## Quick Start (30-Second Proof Commands)

```bash
cd cafe-pos-week2

# Compile
mvn -q compile -DskipTests

# Run Week 8 demos (Command + Adapter)
java -cp target/classes com.cafepos.demo.Week8Demo_Command     # Command pattern
java -cp target/classes com.cafepos.demo.Week8Demo_Adapter     # Adapter pattern  

# Run Week 9 demos (Composite/Iterator + State)
java -cp target/classes com.cafepos.demo.Week9Demo_Menu        # Composite/Iterator
java -cp target/classes com.cafepos.demo.Week9Demo_State       # State pattern

# Run Week 10 demos (MVC + EventBus)
java -cp target/classes com.cafepos.demo.Week10Demo_MVC        # MVC + Layered
java -cp target/classes com.cafepos.ui.EventWiringDemo         # EventBus

# Run all tests
mvn test
```

---

## Architecture Overview

### Four-Layer Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION (UI)                            │
│  com.cafepos.ui     → OrderController, ConsoleView              │
│  com.cafepos.demo   → Week*Demo classes, FinalDemo              │
└───────────────────────────────┬─────────────────────────────────┘
                                │ depends on
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION (Use Cases)                       │
│  com.cafepos.app    → CheckoutService, ReceiptFormatter         │
│  com.cafepos.app.events → EventBus, OrderEvent, OrderCreated    │
└───────────────────────────────┬─────────────────────────────────┘
                                │ depends on
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DOMAIN (Core Model)                           │
│  com.cafepos.domain → Order, LineItem, OrderRepository          │
│  com.cafepos.common → Money (value object)                      │
└───────────────────────────────▲─────────────────────────────────┘
                                │ implements
┌───────────────────────────────┴─────────────────────────────────┐
│                    INFRASTRUCTURE (Adapters)                     │
│  com.cafepos.infra    → InMemoryOrderRepository, Wiring         │
│  com.cafepos.printing → LegacyPrinterAdapter                    │
└─────────────────────────────────────────────────────────────────┘
```

### Patterns Implemented

| Week | Pattern | Package/Classes | Purpose |
|------|---------|-----------------|---------|
| 8 | **Command** | `com.cafepos.command.*` | Decouple UI actions, enable undo |
| 8 | **Adapter** | `com.cafepos.printing.LegacyPrinterAdapter` | Integrate legacy printer |
| 9 | **Composite** | `com.cafepos.menu.Menu, MenuItem, MenuComponent` | Hierarchical menu structure |
| 9 | **Iterator** | `com.cafepos.menu.CompositeIterator` | Uniform menu traversal |
| 9 | **State** | `com.cafepos.state.*` | Order lifecycle without conditionals |
| 10 | **MVC** | `com.cafepos.ui.OrderController, ConsoleView` | Separate concerns in UI |
| 10 | **EventBus** | `com.cafepos.app.events.EventBus` | Loose coupling between layers |

### ADRs (Architecture Decision Records)

- **[ADR-001: EventBus for UI-Application Decoupling](docs/ADR-001-EventBus-Decoupling.md)** - Why we chose in-process events over alternatives
- **[ADR-002: Layered Architecture](docs/ADR-002-Layered-Architecture.md)** - Four-layer structure and dependency rules

---

## Design Notes (Reflection & Evidence of Effort)

###  Smells Removed
- **God Class & Long Method**: `OrderManagerGod.process(...)` handled creation, pricing, discount, tax, payment I/O, and receipt formatting.
- **Primitive Obsession**: Removed string-based discount codes and hardcoded `TAX_PERCENT` primitive.
- **Duplicated Logic**: Money/BigDecimal math was repeated inline across multiple branches.
- **Feature Envy / Shotgun Surgery**: Tax and discount rules embedded directly in one method.
- **Global/Static State**: Removed `LAST_DISCOUNT_CODE` and `TAX_PERCENT` dependencies from behavior flow.

###  Refactorings Applied
- **Extract Class**: `DiscountPolicy`, `FixedCouponDiscount`, `LoyaltyPercentDiscount`, `NoDiscount`
- **Extract Class**: `TaxPolicy`, `FixedRateTaxPolicy`
- **Extract Class**: `ReceiptPrinter`
- **Replace Conditional with Polymorphism**: Used `PaymentStrategy` instead of paymentType string
- **Constructor Injection**: Injected factory, policies, printer, and strategy to support Dependency Inversion
- **Remove Global State**: All configuration is passed via constructor (Dependency Injection)

###  Why This Design
- Isolates pricing rules
- Eliminates duplication
- Enables swapping discount/tax/policy behaviors without touching unrelated code
- Makes components independently testable and reusable

###  SOLID Principle Compliance
- **S (Single Responsibility)** – Each class now has one purpose (e.g., pricing, printing, tax).
- **O (Open/Closed)** – New discounts/taxes can be added via new classes without modifying existing code.
- **L (Liskov Substitution)** – Any `DiscountPolicy` or `TaxPolicy` can replace another without breaking behavior.
- **I (Interface Segregation)** – Small, focused policy interfaces prevent unnecessary dependencies.
- **D (Dependency Inversion)** – High-level modules depend on abstractions, not concrete implementations.

###  Extensibility Example
To add a new discount:
1. Create a new class implementing `DiscountPolicy`
2. Write a unit test for it
3. Inject it via `PricingService`
> No changes required to existing pricing or receipt classes

---

##  Final Responsibilities (After Refactor)
CheckoutService (orchestrates the overall process)
  - ProductFactory (builds Product from recipe string)
  - Product / Decorators (Priced.price() or basePrice())
  - PricingService (applies DiscountPolicy and TaxPolicy to compute totals)
  - ReceiptPrinter (formats output exactly like legacy system)
  - PaymentStrategy (handles actual payment I/O: Cash, Card, Wallet)


###  Design Constraints
-  No global/static state
-  Tax percentage and policies are constructor-injected
-  New features added via new classes (OCP)
-  Each class has exactly one reason to change (SRP)

---

##  Architecture Trade-offs: Layering vs Partitioning

### Why a Layered Monolith?

We chose a Layered Monolith because it provides clear organization without unnecessary complexity. The four layers (Presentation → Application → Domain ← Infrastructure) separate UI concerns from business logic, making the codebase easier to test and maintain. For a project of this scope, microservices would introduce more overhead than benefit—dealing with network communication and distributed deployment would take time away from implementing actual features. A monolith allows for faster development, simpler debugging, and easier refactoring since all components are in one place.

### Natural Seams for Future Partitioning

Several seams are candidates for extraction into separate services as the system grows:
- **Payments**: High security requirements and potential third-party integrations make this a natural boundary. Could become a Payment Gateway service.
- **Notifications**: Customer alerts (order ready, delivery updates) could scale independently and integrate with SMS/email providers.
- **Inventory/Catalog**: If product management grows complex, separating it allows independent scaling and caching strategies.
- **Delivery Tracking**: Real-time location updates may require different scaling characteristics than the core POS.

### Connectors/Protocols for Splitting

When partitioning, we would define:
- **Events (Async)**: Domain events via message queues (e.g., `OrderCreated`, `OrderPaid`) for loose coupling between services.
- **REST APIs (Sync)**: Request-response patterns for queries and commands that need immediate feedback.
- **Shared Contracts**: API schemas and event contracts versioned independently to enable backward-compatible evolution.

---

## Test Coverage

### Test Classes by Week

| Test Class | Coverage |
|------------|----------|
| `Week8CommandTest`, `CommandUndoTests`, `MacroCommandTests` | Command pattern execution, undo, macro |
| `Week8AdapterTest` | Legacy printer adapter integration |
| `Week9CompositeIteratorTests` | Menu hierarchy, item traversal |
| `Week9StateTests` | Order lifecycle state transitions |
| `Week9IntegrationTests` | End-to-end order flow |
| `Week10ArchitectureTests` | MVC, EventBus, Repository, Wiring |

### Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=Week10ArchitectureTests

# With verbose output
mvn test -Dsurefire.useFile=false
```

---

## Project Structure

```
cafe-pos-week2/
├── docs/
│   ├── ADR-001-EventBus-Decoupling.md
│   └── ADR-002-Layered-Architecture.md
├── src/
│   ├── main/java/com/cafepos/
│   │   ├── app/           # Application layer (use cases)
│   │   ├── catalog/       # Domain - products
│   │   ├── command/       # Command pattern
│   │   ├── common/        # Domain - value objects
│   │   ├── decorator/     # Product decorators
│   │   ├── demo/          # Presentation - demo runners
│   │   ├── domain/        # Domain entities & interfaces
│   │   ├── factory/       # Product factory
│   │   ├── infra/         # Infrastructure adapters
│   │   ├── menu/          # Composite/Iterator
│   │   ├── order/         # Observer pattern
│   │   ├── payment/       # Payment strategies
│   │   ├── pricing/       # Pricing policies
│   │   ├── printing/      # Printer adapter
│   │   ├── state/         # State pattern
│   │   └── ui/            # Presentation - MVC
│   └── test/java/com/cafepos/
│       └── Week*Tests.java
├── pom.xml
└── README.md
```

---



