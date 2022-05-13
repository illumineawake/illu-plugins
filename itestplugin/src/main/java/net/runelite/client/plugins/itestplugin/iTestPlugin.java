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
package net.runelite.client.plugins.itestplugin;

import com.google.inject.Injector;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.itestplugin.tasks.TimeoutTask;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scene.Area;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.plugins.iutils.scripts.UtilsScript;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iTest",
        enabledByDefault = false,
        description = "Illumine - Test plugin",
        tags = {"illumine", "task", "test", "bot"}
)
@Slf4j
public class iTestPlugin extends UtilsScript {
    @Inject
    private Injector injector;

    @Inject
    private Client client;

    @Inject
    private iTestConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private iTestOverlay overlay;

    @Inject
    private iUtils utils;

    @Inject
    public ReflectBreakHandler chinBreakHandler;

    @Inject
    private ConfigManager configManager;

    private TaskSet tasks = new TaskSet();
    public static LocalPoint beforeLoc = new LocalPoint(0, 0);

    MenuEntry targetMenu;
    Instant botTimer;
    Player player;

    public static boolean startBot;
    public static long sleepLength;
    public static int tickLength;
    public static int timeout;
    private static boolean stepping = false;
    public static String status = "starting...";

    @Provides
    iTestConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iTestConfig.class);
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
                injector.getInstance(TimeoutTask.class)
        );
    }

    public void resetVals() {
        log.debug("stopping iTest plugin");
        overlayManager.remove(overlay);
        chinBreakHandler.stopPlugin(this);
        startBot = false;
        botTimer = null;
        tasks.clear();
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iTest")) {
            return;
        }
        log.debug("button {} pressed!", configButtonClicked.getKey());
        if (configButtonClicked.getKey().equals("startButton")) {
            if (!startBot) {
                Player player = client.getLocalPlayer();
                if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
                    log.info("starting iTest plugin");
                    loadTasks();
                    startBot = true;
                    chinBreakHandler.startPlugin(this);
                    timeout = 0;
                    targetMenu = null;
                    botTimer = Instant.now();
                    overlayManager.add(overlay);
                    beforeLoc = client.getLocalPlayer().getLocalLocation();
                } else {
                    log.info("Start logged in");
                }
            } else {
                resetVals();
            }
        }
    }

    public void updateStats() {
        //templatePH = (int) getPerHour(totalBraceletCount);
        //coinsPH = (int) getPerHour(totalCoins - ((totalCoins / BRACELET_HA_VAL) * (unchargedBraceletCost + revEtherCost + natureRuneCost)));
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
        if (Game.isBusy()) {
            log.info("Waiting");
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            if (chinBreakHandler.shouldBreak(this)) {
                status = "Taking a break";
                chinBreakHandler.startBreak(this);
                timeout = 5;
            }
            log.info("Game tick: {}, steppppping: {}", client.getTickCount(), stepping);
//            walking.walkTo(GRAND_EXCHANGE);
            game.waitUntil(() -> game.localPlayer().position().distanceTo(GRAND_EXCHANGE) < 50, 5);
//            if (!stepping) {
//                log.info("Walking to GE");
//                walk(GRAND_EXCHANGE);
//            } else {
//                log.info("Stepping");
//            }
//            if (timeout > 0) {
//                timeout--;
//                return;
//            }
//            Task task = tasks.getValidTask();
//
//            if (task != null) {
//                status = task.getTaskDescription();
//                task.onGameTick(event);
//            } else {
//                status = "Task not found";
//                log.debug(status);
//            }
//            beforeLoc = player.getLocalLocation();
        }
    }

    private void walk(Area position) {
        stepping = true;
        game.executorService.submit(() -> {
            walking.walkTo(position);
            stepping = false;
        });
    }
}