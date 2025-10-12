package com.cafepos.catalog;

import java.util.Objects;
import com.cafepos.order.Priced;
import com.cafepos.common.Money;

public final class SimpleProduct implements Product, Priced {
    private final String id;
    private final String name;
    private final Money basePrice;

    public SimpleProduct(String id, String name, Money basePrice) {
      if (id != null && !id.isBlank()) {
         if (name != null && !name.isBlank()) {
            if (basePrice != null && basePrice.asBigDecimal().signum() >= 0) {
               this.id = id;
               this.name = name;
               this.basePrice = basePrice;
            } else {
               throw new IllegalArgumentException("basePrice >= 0 required");
            }
         } else {
            throw new IllegalArgumentException("name required");
         }
      } else {
         throw new IllegalArgumentException("id required");
      }
   }
    public String id() {
        return this.id;
    }
    public String name() {
        return this.name;
    }
    public Money basePrice() {
        return this.basePrice;
    }

    @Override
    public Money price() {
        return this.basePrice;
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        else if (o instanceof SimpleProduct) {
            SimpleProduct p = (SimpleProduct) o;
            return this.id.equals(p.id);
        }else{
            return false;
        }
        
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id});
    }

    public String toString() {
     String varString = this.name;
      return varString + " (€" + String.valueOf(this.basePrice) + ")";
    }

}