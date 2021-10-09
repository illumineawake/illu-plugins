/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;

@Getter
public enum Quest {
    CLIENT_OF_KOUREND("Client of Kourend",
            "1825,3690,0 - Veos on Pisc dock\n1807,3726,0 - Leenz at Pisc general store\n1772,3588,0 - Horace at Hosidius general store\n1545,3630,0 - Jennifer at Shayzien general store\n1551,3719,0 - Munty at Lovakengj general store\n1720,3724,0 - Regath at Arceuus general store\n1713,3883,0 - Dark Altar\n\nBring 1 feather"),
    BIOHAZARD("Biohazard",
            "Biohazard:\n\n2592,3340,0 - Elena's house\n2560,3300,0 - West Ardy gate\n2560,3265,0 - West Ardy Wall climb\n2551,3320,0 - Mourner HQ entry\n2519,3275,0 - Nurse Sarah's house\n\n2932,3215,0 - Chemist's house\n3262,3406,0 - Varrock Fenced Area\n\n2576,3298,0 - Ardy Castle entry"),
    GERTRUDES_CAT("Gertrude's Cat",
            "Gertrudes Cat:\n3251,3268,0 - dairy cow\n3151,3413,0 - Gertrude's house\n(grab doogle leaves and make seasoned sardine here)\n\n3217,3432,0 - varrock news stand\n3308,3491,0 - lumberyard");

    private final String name;
    private final String notes;

    Quest(String name, String notes) {
        this.name = name;
        this.notes = notes;
    }
}
