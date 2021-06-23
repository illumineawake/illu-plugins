package net.runelite.client.plugins.iutils.walking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.scene.Position;

@Getter
@AllArgsConstructor
public enum TeleportTab {

    VARROCK_TELEPORT_TAB(new Position(3212, 3424, 0), "Varrock teleport", ItemID.VARROCK_TELEPORT),
    LUMBRIDGE_TELEPORT_TAB(new Position(3225, 3219, 0), "Lumbridge teleport", ItemID.LUMBRIDGE_TELEPORT),
    FALADOR_TELEPORT_TAB(new Position(2966, 3379, 0), "Falador teleport", ItemID.FALADOR_TELEPORT),
    CAMELOT_TELEPORT_TAB(new Position(2757, 3479, 0), "Camelot teleport", ItemID.CAMELOT_TELEPORT),
    ARDOUGNE_TELEPORT_TAB(new Position(2661, 3300, 0), "Ardougne teleport", ItemID.ARDOUGNE_TELEPORT),
    WEST_ARDOUGNE_TELEPORT_TAB(new Position(2500, 3290, 0), "West ardougne teleport", ItemID.WEST_ARDOUGNE_TELEPORT),
    RIMMINGTON_TELEPORT_TAB(new Position(2954, 3224, 0), "Rimmington teleport", ItemID.RIMMINGTON_TELEPORT),
    TAVERLEY_TELEPORT_TAB(new Position(2894, 3465, 0), "Taverley teleport", ItemID.TAVERLEY_TELEPORT),
    RELLEKKA_TELEPORT_TAB(new Position(2668, 3631, 0), "Rellekka teleport", ItemID.RELLEKKA_TELEPORT),
    BRIMHAVEN_TELEPORT_TAB(new Position(2758, 3178, 0), "Brimhaven teleport", ItemID.BRIMHAVEN_TELEPORT),
    POLLNIVNEACH_TELEPORT_TAB(new Position(3340, 3004, 0), "Pollnivneach teleport", ItemID.POLLNIVNEACH_TELEPORT),
    YANILLE_TELEPORT_TAB(new Position(2544, 3095, 0), "Yanille teleport", ItemID.YANILLE_TELEPORT),
    HOSIDIUS_TELEPORT_TAB(new Position(1744, 3517, 0), "Hosidius teleport", ItemID.HOSIDIUS_TELEPORT),
    SALVE_GRAVEYARD_TELEPORT_TAB(new Position(3432, 3460, 0), "Salve Graveyard teleport", ItemID.SALVE_GRAVEYARD_TELEPORT);

    private final Position location;
    private final String tabletName;
    private final int tabletId;

    public boolean canUse(Game game) {
        return hasRequirements(game) && game.inventory().withName(this.tabletName).exists();
    }

    public boolean hasRequirements(Game game) {
        if (this == SALVE_GRAVEYARD_TELEPORT_TAB && game.varp(302) < 61) {
            return false;
        }
        return this != ARDOUGNE_TELEPORT_TAB || game.varp(165) >= 30;
    }
}