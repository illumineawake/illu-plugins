package net.runelite.client.plugins.irooftopagility;

import lombok.Getter;

@Getter
public enum AlchCourse {
    DRAYNOR("Draynor Village"),
    VARROCK("Varrock"),
    CANAFIS("Canafis"),
    FALADOR("Falador"),
    SEERS("Seers Village"),
    ARDOUGNE("Ardougne");

    private final String name;

    AlchCourse(String name) {
        this.name = name;
    }
}
