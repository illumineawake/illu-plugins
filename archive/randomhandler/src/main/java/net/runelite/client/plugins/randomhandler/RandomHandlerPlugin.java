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

package net.runelite.client.plugins.randomhandler;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.iUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Set;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "Random Handler",
        enabledByDefault = false,
        description = "Auto dismiss random events (illumine edit), notify when random events appear and remove talk/dismiss options on events that aren't yours.",
        type = PluginType.UTILITY
)
@Slf4j
public class RandomHandlerPlugin extends Plugin {
    private static final Set<Integer> EVENT_NPCS = Set.of(
            NpcID.DR_JEKYLL, NpcID.DR_JEKYLL_314,
            NpcID.BEE_KEEPER_6747,
            NpcID.CAPT_ARNAV,
            NpcID.SERGEANT_DAMIEN_6743,
            NpcID.DRUNKEN_DWARF,
            NpcID.FREAKY_FORESTER_6748,
            NpcID.GENIE, NpcID.GENIE_327,
            NpcID.EVIL_BOB, NpcID.EVIL_BOB_6754,
            NpcID.POSTIE_PETE_6738,
            NpcID.LEO_6746,
            NpcID.MYSTERIOUS_OLD_MAN_6750, NpcID.MYSTERIOUS_OLD_MAN_6751,
            NpcID.MYSTERIOUS_OLD_MAN_6752, NpcID.MYSTERIOUS_OLD_MAN_6753,
            NpcID.PILLORY_GUARD,
            NpcID.FLIPPA_6744,
            NpcID.QUIZ_MASTER_6755,
            NpcID.RICK_TURPENTINE, NpcID.RICK_TURPENTINE_376,
            NpcID.SANDWICH_LADY,
            NpcID.DUNCE_6749,
            NpcID.NILES, NpcID.NILES_5439,
            NpcID.MILES, NpcID.MILES_5440,
            NpcID.GILES, NpcID.GILES_5441,
            NpcID.FROG_5429
    );
    private static final Set<String> EVENT_OPTIONS = Set.of(
            "Talk-to",
            "Dismiss"
    );
    private static final int RANDOM_EVENT_TIMEOUT = 150;

    private NPC currentRandomEvent;
    private int lastNotificationTick = -RANDOM_EVENT_TIMEOUT; // to avoid double notifications
    private int attempts = 0;

    @Inject
    private Client client;

    @Inject
    private Notifier notifier;

    @Inject
    private RandomHandlerConfig config;

    @Inject
    private iUtils utils;

    @Provides
    RandomHandlerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(RandomHandlerConfig.class);
    }

    @Override
    protected void startUp() {
        utils.setRandomEvent(false);
    }

    @Override
    protected void shutDown() {
        lastNotificationTick = 0;
        attempts = 0;
        currentRandomEvent = null;
        utils.setRandomEvent(false);
    }

    @Subscribe
    private void onInteractingChanged(InteractingChanged event) {
        Actor source = event.getSource();
        Actor target = event.getTarget();
        Player player = client.getLocalPlayer();

        // Check that the npc is interacting with the player and the player isn't interacting with the npc, so
        // that the notification doesn't fire from talking to other user's randoms
        if (player == null
                || target != player
                || player.getInteracting() == source
                || !(source instanceof NPC)
                || !EVENT_NPCS.contains(((NPC) source).getId())
                || attempts > 3) {
            utils.setRandomEvent(false);
            return;
        }

        log.debug("Random event spawn: {}", source.getName());

        currentRandomEvent = (NPC) source;

        if (client.getTickCount() - lastNotificationTick > RANDOM_EVENT_TIMEOUT) {
            lastNotificationTick = client.getTickCount();

            if (shouldNotify(currentRandomEvent.getId())) {
                notifier.notify("Random event spawned: " + currentRandomEvent.getName());
            }
        }
        utils.setRandomEvent(true);
        utils.clickRandomPoint(0, 400);
    }

    @Subscribe
    private void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();

        if (npc == currentRandomEvent) {
            currentRandomEvent = null;
            utils.setRandomEvent(false);
            attempts = 0;
        }
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event) {
        if (event.getOpcode() >= MenuOpcode.NPC_FIRST_OPTION.getId()
                && event.getOpcode() <= MenuOpcode.NPC_FIFTH_OPTION.getId()
                && EVENT_OPTIONS.contains(event.getOption())) {
            NPC npc = client.getCachedNPCs()[event.getIdentifier()];
            if (npc != null && EVENT_NPCS.contains(npc.getId()) && npc != currentRandomEvent && config.removeMenuOptions()) {
                client.setMenuEntries(Arrays.copyOf(client.getMenuEntries(), client.getMenuEntries().length - 1));
            }
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (currentRandomEvent == null || !config.autoDismiss() || attempts > 3) {
            utils.setRandomEvent(false);
            return;
        }
        MenuEntry dismissMenu = new LegacyMenuEntry("", "", currentRandomEvent.getIndex(), MenuOpcode.NPC_FIFTH_OPTION.getId(), 0, 0, false);
        event.setMenuEntry(dismissMenu);
    }

    private boolean shouldNotify(int id) {
        if (config.notifyAllEvents()) {
            return true;
        }

        switch (id) {
            case NpcID.SERGEANT_DAMIEN_6743:
                return config.notifyDemon();
            case NpcID.FREAKY_FORESTER_6748:
                return config.notifyForester();
            case NpcID.FROG_5429:
                return config.notifyFrog();
            case NpcID.GENIE:
            case NpcID.GENIE_327:
                return config.notifyGenie();
            case NpcID.EVIL_BOB:
            case NpcID.EVIL_BOB_6754:
                return config.notifyBob();
            case NpcID.LEO_6746:
                return config.notifyGravedigger();
            case NpcID.MYSTERIOUS_OLD_MAN_6750:
            case NpcID.MYSTERIOUS_OLD_MAN_6751:
            case NpcID.MYSTERIOUS_OLD_MAN_6752:
            case NpcID.MYSTERIOUS_OLD_MAN_6753:
                return config.notifyMoM();
            case NpcID.QUIZ_MASTER_6755:
                return config.notifyQuiz();
            case NpcID.DUNCE_6749:
                return config.notifyDunce();
            case NpcID.DR_JEKYLL:
            case NpcID.DR_JEKYLL_314:
                return config.notifyDrJekyll();
            default:
                return false;
        }
    }
}
