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

import net.runelite.client.config.*;

@ConfigGroup("PowerFighter")
public interface PowerFighterConfig extends Config
{

	@ConfigSection(
		keyName = "delayConfig",
		name = "Sleep Delay Configuration",
		description = "Configure how the bot handles sleep delays",
		position = 0
	)
	default boolean delayConfig()
	{
		return false;
	}

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

	@ConfigSection(
		keyName = "delayTickConfig",
		name = "Game Tick Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
		position = 10
	)
	default boolean delayTickConfig()
	{
		return false;
	}

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

	@ConfigTitleSection(
			keyName = "instructionsTitle",
			name = "Instructions",
			description = "",
			position = 16
	)
	default Title instructionsTitle()
	{
		return new Title();
	}

	@ConfigItem(
			keyName = "instruction",
			name = "",
			description = "Instructions. Don't enter anything into this field",
			position = 17,
			titleSection = "instructionsTitle"
	)
	default String instruction()
	{
		return "Auto fights NPC's with the provided name. Enable Quick Eater Plugin for eating.";
	}

	@ConfigTitleSection(
		keyName = "generalTitle",
		name = "General Config",
		description = "",
		position = 29
	)
	default Title generalTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "npcName",
		name = "NPC Name",
		description = "Name of NPC. Will attack any NPC containing given name.",
		position = 30,
		titleSection = "generalTitle"
	)
	default String npcName() { return ""; }

	@Range(
		min = 1,
		max = 64
	)
	@ConfigItem(
		keyName = "searchRadius",
		name = "Search radius NPC/loot ",
		description = "The distance (in tiles) to search for target NPC/loot. Center search point is set when you click start.",
		position = 31,
		titleSection = "generalTitle"
	)
	default int searchRadius() { return 20; }

	@ConfigTitleSection(
		keyName = "ammoTitle",
		name = "Ammo Settings",
		description = "",
		position = 32
	)
	default Title ammoTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "lootAmmo",
		name = "Loot Ammo",
		description = "Enable to loot ammo",
		position = 34,
		titleSection = "ammoTitle"
	)
	default boolean lootAmmo()
	{
		return true;
	}

	@ConfigItem(
		keyName = "ammoID",
		name = "Ammo ID",
		description = "Enable to stop when out of food",
		position = 35,
		hidden = true,
		unhide = "lootAmmo",
		titleSection = "ammoTitle"

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
		titleSection = "ammoTitle"

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
		titleSection = "ammoTitle"

	)
	default int randAmmoLootTime()
	{
		return 30;
	}

	@ConfigTitleSection(
		keyName = "lootTitle",
		name = "Loot Settings",
		description = "",
		position = 38
	)
	default Title lootTile()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "lootItems",
		name = "Loot Items",
		description = "Enable to loot items",
		position = 39,
		titleSection = "lootTitle"
	)
	default boolean lootItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lootGEValue",
		name = "Loot all items above GE value",
		description = "Enable to loot all items above the given Grand Exchange value. Uses OSBuddy avg price.",
		position = 40,
		hidden = true,
		unhide = "lootItems",
		titleSection = "lootTitle"

	)
	default boolean lootGEValue()
	{
		return false;
	}

	@ConfigItem(
		keyName = "minGEValue",
		name = "",
		description = "The minimum Grand Exchange value for loot. Uses OSBuddy avg price.",
		position = 41,
		hidden = true,
		unhide = "lootGEValue",
		titleSection = "lootTitle"

	)
	default int minGEValue()
	{
		return 500;
	}

	@ConfigItem(
		keyName = "lootItemNames",
		name = "Item Names to loot (separate with comma)",
		description = "Provide part or all of the item name to loot. Separate each item with a comma. Not case sensitive.",
		position = 42,
		hide = "lootGEValue",
		titleSection = "lootTitle"

	)
	default String lootItemNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "lootClueScrolls",
		name = "Pick-up Clue Scrolls",
		description = "Enable to loot CLue Scrolls",
		position = 43,
		hidden = true,
		unhide = "lootItems",
		titleSection = "lootTitle"

	)
	default boolean lootClueScrolls()
	{
		return false;
	}

	@ConfigItem(
		keyName = "buryBones",
		name = "Loot and Bury Bones",
		description = "Enable to loot and bury Bones",
		position = 43,
		hidden = true,
		unhide = "lootItems",
		titleSection = "lootTitle"
	)
	default boolean buryBones()
	{
		return false;
	}

	@ConfigItem(
		keyName = "lootNPCOnly",
		name = "Loot your NPC Only",
		description = "Enable to only loot NPC's you have killed",
		position = 44,
		hidden = true,
		unhide = "lootItems",
		titleSection = "lootTitle"
	)
	default boolean lootNPCOnly()
	{
		return true;
	}

	@ConfigItem(
		keyName = "stopSlayer",
		name = "Stop on Slayer task completion",
		description = "Enable to stop when Slayer task completes",
		position = 57,
		titleSection = "generalTitle"
	)
	default boolean stopSlayer()
	{
		return false;
	}

	@ConfigItem(
		keyName = "stopAmmo",
		name = "Stop when out of ammo",
		description = "Enable to stop when out of ammo",
		position = 58,
		titleSection = "generalTitle"
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
		titleSection = "generalTitle"
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
		titleSection = "generalTitle",
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
		titleSection = "generalTitle"
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
		titleSection = "generalTitle"
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
		return null;
	}
}
