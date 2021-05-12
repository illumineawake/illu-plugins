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
    private final ItemContainer equipment;

    @Inject
    public Equipment(Game game) {
        this.game = game;
        this.equipment = game.container(94);
    }

    public boolean isEquipped(int id) {
        return game.container(94) != null && Arrays.stream(game.container(94).getItems()).anyMatch(i -> i.getId() == id);
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
        return game.container(94).getItem(slot);
    }

    public Item slot(EquipmentSlot slot) {
        return slot(slot.index);
    }

    /**
     * @param slot
     * @return -1 if item is null
     */
    public int itemId(int slot) {
        var item = equipment.getItem(slot);
        return item != null ? item.getId() : -1;
    }

    public int itemId(EquipmentSlot slot) {
        return itemId(slot.index);
    }
}
