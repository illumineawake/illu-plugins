/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;

@Getter
public enum Farming {
    NONE("None"),
    ALLOTMENTS("Allotments"),
    BUSHES("Bushes"),
    FRUIT_TREES("Fruit Trees"),
    HERBS("Herbs"),
    HOPS("Hops"),
    TREES("Trees");

    private final String name;

    Farming(String name) {
        this.name = name;
    }

}
