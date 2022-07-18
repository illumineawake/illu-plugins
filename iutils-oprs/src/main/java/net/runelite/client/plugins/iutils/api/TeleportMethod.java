package net.runelite.client.plugins.iutils.api;

import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.ui.Bank;
import net.runelite.client.plugins.iutils.walking.BankLocations;
import net.runelite.client.plugins.iutils.walking.TeleportSpell;
import net.runelite.client.plugins.iutils.walking.TeleportTab;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeleportMethod {

    private Game game;
    private TeleportLocation teleportLocation;
    private int quantity;

    public TeleportMethod(Game game, TeleportLocation teleportLocation, int quantity) {
        this.teleportLocation = teleportLocation;
        this.quantity = quantity;
        this.game = game;
    }

    public List<ItemQuantity> getItems() {
        List<ItemQuantity> items = new ArrayList<>();
        TeleportTab tabLocation = this.teleportLocation.getTeleportTab();
        TeleportSpell locationSpell = this.teleportLocation.getTeleportSpell();
        int[][] itemIds = this.teleportLocation.getItemIds();

        if (game.membersWorld()) {
            if (itemIds != null && itemIds.length > 0) {

                ItemQuantity jewelleryTeleport = getItemTeleportInventory();
                if (jewelleryTeleport != null) {
                    return List.of(jewelleryTeleport);
                }

                jewelleryTeleport = getItemTeleportBank();
                return List.of(Objects.requireNonNullElseGet(jewelleryTeleport, () -> new ItemQuantity(itemIds[0][0], 1)));
            }

            if (tabLocation != null && tabLocation.hasRequirements(game)) {
                return List.of(new ItemQuantity(tabLocation.getTabletId(), this.quantity));
            }
        }

        if (locationSpell != null && locationSpell.hasRequirements(game)) {
            for (int i = 0; i < this.quantity; i++) {
                items.addAll(locationSpell.recipe(game));
            }
            return items;
        }

        return items;
    }

    public boolean getTeleport(boolean checkBank) {
        if (hasTeleportInventory()) {
            return true;
        }

        if (checkBank) {
            TeleportTab tabLocation = this.teleportLocation.getTeleportTab();
            TeleportSpell locationSpell = this.teleportLocation.getTeleportSpell();

            if (tabLocation != null) {
                if (bank().withdraw(tabLocation.getTabletId(), 1, false) != 0) {
                    return true;
                }
            }

            if (locationSpell != null) {
                List<ItemQuantity> recipe = locationSpell.recipe(game);
                if (bank().contains(recipe)) {
                    for (ItemQuantity item : recipe) {
                        bank().withdraw(item.id, item.quantity, false);
                    }
                    return true;
                }
            }

            int[][] itemIds = this.teleportLocation.getItemIds();

            if (itemIds != null && itemIds.length > 0) {
                for (int[] itemId : itemIds) {
                    for (int id : itemId) {
                        if (bank().contains(new ItemQuantity(id, 1))) {
                            return bank().withdraw(id, 1, false) != 0;
                        }
                    }
                }
            }
        }

        return false;
    }

    public ItemQuantity getItemTeleportBank() {
        int[][] itemIds = this.teleportLocation.getItemIds();

        if (itemIds != null && itemIds.length > 0) {
            for (int[] itemId : itemIds) {
                for (int id : itemId) {
                    if (bank().contains(new ItemQuantity(id, 1))) {
                        return new ItemQuantity(id, 1);
                    }
                }
            }
        }
        return null;
    }

    public boolean hasTeleportBank() {
        TeleportTab tabLocation = this.teleportLocation.getTeleportTab();
        TeleportSpell locationSpell = this.teleportLocation.getTeleportSpell();
        int[][] itemIds = this.teleportLocation.getItemIds();

        if (tabLocation != null && bank().contains(new ItemQuantity(tabLocation.getTabletId(), this.quantity))) {
            return true;
        }

        if (locationSpell != null && bank().contains(locationSpell.recipe(game))) {
            return true;
        }

        if (itemIds != null && itemIds.length > 0) {
            for (int[] itemId : itemIds) {
                for (int id : itemId) {
                    if (bank().contains(new ItemQuantity(id, 1))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean hasTeleportInventory() {
        TeleportTab tabLocation = this.teleportLocation.getTeleportTab();
        TeleportSpell locationSpell = this.teleportLocation.getTeleportSpell();

        if (tabLocation != null && tabLocation.canUse(game) && game.membersWorld()) {
            return true;
        }

        if (locationSpell != null && locationSpell.canUse(game)) {
            return true;
        }

        return getItemTeleportInventory() != null;
    }

    public ItemQuantity getItemTeleportInventory() {
        int[][] itemIds = this.teleportLocation.getItemIds();

        if (itemIds != null && itemIds.length > 0) {
            for (int[] itemId : itemIds) {
                if (/*game.equipment().withId(itemId).exists() || */game.inventory().withId(itemId).exists()) {
                    return new ItemQuantity(game.inventory().withId(itemId).first().id(), 1);
                }
            }
        }
        return null;
    }

    protected Bank bank() {
        var bank = new Bank(game);

        if (!bank.isOpen()) {
            BankLocations.walkToBank(game);
            if (game.npcs().withName("Banker").withAction("Bank").exists()) {
                game.npcs().withName("Banker").withAction("Bank").nearest().interact("Bank");
            } else if (game.objects().withName("Bank booth").withAction("Bank").exists()) {
                game.objects().withName("Bank booth").withAction("Bank").nearest().interact("Bank");
            } else {
                game.objects().withName("Bank chest").nearest().interact("Use");
            }
            game.waitUntil(bank::isOpen, 10);
            game.tick();
        }

        return bank;
    }
}
