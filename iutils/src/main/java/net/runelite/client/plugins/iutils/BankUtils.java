package net.runelite.client.plugins.iutils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.awt.event.KeyEvent.VK_ENTER;
import static net.runelite.client.plugins.iutils.iUtils.iterating;
import static net.runelite.client.plugins.iutils.iUtils.sleep;

@Slf4j
@Singleton
public class BankUtils {
    @Inject
    private Client client;

    @Inject
    private iUtils utils;

    @Inject
    private MouseUtils mouse;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private MenuUtils menu;

    @Inject
    private CalculationUtils calc;

    @Inject
    private KeyboardUtils keyboard;

    @Inject
    private ExecutorService executorService;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ClientThread clientThread;

    public boolean isDepositBoxOpen() {
        return client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER) != null;
    }

    public boolean isOpen() {
        Widget bankWidget = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        // When you close the bank manually with a hot-key, the widget is still active but hidden.
        return client.getItemContainer(InventoryID.BANK) != null /*&& !bankWidget.isHidden()*/; //TODO handle client thread for isHidden
    }

    public void close() {
        if (!isOpen()) {
            return;
        }
        menu.setEntry(new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), 11, 786434, false)); //close bank
        Widget bankCloseWidget = client.getWidget(WidgetInfo.BANK_PIN_EXIT_BUTTON);
        if (bankCloseWidget != null) {
            executorService.submit(() -> mouse.handleMouseClick(bankCloseWidget.getBounds()));
            return;
        }
        mouse.delayMouseClick(new Point(0, 0), calc.getRandomIntBetweenRange(10, 100));
    }

    public int getBankMenuOpcode(int bankID) {
        return Banks.BANK_CHECK_BOX.contains(bankID) ? MenuAction.GAME_OBJECT_FIRST_OPTION.getId() :
                MenuAction.GAME_OBJECT_SECOND_OPTION.getId();
    }

    //doesn't NPE
    public boolean contains(String itemName) {
        if (isOpen()) {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            for (Item item : bankItemContainer.getItems()) {
                if (itemManager.getItemComposition(item.getId()).getName().equalsIgnoreCase(itemName)) {
                    return true;
                }
            }
        }
        return false;
    }

    //doesn't NPE
    public boolean containsAnyOf(int... ids) {
        if (isOpen()) {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            return new BankItemQuery().idEquals(ids).result(client).size() > 0;
        }
        return false;
    }

    public boolean containsAnyOf(Collection<Integer> ids) {
        if (isOpen()) {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);
            for (int id : ids) {
                if (new BankItemQuery().idEquals(ids).result(client).size() > 0) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    //Placeholders count as being found
    public boolean contains(String itemName, int minStackAmount) {
        if (isOpen()) {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            for (Item item : bankItemContainer.getItems()) {
                if (itemManager.getItemComposition(item.getId()).getName().equalsIgnoreCase(itemName) && item.getQuantity() >= minStackAmount) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean contains(int itemID, int minStackAmount) {
        if (isOpen()) {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);
            final WidgetItem bankItem;
            if (bankItemContainer != null) {
                for (Item item : bankItemContainer.getItems()) {
                    if (item.getId() == itemID) {
                        return item.getQuantity() >= minStackAmount;
                    }
                }
            }
        }
        return false;
    }

    public Widget getBankItemWidget(int id) {
        if (!isOpen()) {
            return null;
        }

        WidgetItem bankItem = new BankItemQuery().idEquals(id).result(client).first();
        if (bankItem != null) {
            return bankItem.getWidget();
        } else {
            return null;
        }
    }

    //doesn't NPE
    public Widget getBankItemWidgetAnyOf(int... ids) {
        if (!isOpen()) {
            return null;
        }

        WidgetItem bankItem = new BankItemQuery().idEquals(ids).result(client).first();
        if (bankItem != null) {
            return bankItem.getWidget();
        } else {
            return null;
        }
    }

    public Widget getBankItemWidgetAnyOf(Collection<Integer> ids) {
        if (!isOpen() && !isDepositBoxOpen()) {
            return null;
        }

        WidgetItem bankItem = new BankItemQuery().idEquals(ids).result(client).first();
        if (bankItem != null) {
            return bankItem.getWidget();
        } else {
            return null;
        }
    }

    public void depositAll() {
        if (!isOpen() && !isDepositBoxOpen()) {
            return;
        }
        executorService.submit(() ->
        {
            Widget depositInventoryWidget = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
            if (isDepositBoxOpen()) {
                menu.setEntry(new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, 12582916, false)); //deposit all in bank interface
            } else {
                menu.setEntry(new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, 786474, false)); //deposit all in bank interface
            }
            if ((depositInventoryWidget != null)) {
                mouse.handleMouseClick(depositInventoryWidget.getBounds());
            } else {
                mouse.clickRandomPointCenter(-200, 200);
            }
        });
    }

    public void depositAllExcept(Collection<Integer> ids) {
        if (!isOpen() && !isDepositBoxOpen()) {
            return;
        }
        Collection<WidgetItem> inventoryItems = inventory.getAllItems();
        List<Integer> depositedItems = new ArrayList<>();
        executorService.submit(() ->
        {
            try {
                iterating = true;
                for (WidgetItem item : inventoryItems) {
                    if (!ids.contains(item.getId()) && item.getId() != 6512 && !depositedItems.contains(item.getId())) //6512 is empty widget slot
                    {
                        log.info("depositing item: " + item.getId());
                        depositAllOfItem(item);
                        sleep(80, 200);
                        depositedItems.add(item.getId());
                    }
                }
                iterating = false;
                depositedItems.clear();
            } catch (Exception e) {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void depositAllOfItem(WidgetItem item) {
        if (!isOpen() && !isDepositBoxOpen()) {
            return;
        }
        boolean depositBox = isDepositBoxOpen();
        menu.setEntry(new LegacyMenuEntry("", "", (depositBox) ? 1 : 8, MenuAction.CC_OP.getId(), item.getIndex(),
                (depositBox) ? 12582914 : 983043, false));
        mouse.handleMouseClick(item.getCanvasBounds());
    }

    public void depositAllOfItem(int itemID) {
        if (!isOpen() && !isDepositBoxOpen()) {
            return;
        }
        depositAllOfItem(inventory.getWidgetItem(itemID));
    }

    public void depositAllOfItems(Collection<Integer> itemIDs) {
        if (!isOpen() && !isDepositBoxOpen()) {
            return;
        }
        Collection<WidgetItem> inventoryItems = inventory.getAllItems();
        List<Integer> depositedItems = new ArrayList<>();
        executorService.submit(() ->
        {
            try {
                iterating = true;
                for (WidgetItem item : inventoryItems) {
                    if (itemIDs.contains(item.getId()) && !depositedItems.contains(item.getId())) //6512 is empty widget slot
                    {
                        log.info("depositing item: " + item.getId());
                        depositAllOfItem(item);
                        sleep(80, 170);
                        depositedItems.add(item.getId());
                    }
                }
                iterating = false;
                depositedItems.clear();
            } catch (Exception e) {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void depositOneOfItem(WidgetItem item) {
        if (!isOpen() && !isDepositBoxOpen() || item == null) {
            return;
        }
        boolean depositBox = isDepositBoxOpen();

        menu.setEntry(new LegacyMenuEntry("", "", (client.getVarbitValue(6590) == 0) ? 2 : 3, MenuAction.CC_OP.getId(), item.getIndex(),
                (depositBox) ? 12582914 : 983043, false));
        mouse.delayMouseClick(item.getCanvasBounds(), calc.getRandomIntBetweenRange(0, 50));
    }

    public void depositOneOfItem(int itemID) {
        if (!isOpen() && !isDepositBoxOpen()) {
            return;
        }
        depositOneOfItem(inventory.getWidgetItem(itemID));
    }

    public void withdrawAllItem(Widget bankItemWidget) {
        executorService.submit(() ->
        {
            menu.setEntry(new LegacyMenuEntry("Withdraw-All", "", 7, MenuAction.CC_OP.getId(), bankItemWidget.getIndex(), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false));
            mouse.clickRandomPointCenter(-200, 200);
        });
    }

    public void withdrawAllItem(int bankItemID) {
        Widget item = getBankItemWidget(bankItemID);
        if (item != null) {
            withdrawAllItem(item);
        } else {
            log.debug("Withdraw all item not found.");
        }
    }

    public void withdrawItem(Widget bankItemWidget) {
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", (client.getVarbitValue(6590) == 0) ? 1 : 2, MenuAction.CC_OP.getId(),
                bankItemWidget.getIndex(), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
        utils.doActionClientTick(entry, bankItemWidget.getBounds(), 0);
    }

    public void withdrawItem(int bankItemID) {
        Widget item = getBankItemWidget(bankItemID);
        if (item != null) {
            withdrawItem(item);
        }
    }

    public void withdrawItemAmount(int bankItemID, int amount) {
        clientThread.invokeLater(() -> {
            Widget item = getBankItemWidget(bankItemID);
            if (item != null) {
                int identifier;
                switch (amount) {
                    case 1:
                        identifier = (client.getVarbitValue(6590) == 0) ? 1 : 2;
                        break;
                    case 5:
                        identifier = 3;
                        break;
                    case 10:
                        identifier = 4;
                        break;
                    default:
                        identifier = (client.getVarbitValue(3960) == amount) ? 5 : 6;
                        break;
                }
                utils.doActionMsTime(
                        new LegacyMenuEntry("", "", identifier, MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false),
                        new Point(client.getCenterX() + calc.getRandomIntBetweenRange(-200, 200), client.getCenterY() + calc.getRandomIntBetweenRange(-200, 200)),
                        50
                );
                if (identifier == 6) {
                    executorService.submit(() -> {
                        sleep(calc.getRandomIntBetweenRange(1000, 1500));
                        keyboard.typeString(String.valueOf(amount));
                        sleep(calc.getRandomIntBetweenRange(80, 250));
                        keyboard.pressKey(VK_ENTER);
                    });
                }
            }
        });
    }
}
