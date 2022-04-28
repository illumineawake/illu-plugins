package net.runelite.client.plugins.iutils.util;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility for i1 -> i3 inventory
 *
 * @author Soxs
 */

@Singleton
@Slf4j
public class LegacyInventoryAssistant {

    @Inject
    Client client;

    @Inject
    ClientThread clientThread;

    public <T> T getFromClientThread(Supplier<T> supplier) {
        if (!client.isClientThread()) {
            CompletableFuture<T> future = new CompletableFuture<>();

            clientThread.invoke(() -> {
                future.complete(supplier.get());
            });
            return future.join();
        } else {
            return supplier.get();
        }
    }

    //region utility for mapping option text to menu entry id. Credits to Owain

    private final Map<Integer, ItemComposition> itemCompositionMap = new HashMap<>();

    public ItemComposition getItemDefinition(int id)
    {
        if (itemCompositionMap.containsKey(id))
        {
            return itemCompositionMap.get(id);
        }
        else
        {
            ItemComposition def = getFromClientThread(() -> client.getItemDefinition(id));
            itemCompositionMap.put(id, def);

            return def;
        }
    }

    public int itemOptionToId(int itemId, String match)
    {
        return itemOptionToId(itemId, java.util.List.of(match));
    }

    public int itemOptionToId(int itemId, List<String> match)
    {
        ItemComposition itemDefinition = getItemDefinition(itemId);

        int index = 0;
        for (String action : itemDefinition.getInventoryActions())
        {
            if (action != null && match.stream().anyMatch(action::equalsIgnoreCase))
            {
                if (index <= 2)
                {
                    return index + 2;
                }
                else
                {
                    return index + 3;
                }
            }

            index++;
        }

        return -1;
    }

    public String selectedItemOption(int itemId, List<String> match)
    {
        ItemComposition itemDefinition = getItemDefinition(itemId);
        for (String action : itemDefinition.getInventoryActions())
        {
            if (action != null && match.stream().anyMatch(action::equalsIgnoreCase))
            {
                return action;
            }
        }
        return match.get(0);
    }

    public MenuAction idToMenuAction(int id)
    {
        if (id <= 5)
        {
            return MenuAction.CC_OP;
        }
        else
        {
            return MenuAction.CC_OP_LOW_PRIORITY;
        }
    }

    //endregion

    //region legacy utility method, credits to Pajeet and Ben93riggs respectively

    public void refreshInventory() {
        if (client.isClientThread())
            client.runScript(6009, 9764864, 28, 1, -1);
        else
            clientThread.invokeLater(() -> client.runScript(6009, 9764864, 28, 1, -1));
    }

    public WidgetItem createWidgetItem(Widget item) {
        boolean isDragged = item.isWidgetItemDragged(item.getItemId());

        int dragOffsetX = 0;
        int dragOffsetY = 0;

        if (isDragged)
        {
            Point p = item.getWidgetItemDragOffsets();
            dragOffsetX = p.getX();
            dragOffsetY = p.getY();
        }
        // set bounds to same size as default inventory
        Rectangle bounds = item.getBounds();
        bounds.setBounds(bounds.x - 1, bounds.y - 1, 32, 32);
        Rectangle dragBounds = item.getBounds();
        dragBounds.setBounds(bounds.x + dragOffsetX, bounds.y + dragOffsetY, 32, 32);

        return new WidgetItem(item.getItemId(), item.getItemQuantity(), item.getIndex(), bounds, item, dragBounds);
    }

    //endregion


    public Collection<WidgetItem> getWidgetItems() {
        return getFromClientThread(() -> {
            Widget geWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER);

            boolean geOpen = geWidget != null/* && !geWidget.isHidden()*/;
            boolean bankOpen = !geOpen && client.getItemContainer(InventoryID.BANK) != null;

            Widget inventoryWidget = client.getWidget(
                    bankOpen ? WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER :
                            geOpen ? WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER :
                                    WidgetInfo.INVENTORY
            );

            if (inventoryWidget == null) {
                return new ArrayList<>();
            }

            if (!bankOpen && !geOpen && inventoryWidget.isHidden())
            {
                refreshInventory();
            }

            Widget[] children = inventoryWidget.getDynamicChildren();

            if (children == null) {
                return new ArrayList<>();
            }

            Collection<WidgetItem> widgetItems = new ArrayList<>();
            for (Widget item : children) {
                if (item.getItemId() != 6512) {
                    widgetItems.add(createWidgetItem(item));
                }
            }

            return widgetItems;
        });
    }

    public WidgetItem getWidgetItem(List<Integer> ids)
    {
        return getWidgetItems().stream().filter(wi -> ids.stream().anyMatch(i -> i == wi.getId())).findFirst().orElse(null);
    }

    public LegacyMenuEntry getLegacyMenuEntry(int itemID, String... option)
    {
        return getLegacyMenuEntry(Collections.singletonList(itemID), Arrays.asList(option), false);
    }

    public LegacyMenuEntry getLegacyMenuEntry(int itemID, List<String> option)
    {
        return getLegacyMenuEntry(Collections.singletonList(itemID), option, false);
    }

    public LegacyMenuEntry getLegacyMenuEntry(int itemID, boolean forceLeftClick, String... option)
    {
        return getLegacyMenuEntry(Collections.singletonList(itemID), Arrays.asList(option), forceLeftClick);
    }

    public LegacyMenuEntry getLegacyMenuEntry(int itemID, boolean forceLeftClick, List<String> option)
    {
        return getLegacyMenuEntry(Collections.singletonList(itemID), option, forceLeftClick);
    }

    public LegacyMenuEntry getLegacyMenuEntry(List<Integer> itemID, List<String> option, boolean forceLeftClick) {
        WidgetItem itemWidget = getWidgetItem(itemID);
        if (itemWidget != null) {
            int id = itemOptionToId(itemWidget.getId(), option);
            return new LegacyMenuEntry(selectedItemOption(itemWidget.getId(), option), "", id, idToMenuAction(id),
                    itemWidget.getIndex(), WidgetInfo.INVENTORY.getId(), forceLeftClick);
        }
        return null;
    }

}
