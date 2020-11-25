/*
 * Copyright (c) 2018, Seth <Sethtroll3@gmail.com>
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
package net.runelite.client.plugins.blastfurnacebot;

import net.runelite.client.config.*;

@ConfigGroup("blastfurnacebot")
public interface BlastFurnaceBotConfig extends Config
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
			max = 10
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
			max = 10
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
			max = 10
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
			keyName = "instructions",
			name = "",
			description = "Instructions. Don't enter anything into this field",
			position = 17,
			titleSection = "instructionsTitle"
	)
	default String instructions()
	{
		return "Task template instructions";
	}

	@ConfigItem(
			keyName = "enableUI",
			name = "Enable UI",
			description = "Enable to turn on in game UI",
			position = 22,
			titleSection = "instructionsTitle"
	)
	default boolean enableUI()
	{
		return true;
	}

	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "Test button that changes variable value",
			position = 23,
			titleSection = "instructionsTitle"
	)
	default Button startButton()
	{
		return new Button();
	}



	@ConfigItem(
		keyName = "cofferThreshold",
		name = "Minimum Coffer amount1",
		description = "Minimum amount of GP to have in coffer before refilling",
		position = 18,
			titleSection = "instructionsTitle"
	)
	default int cofferThreshold()
	{
		return 5000;
	}

	@ConfigItem(
		keyName = "cofferAmount",
		name = "GP into Coffer",
		description = "How much GP to put into coffer when refilling",
		position = 19,
			titleSection = "instructionsTitle"
	)
	default int cofferAmount()
	{
		return 50000;
	}
	@ConfigItem(
		keyName = "bar",
		name = "Bar to smelt",
		description = "Select the type of bar to smelt",
		position = 21,
			titleSection = "instructionsTitle"
	)
	default Bars getBar()
	{
		return Bars.RUNITE_BAR;
	}

}
