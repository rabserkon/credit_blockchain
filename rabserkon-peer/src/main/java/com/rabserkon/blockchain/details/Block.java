package com.rabserkon.blockchain.details;

import com.rabserkon.blockchain.Wallet;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Block implements Serializable {
    private final long blockIndex;
    private static final long serialVersionUID = 1L;
    private String hash; // Хэш блока
    private int minerRewardAmount;
    private String previousHash; // Хэш предыдущего блока
    private PublicKey creator; // Идентификатор создателя блока (участника PoS)
    private int stake; // Количество стейка у создателя блока
    private ArrayList<Transaction> transactions = new ArrayList<>(); // Список транзакций
    private long timeStamp; // Временная метка
    private int nonce; // Нонс (для Proof-of-Work)
    private String id; // Идентификатор блока (автоматически генерируется)
    private byte[] signature;
    private Validator validator;


    public Block(Block block, Validator validator, int stake) {
        this.blockIndex = block.blockIndex + 1;
        this.previousHash = block.getHash();
        this.timeStamp = new Date().getTime();
        this.creator = validator.getPublicKey();
        this.stake = stake;
        this.id = generateBlockId(block.getId()); // Вычисляем id на основе предыдущего блока
        this.hash = calculateHash();
    }

    public Block( Validator validator, int stake) {
        this.blockIndex = 0;
        this.previousHash = "0";
        this.timeStamp = new Date().getTime();
        this.creator = validator.getPublicKey();
        this.stake = stake;
        this.id = generateBlockId("0"); // Вычисляем id на основе предыдущего блока
        this.hash = calculateHash();
    }

    // Метод для вычисления id на основе предыдущего блока
    private String generateBlockId(String previousBlockId) {
        return StringUtil.applySha256(previousBlockId + Long.toString(timeStamp) + Integer.toString(nonce));
    }

    // Вычисление хэша блока
    public String calculateHash() {
        String data = previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + transactions.toString();
        return StringUtil.applySha256(data);
    }

    // Метод для майнинга блока (Proof-of-Stake)
    public void mineBlock(List<Validator> validators, int difficulty) throws MineBlockException {
        // Выбор участника для создания блока на основе PoS
        Random random = new Random();
        int selectedParticipantIndex = random.nextInt(validators.size());
        Validator selectedValidator = validators.get(selectedParticipantIndex);

        // Проверка стейка участника
        if (selectedValidator.getStake() >= difficulty) {
            this.creator = selectedValidator.getPublicKey();
            this.stake = selectedValidator.getStake();
            int totalReward = calculateTotalBlockReward(selectedValidator.getStake()); // Рассчитать общее вознаграждение за блок

            // Создаем "coinbase" транзакцию для майнера
            Transaction minerRewardTransaction = createCoinbaseTransaction(selectedValidator, totalReward);
            this.addTransaction(minerRewardTransaction);

            if (transactions.size() == 0){
                throw new MineBlockException("The block does not contain transactions: " + transactions.size());
            }
            // Процесс создания блока
            try {
                String target = new String(new char[difficulty]).replace('\0', '0');
                while (!hash.substring(0, difficulty).equals(target)) {
                    nonce++;
                    hash = calculateHash();

                }
                System.out.println("The block was created successfully: " + hash);
            } catch (Exception e) {
                hash = null;
                throw new MineBlockException(e.getMessage());
            }

        } else {
            System.out.println("Выбранный участник не имеет достаточного стейка для создания блока.");
        }
    }

    // Метод для создания пустого блока PoS
    public static Block createEmptyPoSBlock(List<Validator> validators, Block previousBlock, float stake) throws BlockCreationException {
        // Выбор участника для создания блока на основе PoS и его стейка
        Validator selectedValidator = selectValidatorBasedOnStake(validators);

        if (selectedValidator != null) {
            // Создание блока
            Block block = new Block(previousBlock, selectedValidator,10);
            return block; // Возвращаем созданный блок без транзакций и подписи
        } else {
            throw new BlockCreationException("No eligible validator found to create the block.");
        }
    }

    public Transaction createCoinbaseTransaction(Validator validator, int blockReward) {
        Transaction coinbaseTransaction = null;

        PublicKey publicKey = validator.getPublicKey();
        // Теперь у вас есть объект PublicKey
        System.out.println("Public Key: " + publicKey);
        // Создаем объект транзакции
        coinbaseTransaction = new СoinbaseTransaction(publicKey, blockReward);
        // Нет необходимости генерировать подпись, так как coinbase-транзакции не подписываются
        // coinbaseTransaction.generateSignature(privateKey); // Не нужно
        // Возвращаем созданную coinbase-транзакцию
        return coinbaseTransaction;
    }

    private int calculateTotalBlockReward(int participantStake) {
        // Рассчитайте общее вознаграждение за блок, например, сложением всех наград за транзакции и вознаграждения майнера.
        // Можете настроить логику расчета вознаграждения по своему усмотрению.
        int transactionRewards = calculateTransactionRewards(participantStake); // Рассчитать награду за включенные транзакции
        int minerReward = minerRewardAmount; // Задать фиксированное вознаграждение майнера
        return transactionRewards + minerReward;
    }

    private int calculateTransactionRewards(int participantStake){
        int transactionRewards = 0;
        // Определите базовое вознаграждение за каждую транзакцию
        int baseRewardPerTransaction = 1; // Пример фиксированной суммы
        // Определите множитель на основе стейка участника (например, 1 стейк = 2 монеты)
        int rewardMultiplier = 1; // Пример множителя
        // Рассчитайте вознаграждение за каждую транзакцию с учетом стейка
        for (Transaction transaction : transactions) {
            // Рассчитайте вознаграждение за транзакцию как базовое вознаграждение * множитель
            int rewardForTransaction = baseRewardPerTransaction * rewardMultiplier;
            // Умножьте вознаграждение на количество стейков участника
            rewardForTransaction *= participantStake;
            transactionRewards += rewardForTransaction;
        }
        return transactionRewards;
    }

    // Добавление транзакции в блок
    public boolean addTransaction(Transaction transaction) {
        // Проверяем, что транзакция корректна и подписана
        if (transaction == null ) {
            System.out.println("Ошибка при добавлении транзакции в блок.");
            return false;
        }
        transactions.add(transaction);
        System.out.println("Транзакция успешно добавлена в блок.");
        return true;
    }


    public boolean addTransaction(Transaction transaction, List<Block> blocks)  {
        // Проверяем, что транзакция корректна и подписана
        if (transaction == null || !transaction.verifySignature() ) {
            System.out.println("Error when adding a transaction to a block.");
            return false;
        }
        if ( !isBalanceValid(blocks, transaction) || blocks.size() == 0 ) {
            System.out.println("bad");
            return false;
        }
        transactions.add(transaction);
        System.out.println("The transaction was successfully added to the block.");
        return true;
    }

    public static boolean isBalanceValid(List<Block> blocks, Transaction currentTransaction) {
        int balance = 0;
        for (Block block : blocks) {
            for (Transaction transaction : block.getTransactions()) {
                if (transaction.getSender() != null && transaction.getSender().equals(currentTransaction.getSender())) {
                    balance -= transaction.getAmount();
                }
                if (transaction.getRecipient().equals(currentTransaction.getSender())) {
                    balance += transaction.getAmount();
                }
            }
        }
        if (balance < currentTransaction.getAmount()) {
            System.out.println("Insufficient balance: " + balance);
            return false;
        }
        return true;
    }

    public static boolean isBalanceValid(List<Block> blocks, Validator validator) {
        int balance = 0;
        for (Block block : blocks) {
            for (Transaction transaction : block.getTransactions()) {
                if (transaction.getSender() != null && transaction.getSender().equals(validator.getPublicKey())) {
                    balance -= transaction.getAmount();
                }
                if (transaction.getRecipient().equals(validator.getPublicKey())) {
                    balance += transaction.getAmount();
                }
            }
        }
        if (balance < validator.getStake()) {
            System.out.println("Insufficient balance for stake in wallet " + validator.getName() +", balance " + balance);
            return false;
        }
        return true;
    }


    public File serializeBlock(String directoryPath) throws MineBlockException {
        if (hash == null){
            throw new MineBlockException("");
        }
        String fileName = directoryPath + File.separator + id + ".block";
        File blockFile = new File(fileName);
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(blockFile))) {
            outputStream.writeObject(this);
            System.out.println("Блок успешно сохранен в файл: " + fileName);
        } catch (IOException e) {
            System.err.println("Error when saving a block to a file: " + e.getMessage());
        }
        return blockFile;
    }

    // Десериализация блока из файла
    public static Block deserializeBlock(String fileName) {
              try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream( fileName))) {
            Block block = (Block) inputStream.readObject();
            System.out.println("The block was successfully loaded from the file:" + fileName);
            return block;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при загрузке блока из файла: " + e.getMessage());
            return null;
        }
    }

    public void signBlock(byte[] signature, Block block) throws Exception, InvalidSignatureException {
        // Хеширование данных блока
        String blockDataHash = calculateBlockDataHash();

        // Проверка, что хеш данных совпадает с ожидаемым
        if (blockDataHash.equals(block.calculateBlockDataHash())) {
            // Хеш данных совпадает, можно установить подпись
            this.signature = signature;
        } else {
            // Хеш данных не совпадает, подпись не действительна
            throw new InvalidSignatureException("Invalid block data hash.");
        }
    }

    private static Validator selectValidatorBasedOnStake(List<Validator> validators){
        return null;
    }


    public boolean verifySignature(Validator validator) throws Exception {
        try {
            // Создание объекта для проверки подписи с использованием публичного ключа
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(validator.getPublicKey());

            // Сериализация блока (или другие данные блока) в байтовый массив
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(this);
            byte[] blockData = byteStream.toByteArray();

            // Обновление объекта для проверки подписи данными блока
            verifier.update(blockData);

            // Проверка подписи
            boolean signatureValid = verifier.verify(this.signature);

            return signatureValid;
        } catch (Exception e) {
            // Обработка ошибки проверки подписи, например, логирование или выброс исключения
            e.printStackTrace();
            return false;
        }
    }

    public String calculateBlockDataHash() throws Exception {
        // Сериализация блока (или другие данные блока) в байтовый массив
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(this);
        byte[] blockData = byteStream.toByteArray();

        // Хеширование данных блока
        return StringUtil.applySha256(blockData);
    }

    public String getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public long getBlockIndex() {
        return blockIndex;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }
}


