package net.runelite.client.plugins.iutils.ui;

import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.InventoryItem;
import net.runelite.client.plugins.iutils.game.iWidget;

public class Bank {
    public final Game game;

    public Bank(Game game) {
        this.game = game;
    }

    public void depositInventory() {
        checkBankOpen();

        if (game.inventory().count() != 0) {
            game.widget(12, 41).interact(0);
            game.waitUntil(() -> game.inventory().count() == 0);
        }
    }

    public void depositEquipment() {
        checkBankOpen();

        if (game.equipment().count() != 0) {
            game.widget(12, 43).interact(0);
            game.waitUntil(() -> game.equipment().count() == 0);
        }
    }

    private void checkBankOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("bank isn't open");
        }

        if (game.widget(12, 113).nestedInterface() == 664) {
            System.out.println("[Bank] Closing bank tutorial");
            game.widget(664, 9).select();
            game.waitUntil(() -> game.widget(12, 113).nestedInterface() == -1);
        }
    }

    /**
     * Withdraws an item from the bank.
     *
     * @param id       the unnoted id of the item to withdraw
     * @param quantity the quantity of that item to withdraw
     * @param noted    whether the noted version of the item should be withdrawn
     * @return the quantity actually withdrawn (may be less then the requested
     * quantity if there are not enough in the bank or the inventory is too full)
     */
    public int withdraw(int id, int quantity, boolean noted) {
        checkBankOpen();

        ItemComposition definition = game.getFromClientThread(() -> game.client().getItemComposition(id));

        var inventoryCapacity = inventoryCapacity(noted ? definition.getLinkedNoteId() : id);

        if (inventoryCapacity == 0) {
            return 0;
        }

        setNotedMode(noted);

        for (iWidget item : game.widget(WidgetInfo.BANK_ITEM_CONTAINER).items()) {
            if (item.itemId() == 6512 || item.itemId() == -1 || item.hidden()) {
                continue;
            }
            if (item.itemId() == id) {
                System.out.println("[Bank] Found item (requested = " + quantity + ", bank = " + item.quantity() + ", capacity = " + inventoryCapacity + ")");

                quantity = Math.min(quantity, item.quantity());

                if (quantity == withdrawDefaultQuantity()) {
                    item.interact(0); // default
                } else if (item.quantity() <= quantity || inventoryCapacity <= quantity) {
                    item.interact(6); // all
                } else if (quantity == 1) {
                    item.interact(1); // 1
                } else if (quantity == 5) {
                    item.interact(2); // 5
                } else if (quantity == 10) {
                    item.interact(3); // 10
                } else if (quantity == withdrawXDefaultQuantity()) {
                    item.interact(4); // last
                } else {
                    item.interact(5);
                    game.tick(3);
                    game.chooseNumber(quantity);
                }

//                bot.waitChange(() -> bot.inventory().withId(id).quantity());
                game.tick();
                return Math.min(inventoryCapacity, quantity);
            }
        }

        System.out.println("[Bank] Item not found");
        return 0;
    }

    private void setNotedMode(boolean noted) {
        if (noted != withdrawNoted()) {
            if (!noted) {
                game.widget(12, 21).interact(0);
            } else {
                game.widget(12, 23).interact(0);
            }

            game.waitUntil(() -> noted == withdrawNoted());
        }
    }

    private void completeBankTutorial() {
        if (game.widget(12, 113).nestedInterface() == 664) {
            System.out.println("[Bank] Closing bank tutorial");
            game.widget(664, 9).select();
            game.waitUntil(() -> game.widget(12, 113).nestedInterface() == -1);
        }
    }

    public int quantity(int id) {
        if (!isOpen()) {
            throw new IllegalStateException("bank not open");
        }

        for (iWidget item : game.widget(WidgetInfo.BANK_ITEM_CONTAINER).items()) {
            if (item.itemId() == 6512 || item.itemId() == -1 || item.hidden()) {
                continue;
            }
            if (item.itemId() == id) {
                return item.quantity();
            }
        }

        return 0;
    }

    public boolean isOpen() {
        return game.getFromClientThread(() -> game.container(InventoryID.BANK) != null);
    }

    public boolean withdrawNoted() {
        return game.varb(3958) == 1;
    }

    public int withdrawDefaultQuantity() {
        switch (game.varb(6590)) {
            case 0:
                return 1;
            case 1:
                return 5;
            case 2:
                return 10;
            case 3:
                return withdrawXDefaultQuantity();
            case 4:
                return Integer.MAX_VALUE;
            default:
                throw new IllegalStateException("unknown withdraw quantity type " + game.varb(6590));
        }
    }

    public int withdrawXDefaultQuantity() {
        return game.varb(3960);
    }

    private int inventoryCapacity(int id) {
        boolean stackable = game.getFromClientThread(() -> game.client.getItemComposition(id).isStackable());
        if (stackable) {
            InventoryItem item = game.inventory().withId(id).first();
            return item == null ? Integer.MAX_VALUE : Integer.MAX_VALUE - item.quantity();
        } else {
            return 28 - game.inventory().size();
        }
    }
}
