package net.runelite.client.plugins.iutils.ui;

import net.runelite.client.plugins.iutils.game.InventoryItem;
import net.runelite.client.plugins.iutils.util.RandomizedStreamAdapter;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class InventoryItemStream extends RandomizedStreamAdapter<InventoryItem, InventoryItemStream> {
    public InventoryItemStream(Stream<InventoryItem> stream) {
        super(stream);
    }

    @Override
    protected InventoryItemStream wrap(Stream<InventoryItem> stream) {
        return new InventoryItemStream(stream);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link InventoryItem#id()}s
     */
    public InventoryItemStream withId(int... ids) {
        return filter(o -> Arrays.stream(ids).anyMatch(id -> o.id() == id));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * a minimum {@link InventoryItem#quantity()}
     */
    public InventoryItemStream withMinimumQuantity(int quantity) {
        return filter(o -> o.quantity() >= quantity);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link InventoryItem#name()}s
     */
    public InventoryItemStream withName(String... names) {
        return filter(o -> Arrays.stream(names).anyMatch(name -> Objects.equals(o.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream whose
     * {@link InventoryItem#name()}s contain any of the given name parts
     */
    public InventoryItemStream withNamePart(String... names) {
        return filter(o -> Arrays.stream(names).anyMatch(name -> o.name().contains(name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link InventoryItem#actions()}s
     */
    public InventoryItemStream withAction(String... actions) {
        return filter(o -> Arrays.stream(actions).anyMatch(action -> o.actions().contains(action)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link InventoryItem#slot()}s
     */
    public InventoryItemStream withSlot(int... slots) {
        return filter(o -> Arrays.stream(slots).anyMatch(slot -> o.slot() == slot));
    }

    /**
     * Returns the total {@link InventoryItem#quantity()} of the elements of this stream.
     */
    public int quantity() {
        return reduce(0, (quantity, item) -> quantity + item.quantity(), Integer::sum);
    }

    public void drop() {
        forEachWaiting(item -> item.interact("Drop"));
    }

    public void forEachWaiting(Consumer<InventoryItem> action) {
        forEach(item -> {
            action.accept(item);
//            item.game().sleepApproximately(300); TODO: implement sleep
        });
    }

    public boolean full() {
        return size() == 28;
    }
}
