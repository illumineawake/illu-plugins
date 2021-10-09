/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Skilling {
    NONE(""),
    ARCEUUS_ESS_MINE("Arceuus Ess Mine", new WorldPoint(2533, 3572, 0)),
    BARB_ASSAULT("Barb Assault", new WorldPoint(2533, 3572, 0)),
    BARB_FISHING("Barbarian Fishing", new WorldPoint(2498, 3508, 0)),
    CRABCLAW_ISLE_BOAT("Crabclaw Isle", new WorldPoint(2533, 3572, 0)),
    HOS_FRUIT_SAFESPOT("HOS Fruit stalls", new WorldPoint(1798, 3605, 0)),
    HOS_KITCHEN("HOS Kitchen", new WorldPoint(1679, 3614, 0)),
    HOS_SALTPETRE("HOS Saltpetre", new WorldPoint(2448, 3222, 0)),
    KOUREND_LIBRARY("Arceuus Library", new WorldPoint(1634, 3796, 0)),
    MOLCH_DOCK("Molch Dock", new WorldPoint(1340, 3646, 0)),
    RED_SALAMANDERS("Red Salamanders", new WorldPoint(2448, 3222, 0)),
    TITHE_FARM("Tithe Farm", new WorldPoint(1792, 3500, 0));

    private final String name;
    private WorldPoint worldPoint;

    Skilling(String name) {
        this.name = name;
    }

    Skilling(String name, WorldPoint worldPoint) {
        this.name = name;
        this.worldPoint = worldPoint;
    }
}
