package net.runelite.client.plugins.iutils.scene;

import net.runelite.client.plugins.iutils.game.iGroundItem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class GroundItemStream extends LocatableStream<iGroundItem, GroundItemStream> {
    public GroundItemStream(Stream<iGroundItem> stream) {
        super(stream);
    }

    @Override
    protected GroundItemStream wrap(Stream<iGroundItem> stream) {
        return new GroundItemStream(stream);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iGroundItem#id()}s
     */
    public GroundItemStream withId(int... ids) {
        return filter(o -> Arrays.stream(ids).anyMatch(id -> o.id() == id));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iGroundItem#id()}s
     */
    public GroundItemStream withId(Collection<Integer> ids) {
        return filter(o -> ids.stream().anyMatch(id -> o.id() == id));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * a minimum {@link iGroundItem#quantity()}
     */
    public GroundItemStream withMinimumQuantity(int quantity) {
        return filter(o -> o.quantity() >= quantity);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iGroundItem#name()}s
     */
    public GroundItemStream withName(String... names) {
        return filter(o -> Arrays.stream(names).anyMatch(name -> Objects.equals(o.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iGroundItem#name()}s
     */
    public GroundItemStream withName(Collection<String> names) {
        return filter(o -> names.stream().anyMatch(name -> Objects.equals(o.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iGroundItem#actions()}s
     */
    public GroundItemStream withAction(String... actions) {
        return filter(o -> Arrays.stream(actions).anyMatch(action -> o.actions().contains(action)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iGroundItem#actions()}s
     */
    public GroundItemStream withAction(Collection<String> actions) {
        return filter(o -> actions.stream().anyMatch(action -> o.actions().contains(action)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iGroundItem#position()}s
     */
    public GroundItemStream withPosition(Position... positions) {
        return filter(o -> Arrays.stream(positions).anyMatch(position -> o.position().equals(position)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link iGroundItem#position()}s
     */
    public GroundItemStream withPosition(Collection<Position> positions) {
        return filter(o -> positions.stream().anyMatch(position -> o.position().equals(position)));
    }
}
