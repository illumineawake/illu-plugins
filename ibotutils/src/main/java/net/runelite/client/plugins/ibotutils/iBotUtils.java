/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.ibotutils;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.http.api.ge.GrandExchangeClient;
import net.runelite.http.api.osbuddy.OSBGrandExchangeClient;
import net.runelite.http.api.osbuddy.OSBGrandExchangeResult;
import okhttp3.OkHttpClient;
import org.pf4j.Extension;

/**
 *
 */
@Extension
@PluginDescriptor(
        name = "iBot Utils",
        type = PluginType.UTILITY,
        description = "Illumine bot utilities",
        hidden = false
)
@Slf4j
@SuppressWarnings("unused")
@Singleton
public class iBotUtils extends Plugin {
    @Inject
    private Client client;

    @Inject
    private iBotUtilsConfig config;

    @Inject
    private MouseUtils mouse;

    @Inject
    private MenuUtils menu;

    @Inject
    private WalkUtils walk;

    @Inject
    private CalculationUtils calc;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    ExecutorService executorService;

    @Inject
    private OSBGrandExchangeClient osbGrandExchangeClient;

    private OSBGrandExchangeResult osbGrandExchangeResult;

    public boolean randomEvent;
    public boolean iterating;

    @Provides
    OSBGrandExchangeClient provideOsbGrandExchangeClient(OkHttpClient okHttpClient)
    {
        return new OSBGrandExchangeClient(okHttpClient);
    }

    @Provides
    GrandExchangeClient provideGrandExchangeClient(OkHttpClient okHttpClient)
    {
        return new GrandExchangeClient(okHttpClient);
    }

    @Provides
	iBotUtilsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iBotUtilsConfig.class);
    }

    @Override
    protected void startUp() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void shutDown() {
        executorService.shutdown();
    }

    public void oneClickCastSpell(WidgetInfo spellWidget, MenuEntry targetMenu, long sleepLength) {
        menu.setEntry(targetMenu, true);
        mouse.delayMouseClick(new Rectangle(0, 0, 100, 100), sleepLength);
        menu.setSelectedSpell(spellWidget);
        mouse.delayMouseClick(new Rectangle(0, 0, 100, 100), calc.getRandomIntBetweenRange(20, 60));
    }

    public void oneClickCastSpell(WidgetInfo spellWidget, MenuEntry targetMenu, Rectangle targetBounds, long sleepLength) {
        menu.setEntry(targetMenu, false);
        menu.setSelectedSpell(spellWidget);
        mouse.delayMouseClick(targetBounds, sleepLength);
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

    public OSBGrandExchangeResult getOSBItem(int itemId)
    {
        log.debug("Looking up OSB item price {}", itemId);
        osbGrandExchangeClient.lookupItem(itemId)
                .subscribe(
                        (osbresult) ->
                        {
                            if (osbresult != null && osbresult.getOverall_average() > 0)
                            {
                                osbGrandExchangeResult = osbresult;
                            }
                        },
                        (e) -> log.debug("Error getting price of item {}", itemId, e)
                );
        if (osbGrandExchangeResult != null)
        {
            return osbGrandExchangeResult;
        }
        else
        {
            return null;
        }
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
     * This method must be called on a new
     * thread, if you try to call it on
     * {@link ClientThread}
     * it will result in a crash/desynced thread.
     */
    public void typeString(String string) {
        assert !client.isClientThread();

        for (char c : string.toCharArray()) {
            pressKey(c);
        }
    }

    public void pressKey(char key) {
        keyEvent(401, key);
        keyEvent(402, key);
        keyEvent(400, key);
    }

    public void pressKey(int key) {
        keyEvent(401, key);
        keyEvent(402, key);
        //keyEvent(400, key);
    }

    private void keyEvent(int id, char key) {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, KeyEvent.VK_UNDEFINED, key
        );

        client.getCanvas().dispatchEvent(e);
    }

    private void keyEvent(int id, int key) {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, key, KeyEvent.CHAR_UNDEFINED
        );
        client.getCanvas().dispatchEvent(e);
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

    public void sleep(int minSleep, int maxSleep) {
        sleep(calc.random(minSleep, maxSleep));
    }

    /**
     * Pauses execution for a given number of milliseconds.
     *
     * @param toSleep The time to sleep in milliseconds.
     */
    public void sleep(int toSleep) {
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

    public void sleep(long toSleep) {
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
    private void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (event.getOpcode() == MenuOpcode.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
                event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
        {
            return;
        }
        if (menu.entry != null)
        {
            client.setLeftClickMenuEntry(menu.entry);
            if (menu.modifiedMenu)
            {
                event.setModified();
            }
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getOpcode() == MenuOpcode.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
                event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
        {
            //Either logging out or world-hopping which is handled by 3rd party plugins so let them have priority
            log.info("Received world-hop/login related click. Giving them priority");
            menu.entry = null;
            return;
        }
        if (menu.entry != null)
        {
            event.consume();
            if (menu.consumeClick)
            {
                log.info("Consuming a click and not sending anything else");
                menu.consumeClick = false;
                return;
            }
            if (event.getOption().equals("Walk here") && walk.walkAction)
            {
                log.debug("Walk action");
                walk.walkTile(walk.coordX, walk.coordY);
                walk.walkAction = false;
                return;
            }
            if (menu.modifiedMenu)
            {
                client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
                client.setSelectedItemSlot(menu.modifiedItemIndex);
                client.setSelectedItemID(menu.modifiedItemID);
                log.info("doing a Modified MOC, mod ID: {}, mod index: {}, param1: {}", menu.modifiedItemID,
                        menu.modifiedItemIndex, menu.entry.getParam1());
                client.invokeMenuAction(menu.entry.getOption(), menu.entry.getTarget(), menu.entry.getIdentifier(),
                        menu.modifiedOpCode, menu.entry.getParam0(), menu.entry.getParam1());
                menu.modifiedMenu = false;
            }
            else
            {
                client.invokeMenuAction(menu.entry.getOption(), menu.entry.getTarget(), menu.entry.getIdentifier(),
                        menu.entry.getOpcode(), menu.entry.getParam0(), menu.entry.getParam1());
            }
            menu.entry = null;
        }
    }
}
