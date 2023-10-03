package com.rabserkon.blockchain;


import com.rabserkon.blockchain.details.Block;
import com.rabserkon.blockchain.details.MineBlockException;
import com.rabserkon.blockchain.details.Validator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


@Component
@PropertySource("classpath:application.properties")
public class P2PNode {

    @Value("${blocks.crypto.path}")
    private String blocksFilepath;
    @Getter
    private List<Socket> peers;
    @Getter
    private List<Block> blockchain;
    @Getter
    private Map<String, Validator> validators;


    public P2PNode() {
        this.peers = new ArrayList<>();
        this.blockchain = loadExistingBlocks("D:/blockchain/blocks");
        this.validators = new HashMap<>();
    }

    public void start(int port) {
        // Инициализация блокчейна с первым блоком (генезис блок)
        if (blockchain.size() == 0){
            Block genesisBlock = new Block(new Validator("Genesis Validator", 100), 0);
            blockchain.add(genesisBlock);
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("P2P Node is listening on port " + port);
            while (true) {
                Socket peerSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + peerSocket.getInetAddress());
                peers.add(peerSocket);

                // Обработка входящих сообщений от пиров
                new Thread(() -> handlePeer(peerSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePeer(Socket peerSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equals("GET_BLOCKS")) {
                    sendBlocks(peerSocket);
                } else if (message.startsWith("ADD_BLOCK")) {
                    String blockData = message.substring("ADD_BLOCK ".length());
                    addBlock(blockData);
                } else if (message.startsWith("REGISTER_VALIDATOR")) {
                    String validatorInfo = message.substring("REGISTER_VALIDATOR ".length());
                    registerValidator(validatorInfo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для отправки блоков пиру
    private void sendBlocks(Socket peerSocket) {
        try {
            PrintWriter writer = new PrintWriter(peerSocket.getOutputStream(), true);
            for (Block block : blockchain) {
                //writer.println("BLOCK " + block.getIndex() + " " + block.getData() + " " + block.getHash());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для добавления нового блока в блокчейн
    private void addBlock(String blockData) {
        // Выбор валидатора на основе PoS
        Validator selectedValidator = selectValidator();
        File file = null;
        // Создайте новый блок на основе данных и предыдущего хеша
        Comparator<Block> blockIndexComparator = Comparator.comparingLong(Block::getBlockIndex);
        // Отсортируйте список блоков
        Collections.sort(blockchain, blockIndexComparator);

        Block previousBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(previousBlock, selectedValidator, 10);
        try {
            newBlock.serializeBlock(blocksFilepath);
        } catch (MineBlockException e) {
            System.err.println(e.getMessage());
        }
        // Добавьте новый блок в блокчейн
        blockchain.add(newBlock);

        // Рассылка нового блока всем пирам
        for (Socket peer : peers) {
            try {
                PrintWriter writer = new PrintWriter(peer.getOutputStream(), true);
                //writer.println("BLOCK " + newBlock.getIndex() + " " + newBlock.getData() + " " + newBlock.getValidator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Validator selectValidator() {
        int totalStake = validators.values().stream().mapToInt(Validator::getStake).sum();
        int randomValue = (int) (Math.random() * totalStake);

        int cumulativeStake = 0;
        for (Validator validator : validators.values()) {
            cumulativeStake += validator.getStake();
            if (randomValue < cumulativeStake) {
                return validator;
            }
        }
        return null; // В случае ошибки
    }

    private void registerValidator(String validatorInfo) {
        String[] parts = validatorInfo.split(" ");
        if (parts.length == 2) {
            String validatorName = parts[0];
            int stake = Integer.parseInt(parts[1]);
            validators.put(validatorName, new Validator(validatorName, stake));
        }
    }

    private  List<Block> loadExistingBlocks(String directoryPath) {
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
