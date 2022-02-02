/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iutils;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.iObject;
import net.runelite.client.plugins.iutils.game.iWidget;
import net.runelite.http.api.ge.GrandExchangeClient;
import okhttp3.OkHttpClient;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 *
 */
@Extension
@PluginDescriptor(
        name = "iUtils",
        description = "Illumine plugin utilities"
)
@Slf4j
@SuppressWarnings("unused")
@Singleton
public class iUtils extends Plugin {

    @Inject
    public Game game;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    public iUtilsConfig config;

    @Inject
    private MouseUtils mouse;

    @Inject
    private ActionQueue action;

    @Inject
    private MenuUtils menu;

    @Inject
    private WalkUtils walk;

    @Inject
    private CalculationUtils calc;

    @Inject
    private InterfaceUtils interfaceUtils;

    @Inject
    private KeyboardUtils keyboard;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private ObjectUtils objectUtils;

    @Inject
    private BankUtils bankUtils;

    @Inject
    ExecutorService executorService;

    @Inject
    private ItemManager itemManager;

    public final static Set<TileObject> objects = new HashSet<>();
    public final static Set<TileItem> tileItems = new HashSet<>();
    public final static Set<NPC> npcs = new HashSet<>();
    public final static List<iWidget> bankitems = new ArrayList<>();
    public final static List<iWidget> bankInventoryitems = new ArrayList<>();

    public boolean randomEvent;
    public static boolean iterating;
    private final List<ActionQueue.DelayedAction> delayedActions = new ArrayList<>();
    private int clientTick = 0;
    private int gameTick = 0;
    int tickActions;

    @Provides
    GrandExchangeClient provideGrandExchangeClient(OkHttpClient okHttpClient) {
        return new GrandExchangeClient(okHttpClient);
    }

    @Provides
    iUtilsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iUtilsConfig.class);
    }

    @Override
    protected void startUp() {
    }

    @Override
    protected void shutDown() {

    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iUtils")) {
            return;
        }
        log.info("button {} pressed!", configButtonClicked.getKey());
        if (configButtonClicked.getKey().equals("startButton")) {

        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN && gameStateChanged.getGameState() != GameState.CONNECTION_LOST) {
            objects.clear();
            npcs.clear();
            tileItems.clear();
            bankitems.clear();
            bankInventoryitems.clear();
        }
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event) {
        objects.add(event.getWallObject());
    }

    @Subscribe
    public void onWallObjectChanged(WallObjectChanged event) {
        objects.remove(event.getPrevious());
        objects.add(event.getWallObject());
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event) {
        objects.remove(event.getWallObject());
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        objects.add(event.getGameObject());
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        objects.remove(event.getGameObject());
    }

    @Subscribe
    public void onDecorativeObjectSpawned(DecorativeObjectSpawned event) {
        objects.add(event.getDecorativeObject());
    }

    @Subscribe
    public void onDecorativeObjectDespawned(DecorativeObjectDespawned event) {
        objects.remove(event.getDecorativeObject());
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event) {
        objects.add(event.getGroundObject());
    }

    @Subscribe
    public void onGroundObjectDespawned(GroundObjectDespawned event) {
        objects.remove(event.getGroundObject());
    }

    @Subscribe
    private void onItemSpawned(ItemSpawned event) {
        tileItems.add(event.getItem());
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned event) {
        tileItems.remove(event.getItem());
    }

    @Subscribe
    public void npcSpawned(NpcSpawned event) {
        npcs.add(event.getNpc());
    }

    @Subscribe
    public void npcDespawned(NpcDespawned event) {
        npcs.remove(event.getNpc());
    }

    @Subscribe
    private void onItemContainerChanged(ItemContainerChanged event) {
        if (client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER) != null && bankUtils.isOpen() &&
                event.getContainerId() == InventoryID.INVENTORY.getId()) {
            loadBankInventoryItems();
        }

        if (bankUtils.isOpen() && event.getContainerId() == InventoryID.BANK.getId()) {
            loadBankItems();
        }
    }

    private void loadBankInventoryItems() {
        bankInventoryitems.clear();
        for (Widget item : client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER).getDynamicChildren()) {
            if (item == null || item.getItemId() == 6512 || item.getItemId() == -1 || item.isHidden()) {
                continue;
            }
            bankInventoryitems.add(new iWidget(game, item));
        }
    }

    private void loadBankItems() {
        bankitems.clear();
        for (Widget item : client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getDynamicChildren()) {
            if (item == null || item.getItemId() == 6512 || item.getItemId() == -1 || item.isHidden()) {
                continue;
            }
            bankitems.add(new iWidget(game, item));
        }
    }

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == 12) {
            loadBankItems();
        }

        if (event.getGroupId() == 15) {
            loadBankInventoryItems();
        }
    }

    @Subscribe
    private void onWidgetClosed(WidgetClosed event) {
        if (event.getGroupId() == 12) {
            bankitems.clear();
        }

        if (event.getGroupId() == 15) {
            bankInventoryitems.clear();
        }
    }

    public void doTestActionGameTick(Runnable runnable, Point point, long ticksToDelay) {

        action.delayGameTicks(ticksToDelay, runnable);
    }

    //Use with caution, does not pair with mouse click and is potentially detectable
    public void doInvokeClientTick(MenuEntry entry, long ticksToDelay) {
        Runnable runnable = () -> client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
                entry.getOpcode(), entry.getParam0(), entry.getParam1());
        action.delayClientTicks(ticksToDelay, runnable);
    }

    public void doActionClientTick(LegacyMenuEntry entry, Rectangle rect, long ticksToDelay) {
        Point point = mouse.getClickPoint(rect);
        doActionClientTick(entry, point, ticksToDelay);
    }

    public void doActionClientTick(LegacyMenuEntry entry, Point point, long ticksToDelay) {
        Runnable runnable = () -> {
            menu.setEntry(entry);
            mouse.handleMouseClick(point);
        };

        action.delayClientTicks(ticksToDelay, runnable);
    }

    public void doGameObjectActionClientTick(GameObject object, int menuOpcodeID, long ticksToDelay) {
        if (object == null || object.getConvexHull() == null) {
            return;
        }
        Rectangle rectangle = (object.getConvexHull().getBounds() != null) ? object.getConvexHull().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", object.getId(), menuOpcodeID, object.getSceneMinLocation().getX(),
                object.getSceneMinLocation().getY(), false);
        doActionClientTick(entry, rectangle, ticksToDelay);
        iObject test;

    }

    public void doTileObjectActionClientTick(TileObject object, int menuOpcodeID, long ticksToDelay) {
        if (object == null) {
            return;
        }
        Rectangle rectangle = (object.getCanvasTilePoly().getBounds() != null) ? object.getCanvasTilePoly().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", object.getId(), menuOpcodeID, object.getLocalLocation().getSceneX(),
                object.getLocalLocation().getSceneY(), false);
        doActionClientTick(entry, rectangle, ticksToDelay);
    }

    public void doNpcActionClientTick(NPC npc, int menuOpcodeID, long ticksToDelay) {
        if (npc == null || npc.getConvexHull() == null) {
            return;
        }
        Rectangle rectangle = (npc.getConvexHull().getBounds() != null) ? npc.getConvexHull().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", npc.getIndex(), menuOpcodeID, 0, 0, false);
        doActionClientTick(entry, rectangle, ticksToDelay);
    }

    public void doItemActionClientTick(WidgetItem item, int menuOpcodeID, int menuParam1ID, long ticksToDelay) {
        if (item == null) {
            return;
        }
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", item.getId(), menuOpcodeID,
                item.getIndex(), menuParam1ID, true);
        doActionClientTick(entry, item.getCanvasBounds().getBounds(), ticksToDelay);
    }

    //Use with caution, does not pair with mouse click and is potentially detectable
    public void doInvokeGameTick(MenuEntry entry, long ticksToDelay) {
        Runnable runnable = () -> client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
                entry.getOpcode(), entry.getParam0(), entry.getParam1());
        action.delayGameTicks(ticksToDelay, runnable);
    }

    public void doActionGameTick(LegacyMenuEntry entry, Rectangle rect, long ticksToDelay) {
        Point point = mouse.getClickPoint(rect);
        doActionGameTick(entry, point, ticksToDelay);
    }

    public void doActionGameTick(LegacyMenuEntry entry, Point point, long ticksToDelay) {

        Runnable runnable = () -> {
            menu.setEntry(entry);
            mouse.handleMouseClick(point);
        };

        action.delayGameTicks(ticksToDelay, runnable);
    }

    public void doGameObjectActionGameTick(GameObject object, int menuOpcodeID, long ticksToDelay) {
        if (object == null || object.getConvexHull() == null) {
            return;
        }
        Rectangle rectangle = (object.getConvexHull().getBounds() != null) ? object.getConvexHull().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", object.getId(), menuOpcodeID, object.getSceneMinLocation().getX(),
                object.getSceneMinLocation().getY(), false);
        doActionGameTick(entry, rectangle, ticksToDelay);
    }

    public void doTileObjectActionGameTick(TileObject object, int menuOpcodeID, long ticksToDelay) {
        if (object == null) {
            return;
        }
        Rectangle rectangle = (object.getCanvasTilePoly().getBounds() != null) ? object.getCanvasTilePoly().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", object.getId(), menuOpcodeID, object.getLocalLocation().getSceneX(),
                object.getLocalLocation().getSceneY(), false);
        doActionGameTick(entry, rectangle, ticksToDelay);
    }

    public void doNpcActionGameTick(NPC npc, int menuOpcodeID, long ticksToDelay) {
        if (npc == null || npc.getConvexHull() == null) {
            return;
        }
        Rectangle rectangle = (npc.getConvexHull().getBounds() != null) ? npc.getConvexHull().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", npc.getIndex(), menuOpcodeID, 0, 0, false);
        doActionGameTick(entry, rectangle, ticksToDelay);
    }

    public void doItemActionGameTick(WidgetItem item, int menuOpcodeID, int menuParam1ID, long ticksToDelay) {
        if (item == null) {
            return;
        }
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", item.getId(), menuOpcodeID,
                item.getIndex(), menuParam1ID, true);
        doActionGameTick(entry, item.getCanvasBounds().getBounds(), ticksToDelay);
    }

    //Use with caution, does not pair with mouse click and is potentially detectable
    public void doInvokeMsTime(LegacyMenuEntry entry, long timeToDelay) {
        Runnable runnable = () -> client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
                entry.getOpcode(), entry.getParam0(), entry.getParam1());
        action.delayTime(timeToDelay, runnable);
    }

    public void doActionMsTime(LegacyMenuEntry entry, Rectangle rect, long timeToDelay) {
        Point point = mouse.getClickPoint(rect);
        doActionMsTime(entry, point, timeToDelay);
    }

    public void doActionMsTime(LegacyMenuEntry entry, Point point, long timeToDelay) {
        Runnable runnable = () -> {
            menu.setEntry(entry);
            mouse.handleMouseClick(point);
        };

        action.delayTime(timeToDelay, runnable);
    }

    public void doGameObjectActionMsTime(GameObject object, int menuOpcodeID, long timeToDelay) {
        if (object == null || object.getConvexHull() == null) {
            return;
        }
        Rectangle rectangle = (object.getConvexHull().getBounds() != null) ? object.getConvexHull().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", object.getId(), menuOpcodeID, object.getSceneMinLocation().getX(),
                object.getSceneMinLocation().getY(), false);
        doActionMsTime(entry, rectangle, timeToDelay);
    }

    public void doTileObjectActionMsTime(TileObject object, int menuOpcodeID, long timeToDelay) {
        if (object == null) {
            return;
        }
        Rectangle rectangle = (object.getCanvasTilePoly().getBounds() != null) ? object.getCanvasTilePoly().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", object.getId(), menuOpcodeID, object.getLocalLocation().getSceneX(),
                object.getLocalLocation().getSceneY(), false);
        doActionMsTime(entry, rectangle, timeToDelay);
    }

    public void doNpcActionMsTime(NPC npc, int menuOpcodeID, long timeToDelay) {
        if (npc == null || npc.getConvexHull() == null) {
            return;
        }
        Rectangle rectangle = (npc.getConvexHull().getBounds() != null) ? npc.getConvexHull().getBounds() :
                new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", npc.getIndex(), menuOpcodeID, 0, 0, false);
        doActionMsTime(entry, rectangle, timeToDelay);
    }

    public void doItemActionMsTime(WidgetItem item, int menuOpcodeID, int menuParam1ID, long timeToDelay) {
        if (item == null) {
            return;
        }
        LegacyMenuEntry entry = new LegacyMenuEntry("", "", item.getId(), menuOpcodeID,
                item.getIndex(), menuParam1ID, true);
        doActionMsTime(entry, item.getCanvasBounds().getBounds(), timeToDelay);
    }

    public void doModifiedActionGameTick(LegacyMenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, Rectangle rect, long ticksToDelay) {
        Point point = mouse.getClickPoint(rect);
        doModifiedActionGameTick(entry, modifiedID, modifiedIndex, modifiedOpcode, point, ticksToDelay);
    }

    public void doModifiedActionGameTick(LegacyMenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, Point point, long ticksToDelay) {
        Runnable runnable = () -> {
            menu.setModifiedEntry(entry, modifiedID, modifiedIndex, modifiedOpcode);
            mouse.handleMouseClick(point);
        };

        action.delayGameTicks(ticksToDelay, runnable);
    }

    //Use with caution, does not pair with mouse click and is potentially detectable
    public void doModifiedInvokeGameTick(LegacyMenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, long ticksToDelay) {
        Runnable runnable = () -> {
            client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
            client.setSelectedItemSlot(modifiedIndex);
            client.setSelectedItemID(modifiedID);
            client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
                    modifiedOpcode, entry.getParam0(), entry.getParam1());
        };

        action.delayGameTicks(ticksToDelay, runnable);
    }

    public void doModifiedActionMsTime(LegacyMenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, Rectangle rect, long timeToDelay) {
        Point point = mouse.getClickPoint(rect);
        doModifiedActionMsTime(entry, modifiedID, modifiedIndex, modifiedOpcode, point, timeToDelay);
    }

    public void doModifiedActionMsTime(LegacyMenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, Point point, long timeToDelay) {
        Runnable runnable = () -> {
            menu.setModifiedEntry(entry, modifiedID, modifiedIndex, modifiedOpcode);
            mouse.handleMouseClick(point);
        };

        action.delayTime(timeToDelay, runnable);
    }

    //Use with caution, does not pair with mouse click and is potentially detectable
    public void doModifiedInvokeMsTime(LegacyMenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, long timeToDelay) {
        Runnable runnable = () -> {
            client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
            client.setSelectedItemSlot(modifiedIndex);
            client.setSelectedItemID(modifiedID);
            client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
                    modifiedOpcode, entry.getParam0(), entry.getParam1());
        };

        action.delayTime(timeToDelay, runnable);
    }

    public void oneClickCastSpell(WidgetInfo spellWidget, LegacyMenuEntry targetMenu, long sleepLength) {
        menu.setEntry(targetMenu, true);
        mouse.delayMouseClick(new Rectangle(0, 0, 100, 100), sleepLength);
        menu.setSelectedSpell(spellWidget);
        mouse.delayMouseClick(new Rectangle(0, 0, 100, 100), calc.getRandomIntBetweenRange(20, 60));
    }

    public void oneClickCastSpell(WidgetInfo spellWidget, LegacyMenuEntry targetMenu, Rectangle targetBounds, long sleepLength) {
        menu.setEntry(targetMenu, false);
        menu.setSelectedSpell(spellWidget);
        mouse.delayMouseClick(targetBounds, sleepLength);
    }

    public void setCombatStyle(int index) {
        LegacyMenuEntry entry = interfaceUtils.getAttackStyleLegacyMenuEntry(index);
        doActionClientTick(entry, new Point(0, 0), 0);
    }

    public void sendGameMessage(String message) {
        String chatMessage = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(message)
                .build();

        chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
    }

    public ItemComposition getCompositionItem(int itemId) {
        log.debug("Looking up CompositionItem: {}", itemId);

        return itemManager.getItemComposition(itemId);
    }

    public int getItemPrice(int itemId, boolean useWikiPrice) {
        log.debug("Looking up price for Item: {}", itemId);

        return itemManager.getItemPriceWithSource(itemId, useWikiPrice);
    }

    //Ganom's
    public int[] stringToIntArray(String string) {
        return Arrays.stream(string.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    public List<Integer> stringToIntList(String string) {
        return (string == null || string.trim().equals("")) ? List.of(0) :
                Arrays.stream(string.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
    }

    public boolean pointOnScreen(Point check) {
        int x = check.getX(), y = check.getY();
        return x > client.getViewportXOffset() && x < client.getViewportWidth()
                && y > client.getViewportYOffset() && y < client.getViewportHeight();
    }


    /**
     * RANDOM EVENT FUNCTIONS
     */
    public void setRandomEvent(boolean random) {
        randomEvent = random;
    }

    public boolean getRandomEvent() {
        return randomEvent;
    }

    /**
     * Pauses execution for a random amount of time between two values.
     *
     * @param minSleep The minimum time to sleep.
     * @param maxSleep The maximum time to sleep.
     * @see #sleep(int)
     */

    public static void sleep(int minSleep, int maxSleep) {
        sleep(CalculationUtils.random(minSleep, maxSleep));
    }

    /**
     * Pauses execution for a given number of milliseconds.
     *
     * @param toSleep The time to sleep in milliseconds.
     */
    public static void sleep(int toSleep) {
        try {
            long start = System.currentTimeMillis();
            Thread.sleep(toSleep);

            // Guarantee minimum sleep
            long now;
            while (start + toSleep > (now = System.currentTimeMillis())) {
                Thread.sleep(start + toSleep - now);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleep(long toSleep) {
        try {
            long start = System.currentTimeMillis();
            Thread.sleep(toSleep);

            // Guarantee minimum sleep
            long now;
            while (start + toSleep > (now = System.currentTimeMillis())) {
                Thread.sleep(start + toSleep - now);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    private void onVarClientIntChanged(VarClientIntChanged event) {
        int index = event.getIndex();
        if (index == VarClientInt.INPUT_TYPE.getIndex() && client.getVar(VarClientInt.INPUT_TYPE) == 7) {
            if (game.closeWidget) {
                log.info("Clearing input dialogue");
                clientThread.invokeLater(() -> client.runScript(138));
            }
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        action.onClientTick(event);
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        tickActions = 0;
        action.onGameTick(event);
        game.closeWidget = false;
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event) {
        if (event.getOpcode() == MenuAction.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
                event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686)) {
            return;
        }
        if (menu.entry != null) {
            client.createMenuEntry(menu.entry.option, menu.entry.target, menu.entry.opcode, menu.entry.identifier, menu.entry.param0, menu.entry.param1, menu.entry.forceLeftClick);
            //            client.insertMenuItem(menu.entry.option, menu.entry.target, menu.entry.opcode, menu.entry.identifier, menu.entry.param0, menu.entry.param1, menu.entry.forceLeftClick);
//            client.setLeftClickMenuEntry(menu.entry);
            if (menu.modifiedMenu) {
                event.setModified();
            }
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuAction() == MenuAction.CC_OP && (event.getWidgetId() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
                event.getWidgetId() == 11927560 || event.getWidgetId() == 4522007 || event.getWidgetId() == 24772686)) {
            //Either logging out or world-hopping which is handled by 3rd party plugins so let them have priority
            log.info("Received world-hop/login related click. Giving them priority");
            menu.entry = null;
            return;
        }
        if (menu.entry != null) {
            tickActions++;
            log.debug("Actions this game tick: {}", tickActions);
            if (menu.consumeClick) {
                event.consume();
                log.info("Consuming a click and not sending anything else");
                menu.consumeClick = false;
                return;
            }
            if (menu.entry.getOption().equals("Walk here")) {
                event.consume();
                log.debug("Walk action: {} {}", walk.coordX, walk.coordY);
                walk.walkTile(walk.coordX, walk.coordY);
                walk.walkAction = false;
                menu.entry = null;
                return;
            }
            if (menu.modifiedMenu) {
                client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
                client.setSelectedItemSlot(menu.modifiedItemIndex);
                client.setSelectedItemID(menu.modifiedItemID);
                log.debug("doing a Modified MOC, mod ID: {}, mod index: {}, param1: {}", menu.modifiedItemID,
                        menu.modifiedItemIndex, menu.entry.getParam1());
                menuAction(event, menu.entry.getOption(), menu.entry.getTarget(), menu.entry.getIdentifier(),
                        MenuAction.of(menu.modifiedOpCode), menu.entry.getParam0(), menu.entry.getParam1());
                menu.modifiedMenu = false;
            } else {
//                System.out.println(String.format("%s, %s, %s, %s, %s, %s", menu.entry.getOption(), menu.entry.getTarget(), menu.entry.getIdentifier(), menu.entry.getOpcode(), menu.entry.getParam0(), menu.entry.getParam1()));
                menuAction(event, menu.entry.getOption(), menu.entry.getTarget(), menu.entry.getIdentifier(),
                        MenuAction.of(menu.entry.getOpcode()), menu.entry.getParam0(), menu.entry.getParam1());
            }
            menu.entry = null;
        } else {
            if (!event.isConsumed() && !action.delayedActions.isEmpty() && event.getMenuOption().equals("Walk here")) {
                log.info("Consuming a NULL MOC event");
                event.consume();
            }
        }
    }

    public void menuAction(MenuOptionClicked menuOptionClicked, String option, String target, int identifier, MenuAction menuAction, int param0, int param1) {
        menuOptionClicked.setMenuOption(option);
        menuOptionClicked.setMenuTarget(target);
        menuOptionClicked.setId(identifier);
        menuOptionClicked.setMenuAction(menuAction);
        menuOptionClicked.setActionParam(param0);
        menuOptionClicked.setWidgetId(param1);
    }
}
