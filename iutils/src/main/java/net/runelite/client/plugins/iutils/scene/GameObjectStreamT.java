package net.runelite.client.plugins.iutils.scene;

import net.runelite.client.plugins.iutils.bot.iObjectT;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class GameObjectStreamT extends LocatableStream<iObjectT, GameObjectStreamT> {
    public GameObjectStreamT(Stream<iObjectT> stream) {
        super(stream);
    }

    @Override
    protected GameObjectStreamT wrap(Stream<iObjectT> stream) {
        return new GameObjectStreamT(stream);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObjectT#id()}s
     */
    public GameObjectStreamT withId(int... ids) {
        return filter(o -> Arrays.stream(ids).anyMatch(id -> o.id() == id));
    }

//    /**
//     * Returns a stream consisting of the elements of this stream with
//     * any of the given {@link iObjectT#type()}s
//     */
//    public GameObjectStream withType(ObjectType... types) {
//        return filter(o -> Arrays.stream(types).anyMatch(type -> o.type() == type));
//    }
//
//    /**
//     * Returns a stream consisting of the elements of this stream with
//     * any of the given {@link iObjectT#orientation()}s
//     */
//    public GameObjectStream withOrientation(int... orientations) {
//        return filter(o -> Arrays.stream(orientations).anyMatch(orientation -> o.orientation() == orientation));
//    }
//
//    /**
//     * Returns a stream consisting of the elements of this stream with
//     * any of the given {@link iObjectT#sequence()}s
//     */
//    public GameObjectStream withSequence(int... sequences) {
//        return filter(o -> Arrays.stream(sequences).anyMatch(sequence -> o.sequence() == sequence));
//    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObjectT#name()}s
     */
    public GameObjectStreamT withName(String... names) {
        return filter(o -> Arrays.stream(names).anyMatch(name -> Objects.equals(o.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iObjectT#actions()}s
     */
    public GameObjectStreamT withAction(String... actions) {
        return filter(o -> Arrays.stream(actions).anyMatch(action -> o.actions().contains(action)));
    }
}
