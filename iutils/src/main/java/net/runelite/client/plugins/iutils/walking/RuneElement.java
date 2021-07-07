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
    MIND(ItemID.MIND_RUNE, "Mind"),
    BODY(ItemID.BODY_RUNE, "Body"),
    COSMIC(ItemID.COSMIC_RUNE, "Cosmic"),
    CHAOS(ItemID.CHAOS_RUNE, "Chaos"),
    NATURE(ItemID.NATURE_RUNE, "Nature"),
    LAW(ItemID.LAW_RUNE, "Law"),
    DEATH(ItemID.DEATH_RUNE, "Death"),
    ASTRAL(ItemID.ASTRAL_RUNE, "Astral"),
    BLOOD(ItemID.BLOOD_RUNE, "Blood"),
    SOUL(ItemID.SOUL_RUNE, "Soul"),
    WRATH(ItemID.WRATH_RUNE, "Wrath");

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