package com.cafepos.demo;

import com.cafepos.catalog.Product;
import com.cafepos.common.Money;
import com.cafepos.domain.LineItem;
import com.cafepos.domain.Order;
import com.cafepos.factory.ProductFactory;
import com.cafepos.infra.Wiring;
import com.cafepos.order.CustomerNotifier;
import com.cafepos.order.DeliveryDesk;
import com.cafepos.order.KitchenDisplay;
import com.cafepos.order.OrderIds;
import com.cafepos.payment.CardPayment;
import com.cafepos.payment.CashPayment;
import com.cafepos.payment.WalletPayment;
import com.cafepos.ui.OrderController;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Week6Demo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProductFactory factory = new ProductFactory();
        boolean running = true;

        // Use layered architecture
        var components = Wiring.createDefault();
        var controller = new OrderController(components.repo(), components.checkout());

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

            // Create order using layered architecture (MVC controller)
            long orderId = OrderIds.next();
            controller.createOrder(orderId);
            controller.addItem(orderId, code, 1);

            // Also create domain order for observer pattern demo
            Order order = components.repo().findById(orderId).orElseThrow();
            order.register(new KitchenDisplay());
            order.register(new DeliveryDesk());
            order.register(new CustomerNotifier());

            // === Order summary ===
            System.out.println("\nOrder summary:");
            printOrder(order);

            // === Payment options ===
            System.out.println("\nHow would you like to pay?");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. Wallet");
            System.out.print("> ");
            int payChoice = scanner.nextInt();
            scanner.nextLine();

            switch (payChoice) {
                case 1 -> {
                    System.out.println("Please provide cash (see total above).");
                    double cashProvided = scanner.nextDouble();
                    scanner.nextLine();
                    order.pay(new CashPayment(cashProvided));
                }
                case 2 -> {
                    System.out.println("Please enter your card number:");
                    String cardNumber = scanner.nextLine();
                    order.pay(new CardPayment(cardNumber));
                }
                case 3 -> {
                    System.out.println("Please enter your wallet ID:");
                    String walletId = scanner.nextLine();
                    order.pay(new WalletPayment(walletId));
                }
                default -> {
                    System.out.println("Invalid payment type ):");
                    continue;
                }
            }

            System.out.println("\nProcessing payment...");
            System.out.println("Payment complete. Thank you for your order!");

            // Generate receipt using layered architecture
            int taxRate = 10;
            String receipt = controller.checkout(orderId, taxRate);
            System.out.println("\n--- Receipt ---");
            System.out.println(receipt);

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
}
