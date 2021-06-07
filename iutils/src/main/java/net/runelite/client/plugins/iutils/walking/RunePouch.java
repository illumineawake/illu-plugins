package net.runelite.client.plugins.iutils.walking;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.game.Game;

import javax.inject.Inject;
import java.util.Arrays;

@Slf4j
public class RunePouch {

    private static int SLOT_1_TYPE_BIT = 29, SLOT_1_QUANTITY_BIT = 1624, SLOT_2_TYPE_BIT = 1622, SLOT_2_QUANTITY_BIT = 1625, SLOT_3_TYPE_BIT = 1623, SLOT_3_QUANTITY_BIT = 1626;
    @Inject
    private Game game;

    public enum RuneSlot {
        FIRST(SLOT_1_TYPE_BIT, SLOT_1_QUANTITY_BIT),
        SECOND(SLOT_2_TYPE_BIT, SLOT_2_QUANTITY_BIT),
        THIRD(SLOT_3_TYPE_BIT, SLOT_3_QUANTITY_BIT);

        private int type;
        private int quantityVarbitIndex;
        @Inject
        private Game game;

        RuneSlot(int type, int quantity) {
            this.type = type;
            this.quantityVarbitIndex = quantity;
        }

        public String getRuneName() {
            switch (game.varb(type)) {
                case 1:
                    return "Air rune";
                case 2:
                    return "Water rune";
                case 3:
                    return "Earth rune";
                case 4:
                    return "Fire rune";
                case 5:
                    return "Mind rune";
                case 6:
                    return "Chaos rune";
                case 7:
                    return "Death rune";
                case 8:
                    return "Blood rune";
                case 9:
                    return "Cosmic rune";
                case 10:
                    return "Nature rune";
                case 11:
                    return "Law rune";
                case 12:
                    return "Body rune";
                case 13:
                    return "Soul rune";
                case 14:
                    return "Astral rune";
                case 15:
                    return "Mist rune";
                case 16:
                    return "Mud rune";
                case 17:
                    return "Dust rune";
                case 18:
                    return "Lava rune";
                case 19:
                    return "Steam rune";
                case 20:
                    return "Smoke rune";
                default:
                    return null;
            }
        }

        public int getQuantity(Game game) {
            return game.varb(quantityVarbitIndex);
        }

    }

    public static int getQuantity(Game game, RuneElement runeElement) {
        if (!hasPouch(game)) {
            return 0;
        }

        for (RuneSlot slot : RuneSlot.values()) {
            String runeName = slot.getRuneName();
            if (runeName == null || Arrays.stream(runeElement.getAlternativeNames()).noneMatch(runeName::startsWith)) {
                continue;
            }
            return slot.getQuantity(game);
        }

        return 0;
    }

    private static boolean hasPouch(Game game) {
        return game.inventory().withName("Rune pouch").exists();
    }
}