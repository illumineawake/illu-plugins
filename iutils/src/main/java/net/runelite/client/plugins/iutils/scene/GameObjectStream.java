package net.runelite.client.plugins.iutils.scene;

import net.runelite.api.ObjectComposition;
import net.runelite.client.plugins.iutils.game.iObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class GameObjectStream extends LocatableStream<iObject, GameObjectStream> {
    public GameObjectStream(Stream<iObject> stream) {
        super(stream);
    }

    @Override
    protected GameObjectStream wrap(Stream<iObject> stream) {
        return new GameObjectStream(stream);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObject#id()}s
     */
    public GameObjectStream withId(int... ids) {
        return filter(o -> Arrays.stream(ids).anyMatch(id -> o.id() == id));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObject#id()}s
     */
    public GameObjectStream withId(Collection<Integer> ids) {
        return filter(o -> ids.stream().anyMatch(id -> o.id() == id));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link ObjectComposition#getId()}s
     */
    public GameObjectStream withDefinitionId(int... ids) {
        return filter(o -> Arrays.stream(ids).anyMatch(id -> o.definition() != null && o.definition().getId() == id));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link ObjectComposition#getId()}s
     */
    public GameObjectStream withDefinitionId(Collection<Integer> ids) {
        return filter(o -> ids.stream().anyMatch(id -> o.definition() != null && o.definition().getId() == id));
    }

//    /**
//     * Returns a stream consisting of the elements of this stream with
//     * any of the given {@link iObject#type()}s
//     */
//    public GameObjectStream withType(ObjectType... types) {
//        return filter(o -> Arrays.stream(types).anyMatch(type -> o.type() == type));
//    }
//
//    /**
//     * Returns a stream consisting of the elements of this stream with
//     * any of the given {@link iObject#orientation()}s
//     */
//    public GameObjectStream withOrientation(int... orientations) {
//        return filter(o -> Arrays.stream(orientations).anyMatch(orientation -> o.orientation() == orientation));
//    }
//
//    /**
//     * Returns a stream consisting of the elements of this stream with
//     * any of the given {@link iObject#sequence()}s
//     */
//    public GameObjectStream withSequence(int... sequences) {
//        return filter(o -> Arrays.stream(sequences).anyMatch(sequence -> o.sequence() == sequence));
//    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObject#name()}s
     */
    public GameObjectStream withName(String... names) {
        return filter(o -> Arrays.stream(names).anyMatch(name -> Objects.equals(o.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObject#name()}s
     */
    public GameObjectStream withName(Collection<String> names) {
        return filter(o -> names.stream().anyMatch(name -> Objects.equals(o.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObject#actions()}s
     */
    public GameObjectStream withAction(String... actions) {
        return filter(o -> Arrays.stream(actions).anyMatch(action -> o.actions().contains(action)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObject#actions()}s
     */
    public GameObjectStream withAction(Collection<String> actions) {
        return filter(o -> actions.stream().anyMatch(action -> o.actions().contains(action)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObject#position()}s
     */
    public GameObjectStream withPosition(Position... positions) {
        return filter(o -> Arrays.stream(positions).anyMatch(position -> o.position().equals(position)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObject#position()}s
     */
    public GameObjectStream withPosition(Collection<Position> positions) {
        return filter(o -> positions.stream().anyMatch(position -> o.position().equals(position)));
    }
}
