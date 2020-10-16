/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Skilling
{
	NONE(""),
	BARB_ASSAULT("Barb Assault", new WorldPoint(2533, 3572, 0));

	private final String name;
	private WorldPoint worldPoint;

	Skilling(String name)
	{
		this.name = name;
	}

	Skilling(String name, WorldPoint worldPoint)
	{
		this.name = name;
		this.worldPoint = worldPoint;
	}
}
