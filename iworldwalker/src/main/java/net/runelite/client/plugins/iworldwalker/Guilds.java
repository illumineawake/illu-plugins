/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Guilds {
    NONE(""),
    FARMING_GUILD("Farming Guild", new WorldPoint(1249, 3719, 0)),
    FISHING_GUILD("Fishing Guild", new WorldPoint(2611, 3393, 0)),
    WOODCUTTING_GUILD("Woodcutting Guild", new WorldPoint(1659, 3504, 0));

    private final String name;
    private WorldPoint worldPoint;

    Guilds(String name) {
        this.name = name;
    }

    Guilds(String name, WorldPoint worldPoint) {
        this.name = name;
        this.worldPoint = worldPoint;
    }
}
