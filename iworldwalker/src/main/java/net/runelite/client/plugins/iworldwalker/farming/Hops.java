/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker.farming;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Hops {
    NONE("None"),
    LUMBRIDGE("Lumbridge", new WorldPoint(3231, 3312, 0)),
    SEERS_VILLAGE("Seers' Village", new WorldPoint(2670, 3522, 0)),
    YANILLE("Yanille", new WorldPoint(2578, 3102, 0));

    private final String name;
    private WorldPoint worldPoint;

    Hops(String name, WorldPoint worldPoint) {
        this.name = name;
        this.worldPoint = worldPoint;
    }

    Hops(String name) {
        this.name = name;
    }
}
