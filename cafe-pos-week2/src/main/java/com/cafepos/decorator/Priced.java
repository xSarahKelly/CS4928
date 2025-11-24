package com.cafepos.decorator;
import com.cafepos.domain.common.Money;

public interface Priced {
    Money price();
    
}
