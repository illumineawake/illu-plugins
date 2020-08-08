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
package net.runelite.client.plugins.rooftopagility;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.DecorativeObject;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.GameState;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.Varbits;
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
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import static net.runelite.client.plugins.rooftopagility.RooftopAgilityState.*;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Rooftop Agility",
	enabledByDefault = false,
	description = "Illumine auto rooftop agility plugin",
	tags = {"agility"},
	type = PluginType.SKILLING
)
@Slf4j
public class RooftopAgilityPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BotUtils utils;

	@Inject
	private RooftopAgilityConfig config;

	@Inject
	PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	RooftopAgilityOverlay overlay;

	@Inject
	ItemManager itemManager;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	Player player;
	RooftopAgilityState state;
	Instant botTimer;
	TileItem markOfGrace;
	Tile markOfGraceTile;
	MenuEntry targetMenu;
	LocalPoint beforeLoc = new LocalPoint(0, 0); //initiate to mitigate npe
	WidgetItem alchItem;
	GameObject priffPortal;
	Set<Integer> inventoryItems = new HashSet<>();


	private final Set<Integer> REGION_IDS = Set.of(9781, 12853, 12597, 12084, 12339, 12338, 10806, 10297, 10553, 13358, 13878, 10547, 13105, 9012, 9013, 12895, 13151);
	private final Set<Integer> PORTAL_IDS = Set.of(36241, 36242, 36243, 36244, 36245, 36246);
	WorldPoint CAMELOT_TELE_LOC = new WorldPoint(2705, 3463, 0);
	Set<Integer> AIR_STAFFS = Set.of(ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.DUST_BATTLESTAFF, ItemID.MIST_BATTLESTAFF,
		ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF, ItemID.MYSTIC_DUST_STAFF, ItemID.MYSTIC_SMOKE_STAFF, ItemID.MYSTIC_MIST_STAFF);

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

	@Provides
	RooftopAgilityConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RooftopAgilityConfig.class);
	}

	private void resetVals()
	{
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
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("RooftopAgility"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey())
		{
			case "startButton":
				if (!startAgility)
				{
					startAgility = true;
					chinBreakHandler.startPlugin(this);
					state = null;
					targetMenu = null;
					botTimer = Instant.now();
					restockBank = config.bankRestock();
					inventoryItems.addAll(Set.of(ItemID.NATURE_RUNE, ItemID.MARK_OF_GRACE));
					if (config.alchItemID() != 0)
					{
						inventoryItems.addAll(Set.of(config.alchItemID(), (config.alchItemID() + 1)));
					}
					overlayManager.add(overlay);
				}
				else
				{
					resetVals();
				}
				break;
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("RooftopAgility"))
		{
			switch (event.getKey())
			{
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

	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	public long getMarksPH()
	{
		Duration timeSinceStart = Duration.between(botTimer, Instant.now());
		if (!timeSinceStart.isZero())
		{
			return (int) ((double) mogCollectCount * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
		}
		return 0;
	}

	private boolean shouldCastTeleport()
	{
		return config.camelotTeleport() && client.getBoostedSkillLevel(Skill.MAGIC) >= 45 &&
			CAMELOT_TELE_LOC.distanceTo(client.getLocalPlayer().getWorldLocation()) <= 3 &&
			(utils.inventoryContains(ItemID.LAW_RUNE) && utils.inventoryContains(ItemID.AIR_RUNE, 5) ||
				utils.inventoryContains(ItemID.LAW_RUNE) && utils.isItemEquipped(AIR_STAFFS));
	}

	private boolean shouldAlch()
	{
		return config.highAlch() &&
			config.alchItemID() != 0 &&
			client.getBoostedSkillLevel(Skill.MAGIC) >= 55;
	}

	private void highAlchItem()
	{
		if (!setHighAlch)
		{
			targetMenu = new MenuEntry("Cast", "<col=00ff00>High Level Alchemy</col>", 0,
				MenuOpcode.WIDGET_TYPE_2.getId(), -1, 14286887, false);
			Widget spellWidget = client.getWidget(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY);
			if (spellWidget != null)
			{
				utils.delayMouseClick(spellWidget.getBounds(), sleepDelay());
			}
			else
			{
				utils.delayClickRandomPointCenter(-200, 200, sleepDelay());
			}
			setHighAlch = true;
		}
		else
		{
			alchItem = utils.getInventoryWidgetItem(List.of(config.alchItemID(), (config.alchItemID() + 1)));
			targetMenu = new MenuEntry("Cast", "<col=00ff00>High Level Alchemy</col><col=ffffff> ->",
				alchItem.getId(),
				MenuOpcode.ITEM_USE_ON_WIDGET.getId(),
				alchItem.getIndex(), 9764864,
				false);
			utils.delayMouseClick(alchItem.getCanvasBounds(), sleepDelay());
			alchTimeout = 4 + tickDelay();
		}
	}

	private boolean shouldRestock()
	{
		if (!config.highAlch() ||
			config.alchItemID() == 0 ||
			!restockBank ||
			client.getBoostedSkillLevel(Skill.MAGIC) < 55)
		{
			return false;
		}
		return !utils.inventoryContains(ItemID.NATURE_RUNE) || !utils.inventoryContains(Set.of(config.alchItemID(), (config.alchItemID() + 1)));
	}

	private void restockItems()
	{
		if (utils.isBankOpen())
		{
			if (client.getVarbitValue(Varbits.BANK_NOTE_FLAG.getId()) != 1)
			{
				targetMenu = new MenuEntry("Note", "", 1, MenuOpcode.CC_OP.getId(), -1, 786455, false);
				utils.delayClickRandomPointCenter(-200, 200, sleepDelay());
				return;
			}
			if ((!utils.bankContains(ItemID.NATURE_RUNE, 1) && !utils.inventoryContains(ItemID.NATURE_RUNE)) ||
				(!utils.bankContains(config.alchItemID(), 1) && !utils.inventoryContains(Set.of(config.alchItemID(), config.alchItemID() + 1))))
			{
				log.debug("out of alching items");
				restockBank = false;
				return;
			}
			else
			{
				WidgetItem food = utils.getInventoryWidgetItemMenu(itemManager, "Eat", 33);
				if (food != null)
				{
					inventoryItems.add(food.getId());
				}
				if (utils.inventoryContainsExcept(inventoryItems))
				{
					log.debug("depositing items");
					utils.depositAllExcept(inventoryItems);
					timeout = tickDelay();
					return;
				}
				if (!utils.inventoryFull())
				{
					if (!utils.inventoryContains(ItemID.NATURE_RUNE))
					{
						log.debug("withdrawing Nature runes");
						utils.withdrawAllItem(ItemID.NATURE_RUNE);
						return;
					}
					if (!utils.inventoryContains(Set.of(config.alchItemID(), config.alchItemID() + 1)))
					{
						log.debug("withdrawing Config Alch Item");
						utils.withdrawAllItem(config.alchItemID());
						return;
					}
				}
				else
				{
					log.debug("inventory is full but trying to withdraw items");
				}
			}
		}
		else
		{
			GameObject bankBooth = utils.findNearestGameObject(getCurrentObstacle().getBankID());
			if (bankBooth != null)
			{
				targetMenu = new MenuEntry("", "", bankBooth.getId(),
					MenuOpcode.GAME_OBJECT_SECOND_OPTION.getId(), bankBooth.getSceneMinLocation().getX(),
					bankBooth.getSceneMinLocation().getY(), false);
				utils.delayMouseClick(bankBooth.getConvexHull().getBounds(), sleepDelay());
				timeout = tickDelay();
			}
		}
	}

	private RooftopAgilityObstacles getCurrentObstacle()
	{
		return RooftopAgilityObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
	}

	private void findObstacle()
	{
		RooftopAgilityObstacles obstacle = getCurrentObstacle();
		if (obstacle != null)
		{
			log.debug(String.valueOf(obstacle.getObstacleId()));

			if (obstacle.getObstacleType() == RooftopAgilityObstacleType.DECORATION)
			{
				DecorativeObject decObstacle = utils.findNearestDecorObject(obstacle.getObstacleId());
				if (decObstacle != null)
				{
					targetMenu = new MenuEntry("", "", decObstacle.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), decObstacle.getLocalLocation().getSceneX(), decObstacle.getLocalLocation().getSceneY(), false);
					utils.delayMouseClick(decObstacle.getConvexHull().getBounds(), sleepDelay());
					return;
				}
			}
			if (obstacle.getObstacleType() == RooftopAgilityObstacleType.GROUND_OBJECT)
			{
				GroundObject groundObstacle = utils.findNearestGroundObject(obstacle.getObstacleId());
				if (groundObstacle != null)
				{
					targetMenu = new MenuEntry("", "", groundObstacle.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), groundObstacle.getLocalLocation().getSceneX(), groundObstacle.getLocalLocation().getSceneY(), false);
					utils.delayMouseClick(groundObstacle.getConvexHull().getBounds(), sleepDelay());
					return;
				}
			}
			GameObject objObstacle = utils.findNearestGameObject(obstacle.getObstacleId());
			if (objObstacle != null)
			{
				targetMenu = new MenuEntry("", "", objObstacle.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), objObstacle.getSceneMinLocation().getX(), objObstacle.getSceneMinLocation().getY(), false);
				utils.delayMouseClick(objObstacle.getConvexHull().getBounds(), sleepDelay());
				return;
			}
		}
		else
		{
			log.debug("Not in obstacle area");
		}
	}

	public RooftopAgilityState getState()
	{
		if (timeout > 0)
		{
			if (alchTimeout <= 0 && shouldAlch() && utils.inventoryContains(ItemID.NATURE_RUNE) &&
				utils.inventoryContains(Set.of(config.alchItemID(), (config.alchItemID() + 1))))
			{
				timeout--;
				return HIGH_ALCH;
			}
			if (alchClick)
			{
				RooftopAgilityObstacles currentObstacle = getCurrentObstacle();
				if (currentObstacle != null)
				{
					if (markOfGrace != null && markOfGraceTile != null && config.mogPickup() && (!utils.inventoryFull() || utils.inventoryContains(ItemID.MARK_OF_GRACE)))
					{
						if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0)
						{
							return MARK_OF_GRACE;
						}
					}
					if (currentObstacle.getBankID() == 0 || !shouldRestock())
					{
						timeout--;
						return (shouldCastTeleport()) ? CAST_CAMELOT_TELEPORT : FIND_OBSTACLE;
					}
				}
			}
			return TIMEOUT;
		}
		if (shouldCastTeleport())
		{
			return CAST_CAMELOT_TELEPORT;
		}
		if (utils.isMoving(beforeLoc))
		{
			if (alchTimeout <= 0 && shouldAlch() && (utils.inventoryContains(ItemID.NATURE_RUNE) &&
				utils.inventoryContains(Set.of(config.alchItemID(), (config.alchItemID() + 1)))))
			{
				timeout = tickDelay();
				return HIGH_ALCH;
			}
			timeout = tickDelay();
			return MOVING;
		}
		RooftopAgilityObstacles currentObstacle = RooftopAgilityObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
		if (currentObstacle == null)
		{
			timeout = tickDelay();
			return MOVING;
		}
		if (currentObstacle.getBankID() > 0 && shouldRestock())
		{
			if (utils.findNearestGameObject(currentObstacle.getBankID()) != null)
			{
				return RESTOCK_ITEMS;
			}
			else
			{
				log.debug("should restock but couldn't find bank");
			}
		}
		if (markOfGrace != null && markOfGraceTile != null && config.mogPickup() && (!utils.inventoryFull() || utils.inventoryContains(ItemID.MARK_OF_GRACE)))
		{
			if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0)
			{
				if (markOfGraceTile.getGroundItems().contains(markOfGrace)) //failsafe sometimes onItemDespawned doesn't capture mog despawn
				{
					return MARK_OF_GRACE;
				}
				else
				{
					log.info("Mark of grace not found and markOfGrace was not null");
					markOfGrace = null;
				}
			}
		}
		if (priffPortal != null)
		{
			if (currentObstacle.getLocation().distanceTo(priffPortal.getWorldLocation()) == 0)
			{
				return PRIFF_PORTAL;
			}
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			return HANDLE_BREAK;
		}
		if (!utils.isMoving(beforeLoc))
		{
			return FIND_OBSTACLE;
		}
		return ANIMATING;
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (!startAgility || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (alchTimeout > 0)
		{
			alchTimeout--;
		}
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN && client.getBoostedSkillLevel(Skill.HITPOINTS) > config.lowHP())
		{
			if (!REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()))
			{
				log.debug("not in agility course region");
				return;
			}
			marksPerHour = (int) getMarksPH();
			utils.handleRun(40, 20);
			state = getState();
			beforeLoc = client.getLocalPlayer().getLocalLocation();
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					break;
				case MARK_OF_GRACE:
					log.debug("Picking up mark of grace");
					targetMenu = new MenuEntry("", "", ItemID.MARK_OF_GRACE, 20, markOfGraceTile.getSceneLocation().getX(), markOfGraceTile.getSceneLocation().getY(), false);
					utils.delayClickRandomPointCenter(-200, 200, sleepDelay());
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
					targetMenu = new MenuEntry("", "", 2, MenuOpcode.CC_OP.getId(), -1,
						14286879, false);
					Widget spellWidget = client.getWidget(WidgetInfo.SPELL_CAMELOT_TELEPORT);
					if (spellWidget != null)
					{
						utils.delayMouseClick(spellWidget.getBounds(), sleepDelay());
					}
					else
					{
						utils.delayClickRandomPointCenter(-200, 200, sleepDelay());
					}
					timeout = 2 + tickDelay();
					break;
				case PRIFF_PORTAL:
					log.info("Using Priff portal");
					targetMenu = new MenuEntry("", "", priffPortal.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(),
							priffPortal.getSceneMinLocation().getX(), priffPortal.getSceneMinLocation().getY(), false);
					utils.delayMouseClick(priffPortal.getConvexHull().getBounds(), sleepDelay());
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					timeout = 10;
					break;
			}
		}
		else
		{
			log.debug("client/ player is null or bot isn't started");
			return;
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!startAgility || targetMenu == null)
		{
			return;
		}
		log.debug("MenuEntry string event: " + targetMenu.toString());
		event.setMenuEntry(targetMenu);
		alchClick = (targetMenu.getOption().equals("Cast"));
		timeout = tickDelay();
		targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()))
		{
			return;
		}

		if (PORTAL_IDS.contains(event.getGameObject().getId()))
		{
			log.info("Portal spawned");
			priffPortal = event.getGameObject();
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()))
		{
			return;
		}

		if (PORTAL_IDS.contains(event.getGameObject().getId()))
		{
			log.info("Portal spawned");
			priffPortal = null;
		}
	}

	@Subscribe
	private void onItemSpawned(ItemSpawned event)
	{
		if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) ||
				!config.mogPickup())
		{
			return;
		}

		TileItem item = event.getItem();
		Tile tile = event.getTile();

		if (item.getId() == ItemID.MARK_OF_GRACE)
		{
			log.debug("Mark of grace spawned");
			markOfGrace = item;
			markOfGraceTile = tile;
			WidgetItem mogInventory = utils.getInventoryWidgetItem(ItemID.MARK_OF_GRACE);
			mogInventoryCount = (mogInventory != null) ? mogInventory.getQuantity() : 0;
			mogSpawnCount++;
		}
	}

	@Subscribe
	private void onItemDespawned(ItemDespawned event)
	{
		if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) || !config.mogPickup())
		{
			return;
		}

		TileItem item = event.getItem();

		if (item.getId() == ItemID.MARK_OF_GRACE)
		{
			log.debug("Mark of grace despawned");
			markOfGrace = null;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != 93 || mogInventoryCount == -1)
		{
			return;
		}
		if (event.getItemContainer().count(ItemID.MARK_OF_GRACE) > mogInventoryCount)
		{
			mogCollectCount++;
			mogInventoryCount = -1;
		}
	}
}
