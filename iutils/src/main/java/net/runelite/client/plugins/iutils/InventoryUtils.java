package net.runelite.client.plugins.iutils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.queries.InventoryItemQuery;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static net.runelite.client.plugins.iutils.iUtils.iterating;
import static net.runelite.client.plugins.iutils.iUtils.sleep;

@Slf4j
@Singleton
public class InventoryUtils {
    @Inject
    private Client client;

    @Inject
    private MouseUtils mouse;

    @Inject
    private MenuUtils menu;

    @Inject
    private BankUtils bank;

    @Inject
    private ExecutorService executorService;

    @Inject
    private ItemManager itemManager;

    public void openInventory() {
        if (client == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        client.runScript(915, 3); //open inventory
    }

    public boolean isFull() {
        return getEmptySlots() <= 0;
    }

    public boolean isEmpty() {
        return getEmptySlots() >= 28;
    }

    public boolean isOpen() {
        if (client.getWidget(WidgetInfo.INVENTORY) == null) {
            return false;
        }
        return !client.getWidget(WidgetInfo.INVENTORY).isHidden();
    }

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
        }
    }

    public List<WidgetItem> getItems(Collection<Integer> ids) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        List<WidgetItem> matchedItems = new ArrayList<>();

        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ids.contains(item.getId())) {
                    matchedItems.add(item);
                }
            }
            return matchedItems;
        }
        return null;
    }

    //Requires Inventory visible or returns empty
    public List<WidgetItem> getItems(String itemName) {
        return new InventoryWidgetItemQuery()
                .filter(i -> client.getItemDefinition(i.getId())
                        .getName()
                        .toLowerCase()
                        .contains(itemName))
                .result(client)
                .list;
    }

    public Collection<WidgetItem> getAllItems() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return inventoryWidget.getWidgetItems();
        }
        return null;
    }

    public Collection<Integer> getAllItemIDs() {
        Collection<WidgetItem> inventoryItems = getAllItems();
        if (inventoryItems != null) {
            Set<Integer> inventoryIDs = new HashSet<>();
            for (WidgetItem item : inventoryItems) {
                if (inventoryIDs.contains(item.getId())) {
                    continue;
                }
                inventoryIDs.add(item.getId());
            }
            return inventoryIDs;
        }
        return null;
    }

    public List<Item> getAllItemsExcept(List<Integer> exceptIDs) {
        exceptIDs.add(-1); //empty inventory slot
        ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (inventoryContainer != null) {
            Item[] items = inventoryContainer.getItems();
            List<Item> itemList = new ArrayList<>(Arrays.asList(items));
            itemList.removeIf(item -> exceptIDs.contains(item.getId()));
            return itemList.isEmpty() ? null : itemList;
        }
        return null;
    }

    public WidgetItem getWidgetItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    return item;
                }
            }
        }
        return null;
    }

    public WidgetItem getWidgetItem(Collection<Integer> ids) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ids.contains(item.getId())) {
                    return item;
                }
            }
        }
        return null;
    }

    public Item getItemExcept(List<Integer> exceptIDs) {
        exceptIDs.add(-1); //empty inventory slot
        ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (inventoryContainer != null) {
            Item[] items = inventoryContainer.getItems();
            List<Item> itemList = new ArrayList<>(Arrays.asList(items));
            itemList.removeIf(item -> exceptIDs.contains(item.getId()));
            return itemList.isEmpty() ? null : itemList.get(0);
        }
        return null;
    }

    public WidgetItem getItemMenu(ItemManager itemManager, String menuOption, int opcode, Collection<Integer> ignoreIDs) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ignoreIDs.contains(item.getId())) {
                    continue;
                }
                String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions) {
                    if (action != null && action.equals(menuOption)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public WidgetItem getItemMenu(Collection<String> menuOptions) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions) {
                    if (action != null && menuOptions.contains(action)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public WidgetItem getWidgetItemMenu(ItemManager itemManager, String menuOption, int opcode) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions) {
                    if (action != null && action.equals(menuOption)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public int getItemCount(int id, boolean stackable) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    if (stackable) {
                        return item.getQuantity();
                    }
                    total++;
                }
            }
        }
        return total;
    }

    public boolean containsItem(int itemID) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }

        return new InventoryItemQuery(InventoryID.INVENTORY)
                .idEquals(itemID)
                .result(client)
                .size() >= 1;
    }

    public boolean containsItem(String itemName) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }

        WidgetItem inventoryItem = new InventoryWidgetItemQuery()
                .filter(i -> client.getItemDefinition(i.getId())
                        .getName()
                        .toLowerCase()
                        .contains(itemName))
                .result(client)
                .first();

        return inventoryItem != null;
    }

    public boolean containsStackAmount(int itemID, int minStackAmount) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        Item item = new InventoryItemQuery(InventoryID.INVENTORY)
                .idEquals(itemID)
                .result(client)
                .first();

        return item != null && item.getQuantity() >= minStackAmount;
    }

    public boolean containsItemAmount(int id, int amount, boolean stackable, boolean exactAmount) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    if (stackable) {
                        total = item.getQuantity();
                        break;
                    }
                    total++;
                }
            }
        }
        return (!exactAmount || total == amount) && (total >= amount);
    }

    public boolean containsItemAmount(Collection<Integer> ids, int amount, boolean stackable, boolean exactAmount) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ids.contains(item.getId())) {
                    if (stackable) {
                        total = item.getQuantity();
                        break;
                    }
                    total++;
                }
            }
        }
        return (!exactAmount || total == amount) && (total >= amount);
    }

    public boolean containsItem(Collection<Integer> itemIds) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        return getItems(itemIds).size() > 0;
    }

    public boolean containsAllOf(Collection<Integer> itemIds) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        for (int item : itemIds) {
            if (!containsItem(item)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsExcept(Collection<Integer> itemIds) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        Collection<WidgetItem> inventoryItems = getAllItems();
        List<Integer> depositedItems = new ArrayList<>();

        for (WidgetItem item : inventoryItems) {
            if (!itemIds.contains(item.getId())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean onlyContains(Collection<Integer> itemIds) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }

        Collection<WidgetItem> inventoryItems = getAllItems();
        
        for (WidgetItem item : inventoryItems) {
            if (!itemIds.contains(item.getId())) {
                return false;
            }
        }

        return true;
    }

    public void dropItem(WidgetItem item) {
        assert !client.isClientThread();

        menu.setEntry(new LegacyMenuEntry("", "", item.getId(), MenuAction.ITEM_FIFTH_OPTION.getId(), item.getIndex(), 9764864, false));
        mouse.click(item.getCanvasBounds());
    }

    public void dropItems(Collection<Integer> ids, boolean dropAll, int minDelayBetween, int maxDelayBetween) {
        if (bank.isOpen() || bank.isDepositBoxOpen()) {
            log.info("can't drop item, bank is open");
            return;
        }
        Collection<WidgetItem> inventoryItems = getAllItems();
        executorService.submit(() ->
        {
            try {
                iterating = true;
                for (WidgetItem item : inventoryItems) {
                    if (ids.contains(item.getId())) //6512 is empty widget slot
                    {
                        log.info("dropping item: " + item.getId());
                        sleep(minDelayBetween, maxDelayBetween);
                        dropItem(item);
                        if (!dropAll) {
                            break;
                        }
                    }
                }
                iterating = false;
            } catch (Exception e) {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void dropAllExcept(Collection<Integer> ids, boolean dropAll, int minDelayBetween, int maxDelayBetween) {
        if (bank.isOpen() || bank.isDepositBoxOpen()) {
            log.info("can't drop item, bank is open");
            return;
        }
        Collection<WidgetItem> inventoryItems = getAllItems();
        executorService.submit(() ->
        {
            try {
                iterating = true;
                for (WidgetItem item : inventoryItems) {
                    if (ids.contains(item.getId())) {
                        log.info("not dropping item: " + item.getId());
                        continue;
                    }
                    sleep(minDelayBetween, maxDelayBetween);
                    dropItem(item);
                    if (!dropAll) {
                        break;
                    }
                }
                iterating = false;
            } catch (Exception e) {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void dropInventory(boolean dropAll, int minDelayBetween, int maxDelayBetween) {
        if (bank.isOpen() || bank.isDepositBoxOpen()) {
            log.info("can't drop item, bank is open");
            return;
        }
        Collection<Integer> inventoryItems = getAllItemIDs();
        dropItems(inventoryItems, dropAll, minDelayBetween, maxDelayBetween);
    }

    public void itemsInteract(Collection<Integer> ids, int opcode, boolean exceptItems, boolean interactAll, int minDelayBetween, int maxDelayBetween) {
        Collection<WidgetItem> inventoryItems = getAllItems();
        executorService.submit(() ->
        {
            try {
                iterating = true;
                for (WidgetItem item : inventoryItems) {
                    if ((!exceptItems && ids.contains(item.getId()) || (exceptItems && !ids.contains(item.getId())))) {
                        log.info("interacting inventory item: {}", item.getId());
                        sleep(minDelayBetween, maxDelayBetween);
                        menu.setEntry(new LegacyMenuEntry("", "", item.getId(), opcode, item.getIndex(), WidgetInfo.INVENTORY.getId(),
                                true));
                        mouse.click(item.getCanvasBounds());
                        if (!interactAll) {
                            break;
                        }
                    }
                }
                iterating = false;
            } catch (Exception e) {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void combineItems(Collection<Integer> ids, int item1ID, int opcode, boolean exceptItems, boolean interactAll, int minDelayBetween, int maxDelayBetween) {
        WidgetItem item1 = getWidgetItem(item1ID);
        if (item1 == null) {
            log.info("combine item1 item not found in inventory");
            return;
        }
        Collection<WidgetItem> inventoryItems = getAllItems();
        executorService.submit(() ->
        {
            try {
                iterating = true;
                for (WidgetItem item : inventoryItems) {
                    if ((!exceptItems && ids.contains(item.getId()) || (exceptItems && !ids.contains(item.getId())))) {
                        log.info("interacting inventory item: {}", item.getId());
                        sleep(minDelayBetween, maxDelayBetween);
                        menu.setModifiedEntry(new LegacyMenuEntry("", "", item1.getId(), opcode, item1.getIndex(), WidgetInfo.INVENTORY.getId(),
                                false), item.getId(), item.getIndex(), MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId());
                        mouse.click(item1.getCanvasBounds());
                        if (!interactAll) {
                            break;
                        }
                    }
                }
                iterating = false;
            } catch (Exception e) {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public boolean runePouchContains(int id) {
        Set<Integer> runePouchIds = new HashSet<>();
        if (client.getVar(Varbits.RUNE_POUCH_RUNE1) != 0) {
            runePouchIds.add(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE1)).getItemId());
        }
        if (client.getVar(Varbits.RUNE_POUCH_RUNE2) != 0) {
            runePouchIds.add(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE2)).getItemId());
        }
        if (client.getVar(Varbits.RUNE_POUCH_RUNE3) != 0) {
            runePouchIds.add(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE3)).getItemId());
        }
        for (int runePouchId : runePouchIds) {
            if (runePouchId == id) {
                return true;
            }
        }
        return false;
    }

    public boolean runePouchContains(Collection<Integer> ids) {
        for (int runeId : ids) {
            if (!runePouchContains(runeId)) {
                return false;
            }
        }
        return true;
    }

    public int runePouchQuanitity(int id) {
        Map<Integer, Integer> runePouchSlots = new HashMap<>();
        if (client.getVar(Varbits.RUNE_POUCH_RUNE1) != 0) {
            runePouchSlots.put(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE1)).getItemId(), client.getVar(Varbits.RUNE_POUCH_AMOUNT1));
        }
        if (client.getVar(Varbits.RUNE_POUCH_RUNE2) != 0) {
            runePouchSlots.put(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE2)).getItemId(), client.getVar(Varbits.RUNE_POUCH_AMOUNT2));
        }
        if (client.getVar(Varbits.RUNE_POUCH_RUNE3) != 0) {
            runePouchSlots.put(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE3)).getItemId(), client.getVar(Varbits.RUNE_POUCH_AMOUNT3));
        }
        if (runePouchSlots.containsKey(id)) {
            return runePouchSlots.get(id);
        }
        return 0;
    }
}
