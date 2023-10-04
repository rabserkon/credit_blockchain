package com.rabserkon.blockchain;

import com.rabserkon.blockchain.details.*;
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

        List<Validator> validatorList = new ArrayList<>();
        validatorList.add(new Validator("bom", 100, wallet1));


        ApplicationContext springContext = SpringApplication.run(Main.class, args);

        P2PNode p2PNode = springContext.getBean(P2PNode.class);
        List<Block> blockList = p2PNode.getBlockchain();
        System.out.println("balance wallet 1:" + wallet1.calculateBalance(blockList));
        System.out.println("balance wallet :" + wallet.calculateBalance(blockList));

        try {
            Block block = Block.createEmptyPoSBlock(selectValidator(validatorList), p2PNode.getBlockchain(), 10);
            block.addTransaction(wallet1.createTransaction(wallet.getPublicKey(), 100000));
            block.signBlock(wallet1.signBlock(block), block);
            block.serializeBlock(blocksFilepath);
            blockList.add(block);
        } catch (BlockCreationException e) {
            e.printStackTrace();
        } catch (InvalidSignatureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }







        blockList.forEach((i) -> {
            try {
                System.out.println(i.verifySignature(wallet1.getPublicKey()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        System.out.println("balance wallet 1:" + wallet1.calculateBalance(blockList));
        System.out.println("balance wallet :" + wallet.calculateBalance(blockList));

        p2PNode.start(8000);
    }

    public static Validator selectValidator(List<Validator> validators) {
        int totalStake = validators.stream().mapToInt(Validator::getStake).sum();
        int randomValue = (int) (Math.random() * totalStake);

        int cumulativeStake = 0;
        for (Validator validator : validators) {
            cumulativeStake += validator.getStake();
            if (randomValue < cumulativeStake) {
                return validator;
            }
        }
        return null; // В случае ошибки
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

}
