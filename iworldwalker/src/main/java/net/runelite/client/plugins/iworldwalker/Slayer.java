/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Slayer
{
	NONE(""),
	BURTHORPE_SLAYER("Burthorpe Slayer", new WorldPoint(2931, 3536, 0)),
	TREE_GNOME_NIEVE("Tree Gnome Nieve", new WorldPoint(2611, 3092, 0));

	private final String name;
	private WorldPoint worldPoint;

	Slayer(String name)
	{
		this.name = name;
	}

	Slayer(String name, WorldPoint worldPoint)
	{
		this.name = name;
		this.worldPoint = worldPoint;
	}
}
