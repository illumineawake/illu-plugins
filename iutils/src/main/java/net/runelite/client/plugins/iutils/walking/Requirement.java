package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.game.Game;

public interface Requirement {

    Game game();

    boolean satisfies();
}
