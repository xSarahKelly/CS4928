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
import com.cafepos.order.OrderIds;
import com.cafepos.payment.CardPayment;
import com.cafepos.payment.CashPayment;
import com.cafepos.payment.WalletPayment;
import com.cafepos.command.AddItemCommand;
import com.cafepos.command.OrderService;
import com.cafepos.command.PayOrderCommand;
import com.cafepos.command.PosRemote;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Week8Demo_Commands {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProductFactory factory = new ProductFactory();
        boolean running = true;

        // One order per demo run
        Order order = new Order(OrderIds.next());
        OrderService service = new OrderService(order);
        PosRemote remote = new PosRemote(4); // 0: add item, 2: pay

        while (running) {
            boolean building = true; // NEW: we’re in the “build your order” loop

            while (building) {
                // === Coffee selection ===
                System.out.println("=== Café POS - Week 6 Demo ===");
                System.out.println("Hello customer! What would you like to order?");
                System.out.println("1. Espresso: " + factory.create("ESP").basePrice());
                System.out.println("2. Latte: " + factory.create("LAT").basePrice());
                System.out.println("3. Cappuccino: " + factory.create("CAP").basePrice());
                System.out.println("4. Exit");
                System.out.print("> ");

                int choice = safeInt(scanner);
                scanner.nextLine(); // consume eol
                if (choice == 4) {
                    running = false;
                    System.out.println("Goodbye!");
                    return;
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
                Money shotSurcharge = getPrice(shotVariant).minus(basePrice);
                Money syrupSurcharge = getPrice(syrupVariant).minus(basePrice);
                Money oatSurcharge = getPrice(oatVariant).minus(basePrice);
                Money largeSurcharge = getPrice(largeVariant).minus(basePrice);

                List<String> decorators = new ArrayList<>();
                decorators.add(ask(scanner, "Add an extra shot? (y/n) (" + shotSurcharge + ")", "SHOT"));
                decorators.add(ask(scanner, "Make it large? (y/n) (" + largeSurcharge + ")", "L"));
                decorators.add(ask(scanner, "Add oat milk? (y/n) (" + oatSurcharge + ")", "OAT"));
                decorators.add(ask(scanner, "Add syrup? (y/n) (" + syrupSurcharge + ")", "SYP"));
                decorators.removeIf(String::isEmpty);

                String code = baseCode + (decorators.isEmpty() ? "" : "+" + String.join("+", decorators));

                // Add selected coffee to order
                remote.setSlot(0, new AddItemCommand(service, code, 1));
                remote.press(0);

                System.out.println("Current order:");
                printOrder(order);
                System.out.println("Press Enter to continue, or");
                System.out.println("Press - to undo the last coffee."); // NEW
                String input = scanner.nextLine().trim();

                if (input.equals("-")) {
                    // UNDO path
                    remote.undo();
                    System.out.println("Last coffee removed.");
                    System.out.println("Would you like to: (a) Add a new coffee  or  (e) Exit?"); // NEW
                    System.out.print("> ");
                    String next = scanner.nextLine().trim().toLowerCase();
                    if (next.startsWith("e")) {
                        running = false;
                        System.out.println("Goodbye!");
                        return;
                    }
                    // else: add a new coffee → continue the while(building) loop
                    continue;
                } else {
                    // NO UNDO path
                    System.out.println("Would you like to: (a) Add another coffee  or  (p) Pay?"); // NEW
                    System.out.print("> ");
                    String next = scanner.nextLine().trim().toLowerCase();
                    if (next.startsWith("a")) {
                        // loop to add another coffee
                        continue;
                    } else if (next.startsWith("p")) {
                        // proceed to payment
                        building = false; // break out to payment section
                    } else {
                        System.out.println("Invalid choice. Returning to menu to add another coffee.");
                        continue;
                    }
                }
            }

            // === PAYMENT SECTION (after user chooses Pay) ===
            // Ask for loyalty/discount code BEFORE payment
            System.out.println("\nDo you have a discount or loyalty code? (e.g. LOYAL5) Enter code or NONE:");
            String loyaltyCode = scanner.nextLine().trim();
            if (loyaltyCode.isEmpty() || loyaltyCode.equalsIgnoreCase("NONE")) {
                loyaltyCode = "";
            }
            int loyaltyPercent = extractLoyaltyPercent(loyaltyCode);

            int taxRate = 10;
            var pricing = new PricingService(
                    new LoyaltyPercentDiscount(loyaltyPercent),
                    new FixedRateTaxPolicy(taxRate)
            );
            var printer = new ReceiptFormatter();
            var checkout = new CheckoutService(factory, pricing, printer, taxRate);

            // Order summary (current items)
            System.out.println("\nOrder summary:");
            printOrder(order);

            // Optional preview from last added code (kept from your demo)
            // Note: this preview is per product; final payment covers the whole order via OrderService.
            // If you want a full-order receipt preview, we can swap this later.
            // String previewReceipt = checkout.checkout(code, 1);
            // printSummaryFromReceipt(previewReceipt);

            // Payment options
            System.out.println("\nHow would you like to pay?");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. Wallet");
            System.out.print("> ");
            int payChoice = safeInt(scanner);
            scanner.nextLine(); // consume eol

            switch (payChoice) {
                case 1 -> {
                    System.out.println("Please provide cash (see total above).");
                    double cashProvided = safeDouble(scanner);
                    scanner.nextLine();
                    remote.setSlot(2, new PayOrderCommand(service, new CashPayment(cashProvided), taxRate));
                }
                case 2 -> {
                    System.out.println("Please enter your card number:");
                    String cardNumber = scanner.nextLine();
                    remote.setSlot(2, new PayOrderCommand(service, new CardPayment(cardNumber), taxRate));
                }
                case 3 -> {
                    System.out.println("Please enter your wallet ID:");
                    String walletId = scanner.nextLine();
                    remote.setSlot(2, new PayOrderCommand(service, new WalletPayment(walletId), taxRate));
                }
                default -> {
                    System.out.println("Invalid payment type. Returning to main menu.");
                    continue; // go back to building/payment loop
                }
            }

            System.out.println("\nProcessing payment...");
            remote.press(2);
            System.out.println("Payment complete. Thank you for your order!");

            // (Optional) print a receipt stub using your checkout service for the last product
            // String newReceipt = checkout.checkout(code, 1);
            // System.out.println("\n--- Receipt ---");
            // System.out.println(newReceipt);

            running = false; // Single demo proof; remove if you want another order session
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
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    // Small input helpers so bad input doesn’t crash the demo
    private static int safeInt(Scanner s) {
        while (!s.hasNextInt()) {
            System.out.print("Please enter a number: ");
            s.next();
        }
        return s.nextInt();
    }
    private static double safeDouble(Scanner s) {
        while (!s.hasNextDouble()) {
            System.out.print("Please enter a number: ");
            s.next();
        }
        return s.nextDouble();
    }
}
