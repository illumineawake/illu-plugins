package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.scene.Position;

import java.util.function.Consumer;

public class Transport {
    public final Position source;
    public final Position target;
    public final Consumer<Game> handler;
    public final int targetRadius;
    public final int sourceRadius;

    public Transport(Position source, Position target, int sourceRadius, int targetRadius, Consumer<Game> handler) {
        this.source = source;
        this.target = target;
        this.targetRadius = targetRadius;
        this.handler = handler;
        this.sourceRadius = sourceRadius;
    }
}
