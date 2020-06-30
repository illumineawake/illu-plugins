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

import java.util.function.Consumer;
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
	@ConfigSection(
		keyName = "delayConfig",
		name = "Sleep Delay Configuration",
		description = "Configure how the bot handles sleep delays",
		position = 1
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
		position = 2,
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
		position = 3,
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
		position = 4,
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
		position = 5,
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
		position = 6,
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
		position = 7
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
		position = 8,
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
		position = 9,
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
		position = 10,
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
		position = 11,
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
		position = 12,
		section = "delayTickConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}
	@ConfigTitleSection(
		keyName = "agilityTitle",
		name = "Agility Configuration",
		description = "Supported courses: Gnome, Draynor, Varrock, Canifis, Falador, Pollnivneach, Seers, Rellekka",
		position = 13
	)
	default Title agilityTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "mogPickup",
		name = "Pick up Mark of Grace",
		description = "Enable to pick up Marks of Grace",
		position = 14,
		titleSection = "agilityTitle"
	)
	default boolean mogPickup()
	{
		return true;
	}

	@ConfigItem(
		keyName = "startButton",
		name = "Start/Stop",
		description = "Test button that changes variable value",
		position = 15,
		titleSection = "agilityTitle"
	)
	default Consumer<RooftopAgilityPlugin> testButton()
	{
		return (plugin) ->
		{
			if (plugin.pluginManager.isPluginEnabled(plugin))
			{
				plugin.startAgility = !plugin.startAgility;
			}
			else
			{
				plugin.startAgility = false;
			}
			System.out.println("Start button was pressed in config, status is: " + plugin.startAgility);
		};
	}
}
