package net.runelite.client.plugins.iutils.ui;

import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.iutils.Spells;
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

    public void castSpell(Spells spell) {
        castSpell(spell.getInfo());
    }

    public void castSpell(WidgetInfo spellInfo) {
        game.widget(spellInfo).interact("Cast");
    }

    public void lumbridgeHomeTeleport() {
        // TODO: check response to update timer in Profile
        if (game.localPlayer().position().regionID() == 12850) {
            return;
        }

        castSpell(Spells.LUMBRIDGE_TELEPORT);
        game.waitUntil(() -> game.localPlayer().position().regionID() == 12850, 30);
        game.tick(5);
    }
}
