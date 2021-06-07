/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.imagiccaster;

import lombok.Getter;

@Getter
public enum CastType {

    AUTO_CAST("Auto-cast"),
    SINGLE_CAST("Single cast", "Cast"),
    HIGH_ALCHEMY("High Alchemy", "Cast"),
    TELE_GRAB("Tele Grab", "Cast");

    private final String name;
    private String menuOption = "";

    CastType(String name) {
        this.name = name;
    }

    CastType(String name, String menuOption) {
        this.name = name;
        this.menuOption = menuOption;
    }
}
