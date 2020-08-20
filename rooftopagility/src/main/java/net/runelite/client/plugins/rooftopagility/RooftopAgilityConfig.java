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

import java.time.Instant;
import java.util.function.Consumer;
import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.ConfigTitleSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Title;

@ConfigGroup("RooftopAgility")
public interface RooftopAgilityConfig extends Config
{
	@ConfigTitleSection(
		keyName = "courseTitle",
		name = "Supported Courses",
		description = "",
		position = 0
	)
	default Title courseTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "Course",
		name = "",
		description = "Supported agility courses (don't enter anything into this field)",
		position = 1,
		titleSection = "courseTitle"
	)
	default String Course()
	{
		return "Gnome, Draynor (banking), Al kharid, Varrock (banking), Canifis (banking), Falador (banking), Seers (banking), Pollnivneach, Prifddinas, Rellekka, Ardougne (banking)";
	}

	@ConfigSection(
		keyName = "delayConfig",
		name = "Sleep Delay(ms) Configuration",
		description = "Configure how the bot handles sleep delays in milliseconds",
		position = 2
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
		position = 3,
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
		position = 4,
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
		position = 5,
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
		position = 6,
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
		position = 7,
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
		position = 8
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
		position = 9,
		section = "delayTickConfig"
	)
	default int tickDelayMin()
	{
		return 1;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayMax",
		name = "Game Tick Max",
		description = "",
		position = 10,
		section = "delayTickConfig"
	)
	default int tickDelayMax()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayTarget",
		name = "Game Tick Target",
		description = "",
		position = 11,
		section = "delayTickConfig"
	)
	default int tickDelayTarget()
	{
		return 2;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayDeviation",
		name = "Game Tick Deviation",
		description = "",
		position = 12,
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
		position = 13,
		section = "delayTickConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}

	@ConfigTitleSection(
		keyName = "agilityTitle",
		name = "Agility Configuration",
		description = "",
		position = 14
	)
	default Title agilityTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "mogPickup",
		name = "Pick up Mark of Grace",
		description = "Enable to pick up Marks of Grace",
		position = 15,
		titleSection = "agilityTitle"
	)
	default boolean mogPickup()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lowHP",
		name = "Stop at HP",
		description = "Stop if HP goes below given threshold",
		position = 16,
		titleSection = "agilityTitle"
	)
	default int lowHP()
	{
		return 9;
	}

	@ConfigItem(
		keyName = "highAlch",
		name = "High Alch",
		description = "Enable to High Alch while running",
		position = 17,
		titleSection = "agilityTitle"
	)
	default boolean highAlch()
	{
		return false;
	}

	@ConfigItem(
		keyName = "alchItemID",
		name = "Alch Item ID (un-noted)",
		description = "Item ID (un-noted) of item you wish to high alch.",
		position = 18,
		titleSection = "agilityTitle",
		hidden = true,
		unhide = "highAlch"
	)
	default int alchItemID()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "bankRestock",
		name = "Bank to restock items",
		description = "Go to bank to restock items for high alch. Auto-disables at unsupported locations or bank doesn't contain item.",
		position = 19,
		titleSection = "agilityTitle",
		hidden = true,
		unhide = "highAlch"
	)
	default boolean bankRestock()
	{
		return false;
	}

	@ConfigItem(
		keyName = "camelotTeleport",
		name = "Use Camelot Teleport",
		description = "Use Camelot Teleport if you have hard diaries completed. Requires Air Runes or (Air Staff equipped) and Law Runes in inventory",
		position = 24,
		titleSection = "agilityTitle"
	)
	default boolean camelotTeleport()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableUI",
		name = "Enable UI",
		description = "Enable to turn on in game UI",
		position = 29,
		titleSection = "agilityTitle"
	)
	default boolean enableUI()
	{
		return true;
	}

	@ConfigItem(
		keyName = "startButton",
		name = "Start/Stop",
		description = "Test button that changes variable value",
		position = 30,
		titleSection = "agilityTitle"
	)
	default Button startButton()
	{
		return null;
	}
}
