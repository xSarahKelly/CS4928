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


