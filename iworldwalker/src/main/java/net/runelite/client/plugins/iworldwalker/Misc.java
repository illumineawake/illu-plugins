/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Misc {
    NONE(""),
    GOBLIN_VILLAGE("Goblin Village", new WorldPoint(2956, 3505, 0)),
    PATERDOMUS_TEMPLE("Paterdomus Temple", new WorldPoint(3405, 3489, 0)),
    RIMMINGTON_MINE("Rimmington Mine", new WorldPoint(2977, 3240, 0)),
    WINTERTODT_BANK("Wintertodt Bank", new WorldPoint(1639, 3943, 0)),
    WIZARD_TOWER("Wizard Tower", new WorldPoint(3109, 3169, 0));

    private final String name;
    private WorldPoint worldPoint;

    Misc(String name) {
        this.name = name;
    }

    Misc(String name, WorldPoint worldPoint) {
        this.name = name;
        this.worldPoint = worldPoint;
    }
}
