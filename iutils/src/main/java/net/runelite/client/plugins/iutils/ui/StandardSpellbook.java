package net.runelite.client.plugins.iutils.ui;

import net.runelite.client.plugins.iutils.api.Magic;
import net.runelite.client.plugins.iutils.game.Game;

import javax.inject.Inject;

/**
 * @author kylestev
 */
public class StandardSpellbook {
    private final Game game;

    @Inject
    public StandardSpellbook(Game game) {
        this.game = game;
    }

    public void castSpell(Magic spell) {
        game.widget(218, spell.widgetChild).interact("Cast");
    }

    public void lumbridgeHomeTeleport() {
        // TODO: check response to update timer in Profile
        castSpell(Magic.LUMBRIDGE_HOME_TELEPORT);
        game.waitUntil(() -> game.localPlayer().position().regionID() == 12850, 30);
        game.tick(5);
    }
}
