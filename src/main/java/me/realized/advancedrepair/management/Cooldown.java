package me.realized.advancedrepair.management;

public class Cooldown {

    private final int hand;
    private final int all;

    public Cooldown(int hand, int all) {
        this.hand = hand;
        this.all = all;
    }

    public int getHandCooldown() {
        return hand;
    }

    public int getAllCooldown() {
        return all;
    }
}
