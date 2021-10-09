package net.runelite.client.plugins.iutils;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;

public class ContainerUtils {
    public static int getQuantity(final int itemId, Client client) {
        return getQuantity(List.of(itemId), client);
    }

    public static int getQuantity(final List<Integer> itemIds, Client client) {
        Collection<WidgetItem> inventoryItems = getInventoryItems(client);

        if (inventoryItems == null) {
            return 0;
        }

        int count = 0;

        for (WidgetItem inventoryItem : inventoryItems) {
            if (itemIds.contains(inventoryItem.getId())) {
                count += inventoryItem.getQuantity();
            }
        }

        return count;
    }

    public static boolean hasItem(final int itemId, Client client) {
        Map<Integer, Integer> items = getInventoryItemsMap(List.of(itemId), client);

        return items != null && !items.isEmpty();
    }

    public static boolean hasItems(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getInventoryItemsMap(itemIds, client);

        return items != null && !items.isEmpty() && items.size() == itemIds.size();
    }

    public static boolean hasAnyItem(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getInventoryItemsMap(itemIds, client);

        return items != null && !items.isEmpty();
    }

    public static boolean hasAnyItem(final int itemId, Client client) {
        return hasAnyItem(List.of(itemId), client);
    }

    public static int getFirstInventoryItemsPos(final int itemId, Client client) {
        return getFirstInventoryItemsPos(List.of(itemId), client);
    }

    public static int getFirstInventoryItemsPos(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getInventoryItemsMap(itemIds, client);

        if (items == null || items.isEmpty()) {
            return -1;
        } else {
            return items.entrySet().stream().findFirst().get().getValue();
        }
    }

    @Nullable
    public static Map<Integer, Integer> getInventoryItemsMap(final List<Integer> itemIds, Client client) {
        Collection<WidgetItem> inventoryItems = getInventoryItems(client);

        if (inventoryItems == null) {
            return null;
        }

        Map<Integer, Integer> items = new HashMap<>();

        for (WidgetItem inventoryItem : inventoryItems) {
            if (itemIds.contains(inventoryItem.getId())) {
                items.put(inventoryItem.getId(), inventoryItem.getIndex());
            }
        }

        if (items.isEmpty()) {
            return null;
        }

        return items;
    }

    @Nullable
    public static Collection<WidgetItem> getInventoryItems(Client client) {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

        if (inventory == null || inventory.isHidden()) {
            return null;
        }

        return new ArrayList<>(inventory.getWidgetItems());
    }

    public static int getBankInventoryQuantity(final int itemId, Client client) {
        return getBankInventoryQuantity(List.of(itemId), client);
    }

    public static int getBankInventoryQuantity(final List<Integer> itemIds, Client client) {
        Collection<WidgetItem> bankInventoryItems = getBankInventoryItems(client);

        if (bankInventoryItems == null) {
            return 0;
        }

        int count = 0;

        for (WidgetItem bankInventoryItem : bankInventoryItems) {
            if (itemIds.contains(bankInventoryItem.getId())) {
                count += bankInventoryItem.getQuantity();
            }
        }

        return count;
    }

    public static boolean hasBankInventoryItem(final int itemId, Client client) {
        Map<Integer, Integer> items = getBankInventoryItemsMap(List.of(itemId), client);

        return items != null && !items.isEmpty();
    }

    public static boolean hasBankInventoryItems(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getBankInventoryItemsMap(itemIds, client);

        return items != null && !items.isEmpty() && items.size() == itemIds.size();
    }

    public static boolean hasAnyBankInventoryItem(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getBankInventoryItemsMap(itemIds, client);

        return items != null && !items.isEmpty();
    }

    public static boolean hasAnyBankInventoryItem(final int itemId, Client client) {
        return hasAnyBankInventoryItem(List.of(itemId), client);
    }

    public static int getFirstBankInventoryItemsPos(final int itemId, Client client) {
        return getFirstBankInventoryItemsPos(List.of(itemId), client);
    }

    public static int getFirstBankInventoryItemsPos(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getBankInventoryItemsMap(itemIds, client);

        if (items == null || items.isEmpty()) {
            return -1;
        } else {
            return items.entrySet().stream().findFirst().get().getValue();
        }
    }

    public static Map<Integer, Integer> getBankInventoryItemsMap(final List<Integer> itemIds, Client client) {
        Collection<WidgetItem> bankInventoryItems = getBankInventoryItems(client);

        if (bankInventoryItems == null) {
            return null;
        }

        Map<Integer, Integer> items = new HashMap<>();

        for (WidgetItem bankInventoryItem : bankInventoryItems) {
            if (itemIds.contains(bankInventoryItem.getId())) {
                items.put(bankInventoryItem.getId(), bankInventoryItem.getIndex());
            }
        }

        if (items.isEmpty()) {
            return null;
        }

        return items;
    }

    @Nullable
    public static Collection<WidgetItem> getBankInventoryItems(Client client) {
        Collection<WidgetItem> widgetItems = new ArrayList<>();

        Widget inventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (inventory == null || inventory.isHidden()) {
            return null;
        }

        Widget[] children = inventory.getDynamicChildren();
        for (int i = 0; i < children.length; i++) {
            Widget child = children[i];
            boolean isDragged = child.isWidgetItemDragged(child.getItemId());
            int dragOffsetX = 0;
            int dragOffsetY = 0;

            if (isDragged) {
                Point p = child.getWidgetItemDragOffsets();
                dragOffsetX = p.getX();
                dragOffsetY = p.getY();
            }
            // set bounds to same size as default inventory
            Rectangle bounds = child.getBounds();
            bounds.setBounds(bounds.x - 1, bounds.y - 1, 32, 32);
            Rectangle dragBounds = child.getBounds();
            dragBounds.setBounds(bounds.x + dragOffsetX, bounds.y + dragOffsetY, 32, 32);
            widgetItems.add(new WidgetItem(child.getItemId(), child.getItemQuantity(), i, bounds, child, dragBounds));
        }

        return widgetItems;
    }

    public static boolean hasBankItem(final int itemId, Client client) {
        Map<Integer, Integer> items = getBankItemsMap(List.of(itemId), client);

        return items != null && !items.isEmpty();
    }

    public static boolean hasBankItems(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getBankItemsMap(itemIds, client);

        return items != null && !items.isEmpty() && items.size() == itemIds.size();
    }

    public static boolean hasAnyBankItem(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getBankItemsMap(itemIds, client);

        return items != null && !items.isEmpty();
    }

    public static boolean hasAnyBankItem(final int itemId, Client client) {
        return hasAnyBankItem(List.of(itemId), client);
    }

    public static int getFirstBankItemsPos(final int itemId, Client client) {
        return getFirstBankItemsPos(List.of(itemId), client);
    }

    public static int getFirstBankItemsPos(final List<Integer> itemIds, Client client) {
        Map<Integer, Integer> items = getBankItemsMap(itemIds, client);

        if (items == null || items.isEmpty()) {
            return -1;
        } else {
            return items.entrySet().stream().findFirst().get().getValue();
        }
    }

    private static Map<Integer, Integer> getBankItemsMap(final List<Integer> itemIds, Client client) {
        Collection<Widget> bankItems = getBankItems(client);

        if (bankItems == null) {
            return null;
        }

        Map<Integer, Integer> items = new HashMap<>();

        for (Widget bankItem : bankItems) {
            if (itemIds.contains(bankItem.getItemId())) {
                items.put(bankItem.getItemId(), bankItem.getIndex());
            }
        }

        if (items.isEmpty()) {
            return null;
        }

        return items;
    }

    @Nullable
    private static Collection<Widget> getBankItems(Client client) {
        Collection<Widget> widgetItems = new ArrayList<>();
        Widget bank = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

        if (bank == null || bank.isHidden()) {
            return null;
        }

        Widget[] children = bank.getDynamicChildren();
        for (Widget child : children) {
            if (child.getItemId() == 6512 || child.getItemId() == -1 || child.isSelfHidden()) {
                continue;
            }

            widgetItems.add(child);
        }

        return widgetItems;
    }
}
