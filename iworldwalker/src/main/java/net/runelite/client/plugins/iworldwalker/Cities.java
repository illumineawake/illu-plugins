/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Cities {
    NONE(""),
    LUMBRIDGE_CASTLE("Lumbridge Castle", new WorldPoint(3222, 3218, 0)),
    PORT_SARIM("Port Sarim", new WorldPoint(3041, 3236, 0)),
    RELLEKKA("Rellekka", new WorldPoint(2644, 3677, 0)),
    RIMMINGTON_MINE("Rimmington Mine", new WorldPoint(2977, 3240, 0)),
    WIZARD_TOWER("Wizard Tower", new WorldPoint(3109, 3164, 0));

    private final String name;
    private WorldPoint worldPoint;

    Cities(String name) {
        this.name = name;
    }

    Cities(String name, WorldPoint worldPoint) {
        this.name = name;
        this.worldPoint = worldPoint;
    }
}
