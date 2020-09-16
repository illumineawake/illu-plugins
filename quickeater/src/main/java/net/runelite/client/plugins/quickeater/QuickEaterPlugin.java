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
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.WidgetInfo;
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

	private final Set<Integer> IGNORE_FOOD = Set.of(ItemID.DWARVEN_ROCK_CAKE, ItemID.DWARVEN_ROCK_CAKE_7510);
	private final Set<Integer> DRINK_SET = Set.of(ItemID.JUG_OF_WINE, ItemID.SARADOMIN_BREW1, ItemID.SARADOMIN_BREW2, ItemID.SARADOMIN_BREW3, ItemID.SARADOMIN_BREW4, ItemID.BANDAGES);
	private final Set<Integer> POISON_SET = Set.of(ItemID.ANTIPOISON1, ItemID.ANTIPOISON2, ItemID.ANTIPOISON3, ItemID.ANTIPOISON4, ItemID.SUPERANTIPOISON1, ItemID.SUPERANTIPOISON2, ItemID.SUPERANTIPOISON3, ItemID.SUPERANTIPOISON4,
		ItemID.ANTIDOTE1, ItemID.ANTIDOTE2, ItemID.ANTIDOTE3, ItemID.ANTIDOTE4, ItemID.ANTIDOTE1_5958, ItemID.ANTIDOTE2_5956, ItemID.ANTIDOTE3_5954, ItemID.ANTIDOTE4_5952);
	private final Set<Integer> PRAYER_SET = Set.of(ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4,
		ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4, ItemID.BLIGHTED_SUPER_RESTORE1,
		ItemID.BLIGHTED_SUPER_RESTORE2, ItemID.BLIGHTED_SUPER_RESTORE3, ItemID.BLIGHTED_SUPER_RESTORE4, ItemID.EGNIOL_POTION_1,
		ItemID.EGNIOL_POTION_2,ItemID.EGNIOL_POTION_3,ItemID.EGNIOL_POTION_4);
	private final Set<Integer> STRENGTH_SET = Set.of(ItemID.STRENGTH_POTION1, ItemID.STRENGTH_POTION2, ItemID.STRENGTH_POTION3, ItemID.STRENGTH_POTION4,
		ItemID.SUPER_STRENGTH1, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH4,
		ItemID.DIVINE_SUPER_STRENGTH_POTION1, ItemID.DIVINE_SUPER_STRENGTH_POTION2, ItemID.DIVINE_SUPER_STRENGTH_POTION3, ItemID.DIVINE_SUPER_STRENGTH_POTION4,
		ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4);
	private final Set<Integer> ATTACK_SET = Set.of(ItemID.ATTACK_POTION1, ItemID.ATTACK_POTION2, ItemID.ATTACK_POTION3, ItemID.ATTACK_POTION4,
		ItemID.SUPER_ATTACK1, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK4,
		ItemID.DIVINE_SUPER_ATTACK_POTION1, ItemID.DIVINE_SUPER_ATTACK_POTION2, ItemID.DIVINE_SUPER_ATTACK_POTION3, ItemID.DIVINE_SUPER_ATTACK_POTION4,
		ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4);
	private final Set<Integer> DEFENCE_SET = Set.of(ItemID.DEFENCE_POTION1, ItemID.DEFENCE_POTION2, ItemID.DEFENCE_POTION3, ItemID.DEFENCE_POTION4,
		ItemID.SUPER_DEFENCE1, ItemID.SUPER_DEFENCE2, ItemID.SUPER_DEFENCE3, ItemID.SUPER_DEFENCE4,
		ItemID.DIVINE_SUPER_DEFENCE_POTION1, ItemID.DIVINE_SUPER_DEFENCE_POTION2, ItemID.DIVINE_SUPER_DEFENCE_POTION3, ItemID.DIVINE_SUPER_DEFENCE_POTION4,
		ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4);
	private final Set<Integer> RANGED_SET = Set.of(ItemID.RANGING_POTION1, ItemID.RANGING_POTION2, ItemID.RANGING_POTION3, ItemID.RANGING_POTION4,
		ItemID.BASTION_POTION1, ItemID.BASTION_POTION2, ItemID.BASTION_POTION3, ItemID.BASTION_POTION4,
		ItemID.DIVINE_RANGING_POTION1, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION4,
		ItemID.DIVINE_BASTION_POTION1, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION4);
	private final Set<Integer> MAGIC_SET = Set.of(ItemID.MAGIC_POTION1, ItemID.MAGIC_POTION2, ItemID.MAGIC_POTION3, ItemID.MAGIC_POTION4,
		ItemID.BATTLEMAGE_POTION1, ItemID.BATTLEMAGE_POTION2, ItemID.BATTLEMAGE_POTION3, ItemID.BATTLEMAGE_POTION4,
		ItemID.DIVINE_MAGIC_POTION1, ItemID.DIVINE_MAGIC_POTION2, ItemID.DIVINE_MAGIC_POTION3, ItemID.DIVINE_MAGIC_POTION4,
		ItemID.DIVINE_BATTLEMAGE_POTION1, ItemID.DIVINE_BATTLEMAGE_POTION2, ItemID.DIVINE_BATTLEMAGE_POTION3, ItemID.DIVINE_BATTLEMAGE_POTION4);

	private int timeout;
	private int drinkTimeout;
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
				WidgetInfo.INVENTORY.getId(), false);
			utils.setMenuEntry(targetMenu);
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
				log.debug("Drinking anti-poison");
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
		if (drinkTimeout > 0)
		{
			drinkTimeout--;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			if (timeout > 0)
			{
				timeout--;
				return;
			}
			if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= nextEatHP)
			{
				WidgetItem eatItem = utils.getInventoryItemMenu(itemManager, "Eat", 33,
					IGNORE_FOOD);
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
			if (config.drinkStamina() && drinkTimeout == 0)
			{
				if (drinkEnergy == 0)
				{
					drinkEnergy = utils.getRandomIntBetweenRange(config.maxDrinkEnergy() - config.randEnergy(), config.maxDrinkEnergy());
					log.debug("Max drink energy: {}, Rand drink value: {}, Next drink energy: {}",config.maxDrinkEnergy(), config.randEnergy(), drinkEnergy);
				}
				if (client.getEnergy() < drinkEnergy)
				{
					utils.drinkStamPot();
					drinkEnergy = utils.getRandomIntBetweenRange(config.maxDrinkEnergy() - config.randEnergy(), config.maxDrinkEnergy());
					log.debug("Max drink energy: {}, Rand drink value: {}, Next drink energy: {}",config.maxDrinkEnergy(), config.randEnergy(), drinkEnergy);
					drinkTimeout = 2;
				}
			}
			if(config.keepPNeckEquipped())
			{
				timeout+=4;
				if(utils.inventoryContains(11090))
				{
					if(utils.getEquippedItems()!=null && utils.getEquippedItems().get(2).getId()!=11090)
					{
						targetMenu = new MenuEntry("Wear", "Wear", 11090, MenuOpcode.ITEM_SECOND_OPTION.getId(), utils.getInventoryWidgetItem(11090).getIndex(),
								WidgetInfo.INVENTORY.getId(), false);
						utils.setMenuEntry(targetMenu);
						utils.delayMouseClick(utils.getInventoryWidgetItem(11090).getCanvasBounds(), utils.getRandomIntBetweenRange(25, 200));
					}
				} else {
					utils.sendGameMessage("No phoenix necklaces in inventory.");
				}
			}
		}
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged event)
	{
		/* When logging in thr stat changed event is triggered for all skills and can send a false value of 0 even if the stat is full,
		causing a prayer pot to be incorrectly consumed if enabled. Setting a timeout on login ensures this doesn't occur */
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			drinkTimeout = 4;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		checkSkill(event.getSkill(), event.getBoostedLevel());
	}

	private void checkSkill(Skill skill, int boostedLevel)
	{
		switch (skill)
		{
			case PRAYER:
				if (config.drinkPrayer() && drinkPot(skill, boostedLevel, PRAYER_SET, drinkPrayer))
				{
					drinkPrayer = utils.getRandomIntBetweenRange(config.minPrayerPoints(), config.maxPrayerPoints());
				}
				break;
			case STRENGTH:
			case ATTACK:
			case DEFENCE:
				if (config.drinkStrength())
				{
					drinkPot(Skill.STRENGTH, client.getBoostedSkillLevel(Skill.STRENGTH), STRENGTH_SET, config.strengthPoints());
				}
				if (config.drinkAttack())
				{
					drinkPot(Skill.ATTACK, client.getBoostedSkillLevel(Skill.ATTACK), ATTACK_SET, config.attackPoints());
				}
				if (config.drinkDefence())
				{
					drinkPot(Skill.DEFENCE, client.getBoostedSkillLevel(Skill.DEFENCE), DEFENCE_SET, config.defencePoints());
				}
				break;
			case RANGED:
				if (config.drinkRanged())
				{
					drinkPot(skill, boostedLevel, RANGED_SET, config.rangedPoints());
				}
				if (config.drinkDefence())
				{
					drinkPot(Skill.DEFENCE, client.getBoostedSkillLevel(Skill.DEFENCE), DEFENCE_SET, config.defencePoints());
				}
				break;
			case MAGIC:
				if (config.drinkMagic())
				{
					drinkPot(skill, boostedLevel, MAGIC_SET, config.magicPoints());
				}
				if (config.drinkDefence())
				{
					drinkPot(Skill.DEFENCE, client.getBoostedSkillLevel(Skill.DEFENCE), DEFENCE_SET, config.defencePoints());
				}
				break;
		}
	}

	private boolean drinkPot(Skill skill, int boostedLevel, Set<Integer> itemSet, int drinkPotLevel)
	{
		if (boostedLevel == 0 || boostedLevel > drinkPotLevel)
		{
			return false;
		}
		if (utils.inventoryContains(itemSet) && drinkTimeout == 0)
		{
			WidgetItem itemToDrink = utils.getInventoryWidgetItem(itemSet);
			useItem(itemToDrink);
			drinkTimeout = 4;
			return true;
		}
		if (drinkTimeout == 0)
		{
			utils.sendGameMessage(skill + " is below threshold but we have nothing to regain " + skill);
		}
		return false;
	}
}
