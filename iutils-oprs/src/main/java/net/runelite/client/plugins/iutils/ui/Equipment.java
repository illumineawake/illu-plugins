package net.runelite.client.plugins.iutils.ui;

import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.plugins.iutils.api.EquipmentSlot;
import net.runelite.client.plugins.iutils.game.Game;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Objects;

public class Equipment {
    private final Game game;

    @Inject
    public Equipment(Game game) {
        this.game = game;
    }

    public boolean isEquipped(int id) {
        return game.container(94) != null && Arrays.stream(game.container(94).getItems()).anyMatch(i -> i.getId() == id);
    }

    public boolean isNothingEquipped() {
        if (game.container(94) == null) {
            return true;
        }

        return !Arrays.stream(game.container(94).getItems()).anyMatch(e -> e.getId() != -1);
    }

    public int quantity(int id) {
        if (game.container(94) == null) return 0;

        return Arrays.stream(game.container(94).getItems())
                .filter(Objects::nonNull)
                .filter(i -> i.getId() == id)
                .mapToInt(Item::getQuantity)
                .sum();
    }

    /**
     * @param slot equipment slot
     * @return item at slot or null
     */
    public Item slot(int slot) {
        if (isNothingEquipped()) {
            return null;
        }

        return game.container(94).getItem(slot);
    }

    public Item slot(EquipmentSlot slot) {
        if (slot(slot.index) == null) {
            return new Item(-1, 0);
        }
        return slot(slot.index);
    }

    /**
     * @param slot
     * @return -1 if item is null
     */
    public int itemId(int slot) {
        if (slot(slot) == null) {
            return -1;
        }

        var item = slot(slot);
        return item != null ? item.getId() : -1;
    }

    public int itemId(EquipmentSlot slot) {
        return itemId(slot.index);
    }
}
