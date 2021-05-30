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
package net.runelite.client.plugins.iutils;

import net.runelite.client.config.*;


@ConfigGroup("iUtils")
public interface iUtilsConfig extends Config
{
	@ConfigSection(
			keyName = "delayConfig",
			name = "Sleep Delay Configuration",
			description = "Configure how the game handles sleep delays",
			closedByDefault = true,
			position = 0
	)
	String delayConfig = "delayConfig";

	@Range(
			min = 0,
			max = 5000
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
			max = 5000
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
			max = 5000
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
			max = 5000
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

	@ConfigItem(
		keyName = "getMouse",
		name = "Mouse",
		description = "Choose a mouse movement style",
		position = 6
	)
	default MouseType getMouse()
	{
		return MouseType.NO_MOVE;
	}

	@ConfigItem(
			keyName = "noAFK",
			name = "Prevent AFK",
			description = "Enable to prevent logging out due to AFK.",
			position = 7
	)
	default boolean noAFK()
	{
		return true;
	}


	@ConfigItem(
			keyName = "test",
			name = "Test ID",
			description = "",
			position = 10
	)
	default int test()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "Test button that changes variable value",
			position = 150
	)
	default Button startButton()
	{
		return new Button();
	}
}
