### Design Notes (Reflections & Evidence of Effort)
- Smells removed:
   God Class & Long Method in `OrderManagerGod`;
   Primitive Obsession (string codes, magic %);
   Duplicated Money/BigDecimal math;
   Feature Envy/Shotgun Surgery (inline tax/discount rules); 
   Global/Static State (`TAX_PERCENT`, `LAST_DISCOUNT_CODE`).
  
- Refactorings applied: *Extract Class* (`DiscountPolicy`, `FixedRateTaxPolicy`, `ReceiptPrinter`), *Replace Conditional with Polymorphism* (Week-3 `PaymentStrategy`), *Constructor Injection* (factory/policies/printer/payment), *Remove Global State*, plus small *Orchestrator* (`CheckoutService`) to wire components.
- Why: Isolate pricing rules, kill duplication, and make behavior swappable/testable without touching unrelated code.
  
- SOLID:
     SRP (each class has one reason to change: pricing, tax, printing, payment, orchestration);
     OCP (add new discounts/taxes/strategies without modifying existing classes);
     LSP (any `DiscountPolicy`/`TaxPolicy` can substitute);
     ISP (small, focused interfaces).
     DIP (CheckoutService/PricingService depend on interfaces, not concretes);
  
- Adding a new discount type:
  Create a new class implementing `DiscountPolicy` (e.g., `HappyHourDiscount`), unit-test it, and inject it into `PricingService` via DI; no edits to existing classes or receipt code required.
  Evidence: Characterization tests locked legacy outputs; equivalence tests prove the new flow prints the exact same receipt text; policy unit tests validate isolated behaviors.
