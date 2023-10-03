package com.rabserkon.blockchain.details;

import java.security.PublicKey;

public class СoinbaseTransaction extends Transaction {
    public СoinbaseTransaction(PublicKey recipient, float reward) {
        super(null, recipient, reward); // Устанавливаем отправителя как null
        this.setTransactionId("0"); // Устанавливаем id для coinbase-транзакции как "0"
    }
}
