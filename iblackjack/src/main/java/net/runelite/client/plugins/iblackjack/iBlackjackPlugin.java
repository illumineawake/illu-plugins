/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.iblackjack;

import com.google.inject.Injector;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iblackjack.tasks.*;
import net.runelite.client.plugins.iutils.CalculationUtils;
import net.runelite.client.plugins.iutils.InventoryUtils;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.RandomUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iBlackjack Helper",
        enabledByDefault = false,
        description = "Illumine - Blackjack helper plugin. Handles knocking out and pickpocketing bandits",
        tags = {"illumine", "thieving", "blackjack", "helper", "bot"}
)
@Slf4j
public class iBlackjackPlugin extends Plugin {
    @Inject
    private Injector injector;

    @Inject
    private Client client;

    @Inject
    private iBlackjackConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private iBlackjackOverlay overlay;

    @Inject
    private iUtils utils;

    @Inject
    private CalculationUtils calc;

    @Inject
    private InventoryUtils inventory;

    @Inject
    public ReflectBreakHandler chinBreakHandler;

    @Inject
    private ConfigManager configManager;

    private TaskSet tasks = new TaskSet();
    public static LocalPoint beforeLoc = new LocalPoint(0, 0);
    LegacyMenuEntry targetMenu;
    Instant botTimer;
    Player player;

    public static final int POLLNIVNEACH_REGION = 13358;
    public static final String SUCCESS_BLACKJACK = "You smack the bandit over the head and render them unconscious.";
    public static final String FAILED_BLACKJACK = "Your blow only glances off the bandit's head.";
    public static long nextKnockoutTick = 0;
    public static int selectedNPCIndex;
    public static int eatHP;
    public static boolean inCombat;
    public static boolean startBot;
    public static long sleepLength;
    public static int tickLength;
    public static int timeout;
    public String status = "starting...";
    public int totalCoins;
    public int coinsPH;
    public int startCoins;
    private int failureCount;

    @Provides
    iBlackjackConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iBlackjackConfig.class);
    }

    @Override
    protected void startUp() {
        chinBreakHandler.registerPlugin(this);
    }

    @Override
    protected void shutDown() {
        resetVals();
        chinBreakHandler.unregisterPlugin(this);
    }


    private void loadTasks() {
        tasks.clear();
        tasks.addAll(
                injector.getInstance(TimeoutTask.class),
                injector.getInstance(MovingTask.class),
                injector.getInstance(HopTask.class),
                injector.getInstance(ShopTask.class),
                injector.getInstance(ReturnTask.class),
                injector.getInstance(PickpocketTask.class),
                injector.getInstance(EatTask.class),
                injector.getInstance(LeaveRoomTask.class),
                injector.getInstance(ResetCombatTask.class),
                injector.getInstance(KnockoutTask.class),
                injector.getInstance(DropTask.class),
                injector.getInstance(SelectNPCTask.class),
                injector.getInstance(BreakTask.class)
        );
    }

    public void resetVals() {
        log.debug("stopping Blackjack plugin");
        overlayManager.remove(overlay);
        chinBreakHandler.stopPlugin(this);
        startBot = false;
        botTimer = null;
        tasks.clear();
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iBlackjack")) {
            return;
        }
        log.debug("button {} pressed!", configButtonClicked.getKey());
        if (configButtonClicked.getKey().equals("startButton")) {
            if (!startBot) {
                Player player = client.getLocalPlayer();
                if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
                    log.info("starting Blackjack plugin");
                    loadTasks();
                    startBot = true;
                    inCombat = false;
                    nextKnockoutTick = 0;
                    selectedNPCIndex = 0;
                    failureCount = 0;
                    chinBreakHandler.startPlugin(this);
                    timeout = 0;
                    eatHP = calc.getRandomIntBetweenRange(config.minEatHP(), config.maxEatHP());
                    targetMenu = null;
                    botTimer = Instant.now();
                    overlayManager.add(overlay);
                    beforeLoc = client.getLocalPlayer().getLocalLocation();
                    WidgetItem coinsWidgetItem = inventory.getWidgetItem(ItemID.COINS_995);
                    totalCoins = 0;
                    startCoins = (coinsWidgetItem != null) ? coinsWidgetItem.getQuantity() : 0;
                    coinsPH = 0;
                } else {
                    log.info("Start logged in");
                }
            } else {
                resetVals();
            }
        }
    }

    public void updateStats() {
        WidgetItem coinsWidgetItem = inventory.getWidgetItem(ItemID.COINS_995);
        totalCoins = (coinsWidgetItem != null) ? coinsWidgetItem.getQuantity() : 0;
        coinsPH = (int) getPerHour((totalCoins - startCoins));
    }

    public long getPerHour(int quantity) {
        Duration timeSinceStart = Duration.between(botTimer, Instant.now());
        if (!timeSinceStart.isZero()) {
            return (int) ((double) quantity * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
        }
        return 0;
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startBot || chinBreakHandler.isBreakActive(this)) {
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN
                && client.getLocalPlayer().getWorldLocation().getRegionID() == POLLNIVNEACH_REGION) {
            updateStats();
            if (chinBreakHandler.shouldBreak(this)) {
                status = "Taking a break";
                chinBreakHandler.startBreak(this);
                timeout = 5;
            }
            if (timeout > 0) {
                timeout--;
                return;
            }
            Task task = tasks.getValidTask();

            if (task != null) {
                status = task.getTaskDescription();
                task.onGameTick(event);
            } else {
                status = "Task not found";
                log.debug(status);
            }
            beforeLoc = player.getLocalLocation();
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if (!startBot) {
            return;
        }
        final String msg = event.getMessage();

        if (event.getType() == ChatMessageType.SPAM && (msg.equals(SUCCESS_BLACKJACK) || (msg.equals(FAILED_BLACKJACK)))) {
            failureCount = 0;
            final int ticks = (config.random()) ? RandomUtils.nextInt(3, 4) : 4;
            nextKnockoutTick = client.getTickCount() + ticks;
        }
        if (event.getType() == ChatMessageType.GAMEMESSAGE && (msg.contains("during combat"))) {
            log.info("In combat!");
            inCombat = true;
        }
        if (event.getType() == ChatMessageType.GAMEMESSAGE && (msg.contains("Perhaps I shouldn't do this here"))) {
            log.info("Seen by another bandit, reset NPC");
            utils.sendGameMessage("You've been seen by another bandit, select a new NPC or location.");
            selectedNPCIndex = 0;
            nextKnockoutTick = 0;
        }
        if (event.getType() == ChatMessageType.ENGINE && (msg.contains("I can't reach that"))) {
            if (failureCount >= 3) {
                log.info("Failed to reach target too many times, stopping");
                utils.sendGameMessage("Failed to reach target too many times, stopping");
                selectedNPCIndex = 0;
                nextKnockoutTick = 0;
                failureCount = 0;
            }
            failureCount++;
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (!startBot) {
            return;
        }
        if (event.getMenuOption().equals("Knock-Out") && selectedNPCIndex == 0) {
            final int ticks = (config.random()) ? RandomUtils.nextInt(3, 4) : 4;
            nextKnockoutTick = client.getTickCount() + ticks;
            selectedNPCIndex = event.getId();
        }
    }
}