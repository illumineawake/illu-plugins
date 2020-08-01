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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.Player;
import net.runelite.api.TileItem;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.ActorQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.queries.TileObjectQuery;
import net.runelite.api.queries.TileQuery;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.WorldLocation;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import static net.runelite.client.plugins.powerfighter.PowerFighterState.*;

import net.runelite.client.ui.overlay.OverlayManager;
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

	NPC currentNPC;
	WorldPoint deathLocation;
	List<TileItem> loot = new ArrayList<>();
	List<TileItem> ammoLoot = new ArrayList<>();
	List<String> lootableItems = new ArrayList<>();
	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	PowerFighterState state;
	Instant lootTimer;
	LocalPoint beforeLoc = new LocalPoint(0, 0);

	boolean startBot;
	boolean slayerCompleted;
	long sleepLength;
	int tickLength;
	int timeout;
	int coinsPH;
	int nextAmmoLootTime;
	int killcount;
	String SLAYER_MESSAGE = "return to a Slayer master";

	@Provides
	PowerFighterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PowerFighterConfig.class);
	}

	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{
		log.debug("stopping template plugin");
		overlayManager.remove(overlay);
		startBot = false;
		botTimer = null;
		loot.clear();
		ammoLoot.clear();
		lootableItems.clear();
		currentNPC = null;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("PowerFighter"))
		{
			return;
		}
		log.debug("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startBot)
			{
				log.debug("starting template plugin");
				startBot = true;
				timeout = 0;
				killcount = 0;
				slayerCompleted = false;
				state = null;
				targetMenu = null;
				botTimer = Instant.now();
				overlayManager.add(overlay);
				String[] values = config.lootItemNames().toLowerCase().split("\\s*,\\s*");
				if (config.lootItems() && !config.lootGEValue() && !config.lootItemNames().isBlank())
				{
					lootableItems.clear();
					lootableItems.addAll(Arrays.asList(values));
					log.info("Lootable items are: {}", lootableItems.toString());
				}
			}
			else
			{
				shutDown();
			}

		}
	}

	public void updateStats()
	{
		//templatePH = (int) getPerHour(totalBraceletCount);
		//coinsPH = (int) getPerHour(totalCoins - ((totalCoins / BRACELET_HA_VAL) * (unchargedBraceletCost + revEtherCost + natureRuneCost)));
	}

	public long getPerHour(int quantity)
	{
		Duration timeSinceStart = Duration.between(botTimer, Instant.now());
		if (!timeSinceStart.isZero())
		{
			return (int) ((double) quantity * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
		}
		return 0;
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
			utils.delayMouseClick(lootItem.getTile().getItemLayer().getCanvasTilePoly().getBounds(), sleepDelay());
		}
	}

	private boolean lootableItem(TileItem item)
	{
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();
		return config.lootItems() &&
			((config.lootNPCOnly() && item.getTile().getWorldLocation().equals(deathLocation)) || !config.lootNPCOnly()) &&
			((config.lootGEValue() && utils.getOSBItem(item.getId()).getOverall_average() > config.minGEValue()) ||
				(!config.lootGEValue() && lootableItems.stream().anyMatch(itemName.toLowerCase()::contains)) ||
				(config.buryBones() && itemName.contains("bones")) ||
				(config.lootClueScrolls() && itemName.contains("clue")));
	}

	private void buryBones()
	{
		List<WidgetItem> bones = utils.getInventoryItems("bones");
		executorService.submit(() ->
		{
			utils.iterating = true;
			for (WidgetItem bone : bones)
			{
				targetMenu = new MenuEntry("", "", bone.getId(), MenuOpcode.ITEM_FIRST_OPTION.getId(),
					bone.getIndex(), 9764864, false);
				utils.handleMouseClick(bone.getCanvasBounds());
				utils.sleep(utils.getRandomIntBetweenRange(680, 1800));
			}
			utils.iterating = false;
		});
	}

	private void attackNPC(NPC npc)
	{
		targetMenu = new MenuEntry("", "", npc.getIndex(), MenuOpcode.NPC_SECOND_OPTION.getId(),
			0, 0, false);
		utils.delayMouseClick(currentNPC.getConvexHull().getBounds(), sleepDelay());
		timeout = 2 + tickDelay();
	}

	private NPC findSuitableNPC()
	{
		NPC npc = utils.findNearestNpcTargetingLocal(config.npcName());
		return (npc != null) ? npc :
			utils.findNearestAttackableNpcWithin(player.getWorldLocation(), config.searchRadius(), config.npcName());
	}

	private PowerFighterState getState()
	{
		if (timeout > 0)
		{
			utils.handleRun(20, 20);
			return TIMEOUT;
		}
		if (utils.iterating)
		{
			return ITERATING;
		}
		if (utils.isMoving(beforeLoc))
		{
			return MOVING;
		}
		if (config.lootAmmo() && !utils.isItemEquipped(List.of(config.ammoID())))
		{
			if (utils.inventoryContains(config.ammoID()))
			{
				return EQUIP_AMMO;
			}
			else if (config.stopAmmo())
			{
				return (config.logout()) ? LOG_OUT : MISSING_ITEMS;
			}
		}
		if (config.stopFood() && !utils.inventoryContains(config.foodID()))
		{
			return (config.logout()) ? LOG_OUT : MISSING_ITEMS;
		}
		if (config.stopSlayer() && slayerCompleted)
		{
			return (config.logout()) ? LOG_OUT : SLAYER_COMPLETED;
		}
		if (player.getInteracting() != null)
		{
			currentNPC = (NPC) player.getInteracting();
			if (currentNPC != null && currentNPC.getHealthRatio() == -1) //NPC has noHealthBar, NPC ran away and we are stuck with a target we can't attack
			{
				log.info("interacting and npc has not health bar. Finding new NPC");
				currentNPC = findSuitableNPC();
				if (currentNPC != null)
				{
					return ATTACK_NPC;
				}
				else
				{
					//Click randomly to try get unstuck
					targetMenu = null;
					utils.clickRandomPointCenter(-200, 200);
					return TIMEOUT;
				}
			}
			return IN_COMBAT;
		}
		currentNPC = utils.findNearestNpcTargetingLocal(config.npcName());
		if (currentNPC != null)
		{
			int chance = utils.getRandomIntBetweenRange(0, 1);
			log.info("Chance result: {}", chance);
			return (chance == 0) ? ATTACK_NPC : WAIT_COMBAT;
		}
		if (config.buryBones() && utils.inventoryContains("bones") && utils.inventoryFull())
		{
			return BURY_BONES;
		}
		if (config.lootItems() && !utils.inventoryFull() && !loot.isEmpty())
		{
			return LOOT_ITEMS;
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
						return LOOT_AMMO;
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
			return ATTACK_NPC;
		}
		return NPC_NOT_FOUND;
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!startBot)
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
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
							9764864, false);
						utils.delayMouseClick(ammoItem.getCanvasBounds(), sleepDelay());
					}
					break;
				case LOOT_ITEMS:
					lootItem(loot);
					timeout = tickDelay();
					break;
				case LOOT_AMMO:
					lootItem(ammoLoot);
					//timeout = tickDelay();
					break;
				case WAIT_COMBAT:
					timeout = 10 + tickDelay();
					break;
				case IN_COMBAT:
				case MOVING:
					timeout = tickDelay();
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
			log.info("Our npc died, updating deathLocation: {}", deathLocation.toString());
			killcount++;
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
			log.info("Adding loot item: {}", client.getItemDefinition(event.getItem().getId()).getName());
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
			log.info("adding loot item: {}", event.getItem().getId());
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
		ammoLoot.remove(event.getItem());
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (startBot && (event.getType() == ChatMessageType.SPAM || event.getType() == ChatMessageType.GAMEMESSAGE))
		{
			log.info("Chat message: {}, Chat Type: {}", event.getMessage(), event.getType());
			if (event.getMessage().contains("I'm already under attack") && event.getType() == ChatMessageType.SPAM)
			{
				log.info("We already have a target. Waiting to auto-retaliate new target");
				timeout = 10;
			}
			if (event.getMessage().contains(SLAYER_MESSAGE) && event.getType() == ChatMessageType.GAMEMESSAGE)
			{
				log.info("Slayer task completed");
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
		log.info("GameState changed to logged in, clearing loot and npc");
		loot.clear();
		ammoLoot.clear();
		currentNPC = null;
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!startBot || targetMenu == null)
		{
			return;
		}
		if (utils.getRandomEvent()) //for random events
		{
			log.debug("Template plugin not overriding due to random event");
			return;
		}
		else
		{
			event.setMenuEntry(targetMenu);
			targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
		}
	}
}