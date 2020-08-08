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
package net.runelite.client.plugins.quickeater;

import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import org.pf4j.Extension;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Quick Eater",
	enabledByDefault = false,
	description = "Illumine - auto eat food and drink some potions below configured values",
	tags = {"illumine", "auto", "bot", "eat", "food", "potions", "stamina", "prayer"},
	type = PluginType.UTILITY
)
@Slf4j
public class QuickEaterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private QuickEaterConfiguration config;

	@Inject
	private BotUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	MenuEntry targetMenu;
	Player player;

	private Set<Integer> DRINK_SET = Set.of(ItemID.JUG_OF_WINE, ItemID.SARADOMIN_BREW1, ItemID.SARADOMIN_BREW2, ItemID.SARADOMIN_BREW3, ItemID.SARADOMIN_BREW4);
	private Set<Integer> PRAYER_SET = Set.of(ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4, ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4);
	private Set<Integer> POISON_SET = Set.of(ItemID.ANTIPOISON1, ItemID.ANTIPOISON2, ItemID.ANTIPOISON3, ItemID.ANTIPOISON4, ItemID.SUPERANTIPOISON1, ItemID.SUPERANTIPOISON2, ItemID.SUPERANTIPOISON3, ItemID.SUPERANTIPOISON4,
		ItemID.ANTIDOTE1, ItemID.ANTIDOTE2, ItemID.ANTIDOTE3, ItemID.ANTIDOTE4, ItemID.ANTIDOTE1_5958, ItemID.ANTIDOTE2_5956, ItemID.ANTIDOTE3_5954, ItemID.ANTIDOTE4_5952);

	private int timeout;
	private int prayerDrinkTimeout;
	private int drinkEnergy;
	private int nextEatHP;
	private int drinkPrayer;

	@Provides
	QuickEaterConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QuickEaterConfiguration.class);
	}

	@Override
	protected void startUp()
	{
		nextEatHP = utils.getRandomIntBetweenRange(config.minEatHP(), config.maxEatHP());
		drinkPrayer = utils.getRandomIntBetweenRange(config.minPrayerPoints(), config.maxPrayerPoints());
	}

	@Override
	protected void shutDown()
	{

	}

	private void useItem(WidgetItem item)
	{
		if (item != null)
		{
			targetMenu = new MenuEntry("", "", item.getId(), MenuOpcode.ITEM_FIRST_OPTION.getId(), item.getIndex(),
				9764864, false);
			utils.delayMouseClick(item.getCanvasBounds(), utils.getRandomIntBetweenRange(25, 200));
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event)
	{
		if (config.drinkAntiPoison() && event.getIndex() == VarPlayer.POISON.getId() && client.getVarpValue(VarPlayer.POISON.getId()) > 0)
		{
			if (utils.inventoryContains(POISON_SET))
			{
				log.info("Drinking anti-poison");
				WidgetItem poisonItem = utils.getInventoryWidgetItem(POISON_SET);
				useItem(poisonItem);
			}
			else
			{
				utils.sendGameMessage("You are Poisoned but missing anti-poison");
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (prayerDrinkTimeout > 0)
		{
			prayerDrinkTimeout--;
		}
		if (!config.drinkStamina())
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			if (timeout > 0)
			{
				timeout--;
				return;
			}
			if (drinkEnergy == 0)
			{
				drinkEnergy = utils.getRandomIntBetweenRange(config.maxDrinkEnergy() - config.randEnergy(), config.maxDrinkEnergy());
			}
			if (client.getEnergy() < drinkEnergy)
			{
				utils.drinkStamPot();
			}
		}
	}

	@Subscribe
	private void onHitsplatApplied(HitsplatApplied event)
	{
		if (event.getActor() != client.getLocalPlayer() || client.getBoostedSkillLevel(Skill.HITPOINTS) > nextEatHP)
		{
			return;
		}
		WidgetItem eatItem = utils.getInventoryItemMenu(itemManager, "Eat", 33,
			Set.of(ItemID.DWARVEN_ROCK_CAKE, ItemID.DWARVEN_ROCK_CAKE_7510));
		if (eatItem != null)
		{
			useItem(eatItem);
			nextEatHP = utils.getRandomIntBetweenRange(config.minEatHP(), config.maxEatHP());
			log.debug("Next Eat HP: {}", nextEatHP);
			return;
		}
		if (utils.inventoryContains(DRINK_SET))
		{
			WidgetItem drinkItem = utils.getInventoryWidgetItem(DRINK_SET);
			useItem(drinkItem);
			nextEatHP = utils.getRandomIntBetweenRange(config.minEatHP(), config.maxEatHP());
			log.debug("Next Eat HP: {}", nextEatHP);
			return;
		}
		utils.sendGameMessage("Health is below threshold but we're out of food");
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (!event.getSkill().equals(Skill.PRAYER) || !config.drinkPrayer() ||
			event.getBoostedLevel() > drinkPrayer ||
			prayerDrinkTimeout > 0)
		{
			return;
		}
		if (utils.inventoryContains(PRAYER_SET))
		{
			WidgetItem prayerItem = utils.getInventoryWidgetItem(PRAYER_SET);
			useItem(prayerItem);
			drinkPrayer = utils.getRandomIntBetweenRange(config.minPrayerPoints(), config.maxPrayerPoints());
			prayerDrinkTimeout = 3;
		}
		else
		{
			utils.sendGameMessage("Prayer is below threshold but we have nothing to regain prayer");
		}
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged event)
	{
		/* When logging in thr stat changed event is triggered for all skills and can send a false value of 0 even if the stat is full,
		causing a prayer pot to be incorrectly consumed if enabled. Setting a timeout on login ensures this doesn't occur */
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			prayerDrinkTimeout = 2;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (targetMenu == null)
		{
			return;
		}
		if (utils.getRandomEvent()) //for random events
		{
			log.debug("Quick Eater not overriding due to random event");
			return;
		}
		else
		{
			event.setMenuEntry(targetMenu);
			targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
		}
	}
}