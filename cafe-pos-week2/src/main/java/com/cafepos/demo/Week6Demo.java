package com.cafepos.demo;

import com.cafepos.application.CheckoutService;
import com.cafepos.application.ReceiptFormatter;
import com.cafepos.catalog.Product;
import com.cafepos.domain.LineItem;
import com.cafepos.domain.Order;
import com.cafepos.domain.common.Money;
import com.cafepos.domain.factory.ProductFactory;
import com.cafepos.domain.pricing.FixedRateTaxPolicy;
import com.cafepos.domain.pricing.LoyaltyPercentDiscount;
import com.cafepos.domain.pricing.PricingService;
import com.cafepos.order.CustomerNotifier;
import com.cafepos.order.DeliveryDesk;
import com.cafepos.order.KitchenDisplay;
import com.cafepos.order.OrderIds;
import com.cafepos.payment.CardPayment;
import com.cafepos.payment.CashPayment;
import com.cafepos.payment.WalletPayment;
import com.cafepos.smells.OrderManagerGod;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Week6Demo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProductFactory factory = new ProductFactory();
        boolean running = true;
         
        

        while (running) {
            System.out.println("=== Café POS - Week 6 Demo ===");
            System.out.println("Hello customer! What would you like to order?");
            System.out.println("1. Espresso: " + factory.create("ESP").basePrice());
            System.out.println("2. Latte: " + factory.create("LAT").basePrice());
            System.out.println("3. Cappuccino: " + factory.create("CAP").basePrice());
            System.out.println("4. Exit");
            System.out.print("> ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 4) {
                running = false;
                System.out.println("Goodbye!");
                break;
            }

            String baseCode = switch (choice) {
                case 1 -> "ESP";
                case 2 -> "LAT";
                case 3 -> "CAP";
                default -> null;
            };

            if (baseCode == null) {
                System.out.println("Invalid choice, try again.");
                continue;
            }

            // --- Base product and add-ons ---
            Product baseProduct = factory.create(baseCode);
            Product shotVariant = factory.create(baseCode + "+SHOT");
            Product syrupVariant = factory.create(baseCode + "+SYP");
            Product oatVariant = factory.create(baseCode + "+OAT");
            Product largeVariant = factory.create(baseCode + "+L");

            Money basePrice = baseProduct.basePrice();
            Money shotPrice = getPrice(shotVariant);
            Money syrupPrice = getPrice(syrupVariant);
            Money oatPrice = getPrice(oatVariant);
            Money largePrice = getPrice(largeVariant);

            Money shotSurcharge = shotPrice.minus(basePrice);
            Money syrupSurcharge = syrupPrice.minus(basePrice);
            Money oatSurcharge = oatPrice.minus(basePrice);
            Money largeSurcharge = largePrice.minus(basePrice);

            List<String> decorators = new ArrayList<>();
            decorators.add(ask(scanner, "Add an extra shot? (y/n) (" + shotSurcharge + ")", "SHOT"));
            decorators.add(ask(scanner, "Make it large? (y/n) (" + largeSurcharge + ")", "L"));
            decorators.add(ask(scanner, "Add oat milk? (y/n) (" + oatSurcharge + ")", "OAT"));
            decorators.add(ask(scanner, "Add syrup? (y/n) (" + syrupSurcharge + ")", "SYP"));
            decorators.removeIf(String::isEmpty);

            String code = baseCode + (decorators.isEmpty() ? "" : "+" + String.join("+", decorators));
            Product product = factory.create(code);

            Order order = new Order(OrderIds.next());
            order.register(new KitchenDisplay());
        order.register(new DeliveryDesk());
        order.register(new CustomerNotifier());
            order.addItem(new LineItem(product, 1));
            

            // === Ask for loyalty/discount code BEFORE payment ===
            System.out.println("\nDo you have a discount or loyalty code? (e.g. LOYAL5) Enter code or NONE:");
            String loyaltyCode = scanner.nextLine().trim();
            if (loyaltyCode.isEmpty() || loyaltyCode.equalsIgnoreCase("NONE")) {
                loyaltyCode = "";
            }
            int loyaltyPercent = extractLoyaltyPercent(loyaltyCode);

            // === PricingService setup (NEW behavior consistency) ===
            int taxRate = 10;
            var pricing = new PricingService(
                    new LoyaltyPercentDiscount(loyaltyPercent),
                    new FixedRateTaxPolicy(taxRate)
            );
            var printer = new ReceiptFormatter();
            var checkout = new CheckoutService(factory, pricing, printer, taxRate);

            // === Order summary using PricingService ===
            System.out.println("\nOrder summary:");
            printOrder(order);

            String previewReceipt = checkout.checkout(order.id(), taxRate);
            printSummaryFromReceipt(previewReceipt);

            // === Payment options ===
            System.out.println("\nHow would you like to pay?");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. Wallet");
            System.out.print("> ");
            int payChoice = scanner.nextInt();
            scanner.nextLine();

            String paymentType;
            switch (payChoice) {
                case 1 -> {
                    System.out.println("Please provide cash (see total above).");
                    double cashProvided = scanner.nextDouble();
                    scanner.nextLine();
                    order.pay(new CashPayment(cashProvided));
                    paymentType = "CASH";
                }
                case 2 -> {
                    System.out.println("Please enter your card number:");
                    String cardNumber = scanner.nextLine();
                    order.pay(new CardPayment(cardNumber));
                    paymentType = "CARD";
                }
                case 3 -> {
                    System.out.println("Please enter your wallet ID:");
                    String walletId = scanner.nextLine();
                    order.pay(new WalletPayment(walletId));
                    paymentType = "WALLET";
                }
                default -> {
                    System.out.println("Invalid payment type ):");
                    continue;
                }
            }

            System.out.println("\nProcessing payment...");
            System.out.println("Payment complete. Thank you for your order!");

            // === 30-second proof ===
            String oldReceipt = OrderManagerGod.process(
                    code, 1, paymentType,
                    loyaltyCode.isEmpty() ? "NONE" : loyaltyCode, true
            );

            String newReceipt = checkout.checkout(order.id(), 1);

            System.out.println("\n--- Old Receipt ---\n" + oldReceipt);
            System.out.println("\n--- New Receipt ---\n" + newReceipt);
            System.out.println("\nMatch: " + oldReceipt.equals(newReceipt));

            running = false; // Single demo proof
        }
    }

    // === Helper Methods ===
    private static String ask(Scanner scanner, String prompt, String code) {
        System.out.println(prompt);
        System.out.print("> ");
        String input = scanner.nextLine();
        return input.equalsIgnoreCase("y") ? code : "";
    }

    private static Money getPrice(Product p) {
        if (p instanceof com.cafepos.decorator.Priced priced) {
            return priced.price();
        }
        return p.basePrice();
    }

    private static void printOrder(Order order) {
        System.out.println("Order #" + order.id());
        for (LineItem li : order.items()) {
            System.out.println(" - " + li.product().name() + " x" + li.quantity() + " = " + li.lineTotal());
        }
    }

    /**
     * Extracts subtotal, discount, tax, and total from receipt text
     * generated by ReceiptPrinter to keep display consistent.
     */
    private static void printSummaryFromReceipt(String receiptText) {
        System.out.println("\n--- Pricing Breakdown ---");
        String[] lines = receiptText.split("\n");
        for (String line : lines) {
            if (line.contains("Subtotal") ||
                line.contains("Discount") ||
                line.contains("Tax") ||
                line.contains("Total")) {
                System.out.println(line);
            }
        }
    }

    private static int extractLoyaltyPercent(String loyaltyCode) {
        if (loyaltyCode == null || loyaltyCode.isEmpty()) return 0;
        String code = loyaltyCode.trim().toUpperCase();
        if (code.startsWith("LOYAL")) {
            try {
                return Integer.parseInt(code.substring(5));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }
}
