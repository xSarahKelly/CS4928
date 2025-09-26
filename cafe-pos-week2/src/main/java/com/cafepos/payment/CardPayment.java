package com.cafepos.payment;
import com.cafepos.order.Order;

public final class CardPayment implements PaymentStrategy {
    private final String cardNumber;
    public CardPayment(String cardNumber) {
        if( cardNumber == null || cardNumber.isEmpty()){
            throw new IllegalArgumentException("Card number cannot be empty");
        }
        this.cardNumber = cardNumber;
    }

    @Override
    public void pay(Order order) {
        if(order == null){
            throw new IllegalArgumentException("Order cannot be null");
        }
        String maskedCard = maskCardNum(cardNumber);
        System.out.println("[Card] Customer paid" + order.totalWithTax(10) + "EUR with card" + maskedCard);
    }
    private String maskCardNum(String cardNumber) {
        int length = cardNumber.length();
        if(length <=4){
            return cardNumber;
        }
        return "****" + cardNumber.substring(length - 4);
    }
}