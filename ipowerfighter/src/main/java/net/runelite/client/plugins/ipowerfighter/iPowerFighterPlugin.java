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
package net.runelite.client.plugins.ipowerfighter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.plugins.iutils.util.LegacyInventoryAssistant;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.runelite.client.plugins.iutils.iUtils.iterating;
import static net.runelite.client.plugins.iutils.iUtils.sleep;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iPower Fighter",
        enabledByDefault = false,
        description = "Illumine - Power Fighter plugin",
        tags = {"illumine", "combat", "ranged", "magic", "bot"}
)
@Slf4j
public class iPowerFighterPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private iPowerFighterConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private iPowerFighterOverlay overlay;

    @Inject
    private iUtils utils;

    @Inject
    private MouseUtils mouse;

    @Inject
    private PlayerUtils playerUtils;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private InterfaceUtils interfaceUtils;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    @Inject
    private NPCUtils npc;

    @Inject
    private WalkUtils walk;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ExecutorService executorService;

    @Inject
    private ReflectBreakHandler chinBreakHandler;

    @Inject
    private LegacyInventoryAssistant inventoryAssistant;

    NPC currentNPC;
    WorldPoint deathLocation;
    List<TileItem> loot = new ArrayList<>();
    List<TileItem> ammoLoot = new ArrayList<>();
    List<String> lootableItems = new ArrayList<>();
    Set<String> alchableItems = new HashSet<>();
    Set<Integer> alchBlacklist = Set.of(ItemID.NATURE_RUNE, ItemID.FIRE_RUNE, ItemID.COINS_995, ItemID.RUNE_POUCH, ItemID.HERB_SACK, ItemID.OPEN_HERB_SACK, ItemID.XERICS_TALISMAN, ItemID.HOLY_WRENCH); //Temp fix until isTradeable is fixed
    List<Item> alchLoot = new ArrayList<>();
    LegacyMenuEntry targetMenu;
    Instant botTimer;
    Instant newLoot;
    Player player;
    iPowerFighterState state;
    Instant lootTimer;
    LocalPoint beforeLoc = new LocalPoint(0, 0);
    WorldPoint startLoc;
    int itemValue;

    int highAlchCost;
    boolean startBot;
    boolean menuFight;
    String npcName;
    boolean slayerCompleted;
    long sleepLength;
    int tickLength;
    int timeout;
    int nextAmmoLootTime;
    int nextItemLootTime;
    int killCount;


    String SLAYER_MSG = "return to a Slayer master";
    String SLAYER_BOOST_MSG = "You'll be eligible to earn reward points if you complete tasks";
    Set<Integer> BONE_BLACKLIST = Set.of(ItemID.CURVED_BONE, ItemID.LONG_BONE);
    Set<Integer> BRACELETS = Set.of(ItemID.BRACELET_OF_SLAUGHTER, ItemID.EXPEDITIOUS_BRACELET);

    @Provides
    iPowerFighterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iPowerFighterConfig.class);
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

    private void resetVals() {
        log.debug("stopping power fighter plugin");
        overlayManager.remove(overlay);
        menuFight = false;
        chinBreakHandler.stopPlugin(this);
        startBot = false;
        botTimer = null;
        newLoot = null;
        lootTimer = null;
        loot.clear();
        ammoLoot.clear();
        lootableItems.clear();
        alchLoot.clear();
        currentNPC = null;
        state = null;
    }

    private void start() {
        log.debug("starting template plugin");
        if (client == null || client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) {
            log.info("startup failed, log in before starting");
            return;
        }
        startBot = true;
        chinBreakHandler.startPlugin(this);
        timeout = 0;
        killCount = 0;
        slayerCompleted = false;
        state = null;
        targetMenu = null;
        botTimer = Instant.now();
        overlayManager.add(overlay);
        updateConfigValues();
        if (config.alchItems()) {
            highAlchCost = utils.getItemPrice(ItemID.NATURE_RUNE, true) + (utils.getItemPrice(ItemID.FIRE_RUNE, true) * 5);
        }
        startLoc = client.getLocalPlayer().getWorldLocation();
        if (config.safeSpot()) {
            utils.sendGameMessage("Safe spot set: " + startLoc.toString());
        }
        beforeLoc = client.getLocalPlayer().getLocalLocation();
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iPowerFighter")) {
            return;
        }
        if (configButtonClicked.getKey().equals("startButton")) {
            if (!startBot) {
                start();
            } else {
                resetVals();
            }
        }
    }

    private void updateConfigValues() {
        alchableItems.clear();
        if (config.alchItems() && config.alchByName() && !config.alchNames().equals("0") && !config.alchNames().equals("")) {
            alchableItems.addAll(Stream.of(config.alchNames()
                    .toLowerCase()
                    .split(",", -1))
                    .map(String::trim)
                    .collect(Collectors.toList()));
            log.debug("alchable items list: {}", alchableItems.toString());
        }
        String[] values = config.lootItemNames().toLowerCase().split("\\s*,\\s*");
        if (config.lootItems() && !config.lootItemNames().isBlank()) {
            lootableItems.clear();
            lootableItems.addAll(Arrays.asList(values));
            log.debug("Lootable items are: {}", lootableItems.toString());
        }
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

    private TileItem getNearestTileItem(List<TileItem> tileItems) {
        int currentDistance;
        TileItem closestTileItem = tileItems.get(0);
        int closestDistance = closestTileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
        for (TileItem tileItem : tileItems) {
            currentDistance = tileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
            if (currentDistance < closestDistance) {
                closestTileItem = tileItem;
                closestDistance = currentDistance;
            }
        }
        return closestTileItem;
    }

    private void lootItem(List<TileItem> itemList) {
        TileItem lootItem = getNearestTileItem(itemList);
        if (lootItem != null) {
            targetMenu = new LegacyMenuEntry("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(),
                    lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY(), false);
            menu.setEntry(targetMenu);
            mouse.delayMouseClick(lootItem.getTile().getItemLayer().getCanvasTilePoly().getBounds(), sleepDelay());
        }
    }

    private boolean lootableItem(TileItem item) {
        String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();

        int itemTotalValue = utils.getItemPrice(item.getId(), true) * item.getQuantity();

        return config.lootItems() &&
                ((config.lootNPCOnly() && item.getTile().getWorldLocation().equals(deathLocation)) ||
                        (!config.lootNPCOnly() && item.getTile().getWorldLocation().distanceTo(startLoc) < config.lootRadius())) &&
                ((config.lootValue() && itemTotalValue > config.minTotalValue()) ||
                        lootableItems.stream().anyMatch(itemName.toLowerCase()::contains) ||
                        config.buryBones() && itemName.contains("bones") ||
                        config.scatterAshes() && itemName.contains("ashes") ||
                        config.lootClueScrolls() && itemName.contains("scroll"));
    }

    private boolean canAlch() {
        return config.alchItems() &&
                client.getBoostedSkillLevel(Skill.MAGIC) >= 55 &&
                ((inventory.containsItem(ItemID.NATURE_RUNE) && inventory.containsStackAmount(ItemID.FIRE_RUNE, 5))
                        || (inventory.runePouchQuanitity(ItemID.FIRE_RUNE) >= 5 && inventory.runePouchContains(ItemID.NATURE_RUNE) && inventory.containsItem(ItemID.RUNE_POUCH)));
    }

    private boolean alchableItem(int itemID) {
        if (itemID == 0 || alchBlacklist.contains(itemID)) {
            return false;
        }
        if (config.alchByValue()) {
            itemValue = utils.getItemPrice(itemID, true);
        }
        ItemComposition itemDef = client.getItemDefinition(itemID);
//		if (!itemDef.isTradeable() || itemDef.getPrice() < 10) { return false; }
	/*	if (itemDef != null) { //Currently bugged (https://discord.com/channels/734831848173338684/744402742839345182/788226017978220544)
			if (!itemDef.isTradeable()) {
				log.debug("Tried to alch untradeable item {}, adding to blacklist", itemDef.getName());
				alchBlacklist.add(itemID);
				return false;
			}
		}*/
        log.debug("Checking alch value of item: {}", itemDef.getName());
        return config.alchItems() &&
                (config.alchByValue() && itemDef.getHaPrice() > highAlchCost &&
                        itemDef.getHaPrice() > itemValue &&
                        itemDef.getHaPrice() < config.maxAlchValue()) ||
                (config.alchByName() && !alchableItems.isEmpty() && alchableItems.stream().anyMatch(itemDef.getName().toLowerCase()::contains));
    }

    private void castHighAlch(Integer itemID) {
        WidgetItem alchItem = inventory.getWidgetItem(itemID);
        if (alchItem != null) {
            log.debug("Alching item: {}", alchItem.getId());
            targetMenu = new LegacyMenuEntry("Cast", "High Level Alchemy -> Item",
                    0,
                    MenuAction.WIDGET_TARGET_ON_WIDGET.getId(),
                    alchItem.getIndex(), WidgetInfo.INVENTORY.getId(),
                    false);
            utils.oneClickCastSpell(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY, targetMenu, alchItem.getCanvasBounds().getBounds(), sleepDelay());
        } else {
            log.debug("castHighAlch widgetItem is null");
        }
    }

    private void buryBones() {
        List<WidgetItem> bones = inventory.getItems("bones");
        executorService.submit(() ->
        {
            iterating = true;
            for (WidgetItem bone : bones) {
                if (BONE_BLACKLIST.contains(bone.getId())) {
                    continue;
                }
                targetMenu = inventoryAssistant.getLegacyMenuEntry(bone.getId(), "bury");
                menu.setEntry(targetMenu);
                mouse.handleMouseClick(bone.getCanvasBounds());
                sleep(calc.getRandomIntBetweenRange(1200, 1400));
            }
            iterating = false;
        });
    }

    private void scatterAshes() {
        List<WidgetItem> ashes = inventory.getItems("ashes");
        executorService.submit(() ->
        {
            iterating = true;
            for (WidgetItem ashe : ashes) {
                if (BONE_BLACKLIST.contains(ashe.getId())) {
                    continue;
                }
                //targetMenu = new LegacyMenuEntry("", "", ashe.getId(), MenuAction.ITEM_FIRST_OPTION.getId(),
                //        ashe.getIndex(), WidgetInfo.INVENTORY.getId(), false);
                targetMenu = inventoryAssistant.getLegacyMenuEntry(ashe.getId(), "scatter");
                menu.setEntry(targetMenu);
                mouse.handleMouseClick(ashe.getCanvasBounds());
                sleep(calc.getRandomIntBetweenRange(800, 2200));
            }
            iterating = false;
        });
    }

    private void attackNPC(NPC npc) {
        targetMenu = new LegacyMenuEntry("", "", npc.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(),
                0, 0, false);

        utils.doActionMsTime(targetMenu, currentNPC.getConvexHull().getBounds(), sleepDelay());
        timeout = 2 + tickDelay();
    }

    private NPC findSuitableNPC() {
        npcName = menuFight ? npcName : config.npcName();
        if (config.exactNpcOnly()) {
            NPC npcTarget = npc.findNearestNpcTargetingLocal(npcName, true);
            return (npcTarget != null) ? npcTarget :
                    npc.findNearestAttackableNpcWithin(startLoc, config.searchRadius(), npcName, true);
        } else {
            NPC npcTarget = npc.findNearestNpcTargetingLocal(npcName, false);
            return (npcTarget != null) ? npcTarget :
                    npc.findNearestAttackableNpcWithin(startLoc, config.searchRadius(), npcName, false);
        }

    }

    private boolean shouldEquipBracelet() {
        return !playerUtils.isItemEquipped(BRACELETS) && inventory.containsItem(BRACELETS) && config.equipBracelet();
    }

    private combatType getEligibleAttackStyle() {

        int attackLevel = client.getRealSkillLevel(Skill.ATTACK);
        int strengthLevel = client.getRealSkillLevel(Skill.STRENGTH);
        int defenceLevel = client.getRealSkillLevel(Skill.DEFENCE);

        if ((attackLevel >= config.attackLvl() && strengthLevel >= config.strengthLvl() && defenceLevel >= config.defenceLvl())) {
            return config.continueType();
        }
        int highestDiff = config.attackLvl() - attackLevel;
        combatType type = combatType.ATTACK;

        if ((config.strengthLvl() - strengthLevel) > highestDiff ||
                (strengthLevel < config.strengthLvl() && strengthLevel < attackLevel && strengthLevel < defenceLevel)) {
            type = combatType.STRENGTH;
        }
        if ((config.defenceLvl() - defenceLevel) > highestDiff ||
                (defenceLevel < config.defenceLvl() && defenceLevel < attackLevel && defenceLevel < strengthLevel)) {
            type = combatType.DEFENCE;
        }
        return type;
    }

    private int getCombatStyle() {
        if (!config.combatLevels()) {
            return -1;
        }
        combatType attackStyle = getEligibleAttackStyle();
        if (attackStyle.equals(combatType.STOP)) {
            resetVals();
        } else {
            switch (client.getVarpValue(VarPlayer.ATTACK_STYLE.getId())) {
                case 0:
                    return (attackStyle.equals(combatType.ATTACK)) ? -1 : attackStyle.index;
                case 1:
                case 2:
                    return (attackStyle.equals(combatType.STRENGTH)) ? -1 : attackStyle.index;
                case 3:
                    return (attackStyle.equals(combatType.DEFENCE)) ? -1 : attackStyle.index;
            }
        }
        return -1;
    }

    private iPowerFighterState getState() {
        if (timeout > 0) {
            playerUtils.handleRun(20, 20);
            return iPowerFighterState.TIMEOUT;
        }
        if (iterating) {
            return iPowerFighterState.ITERATING;
        }
        if (playerUtils.isMoving(beforeLoc)) {
            return iPowerFighterState.MOVING;
        }
        if (shouldEquipBracelet()) {
            return iPowerFighterState.EQUIP_BRACELET;
        }
        int combatStyle = getCombatStyle();
        if (config.combatLevels() && combatStyle != -1) {
            log.info("Changing combat style to: {}", combatStyle);
            utils.setCombatStyle(combatStyle);
            return iPowerFighterState.TIMEOUT;
        }
        if (config.lootAmmo() && !playerUtils.isItemEquipped(List.of(config.ammoID()))) {
            if (inventory.containsItem(config.ammoID())) {
                return iPowerFighterState.EQUIP_AMMO;
            } else if (config.stopAmmo()) {
                if (config.safeSpot() && startLoc.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius())) {
                    return iPowerFighterState.RETURN_SAFE_SPOT;
                }
                return (config.logout()) ? iPowerFighterState.LOG_OUT : iPowerFighterState.MISSING_ITEMS;
            }
        }
        if (config.stopFood() && !inventory.containsItem(config.foodID())) {
            if (config.safeSpot() && startLoc.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius())) {
                return iPowerFighterState.RETURN_SAFE_SPOT;
            }
            return (config.logout()) ? iPowerFighterState.LOG_OUT : iPowerFighterState.MISSING_ITEMS;
        }
        if (config.stopSlayer() && slayerCompleted) {
            if (config.safeSpot() && startLoc.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius())) {
                return iPowerFighterState.RETURN_SAFE_SPOT;
            }
            return (config.logout()) ? iPowerFighterState.LOG_OUT : iPowerFighterState.SLAYER_COMPLETED;
        }
        if (config.lootOnly()) {
            return (config.lootItems() && !inventory.isFull() && !loot.isEmpty()) ? iPowerFighterState.LOOT_ITEMS : iPowerFighterState.TIMEOUT;
        }
        if (config.forceLoot() && config.lootItems() && !inventory.isFull() && !loot.isEmpty()) {
            if (newLoot != null) {
                Duration duration = Duration.between(newLoot, Instant.now());
                nextItemLootTime = (nextItemLootTime == 0) ? calc.getRandomIntBetweenRange(10, 50) : nextItemLootTime;
                if (duration.toSeconds() > nextItemLootTime) {
                    nextItemLootTime = calc.getRandomIntBetweenRange(10, 50);
                    return iPowerFighterState.FORCE_LOOT;
                }
            }
        }
        if (config.safeSpot() && npc.findNearestNpcTargetingLocal("", false) != null &&
                startLoc.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius())) {
            return iPowerFighterState.RETURN_SAFE_SPOT;
        }
        if (player.getInteracting() != null) {
            currentNPC = (NPC) player.getInteracting();
            if (currentNPC != null && currentNPC.getHealthRatio() == -1) //NPC has noHealthBar, NPC ran away and we are stuck with a target we can't attack
            {
                log.debug("interacting and npc has not health bar. Finding new NPC");
                currentNPC = findSuitableNPC();
                if (currentNPC != null) {
                    return iPowerFighterState.ATTACK_NPC;
                } else {
                    log.debug("Clicking randomly to try get unstuck");
                    targetMenu = null;
                    mouse.clickRandomPointCenter(-100, 100);
                    return iPowerFighterState.TIMEOUT;
                }
            }
            return iPowerFighterState.IN_COMBAT;
        }
        npcName = menuFight ? npcName : config.npcName();
        if (config.exactNpcOnly()) {
            currentNPC = npc.findNearestNpcTargetingLocal(npcName, true);
        } else {
            currentNPC = npc.findNearestNpcTargetingLocal(npcName, false);
        }

        if (currentNPC != null) {
            int chance = calc.getRandomIntBetweenRange(0, 1);
            log.debug("Chance result: {}", chance);
            return (chance == 0) ? iPowerFighterState.ATTACK_NPC : iPowerFighterState.WAIT_COMBAT;
        }
        if (chinBreakHandler.shouldBreak(this)) {
            return iPowerFighterState.HANDLE_BREAK;
        }
        if (config.buryBones() && inventory.containsItem("Bones") && (inventory.isFull() || config.buryOne())) {
            return iPowerFighterState.BURY_BONES;
        }
        if (config.scatterAshes() && inventory.containsItem("ashes") && (inventory.isFull() || config.buryOne())) {
            return iPowerFighterState.SCATTER_ASHES;
        }
        if (canAlch() && !alchLoot.isEmpty()) {
            log.debug("high alch conditions met");
            return iPowerFighterState.HIGH_ALCH;
        }
        if (config.lootItems() && !inventory.isFull() && !loot.isEmpty()) {
            return iPowerFighterState.LOOT_ITEMS;
        }
        if (config.lootAmmo() && (!inventory.isFull() || inventory.containsItem(config.ammoID()))) {
            if (ammoLoot.isEmpty() || nextAmmoLootTime == 0) {
                nextAmmoLootTime = calc.getRandomIntBetweenRange(config.minAmmoLootTime(),
                        (config.minAmmoLootTime() + config.randAmmoLootTime()));
            }
            if (!ammoLoot.isEmpty()) {
                if (lootTimer != null) {
                    Duration duration = Duration.between(lootTimer, Instant.now());
                    if (duration.toSeconds() > nextAmmoLootTime) {
                        return iPowerFighterState.LOOT_AMMO;
                    }
                } else {
                    lootTimer = Instant.now();
                }
            }
        }
        currentNPC = findSuitableNPC();
        if (currentNPC != null) {
            return iPowerFighterState.ATTACK_NPC;
        }
        return iPowerFighterState.NPC_NOT_FOUND;
    }


    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startBot || chinBreakHandler.isBreakActive(this)) {
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            if (!client.isResized()) {
                utils.sendGameMessage("illu - client must be set to resizable");
                startBot = false;
                return;
            }
            state = getState();
            switch (state) {
                case TIMEOUT:
                    timeout--;
                    break;
                case ITERATING:
                    break;
                case ATTACK_NPC:
                    attackNPC(currentNPC);
                    break;
                case BURY_BONES:
                    buryBones();
                    timeout = tickDelay();
                    break;
                case SCATTER_ASHES:
                    scatterAshes();
                    timeout = tickDelay();
                    break;
                case EQUIP_AMMO:
                    WidgetItem ammoItem = inventory.getWidgetItem(config.ammoID());
                    if (ammoItem != null) {
                        //targetMenu = new LegacyMenuEntry("", "", ammoItem.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), ammoItem.getIndex(),
                        //        WidgetInfo.INVENTORY.getId(), false);
                        //menu.setEntry(targetMenu);
                        //mouse.delayMouseClick(ammoItem.getCanvasBounds(), sleepDelay());
                        inventory.interactWithItem(ammoItem.getId(), sleepDelay(), "wear", "equip", "wield");
                    }
                    break;
                case EQUIP_BRACELET:
                    WidgetItem bracelet = inventory.getWidgetItem(BRACELETS);
                    if (bracelet != null) {
                        log.debug("Equipping bracelet");
                        //targetMenu = new LegacyMenuEntry("", "", bracelet.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), bracelet.getIndex(),
                        //        WidgetInfo.INVENTORY.getId(), false);
                        //menu.setEntry(targetMenu);
                        //mouse.delayMouseClick(bracelet.getCanvasBounds(), sleepDelay());
                        inventory.interactWithItem(bracelet.getId(), sleepDelay(), "wear", "equip", "wield");
                    }
                    break;
                case HIGH_ALCH:
                    castHighAlch(alchLoot.get(0).getId());
                    timeout = 4 + tickDelay();
                    break;
                case FORCE_LOOT:
                case LOOT_ITEMS:
                    lootItem(loot);
                    timeout = tickDelay();
                    break;
                case LOOT_AMMO:
                    lootItem(ammoLoot);
                    break;
                case WAIT_COMBAT:
                    if (config.safeSpot()) {
                        new TimeoutUntil(
                                () -> startLoc.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius()),
                                () -> playerUtils.isMoving(),
                                3);
                    } else {
                        new TimeoutUntil(
                                () -> playerUtils.isAnimating(),
                                () -> playerUtils.isMoving(),
                                3);
                    }
                    break;
                case IN_COMBAT:
                    timeout = tickDelay();
                    break;
                case HANDLE_BREAK:
                    chinBreakHandler.startBreak(this);
                    timeout = 10;
                    break;
                case RETURN_SAFE_SPOT:
                    walk.sceneWalk(startLoc, config.safeSpotRadius(), sleepDelay());
                    timeout = 2 + tickDelay();
                    break;
                case LOG_OUT:
                    if (player.getInteracting() == null) {
                        interfaceUtils.logout();
                    } else {
                        timeout = 5;
                    }
                    shutDown();
                    break;
            }
            beforeLoc = player.getLocalLocation();
        }
    }

    @Subscribe
    private void onActorDeath(ActorDeath event) {
        if (!startBot) {
            return;
        }
        if (event.getActor() == currentNPC) {
            deathLocation = event.getActor().getWorldLocation();
            log.debug("Our npc died, updating deathLocation: {}", deathLocation.toString());
            killCount++;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (!startBot || client.getLocalPlayer() == null || event.getContainerId() != InventoryID.INVENTORY.getId() || !canAlch()) {
            return;
        }
        log.debug("Processing inventory change");
        final ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (inventoryContainer == null) {
            return;
        }
        List<Item> currentInventory = List.of(inventoryContainer.getItems());
        if (state == iPowerFighterState.HIGH_ALCH) {
            alchLoot.removeIf(item -> !currentInventory.contains(item));
            log.debug("Container changed during high alch phase, after removed high alch items, alchLoot: {}", alchLoot.toString());
        } else {
            alchLoot.addAll(currentInventory.stream()
                    .filter(item -> alchableItem(item.getId()))
                    .collect(Collectors.toList()));
            log.debug("Final alchLoot items: {}", alchLoot.toString());
        }
    }


    @Subscribe
    private void onItemSpawned(ItemSpawned event) {
        if (!startBot) {
            return;
        }
        if (lootableItem(event.getItem())) {
            log.debug("Adding loot item: {}", client.getItemDefinition(event.getItem().getId()).getName());
            if (loot.isEmpty()) {
                log.debug("Starting force loot timer");
                newLoot = Instant.now();
            }
            loot.add(event.getItem());
        }
        if (config.lootAmmo() && event.getItem().getId() == config.ammoID()) {
            for (TileItem item : ammoLoot) {
                if (item.getTile() == event.getTile()) //Don't add if we already have ammo at that tile, as they are stackable
                {
                    return;
                }
            }
            log.debug("adding ammo loot item: {}", event.getItem().getId());
            ammoLoot.add(event.getItem());
        }
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned event) {
        if (!startBot) {
            return;
        }
        loot.remove(event.getItem());
        if (loot.isEmpty()) {
            newLoot = null;
        }
        if (ammoLoot.isEmpty()) {
            lootTimer = null;
        }
        ammoLoot.remove(event.getItem());
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if (startBot && (event.getType() == ChatMessageType.SPAM || event.getType() == ChatMessageType.GAMEMESSAGE)) {
            if (event.getMessage().contains("I'm already under attack") && event.getType() == ChatMessageType.SPAM) {
                log.debug("We already have a target. Waiting to auto-retaliate new target");
                //! If we are underattack, probably are not in safespot --> prioritize returning to safety
                if (config.safeSpot() && startLoc.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius())) {
                    walk.sceneWalk(startLoc, config.safeSpotRadius(), sleepDelay());
                    new TimeoutUntil(
                            () -> startLoc.distanceTo(player.getWorldLocation()) < (config.safeSpotRadius()),
                            () -> playerUtils.isMoving(),
                            3);
                } else { //! Otherwise no safespot? Just AFK and auto retaliate.
                    timeout = 10;
                }
                return;
            }
            if (event.getMessage().contains(SLAYER_MSG) || event.getMessage().contains(SLAYER_BOOST_MSG) &&
                    event.getType() == ChatMessageType.GAMEMESSAGE) {
                log.debug("Slayer task completed");
                slayerCompleted = true;
            }
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (!startBot || event.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        log.debug("GameState changed to logged in, clearing loot and npc");
        loot.clear();
        ammoLoot.clear();
        alchLoot.clear();
        currentNPC = null;
        state = iPowerFighterState.TIMEOUT;
        timeout = 2;
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (!config.insertMenu()) {
            return;
        }

        if (event.getMenuOption().equals("iFight")) {
            menuFight = true;
            npcName = StringUtils.substringBetween(event.getMenuTarget(), ">", "<");
            log.info("Fighting: {}", npcName);
            start();
        }

        if (event.getMenuOption().equals("Stop iFight")) {
            log.info("Stop fighting");
            resetVals();
        }
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event) {
        if (!config.insertMenu() || !event.getOption().equals("Attack")) {
            return;
        }

        if (!startBot) {
            addMenuEntry(event, "iFight");
        } else {
            addMenuEntry(event, "Stop iFight");
        }
    }

    private void addMenuEntry(MenuEntryAdded event, String option) { //TODO: Update to new menu entry
        client.createMenuEntry(-1).setOption(option)
                .setTarget(event.getTarget())
                .setIdentifier(0)
                .setParam1(0)
                .setParam1(0)
                .setType(MenuAction.RUNELITE);
//        MenuEntry entry = new MenuEntry();
//        entry.setOption(option);
//        entry.setTarget(event.getTarget());
//        entry.setOpcode(MenuAction.RUNELITE.getId());
//        entries.add(0, entry);
//        client.setMenuEntries(entries.toArray(new MenuEntry[0]));
    }
}
