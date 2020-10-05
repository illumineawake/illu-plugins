/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.botutils;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import org.pf4j.Extension;

/**
 *
 */
@Extension
@PluginDescriptor(
        name = "BotUtils",
        type = PluginType.UTILITY,
        description = "Illumine bot utilities",
        hidden = false
)
@Slf4j
@SuppressWarnings("unused")
@Singleton
public class BotUtils extends Plugin {
    @Inject
    private Client client;

    @Inject
    private BotUtilsConfig config;

    @Inject
    private MouseUtils mouse;

    @Inject
    private MenuUtils menu;

    @Inject
    private CalculationUtils calc;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    ExecutorService executorService;

    public boolean randomEvent;
    public boolean iterating;

    @Provides
    BotUtilsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BotUtilsConfig.class);
    }

    @Override
    protected void startUp() {
        /*executorService = Executors.newSingleThreadExecutor();*/
    }

    @Override
    protected void shutDown() {
        /*executorService.shutdown();*/
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
}
