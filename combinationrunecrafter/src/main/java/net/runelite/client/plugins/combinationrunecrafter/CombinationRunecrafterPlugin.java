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
package net.runelite.client.plugins.combinationrunecrafter;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import static net.runelite.client.plugins.combinationrunecrafter.CombinationRunecrafterState.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Combination Runecrafter Plugin",
	enabledByDefault = false,
	description = "Illumine - Combination Runecrafting plugin",
	tags = {"illumine", "runecrafting", "bot", "smoke", "steam", "lava", "combination"},
	type = PluginType.SKILLING
)
@Slf4j
public class CombinationRunecrafterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private CombinationRunecrafterConfig config;

	@Inject
	private BotUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CombinationRunecrafterOverlay overlay;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	CombinationRunecrafterState state;
	CombinationRunecrafterState necklaceState;
	CombinationRunecrafterState staminaState;

	LocalPoint beforeLoc = new LocalPoint(0, 0);
	GameObject bankChest;
	GameObject mysteriousRuins;
	GameObject fireAltar;
	Widget bankItem;
	WidgetItem useableItem;

	Set<Integer> DUEL_RINGS = Set.of(ItemID.RING_OF_DUELING2, ItemID.RING_OF_DUELING3, ItemID.RING_OF_DUELING4, ItemID.RING_OF_DUELING5, ItemID.RING_OF_DUELING6, ItemID.RING_OF_DUELING7, ItemID.RING_OF_DUELING8);
	Set<Integer> BINDING_NECKLACE = Set.of(ItemID.BINDING_NECKLACE);
	Set<Integer> STAMINA_POTIONS = Set.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4);
	Set<Integer> TIARAS = Set.of(ItemID.FIRE_TIARA);
	List<Integer> REQUIRED_ITEMS = new ArrayList<>();

	boolean startBot;
	boolean setTalisman;
	boolean outOfNecklaces;
	boolean outOfStaminaPots;
	long sleepLength;
	int tickLength;
	int timeout;
	int coinsPH;
	int beforeEssence;
	int totalEssence;
	int beforeMaterialRunes;
	int totalMaterialRunes;
	int beforeTalisman;
	int totalTalisman;
	int totalCraftedRunes;
	int beforeCraftedRunes;
	int currentCraftedRunes;
	int totalDuelRings;
	int totalNecklaces;
	int totalStaminaPots;
	int runesPH;
	int profitPH;
	int totalProfit;
	int runesCost;
	int essenceCost;
	int talismanCost;
	int duelRingCost;
	int necklaceCost;
	int staminaPotCost;
	int materialRuneCost;
	int essenceTypeID;
	int talismanID;
	int materialRuneID;
	int createdRuneTypeID;

	@Provides
	CombinationRunecrafterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CombinationRunecrafterConfig.class);
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
		log.info("stopping Combination Runecrafting plugin");
		chinBreakHandler.stopPlugin(this);
		startBot = false;
		botTimer = null;
		overlayManager.remove(overlay);
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("CombinationRunecrafter"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startBot)
			{
				startBot = true;
				chinBreakHandler.startPlugin(this);
				botTimer = Instant.now();
				initCounters();
				state = null;
				necklaceState = null;
				targetMenu = null;
				setTalisman = false;
				createdRuneTypeID = config.getRunecraftingType().getCreatedRuneID();
				talismanID = config.getRunecraftingType().getTalismanID();
				materialRuneID = config.getRunecraftingType().getMaterialRuneID();
				essenceTypeID = config.getEssence().getId();
				REQUIRED_ITEMS = List.of(talismanID, materialRuneID, essenceTypeID);
				updatePrices();
				botTimer = Instant.now();
				overlayManager.add(overlay);
			}
			else
			{
				resetVals();
			}
		}
	}

	@Subscribe
	private void onConfigChange(ConfigChanged event)
	{
		if (!event.getGroup().equals("CombinationRunecrafter"))
		{
			return;
		}
		switch (event.getKey())
		{
			case "getEssence":
				essenceTypeID = config.getEssence().getId();
				essenceCost = (essenceTypeID != ItemID.DAEYALT_ESSENCE) ?
					utils.getOSBItem(essenceTypeID).getOverall_average() : 0;
				break;
			case "getRunecraftingType":
				createdRuneTypeID = config.getRunecraftingType().getCreatedRuneID();
				talismanID = config.getRunecraftingType().getTalismanID();
				materialRuneID = config.getRunecraftingType().getMaterialRuneID();
				break;
		}
		setTalisman = false;
		REQUIRED_ITEMS = List.of(talismanID, materialRuneID, essenceTypeID);
		updatePrices();
	}

	private void initCounters()
	{
		timeout = 0;
		coinsPH = 0;
		beforeEssence = 0;
		totalEssence = 0;
		beforeMaterialRunes = 0;
		totalMaterialRunes = 0;
		beforeTalisman = 0;
		totalTalisman = 0;
		beforeCraftedRunes = 0;
		totalCraftedRunes = 0;
		totalDuelRings = 0;
		totalNecklaces = 0;
		totalStaminaPots = 0;
		runesPH = 0;
		profitPH = 0;
		totalProfit = 0;
		currentCraftedRunes = 0;
	}

	private void updatePrices()
	{
		runesCost = utils.getOSBItem(createdRuneTypeID).getOverall_average();
		essenceCost = (essenceTypeID != ItemID.DAEYALT_ESSENCE) ?
			utils.getOSBItem(essenceTypeID).getOverall_average() : 0;
		talismanCost = utils.getOSBItem(talismanID).getOverall_average();
		duelRingCost = utils.getOSBItem(ItemID.RING_OF_DUELING8).getOverall_average();
		materialRuneCost = utils.getOSBItem(materialRuneID).getOverall_average();
		necklaceCost = utils.getOSBItem(ItemID.BINDING_NECKLACE).getOverall_average();
		staminaPotCost = utils.getOSBItem(ItemID.STAMINA_POTION4).getOverall_average();
		log.info("Item prices set to at - Crafted Runes: {}gp, Essence: {}gp, Talisman: {}gp, " +
				"Ring of Dueling {}gp, Material Runes: {}gp, Binding Necklace: {}gp, Stamina Potion (4): {}gp",
			runesCost, essenceCost, talismanCost, duelRingCost, materialRuneCost, necklaceCost, staminaPotCost);
	}

	private int itemTotals(int itemID, int beforeAmount, boolean stackableItem)
	{
		int currentAmount = utils.getInventoryItemCount(itemID, stackableItem);
		return (beforeAmount > currentAmount) ? beforeAmount - currentAmount : 0;
	}

	private void updateTotals()
	{
		totalEssence += itemTotals(essenceTypeID, beforeEssence, false);
		beforeEssence = utils.getInventoryItemCount(essenceTypeID, false);

		totalMaterialRunes += itemTotals(materialRuneID, beforeMaterialRunes, true);
		beforeMaterialRunes = utils.getInventoryItemCount(materialRuneID, true);

		totalTalisman += itemTotals(talismanID, beforeTalisman, true);
		beforeTalisman = utils.getInventoryItemCount(talismanID, true);

		currentCraftedRunes = utils.getInventoryItemCount(createdRuneTypeID, true);
		if (beforeCraftedRunes < currentCraftedRunes)
		{
			totalCraftedRunes += currentCraftedRunes;
		}
		beforeCraftedRunes = currentCraftedRunes;

		if (!utils.isItemEquipped(DUEL_RINGS) || utils.isItemEquipped(Set.of(ItemID.RING_OF_DUELING1)))
		{
			totalDuelRings++;
		}

		if (config.bindingNecklace() && !outOfNecklaces && !utils.isItemEquipped(BINDING_NECKLACE))
		{
			totalNecklaces++;
		}
	}

	public void updateStats()
	{
		updateTotals();
		runesPH = (int) getPerHour(totalCraftedRunes);
		totalProfit = (int) ((totalCraftedRunes * runesCost) - ((totalEssence * essenceCost) + (totalMaterialRunes * materialRuneCost) +
			(totalTalisman * talismanCost) + (totalDuelRings * duelRingCost) + (totalNecklaces * necklaceCost) +
			((totalStaminaPots * 0.25) * staminaPotCost)));
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

	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.info("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private void teleportRingOfDueling(int menuIdentifier)
	{
		targetMenu = new MenuEntry("", "", menuIdentifier, MenuOpcode.CC_OP.getId(), -1,
			25362455, false);
		Widget ringWidget = client.getWidget(WidgetInfo.EQUIPMENT_RING);
		if (ringWidget != null)
		{
			utils.delayMouseClick(ringWidget.getBounds(), sleepDelay());
		}
		else
		{
			utils.delayClickRandomPointCenter(-200, 200, sleepDelay());
		}
	}

	private CombinationRunecrafterState getItemState(Set<Integer> itemIDs)
	{
		if (utils.inventoryContains(itemIDs))
		{
			useableItem = utils.getInventoryWidgetItem(itemIDs);
			return ACTION_ITEM;
		}
		if (utils.bankContainsAnyOf(itemIDs))
		{
			bankItem = utils.getBankItemWidgetAnyOf(itemIDs);
			return WITHDRAW_ITEM;
		}
		return OUT_OF_ITEM;
	}

	private boolean shouldSipStamina()
	{
		return (config.staminaPotion() && client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0) &&
			(client.getEnergy() <= (75 - utils.getRandomIntBetweenRange(0, 40)) ||
				(utils.inventoryContains(STAMINA_POTIONS) && client.getEnergy() < 75));
	}

	private CombinationRunecrafterState getRequiredItemState()
	{
		if ((!utils.inventoryContains(talismanID) && !utils.bankContains(talismanID, 1)) ||
			(!utils.inventoryContains(materialRuneID) && !utils.bankContains(materialRuneID, 26)) ||
			(!utils.inventoryContains(essenceTypeID) && !utils.bankContains(essenceTypeID, 10)))
		{
			bankItem = null;
			return OUT_OF_ITEM;
		}
		for (int itemID : REQUIRED_ITEMS)
		{
			if (!utils.inventoryContains(itemID))
			{
				bankItem = utils.getBankItemWidget(itemID);
				return (itemID == talismanID) ? WITHDRAW_ITEM : WITHDRAW_ALL_ITEM;
			}
		}
		return OUT_OF_ITEM;
	}

	private CombinationRunecrafterState getState()
	{
		if (timeout > 0)
		{
			utils.handleRun(20, 30);
			return TIMEOUT;
		}
		if (utils.iterating)
		{
			return ITERATING;
		}
		if (utils.isMoving(beforeLoc) || player.getAnimation() == 714) //teleport animation
		{
			utils.handleRun(20, 30);
			return MOVING;
		}
		if (!utils.isItemEquipped(TIARAS))
		{
			utils.sendGameMessage("Fire Tiara not equipped. Stopping.");
			return OUT_OF_ITEM;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			return HANDLE_BREAK;
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
				if (!utils.isItemEquipped(DUEL_RINGS))
				{
					return getItemState(DUEL_RINGS);
				}
				if (config.bindingNecklace() && !utils.isItemEquipped(BINDING_NECKLACE))
				{
					necklaceState = getItemState(BINDING_NECKLACE);
					if (!(necklaceState == OUT_OF_ITEM && !config.stopNecklace()))
					{
						return necklaceState;
					}
					else
					{
						outOfNecklaces = true;
					}
				}
				if (shouldSipStamina())
				{
					staminaState = getItemState(STAMINA_POTIONS);
					if (!(staminaState == OUT_OF_ITEM && !config.stopStamina()))
					{
						return staminaState;
					}
					else
					{
						outOfStaminaPots = true;
					}
				}
				if (utils.inventoryContainsExcept(REQUIRED_ITEMS))
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
					utils.delayMouseClick(mysteriousRuins.getConvexHull().getBounds(), sleepDelay());
					timeout = tickDelay();
					break;
				case TELEPORT_CASTLE_WARS:
					teleportRingOfDueling(3);
					timeout = tickDelay();
					break;
				case SET_TALISMAN:
					WidgetItem airTalisman = utils.getInventoryWidgetItem(talismanID);
					targetMenu = new MenuEntry("Use", "Use", talismanID, MenuOpcode.ITEM_USE.getId(),
						airTalisman.getIndex(), 9764864, false);
					utils.delayMouseClick(airTalisman.getCanvasBounds(), sleepDelay());
					setTalisman = true;
					break;
				case USE_FIRE_ALTAR:
					targetMenu = new MenuEntry("Use", "<col=ff9040>Air talisman<col=ffffff> -> <col=ffff>Altar",
						fireAltar.getId(), MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(), fireAltar.getSceneMinLocation().getX(),
						fireAltar.getSceneMinLocation().getY(), false);
					utils.delayMouseClick(fireAltar.getConvexHull().getBounds(), sleepDelay());
					timeout = tickDelay();
					break;
				case OPEN_BANK:
					targetMenu = new MenuEntry("", "", bankChest.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(),
						bankChest.getSceneMinLocation().getX(), bankChest.getSceneMinLocation().getY(), false);
					utils.delayMouseClick(bankChest.getConvexHull().getBounds(), sleepDelay());
					timeout = tickDelay();
					break;
				case TELEPORT_DUEL_ARENA:
					teleportRingOfDueling(2);
					timeout = tickDelay();
					break;
				case DEPOSIT_ALL:
					utils.depositAll();
					break;
				case DEPOSIT_ALL_EXCEPT:
					utils.depositAllExcept(REQUIRED_ITEMS);
					break;
				case ACTION_ITEM:
					if (useableItem != null)
					{
						if (STAMINA_POTIONS.contains(useableItem.getId()))
						{
							totalStaminaPots++;
						}
						targetMenu = new MenuEntry("", "", 9, MenuOpcode.CC_OP_LOW_PRIORITY.getId(),
							useableItem.getIndex(), 983043, false);
						utils.delayMouseClick(useableItem.getCanvasBounds(), sleepDelay());
					}
					break;
				case WITHDRAW_ITEM:
					utils.withdrawItem(bankItem);
					break;
				case WITHDRAW_ALL_ITEM:
					utils.withdrawAllItem(bankItem);
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					setTalisman = false;
					timeout = 10;
					break;
				case OUT_OF_ITEM:
					utils.sendGameMessage("Out of required items. Stopping.");
					if (config.logout())
					{
						utils.logout();
					}
					startBot = false;
					resetVals();
					break;
			}
			beforeLoc = player.getLocalLocation();
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!startBot)
		{
			return;
		}
		if (event.getOpcode() == MenuOpcode.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
			event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
		{
			//Either logging out or world-hopping which is handled by 3rd party plugins so let them have priority
			log.info("Received world-hop/login related click. Giving them priority");
			targetMenu = null;
			return;
		}
		if (config.disableMouse())
		{
			event.consume();
		}
		if (utils.getRandomEvent()) //for random events
		{
			log.debug("Combination Runecrafter plugin not overriding due to random event");
		}
		else
		{
			if (targetMenu != null)
			{
				client.invokeMenuAction(targetMenu.getOption(), targetMenu.getTarget(), targetMenu.getIdentifier(),
					targetMenu.getOpcode(), targetMenu.getParam0(), targetMenu.getParam1());
				targetMenu = null;
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (!startBot)
		{
			return;
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			setTalisman = false;
			state = TIMEOUT;
			timeout = 2;
		}
	}
}
