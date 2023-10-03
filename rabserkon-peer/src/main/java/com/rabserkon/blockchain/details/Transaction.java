package com.rabserkon.blockchain.details;

import java.io.Serializable;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
//Залог других криптовалют
//Специальные кошельки в системе

public class Transaction implements Serializable {
    // Уникальный идентификатор транзакции
    private String transactionId;
    // Отправитель
    private PublicKey sender;
    // Получатель
    private PublicKey recipient;
    // Сумма перевода
    private float amount;
    // Подпись отправителя
    private byte[] signature;

    // Конструктор
    public Transaction(PublicKey sender, PublicKey recipient, float amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.transactionId = calculateHash();
    }

    // Вычисление хэша для транзакции
    private String calculateHash() {
        String data = null;
        if (sender == null){
            data = StringUtil.getStringFromKey(recipient) + Float.toString(amount);
        } else {
            data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(amount);
        }
        if (data ==  null){
            return null;
        }
        return StringUtil.applySha256(data);
    }

    // Подписать транзакцию с использованием приватного ключа отправителя
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(amount);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // Проверить подпись транзакции
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(amount);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public String getTransactionId() {
        return transactionId;
    }


    public PublicKey getSender() {
        return sender;
    }

    public void setSender(PublicKey sender) {
        this.sender = sender;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public void setRecipient(PublicKey recipient) {
        this.recipient = recipient;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", amount=" + amount +
                ", signature=" + Arrays.toString(signature) +
                '}';
    }
}


class StringUtil {

    public static String applySha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);

            // Преобразование хеша в строку шестнадцатеричного представления
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Метод для вычисления хэша SHA-256
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Метод для подписи данных с использованием ECDSA
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    // Метод для проверки подписи
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Преобразование ключа в строку (для удобства)
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

}
