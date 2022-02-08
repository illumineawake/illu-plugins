package net.runelite.client.plugins.iutils.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.InventoryItem;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.game.iWidget;
import net.runelite.client.plugins.iutils.iUtils;

import javax.inject.Inject;
import java.util.*;

@Slf4j
public class Bank {
    public final Game game;

    @Inject
    public Bank(Game game) {
        this.game = game;
    }

    public void depositInventory() {
        checkBankOpen();

        if (game.inventory().count() != 0) {
            game.widget(12, 42).interact(0);
            game.waitUntil(() -> game.inventory().count() == 0, 5);
        }
    }

    public void depositEquipment() {
        checkBankOpen();

        if (game.equipment().count() != 0) {
            game.widget(12, 44).interact(0);
            game.tick();
        }
    }

    private void checkBankOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("bank isn't open");
        }

        if (game.widget(12, 114) != null && game.widget(12, 114).nestedInterface() == 664) {
            log.info("[Bank] Closing bank tutorial");
            game.widget(664, 9).select();
            game.tick();
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
        setNotedMode(noted);

        var inventoryCapacity = inventoryCapacity(noted ? definition.getLinkedNoteId() : id);

        if (inventoryCapacity == 0) {
            return 0;
        }

        for (iWidget item : iUtils.bankitems) {
            if (item.itemId() == id) {
                log.info("[Bank] Found item (requested = " + quantity + ", bank = " + item.quantity() + ", capacity = " + inventoryCapacity + ")");

                quantity = Math.min(quantity, item.quantity());

                if (quantity == withdrawDefaultQuantity()) {
                    log.info("Withdrawing default quantity: " + withdrawDefaultQuantity());
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
                    game.chooseNumber(quantity);
                    game.tick();
                }

                return Math.min(inventoryCapacity, quantity);
            }
        }

        log.info("[Bank] Item not found");
        return 0;
    }

    /**
     * Deposits all of an item to the bank except for the given IDs.
     *
     *@param delay allow a random delay between depositing items
     * @param ids       the ids of the items to NOT deposit
     */
    public void depositExcept(boolean delay, Collection<Integer> ids) {
        var items = game.inventory().withoutId(ids).all();
        Set<Integer> deposited = new HashSet<>();
        while (game.inventory().withoutId(ids).exists()) {
            for (InventoryItem item : items) {
                if (deposited.contains(item.id())) {
                    continue;
                }
                deposit(item.id(), Integer.MAX_VALUE);
                deposited.add(item.id());
                if (delay) game.randomDelay();
            }
        }
    }

    /**
     * Deposits all of an item to the bank except for the given IDs.
     *
     *@param delay allow a random delay between depositing items
     * @param ids       the ids of the items to NOT deposit
     */
    public void depositExcept(boolean delay, int... ids) {
        var items = game.inventory().withoutId(ids).all();
        Set<Integer> deposited = new HashSet<>();
        while (game.inventory().withoutId(ids).exists()) {
            for (InventoryItem item : items) {
                if (deposited.contains(item.id())) {
                    continue;
                }
                deposit(item.id(), Integer.MAX_VALUE);
                deposited.add(item.id());
                if (delay) game.randomDelay();
            }
        }
    }

    /**
     * Deposits all of an item to the bank.
     *
     *@param delay allow a random delay between depositing items
     * @param ids       the ids of the items to deposit all of
     */
    public void depositAll(boolean delay, int... ids) {
        game.inventory().withId(ids).forEach(i -> {
            deposit(i.id(), Integer.MAX_VALUE);
            if (delay) game.randomDelay();
        });
    }

    /**
     * Deposits an item to the bank.
     *
     * @param id       the id of the item to deposit
     * @param quantity the quantity of that item to deposit
     * @return whether inventory item was successfully found and deposited
     */
    public boolean deposit(int id, int quantity) {
        checkBankOpen();

        iWidget targetItem = null;
        int count = 0;

        for (iWidget item : iUtils.bankInventoryitems) {
            if (item.itemId() == id) {
                if (targetItem == null) {
                    targetItem = item;
                }

                if (item.quantity() > 1) {
                    count = item.quantity();
                    break;
                } else {
                    count++;
                }
            }
        }

        if (targetItem != null) {
            log.info("[Bank] Found item (requested = " + quantity + ", inventory = " + count + ")");

            quantity = Math.min(quantity, count);

            if (quantity == withdrawDefaultQuantity()) {
                targetItem.interact(1); // default
            } else if (count <= quantity) {
                targetItem.interact(7); // all
            } else if (quantity == 1) {
                targetItem.interact(2); // 1
            } else if (quantity == 5) {
                targetItem.interact(3); // 5
            } else if (quantity == 10) {
                targetItem.interact(4); // 10
            } else if (quantity == withdrawXDefaultQuantity()) {
                targetItem.interact(5); // last
            } else {
                targetItem.interact(6);
                game.tick(2);
                game.chooseNumber(quantity);
            }

            return true;
        }

        log.info("[Bank] Inventory Item not found");
        return false;
    }

    public int item(int... ids) {
        for (var item : items()) {
            for (var id : ids) {
                if (item.itemId() == id) {
                    return item.itemId();
                }
            }
        }
        return -1;
    }

    public boolean contains(int... ids) {
        return Arrays.stream(ids).anyMatch(i -> items().stream()
                .anyMatch(b -> b.itemId() == i && b.quantity() >= 1));
    }

    public boolean contains(ItemQuantity... items) {
        return Arrays.stream(items).allMatch(i -> items().stream()
                .anyMatch(b -> b.itemId() == i.id && b.quantity() >= i.quantity));
    }

    public boolean contains(List<ItemQuantity> items) {
        return items.stream().allMatch(i -> items().stream()
                .anyMatch(b -> b.itemId() == i.id && b.quantity() >= i.quantity));
    }

    private void setNotedMode(boolean noted) {
        if (noted != withdrawNoted()) {
            if (!noted) {
                game.widget(12, 22).interact(0);
            } else {
                game.widget(12, 24).interact(0);
            }

            game.waitUntil(() -> noted == withdrawNoted());
        }
    }

    private void completeBankTutorial() {
        if (game.widget(12, 114).nestedInterface() == 664) {
            log.info("[Bank] Closing bank tutorial");
            game.widget(664, 9).select();
            game.waitUntil(() -> game.widget(12, 114).nestedInterface() == -1);
        }
    }

//    public HashMap<Integer, Integer> items() {
//        HashMap<Integer, Integer> itemMap = new HashMap<>();
//        if (!isOpen()) {
//            throw new IllegalStateException("bank not open");
//        }
//
//        List<iWidget> allItems = game.widget(WidgetInfo.BANK_ITEM_CONTAINER).items();
//
//        for (iWidget item : allItems) {
//            if (item.itemId() == 6512 || item.itemId() == -1 || item.hidden()) {
//                continue;
//            }
//            log.info("Bank item id: " + item.itemId());
//            itemMap.put(item.itemId(), item.quantity());
//        }
//
//        return itemMap;
//    }

    public List<iWidget> items() {
        if (iUtils.bankitems.isEmpty()) {
            game.tick(); //Give time for items to load
        }
        return iUtils.bankitems;
    }

    public int quantity(int id) {
        if (!isOpen()) {
            throw new IllegalStateException("bank not open");
        }
//        List<iWidget> items = game.widget(WidgetInfo.BANK_ITEM_CONTAINER).items();
        for (iWidget item : iUtils.bankitems) {
//            if (item.itemId() == 6512 || item.itemId() == -1 || item.hidden()) {
//                continue;
//            }
            if (item.itemId() == id) {
                return item.quantity();
            }
        }

        return 0;
    }

    public boolean isOpen() {
        return game.getFromClientThread(() -> game.container(InventoryID.BANK) != null);
    }

    public void close() {
        if (isOpen()) {
            game.widget(12, 2, 11).interact(0);
            game.clientThread.invoke(() -> game.client.runScript(138));
        }
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
