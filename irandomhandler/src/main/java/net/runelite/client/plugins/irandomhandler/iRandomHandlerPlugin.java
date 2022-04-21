/*
 *  Copyright (c) 2018, trimbe <github.com/trimbe>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.irandomhandler;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Set;

@PluginDependency(iUtils.class)
@Extension
@PluginDescriptor(
        name = "iRandom Handler",
        enabledByDefault = false,
        description = "illumine - Dismiss random events and handle genie",
        tags = {"illumine", "random", "event", "genie", "bot"}
)
@Slf4j
public class iRandomHandlerPlugin extends Plugin {
    private static final Set<Integer> EVENT_NPCS = Set.of(
            NpcID.BEE_KEEPER_6747,
            NpcID.CAPT_ARNAV,
            NpcID.DR_JEKYLL, NpcID.DR_JEKYLL_314,
            NpcID.DRUNKEN_DWARF,
            NpcID.DUNCE_6749,
            NpcID.EVIL_BOB, NpcID.EVIL_BOB_6754,
            NpcID.FLIPPA_6744,
            NpcID.FREAKY_FORESTER_6748,
            NpcID.FROG_5429,
            NpcID.GENIE, NpcID.GENIE_327,
            NpcID.GILES, NpcID.GILES_5441,
            NpcID.LEO_6746,
            NpcID.MILES, NpcID.MILES_5440,
            NpcID.MYSTERIOUS_OLD_MAN_6750, NpcID.MYSTERIOUS_OLD_MAN_6751,
            NpcID.MYSTERIOUS_OLD_MAN_6752, NpcID.MYSTERIOUS_OLD_MAN_6753,
            NpcID.NILES, NpcID.NILES_5439,
            NpcID.PILLORY_GUARD,
            NpcID.POSTIE_PETE_6738,
            NpcID.QUIZ_MASTER_6755,
            NpcID.RICK_TURPENTINE, NpcID.RICK_TURPENTINE_376,
            NpcID.SANDWICH_LADY,
            NpcID.SERGEANT_DAMIEN_6743
    );
    private static final Set<String> EVENT_OPTIONS = Set.of(
            "Talk-to",
            "Dismiss"
    );
    private static final int RANDOM_EVENT_TIMEOUT = 150;

    private NPC currentRandomEvent;
    private NPC randomToDismiss;
    private LegacyMenuEntry targetMenu;
    private boolean genie;
    long sleepLength;
    int tickLength;
    int timeout;

    @Inject
    private Client client;

    @Inject
    private iUtils utils;

    @Inject
    private MouseUtils mouse;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    @Inject
    private Notifier notifier;

    @Inject
    private iRandomHandlerConfig config;

    @Provides
    iRandomHandlerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(iRandomHandlerConfig.class);
    }

    @Override
    protected void startUp() {
        utils.setRandomEvent(false);
        genie = false;
        currentRandomEvent = null;
        randomToDismiss = null;
    }

    @Override
    protected void shutDown() {
        currentRandomEvent = null;
        randomToDismiss = null;
        utils.setRandomEvent(false);
        genie = false;
    }

    private long sleepDelay() {
        sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
    }

    private int tickDelay() {
        tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        log.debug("tick delay for {} ticks", tickLength);
        return tickLength;
    }

    @Subscribe
    private void onInteractingChanged(InteractingChanged event) {
        Actor source = event.getSource();
        Actor target = event.getTarget();
        Player player = client.getLocalPlayer();

        // Check that the npc is interacting with the player and the player isn't interacting with the npc, so
        // that the npc isn't set for another user's random
        if (player == null
                || target != player
                || player.getInteracting() == source
                || !(source instanceof NPC)
                || !EVENT_NPCS.contains(((NPC) source).getId())) {
            return;
        }
        log.debug("Random event spawn: {}", source.getName());
        currentRandomEvent = (NPC) source;
        if (shouldDismiss(currentRandomEvent.getId())) {
            log.debug("Random spawned that should be dismissed, setting tick timeout");
            randomToDismiss = currentRandomEvent;
            timeout = tickDelay();
            utils.setRandomEvent(true);
        }
    }

    @Subscribe
    private void GameTick(GameTick event) {
        if (randomToDismiss == null) {
            return;
        }
        if (timeout > 0) {
            timeout--;
            return;
        }
        if (randomToDismiss.getId() == NpcID.GENIE || randomToDismiss.getId() == NpcID.GENIE_327) {
            log.debug("Handling genie random event");
            genie = true;
            if (client.getWidget(231, 3) != null) {
                log.debug("Genie click here to continue found, progressing...");
                targetMenu = new LegacyMenuEntry("Continue", "", 0, MenuAction.WIDGET_CONTINUE.getId(),
                        -1, 15138820, false);
                menu.setEntry(targetMenu);
                mouse.delayMouseClick(randomToDismiss.getConvexHull().getBounds(), sleepDelay());
                timeout = 2 + tickDelay();
                return;
            }
        }
        log.debug("Dismissing random event");
        targetMenu = new LegacyMenuEntry("", "", randomToDismiss.getIndex(),
                (genie) ? MenuAction.NPC_FIRST_OPTION.getId() : MenuAction.NPC_FIFTH_OPTION.getId(),
                0, 0, false);
        menu.setEntry(targetMenu);
        mouse.delayMouseClick(randomToDismiss.getConvexHull().getBounds(), sleepDelay());
        timeout = 2 + tickDelay();
    }

    @Subscribe
    private void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();

        if (npc == currentRandomEvent) {
            currentRandomEvent = null;
            randomToDismiss = null;
            utils.setRandomEvent(false);
            genie = false;
        }
    }

    private boolean shouldDismiss(int id) {
        if (config.dismissAllEvents()) {
            return true;
        }

        switch (id) {
            case NpcID.BEE_KEEPER_6747:
                return config.dismissBeekeeper();
            case NpcID.SERGEANT_DAMIEN_6743:
                return config.dismissDemon();
            case NpcID.FREAKY_FORESTER_6748:
                return config.dismissForester();
            case NpcID.FROG_5429:
                return config.dismissFrog();
            case NpcID.GENIE:
            case NpcID.GENIE_327:
                return config.dismissGenie();
            case NpcID.DR_JEKYLL:
            case NpcID.DR_JEKYLL_314:
                return config.dismissJekyll();
            case NpcID.DRUNKEN_DWARF:
                return config.dismissDwarf();
            case NpcID.EVIL_BOB:
            case NpcID.EVIL_BOB_6754:
                return config.dismissBob();
            case NpcID.LEO_6746:
                return config.dismissGravedigger();
            case NpcID.MYSTERIOUS_OLD_MAN_6750:
            case NpcID.MYSTERIOUS_OLD_MAN_6751:
            case NpcID.MYSTERIOUS_OLD_MAN_6752:
            case NpcID.MYSTERIOUS_OLD_MAN_6753:
                return config.dismissMoM();
            case NpcID.QUIZ_MASTER_6755:
                return config.dismissQuiz();
            case NpcID.DUNCE_6749:
                return config.dismissDunce();
            case NpcID.SANDWICH_LADY:
                return config.dismissSandwich();
            default:
                return false;
        }
    }
}
