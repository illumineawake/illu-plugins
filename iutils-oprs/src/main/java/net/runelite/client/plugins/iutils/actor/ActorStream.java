package net.runelite.client.plugins.iutils.actor;

import net.runelite.client.plugins.iutils.game.iActor;
import net.runelite.client.plugins.iutils.scene.LocatableStream;
import net.runelite.client.plugins.iutils.scene.Position;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class ActorStream<T extends iActor, S extends ActorStream<T, S>> extends LocatableStream<T, S> {
    public ActorStream(Stream<T> stream) {
        super(stream);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#name()}s
     */
    public S withName(String... names) {
        return filter(a -> Arrays.stream(names).anyMatch(name -> Objects.equals(a.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#name()}s
     */
    public S withName(Collection<String> names) {
        return filter(a -> names.stream().anyMatch(name -> Objects.equals(a.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * a {@link iActor#combatLevel()} inside the given range.
     *
     * @param min the minimum combat level (inclusive)
     * @param max the maximum combat level (inclusive)
     */
    public S withCombatLevel(int min, int max) {
        return filter(a -> a.combatLevel() >= min && a.combatLevel() <= max);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * a {@link iActor#target()}.
     */
    public S withTarget() {
        return filter(a -> a.target() != null);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * no {@link iActor#target()}.
     */
    public S withoutTarget() {
        return filter(a -> a.target() == null);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#target()}s
     */
    public S withTarget(iActor... targets) {
        return filter(a -> Arrays.stream(targets).anyMatch(target -> Objects.equals(a.target(), target)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#target()}s
     */
    public S withTarget(Collection<iActor> targets) {
        return filter(a -> targets.stream().anyMatch(target -> Objects.equals(a.target(), target)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#orientation()}s
     */
    public S withOrientation(int... orientations) {
        return filter(o -> Arrays.stream(orientations).anyMatch(orientation -> o.orientation() == orientation));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#orientation()}s
     */
    public S withOrientation(Collection<Integer> orientations) {
        return filter(o -> orientations.stream().anyMatch(orientation -> o.orientation() == orientation));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#position()}s
     */
    public S withPosition(Position... positions) {
        return filter(o -> Arrays.stream(positions).anyMatch(position -> o.position().equals(position)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#position()}s
     */
    public S withPosition(Collection<Position> positions) {
        return filter(o -> positions.stream().anyMatch(position -> o.position().equals(position)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * an animation.
     */
    public S withAnimation() {
        return filter(a -> a.animation() != -1);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#animation()}s
     */
    public S withAnimation(int... animations) {
        return withAnimation().filter(o -> Arrays.stream(animations).anyMatch(orientation -> o.animation() == orientation));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iActor#animation()}s
     */
    public S withAnimation(Collection<Integer> animations) {
        return withAnimation().filter(o -> animations.stream().anyMatch(orientation -> o.animation() == orientation));
    }
}
