package com.cafepos.demo;

import com.cafepos.catalog.Product;
import com.cafepos.domain.LineItem;
import com.cafepos.domain.Order;
import com.cafepos.domain.common.Money;
import com.cafepos.domain.factory.ProductFactory;
import com.cafepos.order.OrderIds;
import com.cafepos.payment.CardPayment;
import com.cafepos.payment.CashPayment;
import com.cafepos.payment.WalletPayment;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public final class Week5Demo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProductFactory factory = new ProductFactory();
        boolean running = true;

        while (running) {
            System.out.println("Hello customer! What would you like to order?");
            System.out.println("1. Espresso");
            System.out.println("2. Latte");
            System.out.println("3. Cappuccino");
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

            List<String> decorators = new ArrayList<>();

            decorators.add(ask(scanner, "Add an extra shot? (y/n)", "SHOT"));
            decorators.add(ask(scanner, "Make it large? (y/n)", "L"));
            decorators.add(ask(scanner, "Add oat milk? (y/n)", "OAT"));
            decorators.removeIf(String::isEmpty);

            String code = baseCode + (decorators.isEmpty() ? "" : "+" + String.join("+", decorators));
            Product product = factory.create(code);

            Order order = new Order(OrderIds.next());
            order.addItem(new LineItem(product, 1));

            System.out.println("\nOrder summary:");
            printOrder(order);
            printOrderTotals(order);

            System.out.println("\nHow would you like to pay?");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. Wallet");
            System.out.print("> ");
            int payChoice = scanner.nextInt();
            scanner.nextLine();

            switch (payChoice) {
                case 1 -> {
                    System.out.println("How much cash are you providing? (Total: " + order.totalWithTax(10) + ")");
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
            running = false;
        }
    }

    // Helper methods (unchanged)
    private static String ask(Scanner scanner, String prompt, String code) {
        System.out.println(prompt);
        System.out.print("> ");
        String input = scanner.nextLine();
        return input.equalsIgnoreCase("y") ? code : "";
    }

    private static void printOrder(Order order) {
        System.out.println("Order #" + order.id());
        for (LineItem li : order.items()) {
            System.out.println(" - " + li.product().name() + " x" + li.quantity() + " = " + li.lineTotal());
        }
    }

    private static void printOrderTotals(Order order) {
        System.out.println("Subtotal: " + order.subtotal());
        System.out.println("Tax (10%): " + order.taxAtPercent(10));
        System.out.println("Total: " + order.totalWithTax(10));
    }
}

