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
package net.runelite.client.plugins.powerskiller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.iUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.powerskiller.PowerSkillerState.*;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "Power Skiller",
        enabledByDefault = false,
        description = "Illumine auto power-skill plugin",
        tags = {"tick"},
        type = PluginType.SKILLING
)
@Slf4j
public class PowerSkillerPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private PowerSkillerConfiguration config;

    @Inject
    private iUtils utils;

    @Inject
    private ConfigManager configManager;

    private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
    private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
            new ThreadPoolExecutor.DiscardPolicy());

    PowerSkillerState state;
    GameObject targetObject;
    GameObject nextTree;
    MenuEntry targetMenu;

    int timeout = 0;


    private final Set<Integer> itemIds = new HashSet<>();
    private final Set<Integer> gameObjIds = new HashSet<>();

    WorldPoint skillLocation;

    @Provides
    PowerSkillerConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PowerSkillerConfiguration.class);
        //TODO make GUI that can be updated in realtime, may require new JPanel
    }

    @Override
    protected void startUp() {

    }

    @Override
    protected void shutDown() {
        configManager.setConfiguration("PowerSkiller", "startBot", false);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("PowerSkiller")) {
            return;
        }
        getConfigValues();
        if (event.getKey().equals("startBot")) {
            if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN)) {
                if (config.startBot()) {
                    skillLocation = client.getLocalPlayer().getWorldLocation();
                    getConfigValues();
                    log.info("Starting power-skiller at location: " + skillLocation);
                }
            } else {
                if (config.startBot()) {
                    log.info("Stopping bot");
                    configManager.setConfiguration("PowerSkiller", "startBot", false);
                }
            }
        }
    }

    private void getConfigValues() {
        gameObjIds.clear();

        for (int i : utils.stringToIntArray(config.gameObjects())) {
            gameObjIds.add(i);
        }

        itemIds.clear();

        for (int i : utils.stringToIntArray(config.items())) {
            itemIds.add(i);
        }
    }

    //enables run if below given minimum energy with random positive variation
    private void handleRun(int minEnergy, int randMax) {
        if (utils.isRunEnabled()) {
            return;
        } else if (client.getEnergy() > (minEnergy + calc.getRandomIntBetweenRange(0, randMax))) {
            log.info("enabling run");
            targetMenu = new LegacyMenuEntry("Toggle Run", "", 1, 57, -1, 10485782, false);
            mouse.clickRandomPointCenter(-100, 100);
        }
    }

    private void interactTree() {
        nextTree = object.findNearestGameObjectWithin(skillLocation, config.locationRadius(), gameObjIds);
        if (nextTree != null) {
            targetObject = nextTree;
            targetMenu = new LegacyMenuEntry("", "", nextTree.getId(), 3, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
            mouse.clickRandomPointCenter(-100, 100);
        } else {
            log.info("tree is null");
        }
    }

    private void dropInventory() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            if (!items.isEmpty()) {
                log.info("dropping " + items.size() + " items.");
                utils.sendGameMessage("dropping " + items.size() + " items.");
                state = ITERATING;
                executorService.submit(() ->
                {
                    items.stream()
                            .filter(item -> itemIds.contains(item.getId()))
                            .forEach((item) -> {
                                targetMenu = new LegacyMenuEntry("", "", item.getId(), 37, item.getIndex(), 9764864, false);
                                mouse.clickRandomPointCenter(-100, 100);
                                try {
                                    Thread.sleep(calc.getRandomIntBetweenRange(config.randLow(), config.randHigh()));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                    state = ANIMATING; //failsafe so it doesn't get stuck looping. I should probs handle this better
                });
            } else {
                log.info("inventory list is empty");
            }
        } else {
            log.info("inventory container is null");
        }
    }

    public PowerSkillerState getState() {
        if (timeout > 0) {
            return TIMEOUT;
        }
        if (state == ITERATING && !inventory.inventoryEmpty()) {
            return ITERATING;
        }
        if (inventory.inventoryFull()) {
            return DROPPING;
        }
        if (playerUtils.isMoving()) {
            timeout = 2;
            return MOVING;
        }
        if (!utils.isInteracting() && !inventory.inventoryFull()) {
            return FIND_OBJECT;
        }
        return ANIMATING; //need to determine an appropriate default
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (client != null && client.getLocalPlayer() != null && config.startBot()) {
            handleRun(40, 20);
            state = getState();
            switch (state) {
                case TIMEOUT:
                    timeout--;
                    return;
                case DROPPING:
                    dropInventory();
                    return;
                case FIND_OBJECT:
                    interactTree();
                    return;
                case ANIMATING:
                case ITERATING:
                case MOVING:
                    return; //May be needed
            }
        } else {
            return;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (config.startBot()) {
            if (targetMenu == null) {
                log.info("Modified MenuEntry is null");
                return;
            }
            //TODO: build this into utils or use random handler getter?
            if (utils.getRandomEvent()) //for random events
            {
                log.info("Powerskiller not overriding due to random event");
                return;
            } else {
                //log.info("MenuEntry string event: " + targetMenu.toString());
                event.setMenuEntry(targetMenu);
                if (state != ITERATING) {
                    timeout = 2;
                }
                targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
            }
        } else {
            //TODO: capture object clicks for GUI
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (nextTree == null || event.getGameObject() != nextTree) {
            return;
        } else {
            if (client.getLocalDestinationLocation() != null) {
                interactTree(); //This is a failsafe, Player can get stuck with a destination on object despawn and be "forever moving".
            }
        }
    }
}