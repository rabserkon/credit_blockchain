package com.rabserkon.blockchain.details;

import com.rabserkon.blockchain.Wallet;

import java.io.Serializable;
import java.security.PublicKey;

public class Validator implements Serializable {
    private static final long serialVersionUID = 1L;

    private PublicKey publicKey;
    private String name;
    private int stake; // Количество стейка участника

    public Validator(String name, int stake) {
        this.name = name;
        this.stake = stake;
    }

    public Validator(String name, int stake, Wallet wallet) {
        this.name = name;
        this.stake = stake;
        this.publicKey = wallet.getPublicKey();
    }



    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }

    public int getStake() {
        return stake;
    }
}
