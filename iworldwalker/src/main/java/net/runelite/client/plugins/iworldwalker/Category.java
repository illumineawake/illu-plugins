/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Category
{
	NONE("None"),
	BANKS("Banks"),
	CITIES("Cities"),
	GUILDS("Guilds"),
	SKILLING("Skilling"),
	SLAYER("Slayer"),
	MISC("Misc"),
	CUSTOM("Custom");

	private final String name;

	Category(String name)
	{
		this.name = name;
	}

}
