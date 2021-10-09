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
                if (game.widget(593, 11).text().contains("Rapid")) {
                    return CombatStyle.RAPID;
                }
                return CombatStyle.AGGRESSIVE;
            case 2:
                return CombatStyle.SPECIAL;
            case 3:
                return CombatStyle.DEFENSIVE;
            case 4:
                return CombatStyle.MAGIC;
            default:
                throw new IllegalStateException("unexpected combat style id");
        }
    }

    public CombatType currentType() {
        return currentStyle().getCombatType();
    }

    public void setStyle(CombatStyle style) {
        if (currentStyle() != style) {
            game.openInterface(0);
            switch (style) {
                case ACCURATE:
                    game.widget(593, 4).interact(0);
                    break;
                case RAPID:
                case AGGRESSIVE:
                    game.widget(593, 8).interact(0);
                    break;
                case SPECIAL:
                    game.widget(593, 12).interact(0);
                    break;
                case DEFENSIVE:
                    game.widget(593, 16).interact(0);
                    break;
                case MAGIC:
                    game.widget(593, 26).interact(0);
                    return;
            }
            game.waitUntil(() -> currentStyle() == style);
            game.openInterface(3);
        }
    }
}
