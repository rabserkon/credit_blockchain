package com.rabserkon.blockchain;

import com.rabserkon.blockchain.details.Block;
import com.rabserkon.blockchain.details.Transaction;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Wallet implements Serializable{
    private static final long serialVersionUID = 1L;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private List<Transaction> transactions = new ArrayList<>();
    private String fileName;


    public Wallet(String fileName) {
        this.fileName = fileName + ".wal";
        File walletFile = new File(fileName);
        if (walletFile.exists()) {
            Wallet wallet = loadWalletFromFile(walletFile);
            this.privateKey = wallet.privateKey;
            this.publicKey = wallet.publicKey;
        } else {
            KeyPair keyPair = generateKeyPair();
            saveWalletToFile(walletFile);
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        }
    }


    public byte[] signBlock(Block block) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(block);
        byte[] blockData = byteStream.toByteArray();
        // Создание объекта для создания подписи с использованием закрытого ключа
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        // Обновление объекта для создания подписи данными блока
        signature.update(blockData);
        // Выполнение подписи данных блока
        byte[] signatureBytes = signature.sign();
        return signatureBytes;
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC"); // Используем алгоритм ECDSA
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyPairGenerator.initialize(256, random);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return keyPair;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveWalletToFile(File walletFile) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(walletFile))) {
            outputStream.writeObject(this);
            System.out.println("The wallet has been successfully saved to a file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error when saving the wallet to a file: " + e.getMessage());
        }
    }

    private Wallet loadWalletFromFile(File walletFile) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(walletFile))) {
            Wallet loadedWallet = (Wallet) inputStream.readObject();


            System.out.println("The wallet was successfully loaded from the file: " + fileName);
            return loadedWallet;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при загрузке кошелька из файла: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    // Создание и подписание транзакции
    public Transaction createTransaction(PublicKey recipient, int amount) {
        if (amount <= 0) {
            System.out.println("Неверная сумма для транзакции.");
            return null;
        }
        Transaction transaction = new Transaction(publicKey, recipient, amount);
        // Подписываем транзакцию с помощью приватного ключа
        transaction.generateSignature(privateKey);
        transactions.add(transaction);
        return transaction;
    }

    // Получение баланса кошелька (сумма всех входящих и исходящих транзакций)
    public int calculateBalance(List<Block> blocks) {
        int balance = 0;
        for (Block block : blocks) {
            for (Transaction transaction : block.getTransactions()) {
                if (transaction.getSender() != null && transaction.getSender().equals(publicKey)) {
                    balance -= transaction.getAmount();
                }
                if (transaction.getRecipient().equals(publicKey)) {
                    balance += transaction.getAmount();
                }
            }
        }
        return balance;
    }

    public Transaction createCoinbaseTransaction(PublicKey minerPublicKey, int minerReward) {
        Transaction coinbaseTransaction = new Transaction(publicKey, minerPublicKey, minerReward);
        coinbaseTransaction.generateSignature(privateKey);
        return coinbaseTransaction;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "Wallet Address: " + Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String getShortAddress() {
        byte[] publicKeyBytes = publicKey.getEncoded();
        String base64Encoded = Base64.getEncoder().encodeToString(publicKeyBytes);
        return base64Encoded.substring(0, 20); // Получаем первые 20 символов
    }
}
