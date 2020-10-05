/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Location
{
	NONE(""),
	LUMBRIDGE_CASTLE("Lumbridge Castle", new WorldPoint(3222, 3218, 0)),
	GRAND_EXCHANGE("Grand Exchange", new WorldPoint(3163, 3485, 0)),
	VARROCK_EAST_BANK("Varrock East Bank", new WorldPoint(3253, 3421, 0)),
	DRAYNOR_BANK("Draynor Bank", new WorldPoint(3093, 3245, 0)),
	EDGEVILLE_BANK("Edgeville Bank", new WorldPoint(3093, 3496, 0)),
	FALADOR_EAST_BANK("Falador East Bank", new WorldPoint(3012, 3356, 0)),
	FALADOR_WEST_BANK("Falador West Bank", new WorldPoint(2945, 3369, 0)),
	ALKHARID_BANK("Alkharid Bank", new WorldPoint(3270, 3167, 0)),
	SHANTAY_PASS("Shantay Pass", new WorldPoint(3303, 3121, 0)),
	PORT_SARIM("Port Sarim", new WorldPoint(3041, 3236, 0)),
	RIMMINGTON_MINE("Rimmington Mine", new WorldPoint(2977, 3240, 0)),
	GOBLIN_VILLAGE("Goblin Village", new WorldPoint(2956, 3505, 0)),
	CATHERBY_BANK("Catherby Bank", new WorldPoint(2808, 3440, 0)),
	SEERS_BANK("Seers Bank", new WorldPoint(2725, 3491, 0)),
	RELLEKKA("Rellekka", new WorldPoint(2644, 3677, 0)),
	ARDOUGNE_EAST_BANK("Ardougne East Bank", new WorldPoint(2616, 3333, 0)),
	YANILLE_BANK("Yanille Bank", new WorldPoint(2611, 3092, 0)),
	TREE_GNOME_NIEVE("Tree Gnome Nieve", new WorldPoint(2611, 3092, 0)),
	BARBARIAN_BANK("Barbarian Bank", new WorldPoint(2533, 3572, 0)),
	BURTHORPE_SLAYER("Burthorpe Slayer", new WorldPoint(2931, 3536, 0)),
	FISHING_GUILD("Fishing Guild", new WorldPoint(2611,3393,0)),
	WINTERTODT_BANK("Wintertodt Bank", new WorldPoint(1639, 3943, 0)),
	CUSTOM("Custom");

	private final String name;
	private WorldPoint worldPoint;

	Location(String name)
	{
		this.name = name;
	}

	Location(String name, WorldPoint worldPoint)
	{
		this.name = name;
		this.worldPoint = worldPoint;
	}
}
