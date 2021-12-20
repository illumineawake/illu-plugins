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
package net.runelite.client.plugins.iworldwalker;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.CalculationUtils;
import net.runelite.client.plugins.iutils.PlayerUtils;
import net.runelite.client.plugins.iutils.WalkUtils;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.scripts.iScript;
import net.runelite.client.plugins.iutils.walking.Walking;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iWorld Walker Plugin",
        enabledByDefault = false,
        description = "Illumine - World Walker plugin",
        tags = {"illumine", "walk", "web", "travel", "bot"}
)
@Slf4j
public class iWorldWalkerPlugin extends iScript {
    @Inject
    private Client client;

    @Inject
    private Game game;

    @Inject
    public Walking walking;

    @Inject
    private iWorldWalkerConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private iWorldWalkerOverlay overlay;

    @Inject
    private iUtils utils;

    @Inject
    private WalkUtils walk;

    @Inject
    private PlayerUtils playerUtils;

    @Inject
    private CalculationUtils calc;

    @Inject
    private ConfigManager configManager;

    @Inject
    private WorldMapOverlay worldMapOverlay;

    Instant botTimer;
    Player player;
    iWorldWalkerState state;
    LocalPoint beforeLoc = new LocalPoint(0, 0);
    WorldPoint customLocation;
    WorldPoint catLocation;
    WorldPoint mapPoint;
    String farmLocation;
    private Point lastMenuOpenedPoint;

    long sleepLength;
    int tickLength;
    int timeout;

    @Provides
    iWorldWalkerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iWorldWalkerConfig.class);
    }

    @Override
    protected void startUp() {

    }

    @Override
    protected void onStart() {
        log.info("Starting World Walker");
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            beforeLoc = client.getLocalPlayer().getLocalLocation();
            timeout = 0;
            state = null;
            botTimer = Instant.now();
            overlayManager.add(overlay);
            if (config.category().equals(Category.CUSTOM)) {
                customLocation = getCustomLoc();
                if (customLocation != null) {
                    log.info("Custom location set to: {}", customLocation);
                } else {
                    utils.sendGameMessage("Invalid custom location provided: " + config.customLocation());
                    log.info("Invalid custom location provided: {}", config.customLocation());
                    stop();
                }
            }
        } else {
            stop();
        }
    }

    @Override
    protected void onStop() {
        log.info("Stopping World Walker");
        overlayManager.remove(overlay);
        botTimer = null;
        customLocation = null;
        mapPoint = null;
        state = null;
    }

    @Override
    protected void loop() {
        if (client != null && client.getLocalPlayer() != null) {
            log.info("Looping");
            try {
                walking.walkTo(new Position(getLocation()));
            } catch(IllegalStateException e) {
                utils.sendGameMessage("Can't find path to: " + getLocation().toString());
                stop();
                return;
            }
            stop();
        }
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iWorldWalker")) {
            return;
        }
        log.debug("button {} pressed!", configButtonClicked.getKey());
        if (configButtonClicked.getKey().equals("startButton")) {
            if (!started()) {
                start();
            } else {
                stop();
            }
        }
    }

    private WorldPoint getCustomLoc() {
        if (config.category().equals(Category.CUSTOM)) {
            int[] customTemp = utils.stringToIntArray(config.customLocation());
            if (customTemp.length != 3) {
                return null;
            } else {
                return new WorldPoint(customTemp[0], customTemp[1], customTemp[2]);
            }
        }
        return null;
    }

    private WorldPoint getFarmLocation() {
        if (config.category().equals(Category.FARMING)) {
            switch (config.catFarming()) {
                case ALLOTMENTS:
                    return catLocation = config.catFarmAllotments().getWorldPoint();
                case BUSHES:
                    return catLocation = config.catFarmBushes().getWorldPoint();
                case FRUIT_TREES:
                    return catLocation = config.catFarmFruitTrees().getWorldPoint();
                case HERBS:
                    return catLocation = config.catFarmHerbs().getWorldPoint();
                case HOPS:
                    return catLocation = config.catFarmHops().getWorldPoint();
                case TREES:
                    return catLocation = config.catFarmTrees().getWorldPoint();
            }
        }
        return null;
    }

    private String getFarmName() {
        if (config.category().equals(Category.FARMING) && !config.catFarming().equals(Farming.NONE)) {
            switch (config.catFarming()) {
                case ALLOTMENTS:
                    return farmLocation = config.catFarmAllotments().getName();
                case BUSHES:
                    return farmLocation = config.catFarmBushes().getName();
                case FRUIT_TREES:
                    return farmLocation = config.catFarmFruitTrees().getName();
                case HERBS:
                    return farmLocation = config.catFarmHerbs().getName();
                case HOPS:
                    return farmLocation = config.catFarmHops().getName();
                case TREES:
                    return farmLocation = config.catFarmTrees().getName();
            }
        }
        return null;
    }

    @Subscribe
    private void onConfigChange(ConfigChanged event) {
        if (!event.getGroup().equals("iWorldWalker")) {
            return;
        }
        switch (event.getKey()) {
            case "location":
                if (config.category().equals(Category.CUSTOM)) {
                    customLocation = getCustomLoc();
                    if (customLocation != null) {
                        log.info("Custom location set to: {}", customLocation);
                    } else {
                        utils.sendGameMessage("Invalid custom location provided: " + config.customLocation());
                        log.info("Invalid custom location provided: {}", config.customLocation());
                        stop();
                    }
                }
        }
    }

    private long sleepDelay() {
        sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return sleepLength;
    }

    private int tickDelay() {
        tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        return tickLength;
    }

    private WorldPoint getLocation() {
        if (mapPoint != null) {
            return mapPoint;
        }

        switch (config.category()) {
            case BANKS:
                return catLocation = config.catBanks().getWorldPoint();
            case BARCRAWL:
                return catLocation = config.catBarcrawl().getWorldPoint();
            case CITIES:
                return catLocation = config.catCities().getWorldPoint();
            case FARMING:
                return getFarmLocation();
            case GUILDS:
                return catLocation = config.catGuilds().getWorldPoint();
            case SKILLING:
                return catLocation = config.catSkilling().getWorldPoint();
            case SLAYER:
                return catLocation = config.catSlayer().getWorldPoint();
            case MISC:
                return catLocation = config.catMisc().getWorldPoint();
        }
        return (config.category().equals(Category.CUSTOM)) ? customLocation : catLocation;
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        lastMenuOpenedPoint = client.getMouseCanvasPosition();
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        final Widget map = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);

        if (map != null) {

            if (map.getBounds().contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY())) {
                addMenuEntry(event, "illu-Walk here");
                addMenuEntry(event, "illu-Clear Destination");
            }
        } else if (mapPoint != null && event.getOption().equals("Walk here")) {
            addMenuEntry(event, "illu-Clear Destination");
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("illu-Walk here")) {
            mapPoint = calculateMapPoint(client.isMenuOpen() ? lastMenuOpenedPoint : client.getMouseCanvasPosition());
            if (config.closeMap() && !game.widget(595, 38).hidden()) {
                game.widget(595, 38).interact("Close");
            }
            start();
        }
        if (event.getMenuOption().equals("illu-Clear Destination")) {
            mapPoint = null;
            stop();
        }
    }

    private WorldPoint calculateMapPoint(Point point) {
        float zoom = client.getRenderOverview().getWorldMapZoom();
        RenderOverview renderOverview = client.getRenderOverview();
        final WorldPoint mapPoint = new WorldPoint(renderOverview.getWorldMapPosition().getX(), renderOverview.getWorldMapPosition().getY(), 0);
        final Point middle = worldMapOverlay.mapWorldPointToGraphicsPoint(mapPoint);

        final int dx = (int) ((point.getX() - middle.getX()) / zoom);
        final int dy = (int) ((-(point.getY() - middle.getY())) / zoom);

        return mapPoint.dx(dx).dy(dy);
    }

    private void addMenuEntry(MenuEntryAdded event, String option) { //TODO: update to new menu entry
        client.createMenuEntry(-1).setOption(option)
                .setTarget(event.getTarget())
                .setIdentifier(0)
                .setParam1(0)
                .setParam1(0)
                .setType(MenuAction.RUNELITE);
    }
}