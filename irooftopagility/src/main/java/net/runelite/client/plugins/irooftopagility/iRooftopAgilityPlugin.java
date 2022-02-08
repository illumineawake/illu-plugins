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
package net.runelite.client.plugins.irooftopagility;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.runelite.client.plugins.irooftopagility.iRooftopAgilityState.*;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iRooftop Agility",
        enabledByDefault = false,
        description = "Illumine auto rooftop agility plugin",
        tags = {"agility"}
)
@Slf4j
public class iRooftopAgilityPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private iUtils utils;

    @Inject
    private MouseUtils mouse;

    @Inject
    private PlayerUtils playerUtils;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    @Inject
    private ObjectUtils object;

    @Inject
    private BankUtils bank;

    @Inject
    private iRooftopAgilityConfig config;

    @Inject
    PluginManager pluginManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    iRooftopAgilityOverlay overlay;

    @Inject
    ItemManager itemManager;

    @Inject
    private ReflectBreakHandler chinBreakHandler;

    Player player;
    iRooftopAgilityState state;
    Instant botTimer;
    TileItem markOfGrace;
    Tile markOfGraceTile;
    LegacyMenuEntry targetMenu;
    LocalPoint beforeLoc = new LocalPoint(0, 0); //initiate to mitigate npe
    WidgetItem alchItem;
    Portals priffPortal;
    Set<Integer> inventoryItems = new HashSet<>();
    GameObject spawnedPortal;

    private final Set<Integer> REGION_IDS = Set.of(9781, 12853, 12597, 12084, 12339, 12338, 10806, 10297, 10553, 13358, 13878, 10547, 13105, 9012, 9013, 12895, 13151, 13152, 11050, 10794);
    WorldPoint CAMELOT_TELE_LOC = new WorldPoint(2705, 3463, 0);
    Set<Integer> AIR_STAFFS = Set.of(ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.DUST_BATTLESTAFF, ItemID.MIST_BATTLESTAFF,
            ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF, ItemID.MYSTIC_DUST_STAFF, ItemID.MYSTIC_SMOKE_STAFF, ItemID.MYSTIC_MIST_STAFF);

    private final Set<Integer> SUMMER_PIE_IDS = Set.of(
            ItemID.SUMMER_PIE,
            ItemID.HALF_A_SUMMER_PIE
    );

    int timeout;
    int alchTimeout;
    int mogSpawnCount;
    int mogCollectCount;
    int mogInventoryCount = -1;
    int marksPerHour;
    long sleepLength;
    boolean startAgility;
    boolean restockBank;
    boolean setHighAlch;
    boolean alchClick;

    @Override
    protected void startUp() {
        chinBreakHandler.registerPlugin(this);
    }

    @Override
    protected void shutDown() {
        resetVals();
        chinBreakHandler.unregisterPlugin(this);
    }

    @Provides
    iRooftopAgilityConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iRooftopAgilityConfig.class);
    }

    private void resetVals() {
        overlayManager.remove(overlay);
        chinBreakHandler.stopPlugin(this);
        markOfGraceTile = null;
        markOfGrace = null;
        startAgility = false;
        botTimer = null;
        mogSpawnCount = 0;
        mogCollectCount = 0;
        mogInventoryCount = -1;
        marksPerHour = 0;
        alchTimeout = 0;
        inventoryItems.clear();
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iRooftopAgility")) {
            return;
        }
        log.info("button {} pressed!", configButtonClicked.getKey());
        switch (configButtonClicked.getKey()) {
            case "startButton":
                if (!startAgility) {
                    startAgility = true;
                    chinBreakHandler.startPlugin(this);
                    state = null;
                    targetMenu = null;
                    botTimer = Instant.now();
                    restockBank = config.bankRestock();
                    inventoryItems.addAll(Set.of(ItemID.NATURE_RUNE, ItemID.MARK_OF_GRACE));
                    if (config.alchItemID() != 0) {
                        inventoryItems.addAll(Set.of(config.alchItemID(), (config.alchItemID() + 1)));
                    }
                    overlayManager.add(overlay);
                } else {
                    resetVals();
                }
                break;
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("iRooftopAgility")) {
            switch (event.getKey()) {
                case "bankRestock":
                    restockBank = config.bankRestock();
                    break;
                case "alchItemID":
                    inventoryItems.clear();
                    inventoryItems.addAll(Set.of(ItemID.NATURE_RUNE, ItemID.MARK_OF_GRACE, config.alchItemID(), (config.alchItemID() + 1)));
                    break;
            }
        }
    }

    private long sleepDelay() {
        sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return sleepLength;
    }

    private int tickDelay() {
        int tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        log.debug("tick delay for {} ticks", tickLength);
        return tickLength;
    }

    public long getMarksPH() {
        Duration timeSinceStart = Duration.between(botTimer, Instant.now());
        if (!timeSinceStart.isZero()) {
            return (int) ((double) mogCollectCount * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
        }
        return 0;
    }

    private boolean shouldEatSummerPie() {
        return config.boostWithPie() && 
                (client.getBoostedSkillLevel(Skill.AGILITY) < config.pieLevel()) &&
                inventory.containsItem(SUMMER_PIE_IDS);
    }
    
    private boolean shouldCastTeleport() {
        return config.camelotTeleport() && client.getBoostedSkillLevel(Skill.MAGIC) >= 45 &&
                CAMELOT_TELE_LOC.distanceTo(client.getLocalPlayer().getWorldLocation()) <= 3 &&
                (inventory.containsItem(ItemID.LAW_RUNE) && inventory.containsStackAmount(ItemID.AIR_RUNE, 5) ||
                        inventory.containsItem(ItemID.LAW_RUNE) && playerUtils.isItemEquipped(AIR_STAFFS));
    }

    private boolean shouldAlch() {
        return config.highAlch() &&
                config.alchItemID() != 0 &&
                client.getBoostedSkillLevel(Skill.MAGIC) >= 55;
    }

    private void highAlchItem() {
        if (!setHighAlch) {
            targetMenu = new LegacyMenuEntry("Cast", "<col=00ff00>High Level Alchemy</col>", 0,
                    MenuAction.WIDGET_TYPE_2.getId(), -1, 14286887, false);
            Widget spellWidget = client.getWidget(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY);
            if (spellWidget != null) {
                menu.setEntry(targetMenu);
                mouse.delayMouseClick(spellWidget.getBounds(), sleepDelay());
            } else {
                menu.setEntry(targetMenu);
                mouse.delayClickRandomPointCenter(-200, 200, sleepDelay());
            }
            setHighAlch = true;
        } else {
            alchItem = inventory.getWidgetItem(List.of(config.alchItemID(), (config.alchItemID() + 1)));
            targetMenu = new LegacyMenuEntry("Cast", "<col=00ff00>High Level Alchemy</col><col=ffffff> ->",
                    alchItem.getId(),
                    MenuAction.ITEM_USE_ON_WIDGET.getId(),
                    alchItem.getIndex(), 9764864,
                    false);
            menu.setEntry(targetMenu);
            mouse.delayMouseClick(alchItem.getCanvasBounds(), sleepDelay());
            alchTimeout = 4 + tickDelay();
        }
    }

    private void eatSummerPie() {
        WidgetItem summerPieItem = inventory.getWidgetItem(SUMMER_PIE_IDS);
        targetMenu = new LegacyMenuEntry("", "", summerPieItem.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), summerPieItem.getIndex(),
                WidgetInfo.INVENTORY.getId(), false);
        menu.setEntry(targetMenu);
        mouse.delayMouseClick(summerPieItem.getCanvasBounds(), sleepDelay());
    }

    private boolean shouldRestock() {
        if (!config.highAlch() ||
                config.alchItemID() == 0 ||
                !restockBank ||
                client.getBoostedSkillLevel(Skill.MAGIC) < 55) {
            return false;
        }
        return !inventory.containsItem(ItemID.NATURE_RUNE) || !inventory.containsItem(Set.of(config.alchItemID(), (config.alchItemID() + 1)));
    }

    private void restockItems() {
        if (bank.isOpen()) {
            //if (client.getVarbitValue(Varbits.BANK_NOTE_FLAG.getId()) != 1)
            if (client.getVarbitValue(3958) != 1) {
                targetMenu = new LegacyMenuEntry("Note", "", 1, MenuAction.CC_OP.getId(), -1, 786455, false);
                menu.setEntry(targetMenu);
                mouse.delayClickRandomPointCenter(-200, 200, sleepDelay());
                return;
            }
            if ((!bank.contains(ItemID.NATURE_RUNE, 1) && !inventory.containsItem(ItemID.NATURE_RUNE)) ||
                    (!bank.contains(config.alchItemID(), 1) && !inventory.containsItem(Set.of(config.alchItemID(), config.alchItemID() + 1)))) {
                log.debug("out of alching items");
                restockBank = false;
                return;
            } else {
                WidgetItem food = inventory.getWidgetItemMenu(itemManager, "Eat", 33);
                if (food != null) {
                    inventoryItems.add(food.getId());
                }
                if (inventory.containsExcept(inventoryItems)) {
                    log.debug("depositing items");
                    bank.depositAllExcept(inventoryItems);
                    timeout = tickDelay();
                    return;
                }
                if (!inventory.isFull()) {
                    if (!inventory.containsItem(ItemID.NATURE_RUNE)) {
                        log.debug("withdrawing Nature runes");
                        bank.withdrawAllItem(ItemID.NATURE_RUNE);
                        return;
                    }
                    if (!inventory.containsItem(Set.of(config.alchItemID(), config.alchItemID() + 1))) {
                        log.debug("withdrawing Config Alch Item");
                        bank.withdrawAllItem(config.alchItemID());
                        return;
                    }
                } else {
                    log.debug("inventory is full but trying to withdraw items");
                }
            }
        } else {
            GameObject bankBooth = object.findNearestGameObject(getCurrentObstacle().getBankID());
            if (bankBooth != null) {
                targetMenu = new LegacyMenuEntry("", "", bankBooth.getId(),
                        MenuAction.GAME_OBJECT_SECOND_OPTION.getId(), bankBooth.getSceneMinLocation().getX(),
                        bankBooth.getSceneMinLocation().getY(), false);
                menu.setEntry(targetMenu);
                mouse.delayMouseClick(bankBooth.getConvexHull().getBounds(), sleepDelay());
                timeout = tickDelay();
            }
        }
    }

    private iRooftopAgilityObstacles getCurrentObstacle() {
        return iRooftopAgilityObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
    }

    private void findObstacle() {
        iRooftopAgilityObstacles obstacle = getCurrentObstacle();
        if (obstacle != null) {
            log.debug(String.valueOf(obstacle.getObstacleId()));
            if (obstacle.getObstacleType() == iRooftopAgilityObstacleType.DECORATION) {
                DecorativeObject decObstacle = object.findNearestDecorObject(obstacle.getObstacleId());
                if (decObstacle != null) {
                    targetMenu = new LegacyMenuEntry("", "", decObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), decObstacle.getLocalLocation().getSceneX(), decObstacle.getLocalLocation().getSceneY(), false);
                    menu.setEntry(targetMenu);
                    Rectangle clickPoint = (decObstacle.getConvexHull() != null) ? decObstacle.getConvexHull().getBounds() :
                            new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
                    mouse.delayMouseClick(clickPoint, sleepDelay());
                    return;
                }
            }
            if (obstacle.getObstacleType() == iRooftopAgilityObstacleType.GROUND_OBJECT) {
                GroundObject groundObstacle = object.findNearestGroundObject(obstacle.getObstacleId());
                if (groundObstacle != null) {
                    targetMenu = new LegacyMenuEntry("", "", groundObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), groundObstacle.getLocalLocation().getSceneX(), groundObstacle.getLocalLocation().getSceneY(), false);
                    menu.setEntry(targetMenu);
                    Rectangle clickPoint = (groundObstacle.getConvexHull() != null) ? groundObstacle.getConvexHull().getBounds() :
                            new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
                    mouse.delayMouseClick(clickPoint, sleepDelay());
                    return;
                }
            }
            GameObject objObstacle = object.findNearestGameObject(obstacle.getObstacleId());
            if (objObstacle != null) {
                targetMenu = new LegacyMenuEntry("", "", objObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), objObstacle.getSceneMinLocation().getX(), objObstacle.getSceneMinLocation().getY(), false);
                menu.setEntry(targetMenu);
                Rectangle clickPoint = (objObstacle.getConvexHull() != null) ? objObstacle.getConvexHull().getBounds() :
                        new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
                mouse.delayMouseClick(clickPoint, sleepDelay());
                return;
            }
        } else {
            log.debug("Not in obstacle area");
        }
    }

    private Portals getCurrentPortal() {
        //We provide the current varbit value and the enum returns the correlating Portal. i.e. we now have access to the ID of the active portal
        return Portals.getPortal(client.getVarbitValue(9298));
    }

    public iRooftopAgilityState getState() {
        if (timeout > 0) {
            if (alchTimeout <= 0 && shouldAlch() && inventory.containsItem(ItemID.NATURE_RUNE) &&
                    inventory.containsItem(Set.of(config.alchItemID(), (config.alchItemID() + 1)))) {
                timeout--;
                return HIGH_ALCH;
            }
            if (alchClick) {
                iRooftopAgilityObstacles currentObstacle = getCurrentObstacle();
                if (currentObstacle != null) {
                    if (markOfGrace != null && markOfGraceTile != null && config.mogPickup() && (!inventory.isFull() || inventory.containsItem(ItemID.MARK_OF_GRACE))) {
                        if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0) {
                            if (markOfGraceTile.getGroundItems().contains(markOfGrace)) //failsafe sometimes onItemDespawned doesn't capture mog despawn
                            {
                                if (config.course().name().equals("ARDOUGNE") && config.alchMogStack() > 1) {
                                    if (markOfGrace.getQuantity() >= config.alchMogStack()) {
                                        return MARK_OF_GRACE;
                                    }
                                } else {
                                    return MARK_OF_GRACE;
                                }
                            } else {
                                log.info("Mark of grace not found and markOfGrace was not null");
                                markOfGrace = null;
                            }
                        }
                    }
                    if (shouldEatSummerPie()) {
                        timeout--;
                        return EAT_SUMMER_PIE;
                    }
                    if (currentObstacle.getBankID() == 0 || !shouldRestock()) {
                        timeout--;
                        return (shouldCastTeleport()) ? CAST_CAMELOT_TELEPORT : FIND_OBSTACLE;
                    }
                }
            }
            return TIMEOUT;
        }
        if (shouldCastTeleport()) {
            return CAST_CAMELOT_TELEPORT;
        }
        if (playerUtils.isMoving(beforeLoc)) {
            if (alchTimeout <= 0 && shouldAlch() && (inventory.containsItem(ItemID.NATURE_RUNE) &&
                    inventory.containsItem(Set.of(config.alchItemID(), (config.alchItemID() + 1))))) {
                timeout = tickDelay();
                return HIGH_ALCH;
            }
            timeout = tickDelay();
            return MOVING;
        }
        if (shouldEatSummerPie()) {
            return EAT_SUMMER_PIE;
        }
        iRooftopAgilityObstacles currentObstacle = iRooftopAgilityObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
        if (currentObstacle == null) {
            timeout = tickDelay();
            return MOVING;
        }
        if (currentObstacle.getBankID() > 0 && shouldRestock()) {
            if (object.findNearestGameObject(currentObstacle.getBankID()) != null) {
                return RESTOCK_ITEMS;
            } else {
                log.debug("should restock but couldn't find bank");
            }
        }
        if (config.pickupCoins()) {
            TileItem coins = object.getGroundItem(ItemID.COINS_995);
            if (coins != null && currentObstacle.getLocation().distanceTo(coins.getTile().getWorldLocation()) == 0 &&
                    (!inventory.isFull() || inventory.containsItem(ItemID.COINS_995))) {
                    return COINS;
            }
        }
        if (markOfGrace != null && markOfGraceTile != null && config.mogPickup() && (!inventory.isFull() || inventory.containsItem(ItemID.MARK_OF_GRACE))) {
            if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0) {
                if (markOfGraceTile.getGroundItems().contains(markOfGrace)) //failsafe sometimes onItemDespawned doesn't capture mog despawn
                {
                    if (config.course().name().equals("ARDOUGNE") && config.mogStack() > 1) {
                        if (markOfGrace.getQuantity() >= config.mogStack()) {
                            return MARK_OF_GRACE;
                        }
                    } else {
                        return MARK_OF_GRACE;
                    }
                } else {
                    log.info("Mark of grace not found and markOfGrace was not null");
                    markOfGrace = null;
                }
            }
        }
        if (client.getVarbitValue(9298) != 0) {
            log.info("Portal spawned");
            priffPortal = getCurrentPortal();
            spawnedPortal = object.findNearestGameObject(priffPortal.getPortalID());
            if (spawnedPortal != null) {
                if (currentObstacle.getLocation().distanceTo(spawnedPortal.getWorldLocation()) == 0) {
                    return PRIFF_PORTAL;
                }
            }
        }
        if (chinBreakHandler.shouldBreak(this)) {
            return HANDLE_BREAK;
        }
        if (!playerUtils.isMoving(beforeLoc)) {
            return FIND_OBSTACLE;
        }
        return ANIMATING;
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (!startAgility || chinBreakHandler.isBreakActive(this)) {
            return;
        }
        player = client.getLocalPlayer();
        if (alchTimeout > 0) {
            alchTimeout--;
        }
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN && client.getBoostedSkillLevel(Skill.HITPOINTS) > config.lowHP()) {
            if (!client.isResized()) {
                utils.sendGameMessage("illu - client must be set to resizable");
                startAgility = false;
                return;
            }
            if (!REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID())) {
                log.debug("not in agility course region");
                return;
            }
            marksPerHour = (int) getMarksPH();
            playerUtils.handleRun(40, 20);
            state = getState();
            beforeLoc = client.getLocalPlayer().getLocalLocation();
            switch (state) {
                case TIMEOUT:
                    timeout--;
                    break;
                case COINS:
                    timeout = tickDelay();
                    pickCoins();
                    break;
                case MARK_OF_GRACE:
                    log.debug("Picking up mark of grace");
                    targetMenu = new LegacyMenuEntry("", "", ItemID.MARK_OF_GRACE, 20, markOfGraceTile.getSceneLocation().getX(), markOfGraceTile.getSceneLocation().getY(), false);
                    menu.setEntry(targetMenu);
                    mouse.delayClickRandomPointCenter(-200, 200, sleepDelay());
                    break;
                case FIND_OBSTACLE:
                    findObstacle();
                    break;
                case HIGH_ALCH:
                    highAlchItem();
                    break;
                case RESTOCK_ITEMS:
                    restockItems();
                    break;
                case MOVING:
                    break;
                case CAST_CAMELOT_TELEPORT:
                    targetMenu = new LegacyMenuEntry("", "", 2, MenuAction.CC_OP.getId(), -1,
                            WidgetInfo.SPELL_CAMELOT_TELEPORT.getId(), false);
                    Widget spellWidget = client.getWidget(WidgetInfo.SPELL_CAMELOT_TELEPORT);
                    if (spellWidget != null) {
                        menu.setEntry(targetMenu);
                        mouse.delayMouseClick(spellWidget.getBounds(), sleepDelay());
                    } else {
                        menu.setEntry(targetMenu);
                        mouse.delayClickRandomPointCenter(-200, 200, sleepDelay());
                    }
                    timeout = 2 + tickDelay();
                    break;
                case PRIFF_PORTAL:
                    log.info("Using Priff portal");
                    targetMenu = new LegacyMenuEntry("", "", spawnedPortal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                            spawnedPortal.getSceneMinLocation().getX(), spawnedPortal.getSceneMinLocation().getY(), false);
                    menu.setEntry(targetMenu);
                    mouse.delayMouseClick(spawnedPortal.getConvexHull().getBounds(), sleepDelay());
                    break;
                case HANDLE_BREAK:
                    chinBreakHandler.startBreak(this);
                    timeout = 10;
                    break;
                case EAT_SUMMER_PIE:
                    if (!inventory.containsItem(SUMMER_PIE_IDS)) {
                        log.info("Out of Summer Pies");
                        state = OUT_OF_SUMMER_PIES;
                        startAgility = false;
                        return;
                    }
                    eatSummerPie();
                    break;
            }
        } else {
            log.debug("client/ player is null or bot isn't started");
            return;
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN && startAgility) {
            markOfGraceTile = null;
            markOfGrace = null;
            state = TIMEOUT;
            timeout = 2;
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (!startAgility) {
            return;
        }
        if (targetMenu != null) {
            log.debug("MenuEntry string event: " + targetMenu.toString());
            alchClick = (targetMenu.getOption().equals("Cast"));
            timeout = tickDelay();
        }
    }
    /*@Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID())) {
            return;
        }

        if (client.getVarbitValue(9298) != 0) {
            log.info("Portal spawned");
            priffPortal = getCurrentPortal();
        }
    }

    @Subscribe
    private void onGameObjectDespawned(GameObjectDespawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID())) {
            return;
        }

        if (Portals.values() PORTAL_IDS.contains(event.getGameObject().getId()))
        {
            log.info("Portal spawned");
            priffPortal = null;
        }
    }*/

    @Subscribe
    private void onItemSpawned(ItemSpawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) ||
                !config.mogPickup()) {
            return;
        }

        TileItem item = event.getItem();
        Tile tile = event.getTile();

        if (item.getId() == ItemID.MARK_OF_GRACE) {
            log.debug("Mark of grace spawned");
            markOfGrace = item;
            markOfGraceTile = tile;
            WidgetItem mogInventory = inventory.getWidgetItem(ItemID.MARK_OF_GRACE);
            mogInventoryCount = (mogInventory != null) ? mogInventory.getQuantity() : 0;
            mogSpawnCount++;
        }
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) || !config.mogPickup()) {
            return;
        }

        TileItem item = event.getItem();

        if (item.getId() == ItemID.MARK_OF_GRACE) {
            log.debug("Mark of grace despawned");
            markOfGrace = null;
            markOfGraceTile = null;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() != 93 || mogInventoryCount == -1) {
            return;
        }
        if (event.getItemContainer().count(ItemID.MARK_OF_GRACE) > mogInventoryCount) {
            mogCollectCount++;
            mogInventoryCount = -1;
        }
    }

    private void pickCoins() {
        TileItem coins = object.getGroundItem(ItemID.COINS_995);
        if (coins != null) {
            lootItem(coins);
        }
    }

    private void lootItem(TileItem itemToLoot) {
        menu.setEntry(new LegacyMenuEntry("", "", itemToLoot.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), itemToLoot.getTile().getSceneLocation().getX(), itemToLoot.getTile().getSceneLocation().getY(), false));
        mouse.delayMouseClick(itemToLoot.getTile().getItemLayer().getCanvasTilePoly().getBounds(), sleepDelay());
    }
}
