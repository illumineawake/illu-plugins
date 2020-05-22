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
package net.runelite.client.plugins.powerskiller;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("PowerSkiller")
public interface PowerSkillerConfiguration extends Config
{
	@ConfigItem(
			keyName = "gameObjects",
			name = "gameObjects (IDs) to power-skill",
			description = "Seperate with comma",
			position = 0
	)
	default String gameObjects()
	{
		return "0";
	}

	@ConfigItem(
			keyName = "items",
			name = "Items to Drop",
			description = "Seperate with comma",
			position = 1
	)
	default String items()
	{
		return "0";
	}

	@ConfigItem(
			keyName = "worldPointAnchor",
			name = "World Point Anchor",
			description = "Central World Point for where you want to power-skill around",
			position = 2
	)
	default int worldPointAnchor()
	{
		return 70;
	}

	@ConfigItem(
			keyName = "anchorRadius",
			name = "Anchor Radius",
			description = "Radius to search for GameObjects. Format: height,width",
			position = 3
	)
	default String anchorRadius()
	{
		return "10,10";
	}

	@ConfigItem(
			keyName = "randLow",
			name = "Minimum Drop Delay",
			description = "Minimum delay between dropping items",
			position = 3
	)
	default int randLow()
	{
		return 70;
	}

	@ConfigItem(
			keyName = "randLower",
			name = "Maximum Drop Delay",
			description = "Maximum delay between dropping items",
			position = 4
	)
	default int randHigh()
	{
		return 80;
	}

	@ConfigItem(
			keyName = "worldHop",
			name = "World Hop Radius",
			description = "Hop if player is within radius (-1 = disabled)",
			position = 5
	)
	default int worldHop()
	{
		return -1;
	}

	/*@ConfigItem(
			position = 1,
			keyName = "toggle",
			name = "Drop Items",
			description = "Drops Items in config above."
	)
	default Keybind toggle()
	{
		return Keybind.NOT_SET;
	}*/
}
