package net.runelite.client.plugins.icombinationrunecrafter;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum EssenceTypes {
    PURE_ESSENCE("Pure Essence", ItemID.PURE_ESSENCE),
    DAEYALT_ESSENCE("Daeyalt Essence", ItemID.DAEYALT_ESSENCE);

    private final String name;
    private final int id;

    EssenceTypes(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
