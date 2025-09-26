
package com.cafepos.order;

import java.util.concurrent.atomic.AtomicLong;

public final class OrderIds {
   private static final AtomicLong SEQ = new AtomicLong(1000L);

   private OrderIds() {
   }

   public static long next() {
      return SEQ.incrementAndGet();
   }
}
