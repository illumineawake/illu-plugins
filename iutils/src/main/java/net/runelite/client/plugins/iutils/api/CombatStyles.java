package net.runelite.client.plugins.iutils.api;

import net.runelite.client.plugins.iutils.game.Game;

import javax.inject.Inject;

public class CombatStyles {

    @Inject
    private Game game;

    @Inject
    public CombatStyles() {
    }

    public CombatStyle currentStyle() {
        switch (game.varp(43)) {
            case 0:
                return CombatStyle.ACCURATE;
            case 1:
                return CombatStyle.AGGRESSIVE;
            case 2:
                return CombatStyle.SPECIAL;
            case 3:
                return CombatStyle.DEFENSIVE;
            default:
                throw new IllegalStateException("unexpected combat style id");
        }
    }

    public void setStyle(CombatStyle style) {
        if (currentStyle() != style) {
            switch (style) {
                case ACCURATE:
                    game.widget(593, 4).interact(0);
                    break;
                case AGGRESSIVE:
                    game.widget(593, 8).interact(0);
                    break;
                case SPECIAL:
                    game.widget(593, 12).interact(0);
                    break;
                case DEFENSIVE:
                    game.widget(593, 16).interact(0);
            }

            game.waitUntil(() -> currentStyle() == style);
        }
    }
}
