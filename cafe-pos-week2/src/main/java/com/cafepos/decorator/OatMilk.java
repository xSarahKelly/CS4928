package com.cafepos.decorator;
import com.cafepos.catalog.Product;
import com.cafepos.common.Money;
import com.cafepos.order.Priced;

public final class OatMilk extends ProductDecorator implements Priced {
private static final Money SURCHARGE = Money.of(0.50);

public OatMilk(Product base) { 
    super(base); 
}
@Override 
public String name() { 
    return base.name() + " + Oat Milk";
}
@Override
public Money price() {
    return (base instanceof Priced p ? p.price() : base.basePrice()).add(SURCHARGE);
}
}