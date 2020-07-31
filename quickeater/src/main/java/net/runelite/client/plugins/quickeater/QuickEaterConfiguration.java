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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("QuickEater")
public interface QuickEaterConfiguration extends Config
{

	@ConfigItem(
		keyName = "minEatHP",
		name = "Minimum Eat HP",
		description = "Minimum HP to eat at. i.e. will always eat",
		position = 0
	)
	default int minEatHP()	{ return 10; }

	@ConfigItem(
		keyName = "maxEatHP",
		name = "Maximum Eat HP",
		description = "Highest HP to consider eating. Value MUST be higher than minimum HP config. If HP drops below this value bot may randomly decide to eat.",
		position = 1
	)
	default int maxEatHP()	{ return 20; }

	@ConfigItem(
		keyName = "drinkStamina",
		name = "Drink Stamina Potions",
		description = "Enable to drink Stamina Potions below given energy level",
		position = 10
	)
	default boolean drinkStamina() { return false; }

	@ConfigItem(
		keyName = "maxDrinkEnergy",
		name = "Drink stamina below energy",
		description = "This is the maximum energy amount",
		position = 20,
		hidden = true,
		unhide = "drinkStamina"
	)
	default int maxDrinkEnergy() { return 60; }

	@ConfigItem(
		keyName = "randEnergy",
		name = "random variation for drink energy (subtracted from max)",
		description = "A random value that is subtracted from max drink energy. E.g. a random value of '20' with a max drink energy of 60 would " +
			"cause stamina pot to be drunk at a random value between 40 and 60",
		position = 30,
		hidden = true,
		unhide = "drinkStamina"
	)
	default int randEnergy() { return 20; }
}
