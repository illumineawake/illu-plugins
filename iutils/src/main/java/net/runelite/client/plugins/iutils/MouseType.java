package net.runelite.client.plugins.iutils;

public enum MouseType {
    ZERO_MOUSE("0x,0y mouse"),
    NO_MOVE("No move data"),
    MOVE("Move mouse"),
    RECTANGLE("Rectangle mouse");

    public final String name;

    MouseType(String name) {
        this.name = name;
    }
}
