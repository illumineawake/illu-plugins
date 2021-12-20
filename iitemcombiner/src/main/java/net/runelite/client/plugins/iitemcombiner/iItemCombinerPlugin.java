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
package net.runelite.client.plugins.iitemcombiner;

import com.google.inject.Provides;
import com.openosrs.client.util.Groups;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.game.InventoryItem;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scripts.iScript;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iItem Combiner",
        enabledByDefault = false,
        description = "Illumine - Item Combiner plugin",
        tags = {"illumine", "task", "bot"}
)
@Slf4j
public class iItemCombinerPlugin extends iScript {

    @Inject
    private Client client;

    @Inject
    private iItemCombinerConfig config;

    @Inject
    private ConfigManager configManager;

    Instant botTimer;
    public static String status = "starting...";
    private int actionsThisTick;
    private boolean menuAction;
    private String itemOne;
    private String itemTwo;

    @Provides
    iItemCombinerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iItemCombinerConfig.class);
    }

    @Override
    protected void startUp() {

    }

    @Override
    protected void shutDown() {
        stop();
    }

    @Override
    public void onStart() {
        log.info("Starting iItem Combiner");

        if (client != null && game.localPlayer() != null && client.getGameState() == GameState.LOGGED_IN) {
            botTimer = Instant.now();
        } else {
            log.info("Start logged in!");
            stop();
        }
    }

    @Override
    public void onStop() {
        log.info("Stopping iItem Combiner");
        botTimer = null;
        menuAction = false;
        itemOne = "";
        itemTwo = "";
    }

    @Override
    public void loop() {
        if (client != null && client.getLocalPlayer() != null) {

            List<InventoryItem> firstItems = game.inventory().withName(itemOne).all();
            List<InventoryItem> secondItems = game.inventory().withName(itemTwo).all();

            if (firstItems.size() == 0 || secondItems.size() == 0) {
                return;
            }

            for (int i = 0; i < firstItems.size() && i < secondItems.size(); i++) {
                if (actionsThisTick < 10) {
                    firstItems.get(i).useOn(secondItems.get(i));
                    actionsThisTick++;

                    if (config.tickDelay()) {
                        game.tickDelay();
                    }

                    game.sleepDelay();
                }
            }
            game.tick();

        } else {
            stop();
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        actionsThisTick = 0;
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("iCombine")) {
            menuAction = true;
            itemOne = StringUtils.substringBetween(event.getMenuTarget(), ">", "<");
            itemTwo = StringUtils.substringAfterLast(event.getMenuTarget(), ">");
            log.info("Combining item: {} with: {}", itemOne, itemTwo);
            start();
        }

        if (event.getMenuOption().equals("Stop iCombine")) {
            log.info("Stopping iItem Combiner");
            menuAction = false;
            stop();
        }
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event) {
        if (!event.getOption().equals("Use")) {
            return;
        }

        if (event.getTarget().contains("->")) {
            addMenuEntry(event, "iCombine");
        }

        if (menuAction) {
            addMenuEntry(event, "Stop iCombine");
        }
    }

    private void addMenuEntry(MenuEntryAdded event, String option) {
        client.createMenuEntry(-1).setOption(option)
                .setTarget(event.getTarget())
                .setIdentifier(0)
                .setParam1(0)
                .setParam1(0)
                .setType(MenuAction.RUNELITE);
    }
}