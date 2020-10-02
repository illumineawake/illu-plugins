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
package net.runelite.client.plugins.powerfighter;

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
import net.runelite.api.ItemDefinition;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
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
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.osbuddy.OSBGrandExchangeResult;
import org.pf4j.Extension;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Power Fighter",
	enabledByDefault = false,
	description = "Illumine - Power Fighter plugin",
	tags = {"illumine", "combat", "ranged", "magic", "bot"},
	type = PluginType.PVM
)
@Slf4j
public class PowerFighterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PowerFighterConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PowerFighterOverlay overlay;

	@Inject
	private BotUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ExecutorService executorService;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	NPC currentNPC;
	WorldPoint deathLocation;
	List<TileItem> loot = new ArrayList<>();
	List<TileItem> ammoLoot = new ArrayList<>();
	List<String> lootableItems = new ArrayList<>();
	Set<String> alchableItems = new HashSet<>();
	Set<Integer> alchBlacklist = Set.of(ItemID.NATURE_RUNE, ItemID.FIRE_RUNE, ItemID.COINS_995);
	List<Item> alchLoot = new ArrayList<>();
	;
	MenuEntry targetMenu;
	Instant botTimer;
	Instant newLoot;
	Player player;
	PowerFighterState state;
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

	String SLAYER_MESSAGE = "return to a Slayer master";
	Set<Integer> BONE_BLACKLIST = Set.of(ItemID.CURVED_BONE, ItemID.LONG_BONE);
	Set<Integer> BRACELETS = Set.of(ItemID.BRACELET_OF_SLAUGHTER, ItemID.EXPEDITIOUS_BRACELET);

	@Provides
	PowerFighterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PowerFighterConfig.class);
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
		if (!configButtonClicked.getGroup().equalsIgnoreCase("PowerFighter"))
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
				highAlchCost = utils.getOSBItem(ItemID.NATURE_RUNE).getOverall_average() + (utils.getOSBItem(ItemID.FIRE_RUNE).getOverall_average() * 5);
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
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
	}

	private int tickDelay()
	{
		tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
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
			targetMenu = new MenuEntry("", "", lootItem.getId(), MenuOpcode.GROUND_ITEM_THIRD_OPTION.getId(),
				lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY(), false);
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(lootItem.getTile().getItemLayer().getCanvasTilePoly().getBounds(), sleepDelay());
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
			((utils.inventoryContains(ItemID.NATURE_RUNE) && utils.inventoryContainsStack(ItemID.FIRE_RUNE, 5))
				|| (utils.runePouchQuanitity(554) >= 5 && utils.runePouchContains(561)));
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
		ItemDefinition itemDef = client.getItemDefinition(itemID);
		log.debug("Checking alch value of item: {}", itemDef.getName());
		return config.alchItems() &&
			(config.alchByValue() && itemDef.getHaPrice() > highAlchCost &&
				itemDef.getHaPrice() > itemGeValue.getOverall_average() &&
				itemDef.getHaPrice() < config.maxAlchValue()) ||
			(config.alchByName() && !alchableItems.isEmpty() && alchableItems.stream().anyMatch(itemDef.getName().toLowerCase()::contains));
	}

	private void castHighAlch(Integer itemID)
	{
		WidgetItem alchItem = utils.getInventoryWidgetItem(itemID);
		if (alchItem != null)
		{
			log.debug("Alching item: {}", alchItem.getId());
			targetMenu = new MenuEntry("", "",
				alchItem.getId(),
				MenuOpcode.ITEM_USE_ON_WIDGET.getId(),
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
		List<WidgetItem> bones = utils.getInventoryItems("bones");
		executorService.submit(() ->
		{
			utils.iterating = true;
			for (WidgetItem bone : bones)
			{
				if (BONE_BLACKLIST.contains(bone.getId()))
				{
					continue;
				}
				targetMenu = new MenuEntry("", "", bone.getId(), MenuOpcode.ITEM_FIRST_OPTION.getId(),
					bone.getIndex(), WidgetInfo.INVENTORY.getId(), false);
				utils.setMenuEntry(targetMenu);
				utils.handleMouseClick(bone.getCanvasBounds());
				utils.sleep(utils.getRandomIntBetweenRange(800, 2200));
			}
			utils.iterating = false;
		});
	}

	private void attackNPC(NPC npc)
	{
		targetMenu = new MenuEntry("", "", npc.getIndex(), MenuOpcode.NPC_SECOND_OPTION.getId(),
			0, 0, false);
		utils.setMenuEntry(targetMenu);
		utils.delayMouseClick(currentNPC.getConvexHull().getBounds(), sleepDelay());
		timeout = 2 + tickDelay();
	}

	private NPC findSuitableNPC()
	{
		if (config.exactNpcOnly())
		{
			NPC npc = utils.findNearestNpcTargetingLocal(config.npcName(), true);
			return (npc != null) ? npc :
				utils.findNearestAttackableNpcWithin(startLoc, config.searchRadius(), config.npcName(), true);
		}
		else
		{
			NPC npc = utils.findNearestNpcTargetingLocal(config.npcName(), false);
			return (npc != null) ? npc :
				utils.findNearestAttackableNpcWithin(startLoc, config.searchRadius(), config.npcName(), false);
		}

	}

	private boolean shouldEquipBracelet()
	{
		return !utils.isItemEquipped(BRACELETS) && utils.inventoryContains(BRACELETS) && config.equipBracelet();
	}

	private PowerFighterState getState()
	{
		if (timeout > 0)
		{
			utils.handleRun(20, 20);
			return PowerFighterState.TIMEOUT;
		}
		if (utils.iterating)
		{
			return PowerFighterState.ITERATING;
		}
		if (utils.isMoving(beforeLoc))
		{
			return PowerFighterState.MOVING;
		}
		if (shouldEquipBracelet())
		{
			return PowerFighterState.EQUIP_BRACELET;
		}
		if (config.lootAmmo() && !utils.isItemEquipped(List.of(config.ammoID())))
		{
			if (utils.inventoryContains(config.ammoID()))
			{
				return PowerFighterState.EQUIP_AMMO;
			}
			else if (config.stopAmmo())
			{
				return (config.logout()) ? PowerFighterState.LOG_OUT : PowerFighterState.MISSING_ITEMS;
			}
		}
		if (config.stopFood() && !utils.inventoryContains(config.foodID()))
		{
			return (config.logout()) ? PowerFighterState.LOG_OUT : PowerFighterState.MISSING_ITEMS;
		}
		if (config.stopSlayer() && slayerCompleted)
		{
			return (config.logout()) ? PowerFighterState.LOG_OUT : PowerFighterState.SLAYER_COMPLETED;
		}
		if (config.lootOnly())
		{
			return (config.lootItems() && !utils.inventoryFull() && !loot.isEmpty()) ? PowerFighterState.LOOT_ITEMS : PowerFighterState.TIMEOUT;
		}
		if (config.forceLoot() && config.lootItems() && !utils.inventoryFull() && !loot.isEmpty())
		{
			if (newLoot != null)
			{
				Duration duration = Duration.between(newLoot, Instant.now());
				nextItemLootTime = (nextItemLootTime == 0) ? utils.getRandomIntBetweenRange(10, 50) : nextItemLootTime;
				if (duration.toSeconds() > nextItemLootTime)
				{
					nextItemLootTime = utils.getRandomIntBetweenRange(10, 50);
					return PowerFighterState.FORCE_LOOT;
				}
			}
		}
		if (config.safeSpot() && utils.findNearestNpcTargetingLocal("", false) != null &&
			startLoc.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius()))
		{
			return PowerFighterState.RETURN_SAFE_SPOT;
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
					return PowerFighterState.ATTACK_NPC;
				}
				else
				{
					log.debug("Clicking randomly to try get unstuck");
					targetMenu = null;
					utils.clickRandomPointCenter(-200, 200);
					return PowerFighterState.TIMEOUT;
				}
			}
			return PowerFighterState.IN_COMBAT;
		}
		if (config.exactNpcOnly())
		{
			currentNPC = utils.findNearestNpcTargetingLocal(config.npcName(), true);
		}
		else
		{
			currentNPC = utils.findNearestNpcTargetingLocal(config.npcName(), false);
		}

		if (currentNPC != null)
		{
			int chance = utils.getRandomIntBetweenRange(0, 1);
			log.debug("Chance result: {}", chance);
			return (chance == 0) ? PowerFighterState.ATTACK_NPC : PowerFighterState.WAIT_COMBAT;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			return PowerFighterState.HANDLE_BREAK;
		}
		if (config.buryBones() && utils.inventoryContains("bones") && (utils.inventoryFull() || config.buryOne()))
		{
			return PowerFighterState.BURY_BONES;
		}
		if (canAlch() && !alchLoot.isEmpty())
		{
			log.debug("high alch conditions met");
			return PowerFighterState.HIGH_ALCH;
		}
		if (config.lootItems() && !utils.inventoryFull() && !loot.isEmpty())
		{
			return PowerFighterState.LOOT_ITEMS;
		}
		if (config.lootAmmo() && (!utils.inventoryFull() || utils.inventoryContains(config.ammoID())))
		{
			if (ammoLoot.isEmpty() || nextAmmoLootTime == 0)
			{
				nextAmmoLootTime = utils.getRandomIntBetweenRange(config.minAmmoLootTime(),
					(config.minAmmoLootTime() + config.randAmmoLootTime()));
			}
			if (!ammoLoot.isEmpty())
			{
				if (lootTimer != null)
				{
					Duration duration = Duration.between(lootTimer, Instant.now());
					if (duration.toSeconds() > nextAmmoLootTime)
					{
						return PowerFighterState.LOOT_AMMO;
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
			return PowerFighterState.ATTACK_NPC;
		}
		return PowerFighterState.NPC_NOT_FOUND;
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
					WidgetItem ammoItem = utils.getInventoryWidgetItem(config.ammoID());
					if (ammoItem != null)
					{
						targetMenu = new MenuEntry("", "", ammoItem.getId(), MenuOpcode.ITEM_SECOND_OPTION.getId(), ammoItem.getIndex(),
							WidgetInfo.INVENTORY.getId(), false);
						utils.setMenuEntry(targetMenu);
						utils.delayMouseClick(ammoItem.getCanvasBounds(), sleepDelay());
					}
					break;
				case EQUIP_BRACELET:
					WidgetItem bracelet = utils.getInventoryWidgetItem(BRACELETS);
					if (bracelet != null)
					{
						log.debug("Equipping bracelet");
						targetMenu = new MenuEntry("", "", bracelet.getId(), MenuOpcode.ITEM_SECOND_OPTION.getId(), bracelet.getIndex(),
							WidgetInfo.INVENTORY.getId(), false);
						utils.setMenuEntry(targetMenu);
						utils.delayMouseClick(bracelet.getCanvasBounds(), sleepDelay());
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
					utils.walk(startLoc, config.safeSpotRadius(), sleepDelay());
					timeout = 2 + tickDelay();
					break;
				case LOG_OUT:
					if (player.getInteracting() == null)
					{
						utils.logout();
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
		if (state == PowerFighterState.HIGH_ALCH)
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
			if (event.getMessage().contains(SLAYER_MESSAGE) && event.getType() == ChatMessageType.GAMEMESSAGE)
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
		state = PowerFighterState.TIMEOUT;
		timeout = 2;
	}
}
