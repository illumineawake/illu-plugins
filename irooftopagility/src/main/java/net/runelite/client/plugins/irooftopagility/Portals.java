package net.runelite.client.plugins.irooftopagility;

import lombok.AccessLevel;
import lombok.Getter;

import static net.runelite.api.NullObjectID.*;

public enum Portals {
    //Example of the enum, Portal ID first, Varbit value second. I didn't check these ID's or varbit values but you get the picture :)
    PORTAL_ONE(NULL_36241, 1),
    PORTAL_TWO(NULL_36242, 2),
    PORTAL_THREE(NULL_36243, 3),
    PORTAL_FOUR(NULL_36244, 4),
    PORTAL_FIVE(NULL_36245, 5),
    PORTAL_SIX(NULL_36246, 6);

    //getters
    @Getter(AccessLevel.PACKAGE)
    private final int portalID;

    @Getter(AccessLevel.PACKAGE)
    private final int varbitValue;

    //constructor
    Portals(final int portalID, final int varbitValue) {
        this.portalID = portalID;
        this.varbitValue = varbitValue;
    }

    //function that we will use in our plugin. We provide the current Portal varbit value and it returns the correlating Portal. i.e. a varbit value of 2 would return portal 2
    public static Portals getPortal(int varbitValue) {
        for (Portals portal : values()) {
            if (portal.getVarbitValue() == varbitValue) {
                return portal;
            }
        }
        return null;
    }
}
