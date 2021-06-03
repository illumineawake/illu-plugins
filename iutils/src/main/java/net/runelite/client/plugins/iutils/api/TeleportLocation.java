package net.runelite.client.plugins.iutils.api;

import lombok.Getter;
import net.runelite.client.plugins.iutils.walking.TeleportSpell;
import net.runelite.client.plugins.iutils.walking.TeleportTab;

import static net.runelite.client.plugins.iutils.walking.TeleportSpell.*;
import static net.runelite.client.plugins.iutils.walking.TeleportTab.*;

@Getter
public enum TeleportLocation {
    VARROCK(VARROCK_TELEPORT_TAB, VARROCK_TELEPORT),
    LUMBRIDGE(LUMBRIDGE_TELEPORT_TAB, LUMBRIDGE_TELEPORT),
    FALADOR(FALADOR_TELEPORT_TAB, FALADOR_TELEPORT),
    CAMELOT(CAMELOT_TELEPORT_TAB, CAMELOT_TELEPORT),
    ARDOUGNE(ARDOUGNE_TELEPORT_TAB, ARDOUGNE_TELEPORT),
    WEST_ARDOUGNE(WEST_ARDOUGNE_TELEPORT_TAB, ARDOUGNE_TELEPORT),
    RIMMINGTON(RIMMINGTON_TELEPORT_TAB, FALADOR_TELEPORT),
    TAVERLEY(TAVERLEY_TELEPORT_TAB),
    RELLEKKA(RELLEKKA_TELEPORT_TAB),
    BRIMHAVEN(BRIMHAVEN_TELEPORT_TAB),
    POLLNIVNEACH(POLLNIVNEACH_TELEPORT_TAB),
    YANILLE(YANILLE_TELEPORT_TAB),
    HOSIDIUS(HOSIDIUS_TELEPORT_TAB),
    MORYTANIA(SALVE_GRAVEYARD_TELEPORT_TAB);

    private TeleportTab teleportTab;
    private TeleportSpell teleportSpell;

    TeleportLocation(TeleportTab teleportTab, TeleportSpell teleportSpell) {
        this.teleportTab = teleportTab;
        this.teleportSpell = teleportSpell;
    }

    TeleportLocation(TeleportTab teleportTab) {
        this.teleportTab = teleportTab;
        this.teleportSpell = null;
    }
}
