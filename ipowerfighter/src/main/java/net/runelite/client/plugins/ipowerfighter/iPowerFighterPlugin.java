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
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.CalculationUtils;
import net.runelite.client.plugins.iutils.InterfaceUtils;
import net.runelite.client.plugins.iutils.InventoryUtils;
import net.runelite.client.plugins.iutils.MenuUtils;
import net.runelite.client.plugins.iutils.MouseUtils;
import net.runelite.client.plugins.iutils.NPCUtils;
import net.runelite.client.plugins.iutils.PlayerUtils;
import net.runelite.client.plugins.iutils.WalkUtils;
import net.runelite.client.plugins.iutils.iUtils;
import static net.runelite.client.plugins.iutils.iUtils.iterating;
import static net.runelite.client.plugins.iutils.iUtils.sleep;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.osbuddy.OSBGrandExchangeResult;
import org.pf4j.Extension;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "iPower Fighter",
	enabledByDefault = false,
	description = "Illumine - Power Fighter plugin",
	tags = {"illumine", "combat", "ranged", "magic", "bot"}
)
@Slf4j
public class iPowerFighterPlugin extends Plugin
{
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
	private ChinBreakHandler chinBreakHandler;

	NPC currentNPC;
	WorldPoint deathLocation;
	List<TileItem> loot = new ArrayList<>();
	List<TileItem> ammoLoot = new ArrayList<>();
	List<String> lootableItems = new ArrayList<>();
	Set<String> alchableItems = new HashSet<>();
	Set<Integer> alchBlacklist = Set.of(ItemID.NATURE_RUNE, ItemID.FIRE_RUNE, ItemID.COINS_995, ItemID.RUNE_POUCH, ItemID.HERB_SACK, ItemID.OPEN_HERB_SACK, ItemID.XERICS_TALISMAN, ItemID.HOLY_WRENCH); //Temp fix until isTradeable is fixed
	List<Item> alchLoot = new ArrayList<>();
	;
	MenuEntry targetMenu;
	Instant botTimer;
	Instant newLoot;
	Player player;
	iPowerFighterState state;
	Instant lootTimer;
	LocalPoint beforeLoc = new LocalPoint(0, 0);
	WorldPoint startLoc;
	OSBGrandExchangeResult itemGeValue;

	int highAlchCost;
	boolean startBot;
	boolean slayerCompleted;
	long sleepLength;
	int tickLength;
	int timeout;
	int nextAmmoLootTime;
	int nextItemLootTime;
	int killcount;

	String SLAYER_MSG = "return to a Slayer master";
	String SLAYER_BOOST_MSG = "You'll be eligible to earn reward points if you complete tasks";
	Set<Integer> BONE_BLACKLIST = Set.of(ItemID.CURVED_BONE, ItemID.LONG_BONE);
	Set<Integer> BRACELETS = Set.of(ItemID.BRACELET_OF_SLAUGHTER, ItemID.EXPEDITIOUS_BRACELET);

	@Provides
	iPowerFighterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(iPowerFighterConfig.class);
	}

	@Override
	protected void startUp()
	{
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown()
	{
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}

	private void resetVals()
	{
		log.debug("stopping power fighter plugin");
		overlayManager.remove(overlay);
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

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("iPowerFighter"))
		{
			return;
		}
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startBot)
			{
				log.debug("starting template plugin");
				if (client == null || client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN)
				{
					log.info("startup failed, log in before starting");
					return;
				}
				startBot = true;
				chinBreakHandler.startPlugin(this);
				timeout = 0;
				killcount = 0;
				slayerCompleted = false;
				state = null;
				targetMenu = null;
				botTimer = Instant.now();
				overlayManager.add(overlay);
				updateConfigValues();
				if (config.alchItems())
				{
					highAlchCost = utils.getOSBItem(ItemID.NATURE_RUNE).getOverall_average() + (utils.getOSBItem(ItemID.FIRE_RUNE).getOverall_average() * 5);
				}
				startLoc = client.getLocalPlayer().getWorldLocation();
				if (config.safeSpot())
				{
					utils.sendGameMessage("Safe spot set: " + startLoc.toString());
				}
				beforeLoc = client.getLocalPlayer().getLocalLocation();
			}
			else
			{
				resetVals();
			}
		}
	}

	private void updateConfigValues()
	{
		alchableItems.clear();
		if (config.alchItems() && config.alchByName() && !config.alchNames().equals("0") && !config.alchNames().equals(""))
		{
			alchableItems.addAll(Stream.of(config.alchNames()
				.toLowerCase()
				.split(",", -1))
				.map(String::trim)
				.collect(Collectors.toList()));
			log.debug("alchable items list: {}", alchableItems.toString());
		}
		String[] values = config.lootItemNames().toLowerCase().split("\\s*,\\s*");
		if (config.lootItems() && !config.lootItemNames().isBlank())
		{
			lootableItems.clear();
			lootableItems.addAll(Arrays.asList(values));
			log.debug("Lootable items are: {}", lootableItems.toString());
		}
	}

	private long sleepDelay()
	{
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
	}

	private int tickDelay()
	{
		tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private TileItem getNearestTileItem(List<TileItem> tileItems)
	{
		int currentDistance;
		TileItem closestTileItem = tileItems.get(0);
		int closestDistance = closestTileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
		for (TileItem tileItem : tileItems)
		{
			currentDistance = tileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
			if (currentDistance < closestDistance)
			{
				closestTileItem = tileItem;
				closestDistance = currentDistance;
			}
		}
		return closestTileItem;
	}

	private void lootItem(List<TileItem> itemList)
	{
		TileItem lootItem = getNearestTileItem(itemList);
		if (lootItem != null)
		{
			targetMenu = new MenuEntry("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(),
				lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY(), false);
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(lootItem.getTile().getItemLayer().getCanvasTilePoly().getBounds(), sleepDelay());
		}
	}

	private boolean lootableItem(TileItem item)
	{
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();
		return config.lootItems() &&
			((config.lootNPCOnly() && item.getTile().getWorldLocation().equals(deathLocation)) ||
				(!config.lootNPCOnly() && item.getTile().getWorldLocation().distanceTo(startLoc) < config.lootRadius())) &&
			((config.lootGEValue() && utils.getOSBItem(item.getId()).getOverall_average() > config.minGEValue()) ||
				lootableItems.stream().anyMatch(itemName.toLowerCase()::contains) ||
				config.buryBones() && itemName.contains("bones") ||
				config.lootClueScrolls() && itemName.contains("scroll"));
	}

	private boolean canAlch()
	{
		return config.alchItems() &&
			client.getBoostedSkillLevel(Skill.MAGIC) >= 55 &&
			((inventory.containsItem(ItemID.NATURE_RUNE) && inventory.containsStackAmount(ItemID.FIRE_RUNE, 5))
				|| (inventory.runePouchQuanitity(ItemID.FIRE_RUNE) >= 5 && inventory.runePouchContains(ItemID.NATURE_RUNE) && inventory.containsItem(ItemID.RUNE_POUCH)));
	}

	private boolean alchableItem(int itemID)
	{
		if (itemID == 0 || alchBlacklist.contains(itemID))
		{
			return false;
		}
		if (config.alchByValue())
		{
			itemGeValue = utils.getOSBItem(itemID);
		}
		ItemComposition itemDef = client.getItemDefinition(itemID);
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
				itemDef.getHaPrice() > itemGeValue.getOverall_average() &&
				itemDef.getHaPrice() < config.maxAlchValue()) ||
			(config.alchByName() && !alchableItems.isEmpty() && alchableItems.stream().anyMatch(itemDef.getName().toLowerCase()::contains));
	}

	private void castHighAlch(Integer itemID)
	{
		WidgetItem alchItem = inventory.getWidgetItem(itemID);
		if (alchItem != null)
		{
			log.debug("Alching item: {}", alchItem.getId());
			targetMenu = new MenuEntry("", "",
				alchItem.getId(),
				MenuAction.ITEM_USE_ON_WIDGET.getId(),
				alchItem.getIndex(), WidgetInfo.INVENTORY.getId(),
				false);
			utils.oneClickCastSpell(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY, targetMenu, alchItem.getCanvasBounds().getBounds(), sleepDelay());
		}
		else
		{
			log.debug("castHighAlch widgetItem is null");
		}
	}

	private void buryBones()
	{
		List<WidgetItem> bones = inventory.getItems("bones");
		executorService.submit(() ->
		{
			iterating = true;
			for (WidgetItem bone : bones)
			{
				if (BONE_BLACKLIST.contains(bone.getId()))
				{
					continue;
				}
				targetMenu = new MenuEntry("", "", bone.getId(), MenuAction.ITEM_FIRST_OPTION.getId(),
					bone.getIndex(), WidgetInfo.INVENTORY.getId(), false);
				menu.setEntry(targetMenu);
				mouse.handleMouseClick(bone.getCanvasBounds());
				sleep(calc.getRandomIntBetweenRange(800, 2200));
			}
			iterating = false;
		});
	}

	private void attackNPC(NPC npc)
	{
		targetMenu = new MenuEntry("", "", npc.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(),
			0, 0, false);
		menu.setEntry(targetMenu);
		mouse.delayMouseClick(currentNPC.getConvexHull().getBounds(), sleepDelay());
		timeout = 2 + tickDelay();
	}

	private NPC findSuitableNPC()
	{
		if (config.exactNpcOnly())
		{
			NPC npcTarget = npc.findNearestNpcTargetingLocal(config.npcName(), true);
			return (npcTarget != null) ? npcTarget :
				npc.findNearestAttackableNpcWithin(startLoc, config.searchRadius(), config.npcName(), true);
		}
		else
		{
			NPC npcTarget = npc.findNearestNpcTargetingLocal(config.npcName(), false);
			return (npcTarget != null) ? npcTarget :
				npc.findNearestAttackableNpcWithin(startLoc, config.searchRadius(), config.npcName(), false);
		}

	}

	private boolean shouldEquipBracelet()
	{
		return !playerUtils.isItemEquipped(BRACELETS) && inventory.containsItem(BRACELETS) && config.equipBracelet();
	}

	private combatType getEligibleAttackStyle()
	{

		int attackLevel = client.getBoostedSkillLevel(Skill.ATTACK);
		int strengthLevel = client.getBoostedSkillLevel(Skill.STRENGTH);
		int defenceLevel = client.getBoostedSkillLevel(Skill.DEFENCE);

		if ((attackLevel >= config.attackLvl() && strengthLevel >= config.strengthLvl() && defenceLevel >= config.defenceLvl()))
		{
			return config.continueType();
		}
		int highestDiff = config.attackLvl() - attackLevel;
		combatType type = combatType.ATTACK;

		if ((config.strengthLvl() - strengthLevel) > highestDiff ||
			(strengthLevel < config.strengthLvl() && strengthLevel < attackLevel && strengthLevel < defenceLevel))
		{
			type = combatType.STRENGTH;
		}
		if ((config.defenceLvl() - defenceLevel) > highestDiff ||
			(defenceLevel < config.defenceLvl() && defenceLevel < attackLevel && defenceLevel < strengthLevel))
		{
			type = combatType.DEFENCE;
		}
		return type;
	}

	private int getCombatStyle()
	{
		if (!config.combatLevels())
		{
			return -1;
		}
		combatType attackStyle = getEligibleAttackStyle();
		if (attackStyle.equals(combatType.STOP))
		{
			resetVals();
		}
		else
		{
			switch (client.getVarpValue(VarPlayer.ATTACK_STYLE.getId()))
			{
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

	private iPowerFighterState getState()
	{
		if (timeout > 0)
		{
			playerUtils.handleRun(20, 20);
			return iPowerFighterState.TIMEOUT;
		}
		if (iterating)
		{
			return iPowerFighterState.ITERATING;
		}
		if (playerUtils.isMoving(beforeLoc))
		{
			return iPowerFighterState.MOVING;
		}
		if (shouldEquipBracelet())
		{
			return iPowerFighterState.EQUIP_BRACELET;
		}
		int combatStyle = getCombatStyle();
		if (config.combatLevels() && combatStyle != -1)
		{
			log.info("Changing combat style to: {}", combatStyle);
			utils.setCombatStyle(combatStyle);
			return iPowerFighterState.TIMEOUT;
		}
		if (config.lootAmmo() && !playerUtils.isItemEquipped(List.of(config.ammoID())))
		{
			if (inventory.containsItem(config.ammoID()))
			{
				return iPowerFighterState.EQUIP_AMMO;
			}
			else if (config.stopAmmo())
			{
				return (config.logout()) ? iPowerFighterState.LOG_OUT : iPowerFighterState.MISSING_ITEMS;
			}
		}
		if (config.stopFood() && !inventory.containsItem(config.foodID()))
		{
			return (config.logout()) ? iPowerFighterState.LOG_OUT : iPowerFighterState.MISSING_ITEMS;
		}
		if (config.stopSlayer() && slayerCompleted)
		{
			return (config.logout()) ? iPowerFighterState.LOG_OUT : iPowerFighterState.SLAYER_COMPLETED;
		}
		if (config.lootOnly())
		{
			return (config.lootItems() && !inventory.isFull() && !loot.isEmpty()) ? iPowerFighterState.LOOT_ITEMS : iPowerFighterState.TIMEOUT;
		}
		if (config.forceLoot() && config.lootItems() && !inventory.isFull() && !loot.isEmpty())
		{
			if (newLoot != null)
			{
				Duration duration = Duration.between(newLoot, Instant.now());
				nextItemLootTime = (nextItemLootTime == 0) ? calc.getRandomIntBetweenRange(10, 50) : nextItemLootTime;
				if (duration.toSeconds() > nextItemLootTime)
				{
					nextItemLootTime = calc.getRandomIntBetweenRange(10, 50);
					return iPowerFighterState.FORCE_LOOT;
				}
			}
		}
		if (config.safeSpot() && npc.findNearestNpcTargetingLocal("", false) != null &&
			startLoc.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius()))
		{
			return iPowerFighterState.RETURN_SAFE_SPOT;
		}
		if (player.getInteracting() != null)
		{
			currentNPC = (NPC) player.getInteracting();
			if (currentNPC != null && currentNPC.getHealthRatio() == -1) //NPC has noHealthBar, NPC ran away and we are stuck with a target we can't attack
			{
				log.debug("interacting and npc has not health bar. Finding new NPC");
				currentNPC = findSuitableNPC();
				if (currentNPC != null)
				{
					return iPowerFighterState.ATTACK_NPC;
				}
				else
				{
					log.debug("Clicking randomly to try get unstuck");
					targetMenu = null;
					mouse.clickRandomPointCenter(-100, 100);
					return iPowerFighterState.TIMEOUT;
				}
			}
			return iPowerFighterState.IN_COMBAT;
		}
		if (config.exactNpcOnly())
		{
			currentNPC = npc.findNearestNpcTargetingLocal(config.npcName(), true);
		}
		else
		{
			currentNPC = npc.findNearestNpcTargetingLocal(config.npcName(), false);
		}

		if (currentNPC != null)
		{
			int chance = calc.getRandomIntBetweenRange(0, 1);
			log.debug("Chance result: {}", chance);
			return (chance == 0) ? iPowerFighterState.ATTACK_NPC : iPowerFighterState.WAIT_COMBAT;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			return iPowerFighterState.HANDLE_BREAK;
		}
		if (config.buryBones() && inventory.containsItem("bones") && (inventory.isFull() || config.buryOne()))
		{
			return iPowerFighterState.BURY_BONES;
		}
		if (canAlch() && !alchLoot.isEmpty())
		{
			log.debug("high alch conditions met");
			return iPowerFighterState.HIGH_ALCH;
		}
		if (config.lootItems() && !inventory.isFull() && !loot.isEmpty())
		{
			return iPowerFighterState.LOOT_ITEMS;
		}
		if (config.lootAmmo() && (!inventory.isFull() || inventory.containsItem(config.ammoID())))
		{
			if (ammoLoot.isEmpty() || nextAmmoLootTime == 0)
			{
				nextAmmoLootTime = calc.getRandomIntBetweenRange(config.minAmmoLootTime(),
					(config.minAmmoLootTime() + config.randAmmoLootTime()));
			}
			if (!ammoLoot.isEmpty())
			{
				if (lootTimer != null)
				{
					Duration duration = Duration.between(lootTimer, Instant.now());
					if (duration.toSeconds() > nextAmmoLootTime)
					{
						return iPowerFighterState.LOOT_AMMO;
					}
				}
				else
				{
					lootTimer = Instant.now();
				}
			}
		}
		currentNPC = findSuitableNPC();
		if (currentNPC != null)
		{
			return iPowerFighterState.ATTACK_NPC;
		}
		return iPowerFighterState.NPC_NOT_FOUND;
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!startBot || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			if (!client.isResized())
			{
				utils.sendGameMessage("illu - client must be set to resizable");
				startBot = false;
				return;
			}
			state = getState();
			switch (state)
			{
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
				case EQUIP_AMMO:
					WidgetItem ammoItem = inventory.getWidgetItem(config.ammoID());
					if (ammoItem != null)
					{
						targetMenu = new MenuEntry("", "", ammoItem.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), ammoItem.getIndex(),
							WidgetInfo.INVENTORY.getId(), false);
						menu.setEntry(targetMenu);
						mouse.delayMouseClick(ammoItem.getCanvasBounds(), sleepDelay());
					}
					break;
				case EQUIP_BRACELET:
					WidgetItem bracelet = inventory.getWidgetItem(BRACELETS);
					if (bracelet != null)
					{
						log.debug("Equipping bracelet");
						targetMenu = new MenuEntry("", "", bracelet.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), bracelet.getIndex(),
							WidgetInfo.INVENTORY.getId(), false);
						menu.setEntry(targetMenu);
						mouse.delayMouseClick(bracelet.getCanvasBounds(), sleepDelay());
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
					timeout = 10 + tickDelay();
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
					if (player.getInteracting() == null)
					{
						interfaceUtils.logout();
					}
					else
					{
						timeout = 5;
					}
					shutDown();
					break;
			}
			beforeLoc = player.getLocalLocation();
		}
	}

	@Subscribe
	private void onActorDeath(ActorDeath event)
	{
		if (!startBot)
		{
			return;
		}
		if (event.getActor() == currentNPC)
		{
			deathLocation = event.getActor().getWorldLocation();
			log.debug("Our npc died, updating deathLocation: {}", deathLocation.toString());
			killcount++;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!startBot || client.getLocalPlayer() == null || event.getContainerId() != InventoryID.INVENTORY.getId() || !canAlch())
		{
			return;
		}
		log.debug("Processing inventory change");
		final ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (inventoryContainer == null)
		{
			return;
		}
		List<Item> currentInventory = List.of(inventoryContainer.getItems());
		if (state == iPowerFighterState.HIGH_ALCH)
		{
			alchLoot.removeIf(item -> !currentInventory.contains(item));
			log.debug("Container changed during high alch phase, after removed high alch items, alchLoot: {}", alchLoot.toString());
		}
		else
		{
			alchLoot.addAll(currentInventory.stream()
				.filter(item -> alchableItem(item.getId()))
				.collect(Collectors.toList()));
			log.debug("Final alchLoot items: {}", alchLoot.toString());
		}
	}


	@Subscribe
	private void onItemSpawned(ItemSpawned event)
	{
		if (!startBot)
		{
			return;
		}
		if (lootableItem(event.getItem()))
		{
			log.debug("Adding loot item: {}", client.getItemDefinition(event.getItem().getId()).getName());
			if (loot.isEmpty())
			{
				log.debug("Starting force loot timer");
				newLoot = Instant.now();
			}
			loot.add(event.getItem());
		}
		if (config.lootAmmo() && event.getItem().getId() == config.ammoID())
		{
			for (TileItem item : ammoLoot)
			{
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
	private void onItemDespawned(ItemDespawned event)
	{
		if (!startBot)
		{
			return;
		}
		loot.remove(event.getItem());
		if (loot.isEmpty())
		{
			newLoot = null;
		}
		if (ammoLoot.isEmpty())
		{
			lootTimer = null;
		}
		ammoLoot.remove(event.getItem());
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (startBot && (event.getType() == ChatMessageType.SPAM || event.getType() == ChatMessageType.GAMEMESSAGE))
		{
			if (event.getMessage().contains("I'm already under attack") && event.getType() == ChatMessageType.SPAM)
			{
				log.debug("We already have a target. Waiting to auto-retaliate new target");
				timeout = 10;
				return;
			}
			if (event.getMessage().contains(SLAYER_MSG) || event.getMessage().contains(SLAYER_BOOST_MSG) &&
				event.getType() == ChatMessageType.GAMEMESSAGE)
			{
				log.debug("Slayer task completed");
				slayerCompleted = true;
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (!startBot || event.getGameState() != GameState.LOGGED_IN)
		{
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
}
