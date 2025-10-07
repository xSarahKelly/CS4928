package com.cafepos.demo;

import com.cafepos.common.Money;
import com.cafepos.order.LineItem;
import com.cafepos.order.Order;
import com.cafepos.order.KitchenDisplay;
import com.cafepos.order.DeliveryDesk;
import com.cafepos.order.CustomerNotifier;
import com.cafepos.payment.CashPayment;
import com.cafepos.payment.CardPayment;
import com.cafepos.payment.WalletPayment;
import com.cafepos.catalog.SimpleProduct;

import java.util.Scanner;

public class Week4OrderDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Order order = new Order(1); // <-- no named arg

        order.register(new KitchenDisplay());
        order.register(new DeliveryDesk());
        order.register(new CustomerNotifier());

        System.out.println("=== Café POS - Interactive Order Demo :) (Week 4) ===");

        boolean running = true;
        while (running) {
            System.out.println("\nChoose an action:");
            System.out.println("1. Add item");
            System.out.println("2. Pay order");
            System.out.println("3. Mark order ready");
            System.out.println("4. Exit");
            System.out.print("> ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Enter product name: ");
                String name = scanner.nextLine();
                System.out.print("Enter price: ");
                double price = scanner.nextDouble();
                System.out.print("Enter quantity: ");
                int qty = scanner.nextInt();
                scanner.nextLine();

                SimpleProduct p = new SimpleProduct(name.toLowerCase(), name, Money.of(price));
                order.addItem(new LineItem(p, qty));


                System.out.println("Subtotal: " + order.subtotal());
                System.out.println("Total with 10% tax: " + order.totalWithTax(10));

            } else if (choice == 2) {
                System.out.println("Payment method: 1=Cash, 2=Card, 3=Wallet");
                System.out.print("> ");
                int payChoice = scanner.nextInt();
                scanner.nextLine();

                if (payChoice == 1) {
                    order.pay(new CashPayment());
                } else if (payChoice == 2) {
                    order.pay(new CardPayment("1234-5678-9876-5432"));
                } else if (payChoice == 3) {
                    order.pay(new WalletPayment("WALLET-001"));
                } else {
                    System.out.println("Invalid payment type ):");
                }

            } else if (choice == 3) {
                order.markReady();

            } else if (choice == 4) {
                System.out.println("Exiting demo...");
                running = false;
            } else {
                System.out.println("Invalid option.");
            }
        }

        scanner.close();
        System.out.println("=== Demo Finished :) ===");
    }
}
