package net.runelite.client.plugins.iutils.ui;

import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.InventoryItem;
import net.runelite.client.plugins.iutils.bot.iWidget;

public class Bank {
    public final Bot bot;

    public Bank(Bot bot) {
        this.bot = bot;
    }

    public void depositInventory() {
        if (!isOpen()) {
            throw new IllegalStateException("bank isn't open");
        }

        bot.widget(12, 41).interact(0);
        bot.tick();
        bot.tick();
        bot.tick();
    }

    public void depositEquipment() {
        if (!isOpen()) {
            throw new IllegalStateException("bank isn't open");
        }

        bot.widget(12, 43).interact(0);
        bot.tick();
        bot.tick();
        bot.tick();
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
    public int withdraw(int id, int quantity, boolean noted) { // todo: doesn't wait until the item is withdrawn
        if (!isOpen()) {
            throw new IllegalStateException("bank isn't open");
        }

        // Close bank tutorial TODO
//        if (bot.widget(12, 113).nestedInterface() == 664) {
//            System.out.println("[Bank] Closing bank tutorial");
//            bot.widget(664, 9).select();
//            bot.waitUntil(() -> bot.widget(12, 113).nestedInterface() == -1);
//        }

        ItemComposition definition = bot.getFromClientThread(() -> bot.client().getItemComposition(id));

        int inventoryCapacity = !noted ? inventoryCapacity(id) : inventoryCapacity(definition.getLinkedNoteId());

        if (noted != withdrawNoted()) {
            System.out.println("[Bank] Switching noted mode");

            if (!noted) {
                bot.widget(12, 21).interact(0);
            } else {
                bot.widget(WidgetInfo.BANK_NOTED_BUTTON).interact(0);
            }

            bot.tick();
            bot.tick();
            bot.tick();
        }


        for (Widget item : bot.widget(WidgetInfo.BANK_ITEM_CONTAINER).items()) {
            if (item.getItemId() == 6512 || item.getItemId() == -1 || item.isSelfHidden())
            {
                continue;
            }
            if (item.getItemId() == id) {
                System.out.println("[Bank] Found item (requested = " + quantity + ", bank = " + item.getItemQuantity() + ", capacity = " + inventoryCapacity + ")");

                quantity = Math.min(quantity, item.getItemQuantity());
                iWidget itemWidget = new iWidget(bot, item); //TODO this could be very wrong

                if (quantity == withdrawDefaultQuantity()) {
                    itemWidget.interact(0); // default
                } else if (item.getItemQuantity() <= quantity || inventoryCapacity <= quantity) {
                    itemWidget.interact(6); // all
                } else if (quantity == 1) {
                    itemWidget.interact(1); // 1
                } else if (quantity == 5) {
                    itemWidget.interact(2); // 5
                } else if (quantity == 10) {
                    itemWidget.interact(3); // 10
                } else if (quantity == withdrawXDefaultQuantity()) {
                    itemWidget.interact(4); // last
                } else {
                    itemWidget.interact(5);
                    bot.tick();
                    bot.tick();
                    bot.tick();
                    bot.chooseNumber(quantity);
                }

                bot.tick();
                bot.tick();
                bot.tick();
                return Math.min(inventoryCapacity, quantity);
            }
        }

        System.out.println("[Bank] Item not found");
        return 0;
    }

    public int quantity(int id) {
        if (!isOpen()) {
            throw new IllegalStateException("bank not open");
        }

        for (Widget item : bot.widget(WidgetInfo.BANK_ITEM_CONTAINER).items()) {
            if (item.getItemId() == id) {
                return item.getItemQuantity();
            }
        }

        return 0;
    }

    public boolean isOpen() {
        return bot.getFromClientThread(() -> bot.container(InventoryID.BANK) != null);
    }

    public boolean withdrawNoted() {
        return bot.varb(3958) == 1;
    }

    public int withdrawDefaultQuantity() {
        switch(bot.varb(6590)) {
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
                throw new IllegalStateException("unknown withdraw quantity type " + bot.varb(6590));
        }
    }

    public int withdrawXDefaultQuantity() {
        return bot.varb(3960);
    }

    private int inventoryCapacity(int id) {
        boolean stackable = bot.getFromClientThread(() -> bot.client.getItemComposition(id).isStackable());
        if (stackable) {
            InventoryItem item = bot.inventory().withId(id).first();
            return item == null ? Integer.MAX_VALUE : Integer.MAX_VALUE - item.quantity();
        } else {
            return 28 - bot.inventory().size();
        }
    }
}
