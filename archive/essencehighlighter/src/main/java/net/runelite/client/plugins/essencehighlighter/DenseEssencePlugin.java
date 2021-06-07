/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.essencehighlighter;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static net.runelite.api.NullItemID.NULL_8975;

@Extension
@PluginDescriptor(
        name = "Dense Essence Highlighter",
        enabledByDefault = false,
        description = "Show minimap icons and clickboxes for abyssal rifts",
        tags = {"abyssal", "minimap", "overlay", "rifts", "rc", "runecrafting"},
        type = PluginType.SKILLING
)

@Slf4j
public class DenseEssencePlugin extends Plugin {


    private static final int DENSE_RUNESTONE_SOUTH_ID = NullObjectID.NULL_10796;
    private static final int DENSE_RUNESTONE_NORTH_ID = NullObjectID.NULL_8981;
    /*private static final int BLOOD_ALTAR_ID = NullObjectID.NULL_27978;
    private static final int DARK_ALTAR_ID = NullObjectID.NULL_27979;*/
    private static final int Dense_runestone_ID = NULL_8975;
    /* added */
    private static final int DENSE_RUNESTONE_SOUTH_DEPLETED = 4928;
    private static final int DENSE_RUNESTONE_NORTH_DEPLETED = 4927;

    @Getter(AccessLevel.PACKAGE)
    private final Set<DecorativeObject> abyssObjects = new HashSet<>();


    @Getter(AccessLevel.PACKAGE)
    private NPC darkMage;

    @Getter(AccessLevel.PACKAGE)
    private GameObject denseRunestoneSouth;

    @Getter(AccessLevel.PACKAGE)
    private GameObject BLOOD_ALTAr;
    @Getter(AccessLevel.PACKAGE)
    private GameObject Dark_ALTAR;


    @Getter(AccessLevel.PACKAGE)
    private GameObject Dense_runestone;

    @Getter(AccessLevel.PACKAGE)
    private GameObject denseRunestoneNorth;

    @Getter(AccessLevel.PACKAGE)
    private boolean denseRunestoneSouthMineable;

    @Getter(AccessLevel.PACKAGE)
    private boolean denseRunestoneNorthMineable;


    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;


    @Inject
    private DenseEssenceHighlighter DenseEssenceHighlighter;


    @Inject
    private EssenceConfig config;

    @Inject
    private Notifier notifier;


    //here maybe
    @Provides
    EssenceConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(EssenceConfig.class);
    }

    @Override
    protected void startUp() throws Exception {

        overlayManager.add(DenseEssenceHighlighter);


        if (client.getGameState() == GameState.LOGGED_IN) {
            updateDenseRunestoneState();
        }
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(DenseEssenceHighlighter);

        denseRunestoneNorth = null;
        denseRunestoneSouth = null;


    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("essenceehighlighter")) {

            overlayManager.add(DenseEssenceHighlighter);

        }
    }


    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState gameState = event.getGameState();
        switch (gameState) {
            case LOADING:

                denseRunestoneNorth = null;
                denseRunestoneSouth = null;
                break;
            case CONNECTION_LOST:
            case HOPPING:
            case LOGIN_SCREEN:
                darkMage = null;
                break;
        }
    }


    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject obj = event.getGameObject();
        int id = obj.getId();

        switch (id) {
            case DENSE_RUNESTONE_SOUTH_ID:
                denseRunestoneSouth = obj;
                break;
			/*case BLOOD_ALTAR_ID:
				BLOOD_ALTAr = obj;
				break;

			case DARK_ALTAR_ID:
				Dark_ALTAR = obj;
				break;*/
            case Dense_runestone_ID:
                Dense_runestone = obj;
                break;

            case DENSE_RUNESTONE_NORTH_ID:
                denseRunestoneNorth = obj;
                break;


        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        switch (event.getGameObject().getId()) {
            case DENSE_RUNESTONE_SOUTH_ID:
                denseRunestoneSouth = null;
                break;
			/*case BLOOD_ALTAR_ID:
				BLOOD_ALTAr = null;
				break;
			case DARK_ALTAR_ID:
				Dark_ALTAR = null;*/

            case Dense_runestone_ID:
                Dense_runestone = null;

            case DENSE_RUNESTONE_NORTH_ID:
                denseRunestoneNorth = null;
                break;
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        updateDenseRunestoneState();
    }

    private void updateDenseRunestoneState() {
        //denseRunestoneSouthMineable = client.getVar(DENSE_RUNESTONE_SOUTH_DEPLETED) == 0;
        denseRunestoneSouthMineable = client.getVarbitValue(DENSE_RUNESTONE_SOUTH_DEPLETED) == 0;
        //denseRunestoneNorthMineable = client.getVar(Varbits.DENSE_RUNESTONE_NORTH_DEPLETED) == 0;
        denseRunestoneNorthMineable = client.getVarbitValue(DENSE_RUNESTONE_NORTH_DEPLETED) == 0;
    }


    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        final NPC npc = event.getNpc();
        if (npc == darkMage) {
            darkMage = null;
        }
    }
}
