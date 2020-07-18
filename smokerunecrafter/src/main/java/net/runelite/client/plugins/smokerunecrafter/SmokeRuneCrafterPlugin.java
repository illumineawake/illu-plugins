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
package net.runelite.client.plugins.smokerunecrafter;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.ObjectID;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.plugins.smokerunecrafter.SmokeRuneCrafterConfig;
import static net.runelite.client.plugins.smokerunecrafter.SmokeRuneCrafterState.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "SmokeRuneCrafter Plugin",
	enabledByDefault = false,
	description = "Illumine - Smoke Runecrafting plugin",
	tags = {"illumine", "runecrafting", "bot"},
	type = PluginType.UTILITY
)
@Slf4j
public class SmokeRuneCrafterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SmokeRuneCrafterConfig config;

	@Inject
	private BotUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SmokeRuneCrafterOverlay overlay;

	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	SmokeRuneCrafterState state;
	ExecutorService executorService;
	LocalPoint beforeLoc = new LocalPoint(0, 0);
	GameObject bankChest;
	GameObject mysteriousRuins;
	GameObject fireAltar;
	Widget bankItem;

	Set<Integer> DUEL_RINGS = Set.of(ItemID.RING_OF_DUELING2, ItemID.RING_OF_DUELING3, ItemID.RING_OF_DUELING4, ItemID.RING_OF_DUELING5, ItemID.RING_OF_DUELING6, ItemID.RING_OF_DUELING7, ItemID.RING_OF_DUELING8);
	Set<Integer> REQUIRED_ITEMS = Set.of(ItemID.AIR_TALISMAN, ItemID.AIR_RUNE, ItemID.PURE_ESSENCE);

	boolean startBot;
	boolean setTalisman;
	long sleepLength;
	int tickLength;
	int timeout;
	int coinsPH;
	int beforeEssence;
	int totalEssence;
	int beforeAirRunes;
	int totalAirRunes;
	int beforeTalisman;
	int totalTalisman;
	int totalSmokeRunes;
	int totalDuelRings;
	int runesPH;
	int profitPH;
	int totalProfit;
	int smokeRunesCost;
	int pureEssenceCost;
	int airTalismanCost;
	int duelRingCost;
	int airRuneCost;
	int beforeSmokeRunes;
	int currentSmokeRunes;

	@Provides
	SmokeRuneCrafterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SmokeRuneCrafterConfig.class);
	}

	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{
		startBot = false;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("SmokeRuneCrafter"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey())
		{
			case "startButton":
				if (!startBot)
				{
					log.info("starting Smoke Runecrafting plugin");
					startBot = true;
					initCounters();
					state = null;
					targetMenu = null;
					setTalisman = false;
					botTimer = Instant.now();
					executorService = Executors.newSingleThreadExecutor();
					overlayManager.add(overlay);
					smokeRunesCost = utils.getOSBItem(ItemID.SMOKE_RUNE).getOverall_average();
					pureEssenceCost = utils.getOSBItem(ItemID.PURE_ESSENCE).getOverall_average();
					airTalismanCost = utils.getOSBItem(ItemID.AIR_TALISMAN).getOverall_average();
					duelRingCost = utils.getOSBItem(ItemID.RING_OF_DUELING8).getOverall_average();
					airRuneCost = utils.getOSBItem(ItemID.AIR_RUNE).getOverall_average();
					log.info("Item prices set to at - Smoke Runes: {}gp, Pure Essence: {}gp, Air Talisman: {}gp, " +
						"Ring of Dueling {}gp, Air Runes: {}gp", smokeRunesCost, pureEssenceCost, airTalismanCost, duelRingCost, airRuneCost);
				}
				else
				{
					log.info("stopping Smoke Runecrafting plugin");
					startBot = false;
					botTimer = null;
					executorService.shutdown();
					overlayManager.remove(overlay);
				}
				break;
		}
	}

	private void initCounters()
	{
		timeout = 0;
		coinsPH = 0;
		beforeEssence = 0;
		totalEssence = 0;
		beforeAirRunes = 0;
		totalAirRunes = 0;
		beforeTalisman = 0;
		totalTalisman = 0;
		beforeSmokeRunes = 0;
		totalSmokeRunes = 0;
		totalDuelRings = 0;
		runesPH = 0;
		profitPH = 0;
		totalProfit = 0;
		currentSmokeRunes = 0;
	}

	private int itemTotals(int itemID, int beforeAmount, boolean stackableItem)
	{
		int currentAmount = utils.getInventoryItemCount(itemID, stackableItem);
		return  (beforeAmount > currentAmount) ? beforeAmount - currentAmount : 0;
	}

	private void updateTotals()
	{
		totalEssence += itemTotals(ItemID.PURE_ESSENCE, beforeEssence, false);
		beforeEssence = utils.getInventoryItemCount(ItemID.PURE_ESSENCE, false);

		totalAirRunes += itemTotals(ItemID.AIR_RUNE, beforeAirRunes, true);
		beforeAirRunes = utils.getInventoryItemCount(ItemID.AIR_RUNE, true);

		totalTalisman += itemTotals(ItemID.AIR_TALISMAN, beforeTalisman, true);
		beforeTalisman = utils.getInventoryItemCount(ItemID.AIR_TALISMAN, true);

		currentSmokeRunes = utils.getInventoryItemCount(ItemID.SMOKE_RUNE, true);
		if (beforeSmokeRunes < currentSmokeRunes)
			totalSmokeRunes += currentSmokeRunes;
		beforeSmokeRunes = currentSmokeRunes;

		if(!utils.isItemEquipped(DUEL_RINGS) || utils.isItemEquipped(Set.of(ItemID.RING_OF_DUELING1)))
			totalDuelRings++;
	}

	public void updateStats()
	{
		updateTotals();
		runesPH = (int) getPerHour(totalSmokeRunes);
		totalProfit = (totalSmokeRunes * smokeRunesCost) - ((totalEssence * pureEssenceCost) + (totalAirRunes * airRuneCost) +
			(totalTalisman * airTalismanCost) + (totalDuelRings * duelRingCost));
		profitPH = (int) getPerHour(totalProfit);
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

	private void sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		log.info("Sleeping for {}ms", sleepLength);
		utils.sleep(sleepLength);
	}

	private int tickDelay()
	{
		tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.info("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private void handleMouseClick()
	{
		executorService.submit(() ->
		{
			try
			{
				sleepDelay();
				utils.clickRandomPointCenter(-100, 100);
			}
			catch (RuntimeException e)
			{
				e.printStackTrace();
			}
		});
	}

	private void teleportCastleWars()
	{
		if (utils.isItemEquipped(DUEL_RINGS) || utils.isItemEquipped(Set.of(ItemID.RING_OF_DUELING1)))
		{
			targetMenu = new MenuEntry("", "", 3, MenuOpcode.CC_OP.getId(), -1,
				25362455, false);
			handleMouseClick();
		}
		else
		{
			log.info("Need to teleport but don't have a ring of dueling");
		}
	}

	private SmokeRuneCrafterState getDuelRingState()
	{
		if(utils.inventoryContains(DUEL_RINGS))
		{
			return EQUIP_DUEL_RING;
		}
		if(utils.bankContainsAnyOf(DUEL_RINGS))
		{
			bankItem = utils.getBankItemWidgetAnyOf(DUEL_RINGS);
			return WITHDRAW_ITEM;
		}
		return OUT_OF_ITEM; //out of duel rings
	}

	private SmokeRuneCrafterState getRequiredItemState()
	{
		if ((!utils.inventoryContains(ItemID.AIR_TALISMAN) && !utils.bankContains(ItemID.AIR_TALISMAN, 1)) ||
			(!utils.inventoryContains(ItemID.AIR_RUNE) && !utils.bankContains(ItemID.AIR_RUNE, 26)) ||
			(!utils.inventoryContains(ItemID.PURE_ESSENCE) && !utils.bankContains(ItemID.PURE_ESSENCE, 10)))
		{
			bankItem = null;
			return OUT_OF_ITEM;
		}
		if (!utils.inventoryContains(ItemID.AIR_TALISMAN))
		{
			bankItem = utils.getBankItemWidget(ItemID.AIR_TALISMAN);
			return WITHDRAW_ITEM;
		}
		if (!utils.inventoryContains(ItemID.AIR_RUNE))
		{
			bankItem = utils.getBankItemWidget(ItemID.AIR_RUNE);
			return WITHDRAW_ALL_ITEM;
		}
		if (!utils.inventoryContains(ItemID.PURE_ESSENCE))
		{
			bankItem = utils.getBankItemWidget(ItemID.PURE_ESSENCE);
			return WITHDRAW_ALL_ITEM;
		}

		return OUT_OF_ITEM;
	}

	private SmokeRuneCrafterState getState()
	{
		if (timeout > 0)
		{
			utils.handleRun(20,30);
			return TIMEOUT;
		}
		if (utils.iterating)
		{
			return ITERATING;
		}
		if (utils.isMoving(beforeLoc) || player.getAnimation() == 714) //teleport animation
		{
			return MOVING;
		}

		mysteriousRuins = utils.findNearestGameObject(34817); //Mysterious Ruins
		fireAltar = utils.findNearestGameObject(ObjectID.ALTAR_34764);
		bankChest = utils.findNearestGameObject(ObjectID.BANK_CHEST_4483);

		if (mysteriousRuins != null)
		{
			if (utils.inventoryContainsAllOf(REQUIRED_ITEMS))
			{
				return ENTER_MYSTERIOUS_RUINS;
			}
			else
			{
				return (utils.isItemEquipped(DUEL_RINGS) || utils.isItemEquipped(Set.of(ItemID.RING_OF_DUELING1))) ?
					TELEPORT_CASTLE_WARS : OUT_OF_ITEM;
			}
		}
		if (fireAltar != null)
		{
			if (utils.inventoryContainsAllOf(REQUIRED_ITEMS))
			{
				return (setTalisman) ? USE_FIRE_ALTAR : SET_TALISMAN;
			}
			else
			{
				return (utils.isItemEquipped(DUEL_RINGS) || utils.isItemEquipped(Set.of(ItemID.RING_OF_DUELING1))) ?
					TELEPORT_CASTLE_WARS : OUT_OF_ITEM;
			}
		}
		if (bankChest != null)
		{
			if (!utils.isBankOpen())
			{
				updateStats();
				return OPEN_BANK;
			}
			if (utils.isBankOpen())
			{
				if (utils.inventoryContainsAllOf(REQUIRED_ITEMS) && utils.isItemEquipped(DUEL_RINGS))
				{
					updateStats();
					return TELEPORT_DUEL_ARENA;
				}
				if (utils.inventoryFull())
				{
					return DEPOSIT_ALL;
				}
				if(!utils.isItemEquipped(DUEL_RINGS))
				{
					return getDuelRingState();
				}
				if(utils.inventoryContainsExcept(REQUIRED_ITEMS))
				{
					return DEPOSIT_ALL_EXCEPT;
				}
				return getRequiredItemState();
			}
		}
		return OUT_OF_AREA;
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
			log.debug(state.name());
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					break;
				case ITERATING:
					break;
				case MOVING:
					timeout = tickDelay();
					break;
				case ENTER_MYSTERIOUS_RUINS:
					targetMenu = new MenuEntry("", "", 34817, MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(),
						mysteriousRuins.getSceneMinLocation().getX(), mysteriousRuins.getSceneMinLocation().getY(), false);
					handleMouseClick();
					timeout = tickDelay();
					break;
				case TELEPORT_CASTLE_WARS:
					teleportCastleWars();
					timeout = tickDelay();
					break;
				case SET_TALISMAN:
					WidgetItem airTalisman = utils.getInventoryWidgetItem(ItemID.AIR_TALISMAN);
					targetMenu = new MenuEntry("Use", "Use", ItemID.AIR_TALISMAN, MenuOpcode.ITEM_USE.getId(),
						airTalisman.getIndex(),	9764864, false);
					handleMouseClick();
					setTalisman = true;
					break;
				case USE_FIRE_ALTAR:
					targetMenu = new MenuEntry("Use", "<col=ff9040>Air talisman<col=ffffff> -> <col=ffff>Altar",
						fireAltar.getId(), MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(), fireAltar.getSceneMinLocation().getX(),
						fireAltar.getSceneMinLocation().getY(), false);
					handleMouseClick();
					timeout = tickDelay();
					break;
				case OPEN_BANK:
					targetMenu = new MenuEntry("", "", bankChest.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(),
						bankChest.getSceneMinLocation().getX(), bankChest.getSceneMinLocation().getY(), false);
					handleMouseClick();
					timeout = tickDelay();
					break;
				case TELEPORT_DUEL_ARENA:
					targetMenu = new MenuEntry("", "", 2, MenuOpcode.CC_OP.getId(), -1,
						25362455, false);
					handleMouseClick();
					timeout = tickDelay();
					break;
				case DEPOSIT_ALL:
					utils.depositAll();
					break;
				case DEPOSIT_ALL_EXCEPT:
					utils.depositAllExcept(REQUIRED_ITEMS);
					break;
				case EQUIP_DUEL_RING:
					WidgetItem item = utils.getInventoryWidgetItem(DUEL_RINGS);
					if (item != null)
					{
						targetMenu = new MenuEntry("", "", 9, MenuOpcode.CC_OP_LOW_PRIORITY.getId(),
							item.getIndex(), 983043, false);
						handleMouseClick();
					}
					break;
				case WITHDRAW_ITEM:
					utils.withdrawItem(bankItem);
					break;
				case WITHDRAW_ALL_ITEM:
					utils.withdrawAllItem(bankItem);
					break;
				case OUT_OF_ITEM:
					utils.sendGameMessage("Out of required items. Stopping.");
					if(config.logout())
						utils.logout();
					startBot = false;
					break;
			}
			beforeLoc = player.getLocalLocation();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!startBot || targetMenu == null)
		{
			return;
		}
		if (utils.getRandomEvent()) //for random events
		{
			log.info("SmokeRuneCrafter plugin not overriding due to random event");
			return;
		}
		else
		{
			event.setMenuEntry(targetMenu);
			targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
		}
	}
}