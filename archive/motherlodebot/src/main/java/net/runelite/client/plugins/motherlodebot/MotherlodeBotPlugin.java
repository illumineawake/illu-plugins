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
package net.runelite.client.plugins.motherlodebot;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.WallObjectQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
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

import static net.runelite.api.ObjectID.*;
import static net.runelite.client.plugins.motherlodebot.MotherlodeBotState.*;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "Motherlode Mine Bot",
        enabledByDefault = false,
        description = "Illumine Motherlode Mine bot plugin",
        tags = {"tick"},
        type = PluginType.SKILLING
)
@Slf4j
public class MotherlodeBotPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private MotherlodeBotConfiguration config;

    @Inject
    private iUtils utils;

    @Inject
    private ConfigManager configManager;

    private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
    private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
            new ThreadPoolExecutor.DiscardPolicy());

    MotherlodeBotState state;
    WallObject oreVein;
    GameObject rockObstacle;
    MenuEntry targetMenu;
    GameObject depositBox;
    GroundObject sack;

    int timeout = 0;
    int predictedSackSize = 0;
    boolean secondCheck;
    boolean collecting;
    boolean banking;
    private static final String STUCK_MESSAGE = "I can't reach that!";

    private static final Set<Integer> MINE_SPOTS = Set.of(ORE_VEIN_26661, ORE_VEIN_26662, ORE_VEIN_26663, ORE_VEIN_26664);
    private static final Set<Integer> ROCK_OBSTACLES = Set.of(ROCKFALL, ROCKFALL_26680);
    private static final Set<Integer> MLM_ORE_TYPES = Set.of(ItemID.RUNITE_ORE, ItemID.ADAMANTITE_ORE,
            ItemID.MITHRIL_ORE, ItemID.GOLD_ORE, ItemID.COAL, ItemID.GOLDEN_NUGGET);

    private static final Set<Integer> MOTHERLODE_MAP_REGIONS = Set.of(14679, 14680, 14681, 14935, 14936, 14937, 15191, 15192, 15193);
    private static final int SE_MLM_REGEION = 14936;
    private static final WorldArea bankArea = new WorldArea(new WorldPoint(3738, 5649, 0), new WorldPoint(3762, 5675, 0));
    private static final WorldArea mineArea = new WorldArea(new WorldPoint(3764, 5633, 0), new WorldPoint(3776, 5644, 0));
    private static final WorldArea obstacleArea = new WorldArea(new WorldPoint(3760, 5638, 0), new WorldPoint(3770, 5653, 0));
    private static final WorldArea stuckArea = new WorldArea(new WorldPoint(3764, 5644, 0), new WorldPoint(3770, 5649, 0));

    LocalPoint beforeLoc = new LocalPoint(0, 0);

    @Provides
    MotherlodeBotConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MotherlodeBotConfiguration.class);
    }

    @Override
    protected void startUp() {
        collecting = false;
        banking = false;
        predictedSackSize = 0;
    }

    @Override
    protected void shutDown() {

    }

    private void mineVein() {
        oreVein = new WallObjectQuery().idEquals(MINE_SPOTS).filter(o -> mineArea.distanceTo(o.getWorldLocation()) == 0).result(client).nearestTo(client.getLocalPlayer());
        //rockObstacle = new GameObjectQuery().idEquals(ROCK_OBSTACLES).filter(o -> (mineArea.distanceTo(o.getWorldLocation()) == 0) && (client.getLocalPlayer().getWorldLocation().getX() < o.getWorldLocation().getX()) && client.getLocalPlayer().getWorldLocation().getY() >= o.getWorldLocation().getY()).result(client).nearestTo(client.getLocalPlayer());

        if (oreVein != null) {
            //targetObject = oreVein;
            targetMenu = new LegacyMenuEntry("", "", oreVein.getId(), 3, oreVein.getLocalLocation().getSceneX(), oreVein.getLocalLocation().getSceneY(), false);
            mouse.clickRandomPointCenter(-100, 100);
        } else {
            log.info("Vein not found");
        }
    }

    private GameObject getNearestObstacle() {
        if (state == WALK_TO_MINE) {
            if (stuckArea.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0) {
                log.info("trying to mine the 'STUCK' obstacle");
                GameObject rockObstacleToMineArea = new GameObjectQuery().idEquals(ROCK_OBSTACLES).filter(o -> (stuckArea.distanceTo(o.getWorldLocation()) == 0)).result(client).nearestTo(client.getLocalPlayer());
                if (rockObstacleToMineArea != null) {
                    return rockObstacleToMineArea;
                }
            }
            GameObject rockObstacleToMineArea = new GameObjectQuery().idEquals(ROCK_OBSTACLES).filter(o -> (obstacleArea.distanceTo(o.getWorldLocation()) == 0) && (client.getLocalPlayer().getWorldLocation().getX() <= o.getWorldLocation().getX()) && client.getLocalPlayer().getWorldLocation().getY() >= o.getWorldLocation().getY()).result(client).nearestTo(client.getLocalPlayer());
            return rockObstacleToMineArea;
        }
        if (state == WALK_TO_BANK) {
            GameObject rockObstacleToBankArea = new GameObjectQuery().idEquals(ROCK_OBSTACLES).filter(o -> (obstacleArea.distanceTo(o.getWorldLocation()) == 0) && (client.getLocalPlayer().getWorldLocation().getX() >= o.getWorldLocation().getX()) && client.getLocalPlayer().getWorldLocation().getY() <= o.getWorldLocation().getY()).result(client).nearestTo(client.getLocalPlayer());
            return rockObstacleToBankArea;
        }
        return null;
    }

    private void mineObstacle(GameObject obstacle) {
        targetMenu = new LegacyMenuEntry("", "", obstacle.getId(), 3, obstacle.getSceneMinLocation().getX(), obstacle.getSceneMinLocation().getY(), false);
        mouse.clickRandomPointCenter(-100, 100);
    }

    private void depositHopper() {
        GameObject hopper = object.findNearestGameObject(26674);
        if (hopper != null) {
            predictedSackSize = getSackSize() + (28 - utils.getInventorySpace());
            log.info("predicted sack size: " + predictedSackSize);
            targetMenu = new LegacyMenuEntry("", "", hopper.getId(), 3, hopper.getSceneMinLocation().getX(), hopper.getSceneMinLocation().getY(), false);
            mouse.clickRandomPointCenter(-100, 100);
        }
    }

    private int getSackSize() {
        return client.getVar(Varbits.SACK_NUMBER);
    }

    private void collectOres() {
        collecting = true;
        if (getSackSize() == 0 && inventory.inventoryEmpty()) {
            log.info("uninitialising collect values");
            collecting = false;
            banking = false;
            predictedSackSize = 0;
            return;
        }
        if (bankArea.distanceTo(client.getLocalPlayer().getWorldLocation()) != 0) {
            log.info("need to collect ores but we're not in the bank area");
            return;
        }
        if (inventory.inventoryContains(ItemID.PAYDIRT)) {
            if (getSackSize() < 82) {
                depositHopper();
                return;
            } else {
                log.info("Sack is full and pay dirt in inventory. Need to drop paydirt");
                utils.dropAll(List.of(ItemID.PAYDIRT));
                return;
            }
        }
        if (inventory.inventoryFull() || inventory.inventoryContains(MLM_ORE_TYPES)) {
            banking = true;
            if (bank.isDepositBoxOpen()) {
                bank.depositAll();
                timeout = 1;
                return;
            } else {
                depositBox = object.findNearestGameObject(ObjectID.BANK_DEPOSIT_BOX_25937);
                if (depositBox != null) {
                    targetMenu = new LegacyMenuEntry("", "", depositBox.getId(), 3, depositBox.getSceneMinLocation().getX(), depositBox.getSceneMinLocation().getY(), false);
                    mouse.clickRandomPointCenter(-100, 100);
                    return;
                } else {
                    log.info("depositBox is null");
                }
            }
        }
        if (!banking && getSackSize() != predictedSackSize && (object.getGameObjects(ObjectID.BROKEN_STRUT) != null)) {
            log.info("Repair strut");
            //repairStrut();
        }
        if (!inventory.inventoryFull() && getSackSize() > 0) {
            sack = utils.findNearestGroundObject(26688);
            if (sack != null) {
                targetMenu = new LegacyMenuEntry("", "", sack.getId(), 3, sack.getLocalLocation().getSceneX(), sack.getLocalLocation().getSceneY(), false);
                mouse.clickRandomPointCenter(-100, 100);
                return;
            } else {
                log.info("sack is null");
            }
        }
    }

    public MotherlodeBotState getState() {
        if (timeout > 0) {
            return TIMEOUT;
        }
        if (client.getLocalPlayer().getWorldLocation().getRegionID() != SE_MLM_REGEION) {
            return OUT_OF_AREA;
        }
        if (playerUtils.isMoving(beforeLoc) || utils.isAnimating()) {
            timeout = 2 + calc.getRandomIntBetweenRange(0, 3);
            secondCheck = false;
            return INTERACTING;
        }
        if (state == COLLECT_ORES || collecting || getSackSize() >= 78 || predictedSackSize >= 78) {
            log.info("COLLECT_ORES VAL: " + COLLECT_ORES + " collecting val: " + collecting + " getSackSize val: " + getSackSize() + " predictedSackSize val: " + predictedSackSize);
            if (getSackSize() == 0 & inventory.inventoryEmpty()) {
                predictedSackSize = 0;
                collecting = false;
                return WALK_TO_MINE;
            } else {
                return COLLECT_ORES;
            }
        }
        if (inventory.inventoryFull()) {
            if (bankArea.distanceTo(client.getLocalPlayer().getWorldLocation()) != 0) {
                return WALK_TO_BANK;
            } else {
                return DEPOSIT_HOPPER;
            }
        }
        if (!inventory.inventoryFull() && (mineArea.distanceTo(client.getLocalPlayer().getWorldLocation()) != 0)) {
            if (getSackSize() < 68) {
                return WALK_TO_MINE;
            }
            return WAITING;
        }
        if (!utils.isAnimating() && !playerUtils.isMoving(beforeLoc) && !inventory.inventoryFull() && mineArea.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0) {
            if (!secondCheck) {
                secondCheck = true;
                return WAITING;
            }
            secondCheck = false;
            return FIND_OBJECT;
        }
        return WAITING; //need to determine an appropriate default
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (client != null && client.getLocalPlayer() != null && client.getGameState() == GameState.LOGGED_IN && !iterating) {
            if (client.getLocalPlayer().getWorldLocation().getRegionID() != SE_MLM_REGEION) {
                return;
            }
            playerUtils.handleRun(40, 20);
            state = getState();
            beforeLoc = client.getLocalPlayer().getLocalLocation();
            log.info(state.toString() + " Predicted sack size: " + predictedSackSize);
            switch (state) {
                case TIMEOUT:
                    timeout--;
                    return;
                case FIND_OBJECT:
                    mineVein();
                    return;
                case INTERACTING:
                    utils.sleep(25, 100);
                    return;
                case DEPOSIT_HOPPER:
                    depositHopper();
                    return;
                case COLLECT_ORES:
                    collectOres();
                    return;
                case WALK_TO_BANK:
                    rockObstacle = getNearestObstacle();
                    if (rockObstacle != null) {
                        mineObstacle(rockObstacle);
                    } else {
                        depositHopper();
                    }
                    return;
                case WALK_TO_MINE:
                    rockObstacle = getNearestObstacle();
                    if (rockObstacle != null) {
                        mineObstacle(rockObstacle);
                    } else {
                        mineVein();
                    }
                    return;
            }
        } else {
            return;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (targetMenu == null) {
            return;
        }
        if (utils.getRandomEvent()) //for random events
        {
            log.info("Motherlode Mine not overriding click due to random event");
            return;
        }
        event.setMenuEntry(targetMenu);
        timeout = 2;
        targetMenu = null;
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event) {
        if (oreVein == null || event.getWallObject() != oreVein) {
            return;
        } else {
            if (client.getLocalDestinationLocation() != null) {
                mineVein(); //This is a failsafe, Player can get stuck with a destination on object despawn and be "forever moving".
            }
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (obstacleArea.distanceTo(event.getTile().getWorldLocation()) != 0 || !ROCK_OBSTACLES.contains(event.getGameObject().getId())) {
            return;
        }
        rockObstacle = getNearestObstacle();
        if (rockObstacle != null) {
            mineObstacle(rockObstacle);
        }
        if (rockObstacle != null) {
            log.info("Rock Obstacle spawned: " + event.getGameObject().getId() + " " + event.getGameObject().getWorldLocation() + " object = " + rockObstacle.toString());
        } else {
            log.info("Rock Obstacle spawned: " + event.getGameObject().getId() + " " + event.getGameObject().getWorldLocation() + " our object is NULL");
        }
    }

	/*@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if(event.getType() != ChatMessageType.ENGINE && event.getMessage() != STUCK_MESSAGE)
		{
			return;
		}
		if (stuckArea.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0)
		{
			utils.sendGameMessage("We are stuck. Trying to get unstuck");
			state = STUCK;
			return;
		}
		else
			utils.sendGameMessage("ERROR: We are stuck somewhere outside of the unstuck area.");
	}*/
}