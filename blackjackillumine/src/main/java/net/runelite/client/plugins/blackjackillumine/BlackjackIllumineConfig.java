/*
 * Copyright (c) 2019, gazivodag <https://github.com/gazivodag>
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

package net.runelite.client.plugins.blackjackillumine;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("blackjack")
public interface BlackjackIllumineConfig extends Config
{
	@ConfigItem(
			keyName = "pickpocketOnAggro",
			name = "Pickpocket when aggro\'d",
			description = "Switches to \"Pickpocket\" when bandit is aggro\'d. Saves food at the cost of slight xp/h.",
			position = 0
	)
	default boolean pickpocketOnAggro()
	{
		return false;
	}

	@ConfigItem(
			keyName = "random",
			name = "Randomly Miss 1 Pickpocket",
			description = "If enabled, this will randomly miss 1 pickpocket every so often." +
					"<br> Not sure why'd you want to do that, but you can.",
			position = 1
	)
	default boolean random()
	{
		return false;
	}

	@ConfigItem(
			keyName = "toggle",
			name = "Toggle",
			description = "Toggles the clicker.",
			position = 2
	)
	default Keybind toggle()
	{
		return Keybind.NOT_SET;
	}

	@Range(
			min = 5,
			max = 98
	)
	@ConfigItem(
			keyName = "hpThreshold",
			name = "Hp Threshold",
			description = "The hp in which the plugin will auto disable.",
			position = 3,
			unhide = "autoDisable"
	)
	default int hpThreshold()
	{
		return 200;
	}

	@ConfigItem(
			keyName = "flash",
			name = "Flash on Low HP",
			description = "Your Screen flashes when you get to low hp.",
			position = 4,
			unhide = "autoDisable"
	)
	default boolean flash()
	{
		return false;
	}

	@ConfigItem(
			keyName = "foodToEat",
			name = "ID of food to eat",
			description = "The food the plugin will use to eat",
			position = 5,
			unhide = "autoDisable"
	)
	default int foodToEat()
	{
		return 1993;
	} //default returns wine
}
