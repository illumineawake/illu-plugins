/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum Barcrawl {
    NONE(""),
    BARBARIAN_ASSAULT("Barb Assault", new WorldPoint(2533, 3572, 0)),
    JOLLY_BOAR_INN("Jolly Boar Inn", new WorldPoint(3280, 3494, 0)),
    BLUE_MOON_INN("Blue Moon Inn", new WorldPoint(3215, 3395, 0)),
    RISING_SUN_INN("Rising Sun Inn", new WorldPoint(2956, 3372, 0)),
    RUSTY_ANCHOR_INN("Rusty Anchor Inn", new WorldPoint(3053, 3254, 0)),
    KARAMJA_SPIRITS_BAR("Karamja Spirits Bar", new WorldPoint(2924, 3144, 0)),
    DEAD_MANS_CHEST("Dead Man's Chest", new WorldPoint(2796, 3160, 0)),
    FLYING_HORSE_INN("Flying Horse Inn", new WorldPoint(2574, 3320, 0)),
    FORESTERS_ARM("Forester's Arm", new WorldPoint(2693, 3492, 0)),
    DRAGON_INN("Dragon Inn", new WorldPoint(2551, 3083, 0));

    private final String name;
    private WorldPoint worldPoint;

    Barcrawl(String name) {
        this.name = name;
    }

    Barcrawl(String name, WorldPoint worldPoint) {
        this.name = name;
        this.worldPoint = worldPoint;
    }
}
