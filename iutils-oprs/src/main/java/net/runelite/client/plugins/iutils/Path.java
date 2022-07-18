package net.runelite.client.plugins.iutils;

import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

class Path {
    private WorldPoint start;
    private WorldPoint end;
    private Player player;

    Path(WorldPoint start, WorldPoint end, Player player) {
        this.start = start;
        this.end = end;
        this.player = player;
    }
}
