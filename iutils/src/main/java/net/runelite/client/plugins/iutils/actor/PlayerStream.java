package net.runelite.client.plugins.iutils.actor;

import net.runelite.client.plugins.iutils.game.iPlayer;

import java.util.Arrays;
import java.util.stream.Stream;

public class PlayerStream extends ActorStream<iPlayer, PlayerStream> {
    public PlayerStream(Stream<iPlayer> stream) {
        super(stream);
    }

    @Override
    protected PlayerStream wrap(Stream<iPlayer> stream) {
        return new PlayerStream(stream);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iPlayer#index()}s
     */
    public PlayerStream withIndex(int... indices) {
        return filter(n -> Arrays.stream(indices).anyMatch(index -> n.index() == index));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iPlayer#index()}s
     */
    public PlayerStream withoutIndex(int... indices) {
        return filter(n -> Arrays.stream(indices).anyMatch(index -> n.index() != index));
    }
}
