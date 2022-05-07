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
package net.runelite.client.plugins.iquickeater;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.util.LegacyInventoryAssistant;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Set;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iQuick Eater",
        enabledByDefault = false,
        description = "Illumine - auto eat food and drink some potions below configured values",
        tags = {"illumine", "auto", "bot", "eat", "food", "potions", "stamina", "prayer"}
)
@Slf4j
public class iQuickEaterPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private iQuickEaterConfiguration config;

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
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    LegacyInventoryAssistant inventoryAssistant;

    LegacyMenuEntry targetMenu;
    Player player;

    private final Set<Integer> IGNORE_FOOD = Set.of(ItemID.DWARVEN_ROCK_CAKE, ItemID.DWARVEN_ROCK_CAKE_7510);
    private final Set<Integer> DRINK_SET = Set.of(ItemID.JUG_OF_WINE, ItemID.SARADOMIN_BREW1, ItemID.SARADOMIN_BREW2, ItemID.SARADOMIN_BREW3, ItemID.SARADOMIN_BREW4, ItemID.XERICS_AID_1, ItemID.XERICS_AID_2, ItemID.XERICS_AID_3, ItemID.XERICS_AID_4, ItemID.XERICS_AID_1_20977, ItemID.XERICS_AID_2_20978, ItemID.XERICS_AID_3_20979, ItemID.XERICS_AID_4_20980, ItemID.XERICS_AID_1_20981, ItemID.XERICS_AID_2_20982, ItemID.XERICS_AID_3_20983, ItemID.XERICS_AID_4_20984, ItemID.BANDAGES);

    private final Set<Integer> POISON_SET = Set.of(ItemID.ANTIPOISON1, ItemID.ANTIPOISON2, ItemID.ANTIPOISON3, ItemID.ANTIPOISON4, ItemID.SUPERANTIPOISON1, ItemID.SUPERANTIPOISON2, ItemID.SUPERANTIPOISON3, ItemID.SUPERANTIPOISON4,
            ItemID.ANTIDOTE1, ItemID.ANTIDOTE2, ItemID.ANTIDOTE3, ItemID.ANTIDOTE4, ItemID.ANTIDOTE1_5958, ItemID.ANTIDOTE2_5956, ItemID.ANTIDOTE3_5954, ItemID.ANTIDOTE4_5952,
            ItemID.ANTIVENOM1, ItemID.ANTIVENOM2, ItemID.ANTIVENOM3, ItemID.ANTIVENOM4, ItemID.ANTIVENOM4_12913, ItemID.ANTIVENOM3_12915, ItemID.ANTIVENOM2_12917, ItemID.ANTIVENOM1_12919);
    private final Set<Integer> PRAYER_SET = Set.of(ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4,
            ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4, ItemID.BLIGHTED_SUPER_RESTORE1,
            ItemID.BLIGHTED_SUPER_RESTORE2, ItemID.BLIGHTED_SUPER_RESTORE3, ItemID.BLIGHTED_SUPER_RESTORE4, ItemID.EGNIOL_POTION_1,
            ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4, ItemID.REVITALISATION_1, ItemID.REVITALISATION_2,
            ItemID.REVITALISATION_3, ItemID.REVITALISATION_4, ItemID.REVITALISATION_1_20957, ItemID.REVITALISATION_2_20958, ItemID.REVITALISATION_3_20959,
            ItemID.REVITALISATION_4_20960, ItemID.REVITALISATION_POTION_1, ItemID.REVITALISATION_POTION_2, ItemID.REVITALISATION_POTION_3, ItemID.REVITALISATION_POTION_4);
    private final Set<Integer> STRENGTH_SET = Set.of(ItemID.STRENGTH_POTION1, ItemID.STRENGTH_POTION2, ItemID.STRENGTH_POTION3, ItemID.STRENGTH_POTION4,
            ItemID.SUPER_STRENGTH1, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH4,
            ItemID.DIVINE_SUPER_STRENGTH_POTION1, ItemID.DIVINE_SUPER_STRENGTH_POTION2, ItemID.DIVINE_SUPER_STRENGTH_POTION3, ItemID.DIVINE_SUPER_STRENGTH_POTION4,
            ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4,
            ItemID.COMBAT_POTION1, ItemID.COMBAT_POTION2, ItemID.COMBAT_POTION3, ItemID.COMBAT_POTION4,
            ItemID.SUPER_COMBAT_POTION1, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION4);
    private final Set<Integer> ATTACK_SET = Set.of(ItemID.ATTACK_POTION1, ItemID.ATTACK_POTION2, ItemID.ATTACK_POTION3, ItemID.ATTACK_POTION4,
            ItemID.SUPER_ATTACK1, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK4,
            ItemID.DIVINE_SUPER_ATTACK_POTION1, ItemID.DIVINE_SUPER_ATTACK_POTION2, ItemID.DIVINE_SUPER_ATTACK_POTION3, ItemID.DIVINE_SUPER_ATTACK_POTION4,
            ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4,
            ItemID.COMBAT_POTION1, ItemID.COMBAT_POTION2, ItemID.COMBAT_POTION3, ItemID.COMBAT_POTION4,
            ItemID.SUPER_COMBAT_POTION1, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION4);
    private final Set<Integer> DEFENCE_SET = Set.of(ItemID.DEFENCE_POTION1, ItemID.DEFENCE_POTION2, ItemID.DEFENCE_POTION3, ItemID.DEFENCE_POTION4,
            ItemID.SUPER_DEFENCE1, ItemID.SUPER_DEFENCE2, ItemID.SUPER_DEFENCE3, ItemID.SUPER_DEFENCE4,
            ItemID.DIVINE_SUPER_DEFENCE_POTION1, ItemID.DIVINE_SUPER_DEFENCE_POTION2, ItemID.DIVINE_SUPER_DEFENCE_POTION3, ItemID.DIVINE_SUPER_DEFENCE_POTION4,
            ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4,
            ItemID.SUPER_COMBAT_POTION1, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION4);
    private final Set<Integer> RANGED_SET = Set.of(ItemID.RANGING_POTION1, ItemID.RANGING_POTION2, ItemID.RANGING_POTION3, ItemID.RANGING_POTION4,
            ItemID.BASTION_POTION1, ItemID.BASTION_POTION2, ItemID.BASTION_POTION3, ItemID.BASTION_POTION4,
            ItemID.DIVINE_RANGING_POTION1, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION4,
            ItemID.DIVINE_BASTION_POTION1, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION4,
            ItemID.SUPER_RANGING_1, ItemID.SUPER_RANGING_2, ItemID.SUPER_RANGING_3, ItemID.SUPER_RANGING_4);
    private final Set<Integer> MAGIC_SET = Set.of(ItemID.MAGIC_POTION1, ItemID.MAGIC_POTION2, ItemID.MAGIC_POTION3, ItemID.MAGIC_POTION4,
            ItemID.BATTLEMAGE_POTION1, ItemID.BATTLEMAGE_POTION2, ItemID.BATTLEMAGE_POTION3, ItemID.BATTLEMAGE_POTION4,
            ItemID.DIVINE_MAGIC_POTION1, ItemID.DIVINE_MAGIC_POTION2, ItemID.DIVINE_MAGIC_POTION3, ItemID.DIVINE_MAGIC_POTION4,
            ItemID.DIVINE_BATTLEMAGE_POTION1, ItemID.DIVINE_BATTLEMAGE_POTION2, ItemID.DIVINE_BATTLEMAGE_POTION3, ItemID.DIVINE_BATTLEMAGE_POTION4);
    private final Set<Integer> ANTI_FIRE_SET = Set.of(ItemID.ANTIFIRE_POTION1, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION4, ItemID.SUPER_ANTIFIRE_POTION1, ItemID.SUPER_ANTIFIRE_POTION2, ItemID.SUPER_ANTIFIRE_POTION3, ItemID.SUPER_ANTIFIRE_POTION4,
            ItemID.EXTENDED_ANTIFIRE1, ItemID.EXTENDED_ANTIFIRE2, ItemID.EXTENDED_ANTIFIRE3, ItemID.EXTENDED_ANTIFIRE4, ItemID.EXTENDED_SUPER_ANTIFIRE1, ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE4);

    private int timeout;
    private int drinkTimeout;
    private int drinkEnergy;
    private int nextEatHP;
    private int drinkPrayer;

    @Provides
    iQuickEaterConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iQuickEaterConfiguration.class);
    }

    @Override
    protected void startUp() {
        if (client != null || !config.usePercent()) {
            getNextEatHP();
        } else {
            nextEatHP = -1;
        }
        drinkPrayer = calc.getRandomIntBetweenRange(config.minPrayerPoints(), config.maxPrayerPoints());
    }

    @Override
    protected void shutDown() {

    }

    private void useItem(WidgetItem item) {
        if (item != null) {
            //targetMenu = new LegacyMenuEntry("", "", item.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), item.getIndex(),
            //        WidgetInfo.INVENTORY.getId(), false);
            targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), "eat", "drink", "invigorate");
            int sleepTime = calc.getRandomIntBetweenRange(25, 200);
            if (config.useInvokes()) {
                utils.doInvokeMsTime(targetMenu, sleepTime);
            } else {
                utils.doActionMsTime(targetMenu, item.getCanvasBounds(), sleepTime);
            }
        }
    }

    @Subscribe
    private void onVarbitChanged(VarbitChanged event) {
        if (config.drinkAntiPoison() && event.getIndex() == VarPlayer.POISON.getId() && client.getVarpValue(VarPlayer.POISON.getId()) > 0) {
            if (inventory.containsItem(POISON_SET)) {
                log.debug("Drinking anti-poison");
                WidgetItem poisonItem = inventory.getWidgetItem(POISON_SET);
                useItem(poisonItem);
            } else {
                utils.sendGameMessage("You are Poisoned but missing anti-poison");
            }
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (event.getKey().equals("minEatHP") || event.getKey().equals("maxEatHP") || event.getKey().equals("usePercent")) {
            getNextEatHP();
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (drinkTimeout > 0) {
            drinkTimeout--;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            if (timeout > 0) {
                timeout--;
                return;
            }
            if (client.getItemContainer(InventoryID.BANK) != null) {
                return;
            }
            if (nextEatHP < 1) {
                getNextEatHP();
            }
            if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= nextEatHP) {
                WidgetItem eatItem = inventory.getItemMenu(itemManager, "Eat", 33,
                        IGNORE_FOOD);
                if (eatItem != null) {
                    useItem(eatItem);
                    getNextEatHP();
                    log.debug("Next Eat HP: {}", nextEatHP);
                    return;
                }
                if (inventory.containsItem(DRINK_SET)) {
                    WidgetItem drinkItem = inventory.getWidgetItem(DRINK_SET);
                    useItem(drinkItem);
                    getNextEatHP();
                    log.debug("Next Eat HP: {}", nextEatHP);
                    return;
                }
                utils.sendGameMessage("Health is below threshold but we're out of food");
            }
            if (config.drinkStamina() && drinkTimeout == 0) {
                if (drinkEnergy == 0) {
                    drinkEnergy = calc.getRandomIntBetweenRange(config.maxDrinkEnergy() - config.randEnergy(), config.maxDrinkEnergy());
                    log.debug("Max drink energy: {}, Rand drink value: {}, Next drink energy: {}", config.maxDrinkEnergy(), config.randEnergy(), drinkEnergy);
                }
                if (client.getEnergy() < drinkEnergy) {
                    playerUtils.drinkStamPot(15 + calc.getRandomIntBetweenRange(0, 30));
                    drinkEnergy = calc.getRandomIntBetweenRange(config.maxDrinkEnergy() - config.randEnergy(), config.maxDrinkEnergy());
                    log.debug("Max drink energy: {}, Rand drink value: {}, Next drink energy: {}", config.maxDrinkEnergy(), config.randEnergy(), drinkEnergy);
                    drinkTimeout = 2;
                }
            }
            if (config.keepPNeckEquipped()) {
                timeout += 4;
                if (inventory.containsItem(11090)) {
                    if (playerUtils.getEquippedItems() != null && playerUtils.getEquippedItems().get(2).getId() != 11090) {
                        //targetMenu = new LegacyMenuEntry("Wear", "Wear", 11090, MenuAction.ITEM_SECOND_OPTION.getId(), inventory.getWidgetItem(11090).getIndex(),
                        //        WidgetInfo.INVENTORY.getId(), false);
                        targetMenu = inventoryAssistant.getLegacyMenuEntry(11090, "wear", "equip", "wield");
                        if (config.useInvokes()) {
                            utils.doInvokeMsTime(targetMenu, calc.getRandomIntBetweenRange(25, 200));
                        } else {
                            menu.setEntry(targetMenu);
                            mouse.delayMouseClick(inventory.getWidgetItem(11090).getCanvasBounds(), calc.getRandomIntBetweenRange(25, 200));
                        }
                    }
                } else {
                    utils.sendGameMessage("No phoenix necklaces in inventory.");
                }
            }

        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        String BURN_MESSAGE = ("You're horribly burnt by the dragon fire!");
        String BURN_EXPIRE = ("antifire potion is about to expire.");
        String IMB_HEART_MESSAGE = ("Your imbued heart has regained its magical power.");

        if (event.getMessage().equals(BURN_MESSAGE) || event.getMessage().contains(BURN_EXPIRE) && config.drinkAntiFire()) {
            if (inventory.containsItem(ANTI_FIRE_SET)) {
                log.debug("Drinking anti-fire");
                WidgetItem antiFireItem = inventory.getWidgetItem(ANTI_FIRE_SET);
                useItem(antiFireItem);
            } else {
                utils.sendGameMessage("You are Burnt but missing anti-fire potions");
            }
        }
        if (event.getMessage().contains(IMB_HEART_MESSAGE) && config.activateImbHeart()) {
            if (inventory.containsItem(ItemID.IMBUED_HEART)) {
                WidgetItem imbHeart = inventory.getWidgetItem(ItemID.IMBUED_HEART);
                useItem(imbHeart);
            }
        }
    }


    @Subscribe
    protected void onGameStateChanged(GameStateChanged event) {
		/* When logging in thr stat changed event is triggered for all skills and can send a false value of 0 even if the stat is full,
		causing a prayer pot to be incorrectly consumed if enabled. Setting a timeout on login ensures this doesn't occur */
        if (event.getGameState() == GameState.LOGGED_IN) {
            drinkTimeout = 4;
        }
        if (event.getGameState() == GameState.LOGIN_SCREEN) {
            nextEatHP = -1;
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        checkSkill(event.getSkill(), event.getBoostedLevel());
    }

    private void checkSkill(Skill skill, int boostedLevel) {
        switch (skill) {
            case PRAYER:
                if (config.drinkPrayer() && drinkPot(skill, boostedLevel, PRAYER_SET, drinkPrayer)) {
                    drinkPrayer = calc.getRandomIntBetweenRange(config.minPrayerPoints(), config.maxPrayerPoints());
                }
                break;
            case STRENGTH:
            case ATTACK:
            case DEFENCE:
                if (config.drinkStrength()) {
                    drinkPot(Skill.STRENGTH, client.getBoostedSkillLevel(Skill.STRENGTH), STRENGTH_SET, config.strengthPoints());
                }
                if (config.drinkAttack()) {
                    drinkPot(Skill.ATTACK, client.getBoostedSkillLevel(Skill.ATTACK), ATTACK_SET, config.attackPoints());
                }
                if (config.drinkDefence()) {
                    drinkPot(Skill.DEFENCE, client.getBoostedSkillLevel(Skill.DEFENCE), DEFENCE_SET, config.defencePoints());
                }
                break;
            case RANGED:
                if (config.drinkRanged()) {
                    drinkPot(skill, boostedLevel, RANGED_SET, config.rangedPoints());
                }
                if (config.drinkDefence()) {
                    drinkPot(Skill.DEFENCE, client.getBoostedSkillLevel(Skill.DEFENCE), DEFENCE_SET, config.defencePoints());
                }
                break;
            case MAGIC:
                if (config.drinkMagic()) {
                    drinkPot(skill, boostedLevel, MAGIC_SET, config.magicPoints());
                }
                if (config.drinkDefence()) {
                    drinkPot(Skill.DEFENCE, client.getBoostedSkillLevel(Skill.DEFENCE), DEFENCE_SET, config.defencePoints());
                }
                break;
        }
    }

    private boolean drinkPot(Skill skill, int boostedLevel, Set<Integer> itemSet, int drinkPotLevel) {
        if (boostedLevel == 0 || boostedLevel > drinkPotLevel) {
            return false;
        }
        if (inventory.containsItem(itemSet) && drinkTimeout == 0) {
            WidgetItem itemToDrink = inventory.getWidgetItem(itemSet);
            useItem(itemToDrink);
            drinkTimeout = 4;
            return true;
        }
        if (drinkTimeout == 0) {
            utils.sendGameMessage(skill + " is below threshold but we have nothing to regain " + skill);
        }
        return false;
    }

    private void getNextEatHP() {
        float hpLevel = client.getRealSkillLevel(Skill.HITPOINTS);
        float minHP = (config.minEatHP() / (float) 100) * hpLevel;
        float maxHP = (config.maxEatHP() / (float) 100) * hpLevel;
        if (hpLevel > 0) {
            if (config.usePercent()) {
                nextEatHP = Math.max(1, calc.getRandomIntBetweenRange(Math.round(minHP), Math.round(maxHP)));
            } else {
                nextEatHP = calc.getRandomIntBetweenRange(config.minEatHP(), config.maxEatHP());
            }
        }
    }
}
