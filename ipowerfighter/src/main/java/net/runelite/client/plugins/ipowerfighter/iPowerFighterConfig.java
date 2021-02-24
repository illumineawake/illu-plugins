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

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitle;
import net.runelite.client.config.Range;

@ConfigGroup("iPowerFighter")
public interface iPowerFighterConfig extends Config
{

	@ConfigTitle(
		keyName = "delayConfig",
		name = "Sleep Delay Configuration",
		description = "Configure how the bot handles sleep delays",
		position = 0
	)
	String delayConfig = "delayConfig";

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepMin",
		name = "Sleep Min",
		description = "",
		position = 1,
		section = "delayConfig"
	)
	default int sleepMin()
	{
		return 60;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepMax",
		name = "Sleep Max",
		description = "",
		position = 2,
		section = "delayConfig"
	)
	default int sleepMax()
	{
		return 350;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepTarget",
		name = "Sleep Target",
		description = "",
		position = 3,
		section = "delayConfig"
	)
	default int sleepTarget()
	{
		return 100;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepDeviation",
		name = "Sleep Deviation",
		description = "",
		position = 4,
		section = "delayConfig"
	)
	default int sleepDeviation()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "sleepWeightedDistribution",
		name = "Sleep Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 5,
		section = "delayConfig"
	)
	default boolean sleepWeightedDistribution()
	{
		return false;
	}

	@ConfigTitle(
		keyName = "delayTickConfig",
		name = "Game Tick Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
		position = 10
	)
	String delayTickConfig = "delayTickConfig";

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayMin",
		name = "Game Tick Min",
		description = "",
		position = 11,
		section = "delayTickConfig"
	)
	default int tickDelayMin()
	{
		return 1;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayMax",
		name = "Game Tick Max",
		description = "",
		position = 12,
		section = "delayTickConfig"
	)
	default int tickDelayMax()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayTarget",
		name = "Game Tick Target",
		description = "",
		position = 13,
		section = "delayTickConfig"
	)
	default int tickDelayTarget()
	{
		return 2;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayDeviation",
		name = "Game Tick Deviation",
		description = "",
		position = 14,
		section = "delayTickConfig"
	)
	default int tickDelayDeviation()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "tickDelayWeightedDistribution",
		name = "Game Tick Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 15,
		section = "delayTickConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}

	@ConfigTitle(
		keyName = "instructionsTitle",
		name = "Instructions",
		description = "",
		position = 16
	)
	String instructionsTitle = "instructionsTitle";

	@ConfigItem(
		keyName = "instruction",
		name = "",
		description = "Instructions. Don't enter anything into this field",
		position = 17,
		title = "instructionsTitle"
	)
	default String instruction()
	{
		return "Auto fights NPC's with the provided name. Enable Quick Eater Plugin for eating.";
	}

	@ConfigTitle(
		keyName = "generalTitle",
		name = "General Config",
		description = "",
		position = 28
	)
	String generalTitle = "generalTitle";

	@ConfigItem(
		keyName = "lootOnly",
		name = "Loot only mode",
		description = "Loot only mode, will loot items and not fight NPCs",
		position = 29,
		title = "generalTitle"
	)
	default boolean lootOnly()
	{
		return false;
	}

	@ConfigItem(
		keyName = "exactNpcOnly",
		name = "Exact NPC only mode",
		description = "Exact NPC only mode, will fight exact NPC names only",
		position = 29,
		title = "generalTitle"
	)
	default boolean exactNpcOnly()
	{
		return false;
	}

	@ConfigItem(
		keyName = "npcName",
		name = "NPC Name",
		description = "Name of NPC. Will attack any NPC containing given name.",
		position = 30,
		hide = "dropInventory",
		title = "generalTitle"
	)
	default String npcName()
	{
		return "chicken";
	}

	@Range(
		min = 1,
		max = 64
	)
	@ConfigItem(
		keyName = "searchRadius",
		name = "Search radius NPC",
		description = "The distance (in tiles) to search for target NPC. Center search point is set when you click start.",
		position = 31,
		hide = "dropInventory",
		title = "generalTitle"
	)
	default int searchRadius()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "safeSpot",
		name = "Safe spot",
		description = "Safe spot will force your character to always return to the tile you started the plugin on",
		position = 32,
		hide = "dropInventory",
		title = "generalTitle"
	)
	default boolean safeSpot()
	{
		return false;
	}

	@ConfigItem(
		keyName = "safeSpotRadius",
		name = "Safe spot radius",
		description = "Radius of the safe spot to return to. 0 will always return to the same tile, 1 will return to a 1 tile radius of safespot",
		position = 33,
		hidden = true,
		unhide = "safeSpot",
		title = "generalTitle"
	)
	default int safeSpotRadius()
	{
		return 1;
	}

	@ConfigTitle(
		keyName = "ammoTitle",
		name = "Ammo Settings",
		description = "",
		position = 32
	)
	String ammoTitle = "ammoTitle";

	@ConfigItem(
		keyName = "lootAmmo",
		name = "Loot Ammo",
		description = "Enable to loot ammo",
		position = 34,
		title = "ammoTitle"
	)
	default boolean lootAmmo()
	{
		return false;
	}

	@ConfigItem(
		keyName = "ammoID",
		name = "Ammo ID",
		description = "Enable to stop when out of food",
		position = 35,
		hidden = true,
		unhide = "lootAmmo",
		title = "ammoTitle"
	)
	default int ammoID()
	{
		return 809;
	}

	@ConfigItem(
		keyName = "minAmmoLootTime",
		name = "Ammo loot min wait (seconds)",
		description = "Minimum time (in seconds) to wait before collecting ammo",
		position = 36,
		hidden = true,
		unhide = "lootAmmo",
		title = "ammoTitle"
	)
	default int minAmmoLootTime()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "randAmmoLootTime",
		name = "Additional random ammo wait time",
		description = "Maximum random value that will be added to the minimum ammo wait time",
		position = 37,
		hidden = true,
		unhide = "lootAmmo",
		title = "ammoTitle"
	)
	default int randAmmoLootTime()
	{
		return 30;
	}

	@ConfigTitle(
		keyName = "lootTitle",
		name = "Loot Settings",
		description = "",
		position = 38
	)
	String lootTitle = "lootTitle";

	@ConfigItem(
		keyName = "lootItems",
		name = "Loot Items",
		description = "Enable to loot items",
		position = 39,
		title = "lootTitle"
	)
	default boolean lootItems()
	{
		return true;
	}

	@Range(
		min = 1,
		max = 64
	)
	@ConfigItem(
		keyName = "lootRadius",
		name = "Loot radius",
		description = "The distance (in tiles) to search for target loot. Center search point is set when you click start.",
		position = 40,
		hidden = true,
		unhide = "lootItems",
		title = "lootItems"
	)
	default int lootRadius()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "lootGEValue",
		name = "Loot all items above GE value",
		description = "Enable to loot all items above the given Grand Exchange value. Uses OSBuddy avg price.",
		position = 41,
		hidden = true,
		unhide = "lootItems",
		title = "lootTitle"
	)
	default boolean lootGEValue()
	{
		return false;
	}

	@ConfigItem(
		keyName = "minGEValue",
		name = "",
		description = "The minimum Grand Exchange value for loot. Uses OSBuddy avg price.",
		position = 42,
		hidden = true,
		unhide = "lootGEValue",
		title = "lootTitle"
	)
	default int minGEValue()
	{
		return 500;
	}

	@ConfigItem(
		keyName = "lootItemNames",
		name = "Item Names to loot (separate with comma)",
		description = "Provide part or all of the item name to loot. Separate each item with a comma. Not case sensitive.",
		position = 43,
		hidden = true,
		unhide = "lootItems",
		title = "lootTitle"
	)
	default String lootItemNames()
	{
		return "rune,head,seed,herb,root,grimy,key,cut";
	}

	@ConfigItem(
		keyName = "lootClueScrolls",
		name = "Pick-up Clue Scrolls",
		description = "Enable to loot CLue Scrolls",
		position = 44,
		hidden = true,
		unhide = "lootItems",
		title = "lootTitle"
	)
	default boolean lootClueScrolls()
	{
		return false;
	}

	@ConfigItem(
		keyName = "buryBones",
		name = "Loot and Bury Bones",
		description = "Enable to loot and bury Bones",
		position = 46,
		hidden = true,
		unhide = "lootItems",
		title = "lootTitle"
	)
	default boolean buryBones()
	{
		return false;
	}

	@ConfigItem(
		keyName = "buryOne",
		name = "Get 1 Bury 1",
		description = "Enable to bury bones as they are picked up. Disable to bury bones once inventory is full.",
		position = 48,
		hidden = true,
		unhide = "buryBones",
		title = "lootTitle"
	)
	default boolean buryOne()
	{
		return false;
	}

	@ConfigItem(
		keyName = "lootNPCOnly",
		name = "Loot your NPC Only",
		description = "Enable to only loot NPC's you have killed",
		position = 50,
		hidden = true,
		unhide = "lootItems",
		title = "lootTitle"
	)
	default boolean lootNPCOnly()
	{
		return true;
	}

	@ConfigItem(
		keyName = "forceLoot",
		name = "Force loot",
		description = "Enable to force loot if loot has been on the ground for a while",
		position = 51,
		hidden = true,
		unhide = "lootItems",
		title = "lootTitle"
	)
	default boolean forceLoot()
	{
		return true;
	}

	@ConfigTitle(
		keyName = "alchTitle",
		name = "Alch Settings",
		description = "",
		position = 90
	)
	String alchTitle = "alchTitle";

	@ConfigItem(
		keyName = "alchItems",
		name = "Alch Items",
		description = "Enable to alch looted items. Requires fire and nature runes in inventory",
		position = 95,
		title = "alchTitle"
	)
	default boolean alchItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "alchByValue",
		name = "Alch Value",
		description = "Alch items that have a higher HA value than GE value",
		position = 100,
		hidden = true,
		unhide = "alchItems",
		title = "alchTitle"
	)
	default boolean alchByValue()
	{
		return false;
	}

	@ConfigItem(
		keyName = "maxAlchValue",
		name = "Max alch value",
		description = "Don't alch items above this value, to prevent alching rare items",
		position = 105,
		hidden = true,
		unhide = "alchByValue",
		title = "alchTitle"
	)
	default int maxAlchValue()
	{
		return 100000;
	}

	@ConfigItem(
		keyName = "alchByName",
		name = "Alch Item Name",
		description = "Alch items that contain provided names. Names should be separated with commas, no spaces.",
		position = 110,
		hidden = true,
		unhide = "alchItems",
		title = "alchTitle"
	)
	default boolean alchByName()
	{
		return false;
	}

	@ConfigItem(
		keyName = "alchNames",
		name = "",
		description = "Alch items that contain provided names. Names should be separated with commas, no spaces.",
		position = 115,
		hidden = true,
		unhide = "alchByName",
		title = "alchTitle"
	)
	default String alchNames()
	{
		return "Steel platebody,Rune scimitar";
	}

	@ConfigTitle(
		keyName = "combatTitle",
		name = "Combat Settings",
		description = "",
		position = 90
	)
	String combatTitle = "combatTitle";

	@ConfigItem(
		keyName = "combatLevels",
		name = "Combat Levels",
		description = "Enable to set levels for the bot to reach and change between",
		position = 120,
		title = "combatTitle"
	)
	default boolean combatLevels()
	{
		return false;
	}

	@ConfigItem(
		keyName = "attackLvl",
		name = "Attack level",
		description = "Attack level to level to",
		position = 125,
		hidden = true,
		unhide = "combatLevels",
		title = "combatTitle"
	)
	default int attackLvl()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "strengthLvl",
		name = "Strength level",
		description = "Strength level to level to",
		position = 130,
		hidden = true,
		unhide = "combatLevels",
		title = "combatTitle"
	)
	default int strengthLvl()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "defenceLvl",
		name = "Defence level",
		description = "Defence level to level to",
		position = 135,
		hidden = true,
		unhide = "combatLevels",
		title = "combatTitle"
	)
	default int defenceLvl()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "continueType",
		name = "Continue after levels",
		description = "Select action ",
		position = 140,
		hidden = true,
		unhide = "combatLevels",
		title = "combatTitle"
	)
	default combatType continueType()
	{
		return combatType.STRENGTH;
	}

	@ConfigItem(
		keyName = "stopSlayer",
		name = "Stop on Slayer task completion",
		description = "Enable to stop when Slayer task completes",
		position = 52,
		title = "generalTitle"
	)
	default boolean stopSlayer()
	{
		return false;
	}

	@ConfigItem(
		keyName = "equipBracelet",
		name = "Equip Bracelets of Slaughter/Expeditious",
		description = "Enable to equip Bracelets of Slaughter/Expeditious Bracelet if in inventory",
		position = 52,
		title = "generalTitle"
	)
	default boolean equipBracelet()
	{
		return false;
	}

	@ConfigItem(
		keyName = "stopAmmo",
		name = "Stop when out of ammo",
		description = "Enable to stop when out of ammo",
		position = 58,
		title = "generalTitle"
	)
	default boolean stopAmmo()
	{
		return true;
	}

	@ConfigItem(
		keyName = "stopFood",
		name = "Stop if out of food",
		description = "Enable to stop when out of food",
		position = 59,
		title = "generalTitle"
	)
	default boolean stopFood()
	{
		return true;
	}

	@ConfigItem(
		keyName = "foodID",
		name = "Food ID",
		description = "Enter the ID of your food so bot knows when to stop",
		position = 60,
		title = "generalTitle",
		hidden = true,
		unhide = "stopFood"
	)
	default int foodID()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "logout",
		name = "Logout on stop",
		description = "Enable to logout when out of food or ammo",
		position = 65,
		title = "generalTitle"
	)
	default boolean logout()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enableUI",
		name = "Enable UI",
		description = "Enable to turn on in game UI",
		position = 70,
		title = "generalTitle"
	)
	default boolean enableUI()
	{
		return true;
	}


	@ConfigItem(
		keyName = "startButton",
		name = "Start/Stop",
		description = "Test button that changes variable value",
		position = 80
	)
	default Button startButton()
	{
		return new Button();
	}
}
