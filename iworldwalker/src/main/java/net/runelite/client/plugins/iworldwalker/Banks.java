/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Banks {
    NONE(""),
    ALKHARID_BANK("Al Kharid Bank", new WorldPoint(3270, 3167, 0)),
    ARDOUGNE_EAST_BANK("Ardy East Bank", new WorldPoint(2616, 3333, 0)),
    BARBARIAN_ASSAULT("Barb Assault", new WorldPoint(2533, 3572, 0)),
    CATHERBY_BANK("Catherby Bank", new WorldPoint(2808, 3440, 0)),
    DRAYNOR_BANK("Draynor Bank", new WorldPoint(3093, 3245, 0)),
    EDGEVILLE_BANK("Edgeville Bank", new WorldPoint(3093, 3496, 0)),
    FALADOR_EAST_BANK("Fally East Bank", new WorldPoint(3012, 3356, 0)),
    FALADOR_WEST_BANK("Fally West Bank", new WorldPoint(2945, 3369, 0)),
    GRAND_EXCHANGE("Grand Exchange", new WorldPoint(3163, 3485, 0)),
    MOUNT_KARUULM("Mount Karuulm", new WorldPoint(1324, 3823, 0)),
    MOUNT_QUIDAMORTEM("Mount Quidamortem", new WorldPoint(1254, 3566, 0)),
    SEERS_BANK("Seers Bank", new WorldPoint(2725, 3491, 0)),
    SHANTAY_PASS("Shantay Pass", new WorldPoint(3303, 3121, 0)),
    VARROCK_EAST_BANK("Varrock East Bank", new WorldPoint(3253, 3421, 0)),
    WINTERTODT_BANK("Wintertodt Bank", new WorldPoint(1639, 3943, 0)),
    YANILLE_BANK("Yanille Bank", new WorldPoint(2611, 3092, 0));

    private final String name;
    private WorldPoint worldPoint;

    Banks(String name) {
        this.name = name;
    }

    Banks(String name, WorldPoint worldPoint) {
        this.name = name;
        this.worldPoint = worldPoint;
    }
}
