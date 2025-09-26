package com.cafepos.order;

import com.cafepos.catalog.Product;
import com.cafepos.common.Money;

public final class LineItem {
   private final Product product;
   private final int quantity;

   public LineItem(Product product, int quantity) {
      if (product == null) {
         throw new IllegalArgumentException("product required");
      } else if (quantity <= 0) {
         throw new IllegalArgumentException("quantity must be > 0");
      } else {
         this.product = product;
         this.quantity = quantity;
      }
   }

   public Product product() {
      return this.product;
   }

   public int quantity() {
      return this.quantity;
   }

   public Money lineTotal() {
      return this.product.basePrice().multiply(this.quantity);
   }
}
