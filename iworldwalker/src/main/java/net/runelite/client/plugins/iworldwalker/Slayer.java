/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Slayer {
    NONE(""),
    BURTHORPE_SLAYER("Turael", new WorldPoint(2931, 3536, 0)),
    FREMENNIK_CAVE("Fremennik Cave", new WorldPoint(2794, 3615, 0)),
    KARUULM_KONAR("Konar", new WorldPoint(1308, 3786, 0)),
    LUMBRIDGE_SWAMP_CAVE("Lumby Swamp Cave", new WorldPoint(3169, 3172, 0)),
    SLAYER_TOWER("Slayer Tower", new WorldPoint(3429, 3530, 0)),
    TREE_GNOME_NIEVE("Nieve", new WorldPoint(2611, 3092, 0));

    private final String name;
    private WorldPoint worldPoint;

    Slayer(String name) {
        this.name = name;
    }

    Slayer(String name, WorldPoint worldPoint) {
        this.name = name;
        this.worldPoint = worldPoint;
    }
}
