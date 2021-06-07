package net.runelite.client.plugins.iutils.walking;


import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.iutils.game.EquipmentItem;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.InventoryItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public enum RuneElement {

    AIR(ItemID.AIR_RUNE, "Air", "Smoke", "Mist", "Dust"),
    EARTH(ItemID.EARTH_RUNE, "Earth", "Lava", "Mud", "Dust"),
    FIRE(ItemID.FIRE_RUNE, "Fire", "Lava", "Smoke", "Steam"),
    WATER(ItemID.WATER_RUNE, "Water", "Mud", "Steam", "Mist"),
    LAW(ItemID.LAW_RUNE, "Law"),
    NATURE(ItemID.NATURE_RUNE, "Nature"),
    SOUL(ItemID.SOUL_RUNE, "Soul");

    private String[] alternativeNames;
    private int runeId;

    RuneElement(int runeId, String... alternativeNames) {
        this.alternativeNames = alternativeNames;
        this.runeId = runeId;
    }

    public String[] getAlternativeNames() {
        return alternativeNames;
    }

    public int getRuneId() {
        return runeId;
    }

    public int getCount(Game game) {
        if (haveStaff(game)) {
            return Integer.MAX_VALUE;
        }
        List<InventoryItem> runes = game.inventory().withNamePart("rune").all();
        List<InventoryItem> items = new ArrayList<>();

        for (InventoryItem rune : runes) {
            for (String alternativeName : alternativeNames) {
                if (rune.name().toLowerCase().startsWith(alternativeName.toLowerCase())) {
                    items.add(rune);
                    log.info("Adding teleport rune: {}", rune.name());
                    break;
                }
            }
        }

        return items.stream().mapToInt(InventoryItem::quantity).sum() + RunePouch.getQuantity(game, this);
    }

    private boolean haveStaff(Game game) {
        EquipmentItem item = game.equipment().withNamePart("staff").first();

        if (item != null) {
            return Arrays.stream(alternativeNames)
                    .anyMatch(a -> item.name().toLowerCase().contains(a.toLowerCase()));
        }
        return false;
    }

}