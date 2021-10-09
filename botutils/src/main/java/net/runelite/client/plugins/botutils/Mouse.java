package net.runelite.client.plugins.botutils;

public enum Mouse {
    ZERO_MOUSE("0x,0y mouse"),
    NO_MOVE("No move data"),
    MOVE("Move mouse"),
    RECTANGLE("Rectangle mouse");

    public final String name;

    Mouse(String name) {
        this.name = name;
    }
}
