package net.runelite.client.plugins.iutils.walking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.api.SpellBook;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.scene.Position;

@Getter
@AllArgsConstructor
public enum TeleportTab {

    VARROCK_TELEPORT_TAB(new Position(3212, 3424, 0), "Varrock teleport"),
    LUMBRIDGE_TELEPORT_TAB(new Position(3225, 3219, 0),"Lumbridge teleport"),
    FALADOR_TELEPORT_TAB(new Position(2966, 3379, 0),"Falador teleport"),
    CAMELOT_TELEPORT_TAB(new Position(2757, 3479, 0),"Camelot Teleport"),
    ARDOUGNE_TELEPORT_TAB(new Position(2661, 3300, 0), "Ardougne teleport"),
    WEST_ARDOUGNE_TELEPORT_TAB(new Position(2500,3290,0), "West ardougne teleport"),
    RIMMINGTON_TELEPORT_TAB(new Position(2954,3224, 0), "Rimmington teleport"),
    TAVERLEY_TELEPORT_TAB(new Position(2894, 3465, 0), "Taverley teleport"),
    RELLEKKA_TELEPORT_TAB(new Position(2668, 3631, 0), "Rellekka teleport"),
    BRIMHAVEN_TELEPORT_TAB(new Position(2758, 3178, 0), "Brimhaven teleport"),
    POLLNIVNEACH_TELEPORT_TAB(new Position(3340, 3004, 0), "Pollnivneach teleport"),
    YANILLE_TELEPORT_TAB(new Position(2544, 3095, 0), "Yanille teleport"),
    HOSIDIUS_TELEPORT_TAB(new Position(1744, 3517, 0), "Hosidius teleport"),
    SALVE_GRAVEYARD_TELEPORT_TAB(new Position(3432, 3460, 0), "Salve Graveyard teleport")
    ;

    private final Position location;
    private final String tabletName;


    public boolean canUse(Game game) {
        return game.inventory().withName(this.tabletName).exists();
    }
}