package com.rabserkon.blockchain;

import com.rabserkon.blockchain.details.Block;
import com.rabserkon.blockchain.details.MineBlockException;
import com.rabserkon.blockchain.details.Transaction;
import com.rabserkon.blockchain.details.Validator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.security.Security;
import java.util.*;

@SpringBootApplication(scanBasePackages = "com.rabserkon.blockchain")
public class Main {

    private static final String walletFilepath = "D:/blockchain/wallets";
    private static final String blocksFilepath = "D:/blockchain/blocks";

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Wallet wallet = new Wallet("D:/blockchain/wallets/bombulis.wal");
        Wallet wallet1 = new Wallet("D:/blockchain/wallets/wallet1.wal");

      /*  List<Validator> validatorList = new ArrayList<>();
        validatorList.add(new Validator("bom", 100, wallet1));

        List<Block> blocks = loadExistingBlocks(blocksFilepath);

        Block block = createNewBlock(blocks, validatorList);
        block.addTransaction(wallet.createTransaction(wallet1.getPublicKey(), 500), blocks);
        block.addTransaction(wallet1.createTransaction(wallet.getPublicKey(), 50), blocks);*/

       /* try {
            block.mineBlock(validatorList, 2);
            block.serializeBlock(blocksFilepath);
            blocks.add(block);
        } catch (MineBlockException e) {
            e.printStackTrace();
        }*/

        ApplicationContext springContext = SpringApplication.run(Main.class, args);

        P2PNode p2PNode = springContext.getBean(P2PNode.class);
        System.out.println("balance wallet 1:" + wallet1.calculateBalance(p2PNode.getBlockchain()));
        System.out.println("balance wallet :" + wallet.calculateBalance(p2PNode.getBlockchain()));

        p2PNode.start(8000);
    }


    // Метод для создания нового блока
    private static Block createNewBlock(List<Block> blocks, List<Validator> participants ) {
        Validator creator = selectBlockCreator(blocks, participants, 3);
        Block block = blocks.isEmpty() ? new Block(creator, 10) : blocks.get(blocks.size() - 1);
        int stake = 10;
        return new Block(block, creator, stake);
    }

    public static Validator selectBlockCreator(List<Block> blocks, final List<Validator> participants, int difficulty) {
        // Создайте список кандидатов, у которых стейк больше или равен требуемой сложности

        participants.removeIf(participant -> !Block.isBalanceValid(blocks, participant));
        System.out.println("participants size: " + participants.size());
        List<Validator> eligibleCandidates = filterEligibleCandidates(participants, difficulty);

        // Если нет подходящих кандидатов, верните null или выполните другие действия
        if (eligibleCandidates.isEmpty()) {
            return null;
        }

        // Выберите случайного кандидата из списка подходящих кандидатов
        Random random = new Random();
        int selectedIndex = random.nextInt(eligibleCandidates.size());
        return eligibleCandidates.get(selectedIndex);
    }



    private static List<Validator> filterEligibleCandidates(List<Validator> participants, int difficulty) {
        // Создайте список для хранения подходящих кандидатов
        List<Validator> eligibleCandidates = new ArrayList<>();

        // Пройдитесь по всем участникам и выберите тех, у кого стейк >= difficulty
        for (Validator participant : participants) {
            if (participant.getStake() >= difficulty) {
                eligibleCandidates.add(participant);
            }
        }

        return eligibleCandidates;
    }

    private static List<Block> loadExistingBlocks(String directoryPath) {
        List<Block> blocks = new ArrayList<>();
        File directory = new File(directoryPath);
        File[] blockFiles = directory.listFiles();
        if (blockFiles != null) {
            for (File blockFile : blockFiles) {
                if (blockFile.isFile() && blockFile.getName().endsWith(".block")) {
                    Block block = Block.deserializeBlock(blockFile.getAbsolutePath());
                    if (block != null) {
                        blocks.add(block);
                    }
                }
            }
        }
        Comparator<Block> blockIndexComparator = Comparator.comparingLong(Block::getBlockIndex);
        Collections.sort(blocks, blockIndexComparator);
        return blocks;
    }
}
