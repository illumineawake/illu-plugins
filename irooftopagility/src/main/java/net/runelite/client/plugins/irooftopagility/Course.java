package net.runelite.client.plugins.irooftopagility;

import lombok.Getter;

@Getter
public enum Course {
    GNOME("Gnome Stronghold"),
    DRAYNOR("Draynor Village"),
    AL_KHARID("Al Kharid"),
    VARROCK("Varrock"),
    CANAFIS("Canafis"),
    APE_ATOLL("Ape Atoll"),
    FALADOR("Falador"),
    SEERS("Seers Village"),
    POLLNIVNEACH("Pollnivneach"),
    PRIFDDINAS("Prifddinas"),
    RELLEKKA("Rellekka"),
    ARDOUGNE("Ardougne");

    private final String name;

    Course(String name) {
        this.name = name;
    }
}
