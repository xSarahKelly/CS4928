package com.cafepos.demo;

import com.cafepos.decorator.Priced;
import com.cafepos.domain.common.Money;
import com.cafepos.domain.factory.ProductFactory;
import com.cafepos.menu.Menu;
import com.cafepos.menu.MenuItem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public final class Week9Demo_Menu {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Café POS - Week 9 Menu CLI ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1) Show full menu (S/M/L + desserts + add-ons)");
            System.out.println("2) Show vegetarian items (from Week9 examples)");
            System.out.println("3) Search item by name (from Week9 examples)");
            System.out.println("4) Quit");
            System.out.print("> ");
            int choice = safeInt(scanner);
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.println();
                    ProductFactory factory = new ProductFactory();
                    printFullMenu(factory);     // Coffees (S/M/L) + Desserts + Add-ons
                }
                case 2 -> {
                    System.out.println("\nVegetarian options:");
                    Menu demo = demoMenu(); // re-use the Week9 example items
                    for (MenuItem mi : demo.vegetarianItems()) {
                        System.out.println(" * " + mi.name() + " = " + mi.price());
                    }
                }
                case 3 -> {
                    Menu demo = demoMenu();
                    System.out.print("Enter item name: ");
                    String q = scanner.nextLine().trim().toLowerCase();
                    var matches = demo.allItems().stream()
                            .filter(c -> c instanceof MenuItem)
                            .map(c -> (MenuItem) c)
                            .filter(mi -> mi.name().toLowerCase().contains(q))
                            .toList();
                    if (matches.isEmpty()) {
                        System.out.println("No items matching \"" + q + "\".");
                    } else {
                        System.out.println("Matches:");
                        for (MenuItem item : matches) {
                            System.out.println(" * " + item.name() + " = " + item.price()
                                    + (item.vegetarian() ? " (V)" : ""));
                        }
                    }
                }
                case 4 -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /* ------------------------- Menu Printers ------------------------- */

    private static void printFullMenu(ProductFactory factory) {
        System.out.println("CAFÉ MENU");
        printCoffeesSized(factory);
        printDesserts();         // <— restored desserts section
        printAddons(factory);
    }

    private static void printCoffeesSized(ProductFactory factory) {
        System.out.println("Coffees");

        // base drinks
        Map<String, String> bases = new LinkedHashMap<>();
        bases.put("ESP", "Espresso");
        bases.put("LAT", "Latte");

        // size options to try (printed only if factory supports the code)
        record SizeOpt(String label, String suffix) {}
        SizeOpt[] sizes = new SizeOpt[] {
                new SizeOpt("S", "+S"),   // print if supported
                new SizeOpt("M", ""),     // base (default)
                new SizeOpt("L", "+L")    // large
        };

        for (var entry : bases.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue();

            StringBuilder line = new StringBuilder(" - ").append(name).append(": ");
            boolean printedAny = false;

            for (SizeOpt sz : sizes) {
                Money price = priceOf(factory, code + sz.suffix());
                if (price != null) {
                    if (printedAny) line.append("  ");
                    line.append(sz.label).append("=").append(price);
                    printedAny = true;
                }
            }

            if (!printedAny) {
                Money base = priceOf(factory, code);
                line.append(base != null ? "M=" + base : "(unavailable)");
            }

            System.out.println(line);
        }
    }

    private static void printDesserts() {
        System.out.println("Desserts");
        // Week 9 demo dessert prices (static menu items)
        System.out.println(" - Cheesecake = " + Money.of(3.50));
        System.out.println(" - Oat Cookie (V) = " + Money.of(1.20));
    }

    private static void printAddons(ProductFactory factory) {
        System.out.println("Add-ons");

        // reference base for surcharges (prefer ESP, fallback LAT)
        Money refBase = priceOf(factory, "ESP");
        if (refBase == null) refBase = priceOf(factory, "LAT");

        Map<String, String> addonCodes = new LinkedHashMap<>();
        addonCodes.put("+SHOT", "Extra shot");
        addonCodes.put("+SYP",  "Syrup");
        addonCodes.put("+OAT",  "Oat milk");

        for (var entry : addonCodes.entrySet()) {
            String suffix = entry.getKey();
            String label = entry.getValue();

            Money priceWithAddon = null;
            if (refBase != null) {
                Money espWith = priceOf(factory, "ESP" + suffix);
                if (espWith != null) priceWithAddon = espWith;
                else {
                    Money latWith = priceOf(factory, "LAT" + suffix);
                    if (latWith != null) priceWithAddon = latWith;
                }
            }

            if (priceWithAddon != null && refBase != null) {
                Money delta = priceWithAddon.minus(refBase);
                System.out.println(" - " + label + " +" + delta);
            } else {
                Money decoratedOnly = priceOf(factory, "ESP" + suffix);
                if (decoratedOnly == null) decoratedOnly = priceOf(factory, "LAT" + suffix);
                if (decoratedOnly != null) {
                    System.out.println(" - " + label + " ~" + decoratedOnly + " (full price shown)");
                }
            }
        }
    }

    /* --------------------------- Helpers --------------------------- */

    private static Money priceOf(ProductFactory factory, String code) {
        try {
            var p = factory.create(code);
            if (p == null) return null;
            if (p instanceof Priced priced) return priced.price(); // decorated product price
            return p.basePrice();                                  // simple/base product
        } catch (Exception ex) {
            return null; // code not supported by factory
        }
    }

    // Week 9 example menu for veg/search options
    private static Menu demoMenu() {
        Menu root = new Menu("CAFÉ MENU");
        Menu drinks = new Menu("Drinks");
        Menu coffee = new Menu("Coffee");
        Menu desserts = new Menu("Desserts");

        coffee.add(new MenuItem("Espresso", Money.of(2.50), true));
        coffee.add(new MenuItem("Latte (Large)", Money.of(3.90), true));
        drinks.add(coffee);

        desserts.add(new MenuItem("Cheesecake", Money.of(3.50), false));
        desserts.add(new MenuItem("Oat Cookie", Money.of(1.20), true));

        root.add(drinks);
        root.add(desserts);
        return root;
    }

    private static int safeInt(Scanner s) {
        while (!s.hasNextInt()) {
            System.out.print("Please enter a number: ");
            s.next();
        }
        return s.nextInt();
    }
}
