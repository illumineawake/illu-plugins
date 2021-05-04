package net.runelite.client.plugins.iutils.ui;

import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.plugins.iutils.api.EquipmentSlot;
import net.runelite.client.plugins.iutils.bot.Bot;

import javax.inject.Inject;
import java.util.Arrays;

public class Equipment {
    private final Bot bot;
    private final ItemContainer equipment;

    @Inject
    public Equipment(Bot bot) {
        this.bot = bot;
        this.equipment = bot.container(94);
    }

    public boolean isEquipped(int id) {
        return Arrays.stream(bot.container(94).getItems()).anyMatch(i -> i.getId() == id);
    }

    public int quantity(int id) {
        return Arrays.stream(bot.container(94).getItems()).filter(i -> i.getId() == id).mapToInt(Item::getQuantity).sum();
    }

    /**
     * @param slot equipment slot
     * @return item at slot or null
     */
    public Item slot(int slot) {
        return bot.container(94).getItem(slot);
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
