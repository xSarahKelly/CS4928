package com.cafepos.demo;
import com.cafepos.catalog.Product;
import com.cafepos.catalog.SimpleProduct;
import com.cafepos.common.Money;
import com.cafepos.decorator.ExtraShot;
import com.cafepos.decorator.OatMilk;
import com.cafepos.decorator.SizeLarge;
import com.cafepos.order.Priced;

public class Week5Demo {
    public static void main(String[] args) {
        Product espresso = new SimpleProduct("P-ESP","Espresso", Money.of(2.50));
        Product decorated = new SizeLarge(new OatMilk(new ExtraShot(espresso)));
        System.out.println(decorated.name() + " " + ((Priced)decorated).price());
    }
}
