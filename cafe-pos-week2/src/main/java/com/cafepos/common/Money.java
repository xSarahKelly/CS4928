package com.cafepos.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    
    private final BigDecimal amount;

    public static Money of(double value) {
        return new Money(BigDecimal.valueOf(value));
    }
// convenience factory from BigDecimal (used internally / by domain)
public static Money of(BigDecimal value) {
    return new Money(value);
}
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    private Money(BigDecimal a) {
        if (a == null) {
            throw new IllegalArgumentException("amount required");
        }else{
            this.amount = a.setScale(2, RoundingMode.HALF_UP);
        }
        }

    public BigDecimal asBigDecimal() {
        return this.amount;
    }

    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("other required");
        }
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be >= 0");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf((long)qty)));
   }
   

    
    public int compareTo(Money o) {
        return this.amount.compareTo(o.amount);
    }

    public boolean equals(Object o) {
        if (this == o) {
         return true;
      } else if (o instanceof Money) {
         Money m = (Money)o;
         return Objects.equals(this.amount, m.amount);
      } else {
         return false;
      }
     }

    public int hashCode() {
        return Objects.hash(new Object[]{this.amount});
    }

    public String toString() {
        return amount.toPlainString();
    }
}

