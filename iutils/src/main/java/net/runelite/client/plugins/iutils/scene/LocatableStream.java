package net.runelite.client.plugins.iutils.scene;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.game.iObject;
import net.runelite.client.plugins.iutils.util.RandomizedStreamAdapter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
public abstract class LocatableStream<T extends Locatable, S extends LocatableStream<T, S>> extends RandomizedStreamAdapter<T, S> {
    //	@Inject private Client client;
    protected LocatableStream(Stream<T> stream) {
        super(stream);
    }

    /**
     * Returns a stream consisting of the elements of this stream whose
     * {@link Locatable#position()} is contained in the given {@link Area}.
     */
    public S inside(Area area) {
        return filter(o -> area.contains(o.position()));
    }

    /**
     * Returns the element of the stream whose {@link Locatable#position()} is
     * nearest to the local player.
     */
    public T nearest() {
        return min(Comparator.comparing(o -> o.position().distanceTo(o.client().getLocalPlayer().getWorldLocation()))).orElse(null);
    }

    /**
     * Returns a stream whose elements have a {@link Locatable#position()} within
     * {@code distance} to the local player.
     */
    public S within(int distance) {
        return filter(o -> o.position().distanceTo(o.client().getLocalPlayer().getWorldLocation()) <= distance);
    }

    /**
     * Returns the element of the stream whose {@link Locatable#position()} is
     * nearest to the given {@link Position}.
     */
    public T nearest(Position position) {
        return min(Comparator.comparing(o -> o.position().distanceTo(position))).orElse(null);
    }

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * by increasing distance to the local player.
     */
    public S nearestFirst() {
        return sorted(Comparator.comparing(o -> o.position().distanceTo(o.client().getLocalPlayer().getWorldLocation())));
    }

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * by increasing distance to the given {@link Position}.
     */
    public S nearestFirst(Position position) {
        return sorted(Comparator.comparing(o -> o.position().distanceTo(position)));
    }

    /**
     * Returns the element of the stream whose {@link Locatable#position()} is
     * nearest to the local player by path length.
     */
    public T nearestPath() {
        return min(Comparator.comparing(o -> o.position().nearestReachable(o.client().getLocalPlayer().getWorldLocation()).pathLength(o.client().getLocalPlayer().getWorldLocation()))).orElse(null);
    }

    /**
     * Returns the element of the stream whose {@link Locatable#position()} is
     * nearest to the given {@link Position} by path length.
     */
    public T nearestPath(Position position) {
        return min(Comparator.comparing(o -> o.position().nearestReachable(position).pathLength(position))).orElse(null);
    }

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * by increasing path distance to the local player.
     */
    public S nearestPathFirst() {
        return sorted(Comparator.comparing(o -> o.position().nearestReachable(o.client().getLocalPlayer().getWorldLocation()).pathLength(o.client().getLocalPlayer().getWorldLocation())));
    }

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * by increasing path distance to the given {@link Position}.
     */
    public S nearestPathFirst(Position position) {
        return sorted(Comparator.comparing(o -> o.position().nearestReachable(position).pathLength(position)));
    }
}
