package com.cafepos.decorator;
import com.cafepos.catalog.Product;
import com.cafepos.domain.common.Money;

public abstract class ProductDecorator implements Product {
//wraps/contains another product interface
protected final Product base;

protected ProductDecorator(Product base) {
if (base == null) throw new
IllegalArgumentException("base product required");
this.base = base;
}

@Override 
public String id() { 
    return base.id(); 
} // id may remain the base product id
@Override 
public Money basePrice() { 
    return base.basePrice(); 
} // original price (not total)
@Override
public String name() { 
    return base.name(); 
}
/*  
public Money finalPrice() { 
//returns the final price of the decorated product i think
    
}
*/
// Concrete decorators will override name() and provide a finalPrice() helper if desired.
}