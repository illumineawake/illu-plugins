package net.runelite.client.plugins.iutils.api;

import lombok.Getter;
import net.runelite.client.plugins.iutils.walking.TeleportLoader;
import net.runelite.client.plugins.iutils.walking.TeleportSpell;
import net.runelite.client.plugins.iutils.walking.TeleportTab;

import static net.runelite.client.plugins.iutils.walking.TeleportSpell.*;
import static net.runelite.client.plugins.iutils.walking.TeleportTab.*;

@Getter
public enum TeleportLocation {
    ARDOUGNE(ARDOUGNE_TELEPORT_TAB, ARDOUGNE_TELEPORT, TeleportLoader.NECKLACE_OF_PASSAGE, TeleportLoader.SKILLS_NECKLACE),
    ARDOUGNE_WEST(WEST_ARDOUGNE_TELEPORT_TAB, ARDOUGNE_TELEPORT, TeleportLoader.NECKLACE_OF_PASSAGE, TeleportLoader.NECKLACE_OF_PASSAGE, TeleportLoader.SKILLS_NECKLACE),
    CAMELOT(CAMELOT_TELEPORT_TAB, CAMELOT_TELEPORT),
    DRAYNOR(null, null, TeleportLoader.AMULET_OF_GLORY),
    FALADOR(FALADOR_TELEPORT_TAB, FALADOR_TELEPORT, TeleportLoader.RING_OF_WEALTH),
    LUMBRIDGE(LUMBRIDGE_TELEPORT_TAB, LUMBRIDGE_TELEPORT),
    MORYTANIA(SALVE_GRAVEYARD_TELEPORT_TAB),
    RIMMINGTON(FALADOR_TELEPORT_TAB, FALADOR_TELEPORT, TeleportLoader.SKILLS_NECKLACE),
    VARROCK_CENTRE(VARROCK_TELEPORT_TAB, VARROCK_TELEPORT),
    GRAND_EXCHANGE(VARROCK_TELEPORT_TAB, VARROCK_TELEPORT, TeleportLoader.RING_OF_WEALTH),
    TREE_GNOME_STRONGHOLD(null, null, TeleportLoader.NECKLACE_OF_PASSAGE),
    WIZARD_TOWER(LUMBRIDGE_TELEPORT_TAB, LUMBRIDGE_TELEPORT, TeleportLoader.NECKLACE_OF_PASSAGE, TeleportLoader.AMULET_OF_GLORY);
    private TeleportTab teleportTab;
    private TeleportSpell teleportSpell;
    private int[][] itemIds;

    TeleportLocation(TeleportTab teleportTab, TeleportSpell teleportSpell) {
        this.teleportTab = teleportTab;
        this.teleportSpell = teleportSpell;
    }

    TeleportLocation(TeleportTab teleportTab) {
        this.teleportTab = teleportTab;
        this.teleportSpell = null;
    }

    TeleportLocation(TeleportTab teleportTab, TeleportSpell teleportSpell, int[]... itemIds) {
        this.teleportTab = teleportTab;
        this.teleportSpell = teleportSpell;
        this.itemIds = itemIds;
    }
}
