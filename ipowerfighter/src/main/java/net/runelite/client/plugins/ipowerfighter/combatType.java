package net.runelite.client.plugins.ipowerfighter;

public enum combatType {
    ATTACK("Attack", 0),
    STRENGTH("Strength", 1),
    DEFENCE("Defence", 3),
    STOP("Stop", -1);

    public String name;
    public int index;

    combatType(String name, int index) {
        this.name = name;
        this.index = index;
    }
}
