package net.runelite.client.plugins.botutils;

import static java.awt.event.KeyEvent.VK_ENTER;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.Point;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class BankUtils {
    @Inject
    private Client client;

    @Inject
    private BotUtils utils;

    @Inject
    private MouseUtils mouse;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private MenuUtils menu;

    @Inject
    private CalculationUtils calc;

    @Inject
    private ExecutorService executorService;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ClientThread clientThread;

    public boolean isDepositBoxOpen() {
        return client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER) != null;
    }

    public boolean isBankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    public void closeBank() {
        if (!isBankOpen()) {
            return;
        }
        menu.setEntry(new MenuEntry("", "", 1, MenuOpcode.CC_OP.getId(), 11, 786434, false)); //close bank
        Widget bankCloseWidget = client.getWidget(WidgetInfo.BANK_PIN_EXIT_BUTTON);
        if (bankCloseWidget != null) {
            executorService.submit(() -> mouse.handleMouseClick(bankCloseWidget.getBounds()));
            return;
        }
        mouse.delayMouseClick(new Point(0, 0), calc.getRandomIntBetweenRange(10, 100));
    }

    public int getBankMenuOpcode(int bankID) {
        return Banks.BANK_CHECK_BOX.contains(bankID) ? MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId() :
                MenuOpcode.GAME_OBJECT_SECOND_OPTION.getId();
    }

    //doesn't NPE
    public boolean bankContains(String itemName) {
        if (isBankOpen()) {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            for (Item item : bankItemContainer.getItems()) {
                if (itemManager.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName)) {
                    return true;
                }
            }
        }
        return false;
    }

    //doesn't NPE
    public boolean bankContainsAnyOf(int... ids) {
        if (isBankOpen()) {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            return new BankItemQuery().idEquals(ids).result(client).size() > 0;
        }
        return false;
    }

    public boolean bankContainsAnyOf(Collection<Integer> ids) {
        if (isBankOpen()) {
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
    public boolean bankContains(String itemName, int minStackAmount) {
        if (isBankOpen()) {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            for (Item item : bankItemContainer.getItems()) {
                if (itemManager.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName) && item.getQuantity() >= minStackAmount) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean bankContains(int itemID, int minStackAmount) {
        if (isBankOpen()) {
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

    public boolean bankContains2(int itemID, int minStackAmount) {
        if (isBankOpen()) {
            clientThread.invokeLater(() -> {
                ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);
                final WidgetItem bankItem;
                if (client.isClientThread()) {
                    bankItem = new BankItemQuery().idEquals(itemID).result(client).first();
                } else {
                    bankItem = new BankItemQuery().idEquals(itemID).result(client).first();
                }

                return bankItem != null && bankItem.getQuantity() >= minStackAmount;
            });
        }
        return false;
    }

    //doesn't NPE
    public Widget getBankItemWidget(int id) {
        if (!isBankOpen()) {
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
        if (!isBankOpen()) {
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
        if (!isBankOpen() && !isDepositBoxOpen()) {
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
        if (!isBankOpen() && !isDepositBoxOpen()) {
            return;
        }
        executorService.submit(() ->
        {
            Widget depositInventoryWidget = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
            if (isDepositBoxOpen()) {
                menu.setEntry(new MenuEntry("", "", 1, MenuOpcode.CC_OP.getId(), -1, 12582916, false)); //deposit all in bank interface
            } else {
                menu.setEntry(new MenuEntry("", "", 1, MenuOpcode.CC_OP.getId(), -1, 786473, false)); //deposit all in bank interface
            }
            if ((depositInventoryWidget != null)) {
                mouse.handleMouseClick(depositInventoryWidget.getBounds());
            } else {
                mouse.clickRandomPointCenter(-200, 200);
            }
        });
    }

    public void depositAllExcept(Collection<Integer> ids) {
        if (!isBankOpen() && !isDepositBoxOpen()) {
            return;
        }
        Collection<WidgetItem> inventoryItems = inventory.getAllInventoryItems();
        List<Integer> depositedItems = new ArrayList<>();
        executorService.submit(() ->
        {
            try {
                utils.iterating = true;
                for (WidgetItem item : inventoryItems) {
                    if (!ids.contains(item.getId()) && item.getId() != 6512 && !depositedItems.contains(item.getId())) //6512 is empty widget slot
                    {
                        log.info("depositing item: " + item.getId());
                        depositAllOfItem(item);
                        utils.sleep(80, 200);
                        depositedItems.add(item.getId());
                    }
                }
                utils.iterating = false;
                depositedItems.clear();
            } catch (Exception e) {
                utils.iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void depositAllOfItem(WidgetItem item) {
        assert !client.isClientThread();

        if (!isBankOpen() && !isDepositBoxOpen()) {
            return;
        }
        boolean depositBox = isDepositBoxOpen();
        menu.setEntry(new MenuEntry("", "", (depositBox) ? 1 : 8, MenuOpcode.CC_OP.getId(), item.getIndex(),
                (depositBox) ? 12582914 : 983043, false));
        mouse.click(item.getCanvasBounds());
    }

    public void depositAllOfItem(int itemID) {
        if (!isBankOpen() && !isDepositBoxOpen()) {
            return;
        }
        depositAllOfItem(inventory.getInventoryWidgetItem(itemID));
    }

    public void depositAllOfItems(Collection<Integer> itemIDs) {
        if (!isBankOpen() && !isDepositBoxOpen()) {
            return;
        }
        Collection<WidgetItem> inventoryItems = inventory.getAllInventoryItems();
        List<Integer> depositedItems = new ArrayList<>();
        executorService.submit(() ->
        {
            try {
                utils.iterating = true;
                for (WidgetItem item : inventoryItems) {
                    if (itemIDs.contains(item.getId()) && !depositedItems.contains(item.getId())) //6512 is empty widget slot
                    {
                        log.info("depositing item: " + item.getId());
                        depositAllOfItem(item);
                        utils.sleep(80, 170);
                        depositedItems.add(item.getId());
                    }
                }
                utils.iterating = false;
                depositedItems.clear();
            } catch (Exception e) {
                utils.iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void depositOneOfItem(WidgetItem item) {
        if (!isBankOpen() && !isDepositBoxOpen() || item == null) {
            return;
        }
        boolean depositBox = isDepositBoxOpen();

        menu.setEntry(new MenuEntry("", "", (client.getVarbitValue(6590) == 0) ? 2 : 3, MenuOpcode.CC_OP.getId(), item.getIndex(),
                (depositBox) ? 12582914 : 983043, false));
        mouse.delayMouseClick(item.getCanvasBounds(), calc.getRandomIntBetweenRange(0, 50));
    }

    public void depositOneOfItem(int itemID) {
        if (!isBankOpen() && !isDepositBoxOpen()) {
            return;
        }
        depositOneOfItem(inventory.getInventoryWidgetItem(itemID));
    }

    public void withdrawAllItem(Widget bankItemWidget) {
        executorService.submit(() ->
        {
            menu.setEntry(new MenuEntry("Withdraw-All", "", 7, MenuOpcode.CC_OP.getId(), bankItemWidget.getIndex(), 786444, false));
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
        executorService.submit(() ->
        {
            menu.setEntry(new MenuEntry("", "", (client.getVarbitValue(6590) == 0) ? 1 : 2, MenuOpcode.CC_OP.getId(),
                    bankItemWidget.getIndex(), 786444, false));
            mouse.clickRandomPointCenter(-200, 200);
        });
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
                        identifier = 6;
                        break;
                }
                menu.setEntry(new MenuEntry("", "", identifier, MenuOpcode.CC_OP.getId(), item.getIndex(), 786444, false));
                mouse.delayClickRandomPointCenter(-200, 200, 50);
                if (identifier == 6) {
                    executorService.submit(() -> {
                        utils.sleep(calc.getRandomIntBetweenRange(1000, 1500));
                        utils.typeString(String.valueOf(amount));
                        utils.sleep(calc.getRandomIntBetweenRange(80, 250));
                        utils.pressKey(VK_ENTER);
                    });
                }
            }
        });
    }
}
