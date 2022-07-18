package net.runelite.client.plugins.iutils.ui;

import net.runelite.client.plugins.iutils.api.EquipmentSlot;
import net.runelite.client.plugins.iutils.game.EquipmentItem;
import net.runelite.client.plugins.iutils.util.RandomizedStreamAdapter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class EquipmentItemStream extends RandomizedStreamAdapter<EquipmentItem, EquipmentItemStream> {
    public EquipmentItemStream(Stream<EquipmentItem> stream) {
        super(stream);
    }

    @Override
    protected EquipmentItemStream wrap(Stream<EquipmentItem> stream) {
        return new EquipmentItemStream(stream);
    }

/**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link EquipmentItem#id()}s
     */
    public EquipmentItemStream withId(int... ids) {
        return filter(o -> Arrays.stream(ids).anyMatch(id -> o.id() == id));
    }
    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link EquipmentItem#id()}s
     */
    public EquipmentItemStream withId(Collection<Integer> ids) {
        return filter(o -> ids.stream().anyMatch(id -> o.id() == id));
    }
    /**
     * Returns a stream consisting of the elements of this stream that don't match
     * any of the given {@link EquipmentItem#id()}s
     */
    public EquipmentItemStream withoutId(Collection<Integer> ids) {
        return filter(o -> ids.stream().noneMatch(id -> o.id() == id));
    }

    /**
     * Returns a stream consisting of the elements of this stream that don't match
     * any of the given {@link EquipmentItem#id()}s
     */
    public EquipmentItemStream withoutId(int... ids) {
        return filter(o -> Arrays.stream(ids).noneMatch(id -> o.id() == id));
    }
    /**
     * Returns a stream consisting of the elements of this stream with
     * a minimum {@link EquipmentItem#quantity()}
     */
    public EquipmentItemStream withMinimumQuantity(int quantity) {
        return filter(o -> o.quantity() >= quantity);
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link EquipmentItem#name()}s
     */
    public EquipmentItemStream withName(String... names) {
        return filter(o -> Arrays.stream(names).anyMatch(name -> Objects.equals(o.name(), name)));
    }

    /**
     * Returns a stream consisting of the elements of this stream whose
     * {@link EquipmentItem#name()}s contain any of the given name parts
     */
    public EquipmentItemStream withNamePart(String... names) {
        return filter(o -> Arrays.stream(names).anyMatch(name -> o.name().toLowerCase().contains(name.toLowerCase())));
    }
    /**
     * Returns a stream consisting of the elements of this stream that don't match
     * any of the given {@link EquipmentItem#name()}s
     */
    public EquipmentItemStream withoutNamePart(String... names) {
        return filter(o -> Arrays.stream(names).noneMatch(name -> o.name().toLowerCase().contains(name.toLowerCase())));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link EquipmentItem#actions()}s
     */
    public EquipmentItemStream withAction(String... actions) {
        return filter(o -> Arrays.stream(actions).anyMatch(action -> o.actions().contains(action)));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link EquipmentSlot}s
     */
    public EquipmentItemStream withSlot(EquipmentSlot... slots) {
        return filter(o -> Arrays.stream(slots).anyMatch(slot -> o.slot() == slot.index));
    }

    /**
     * Returns a stream consisting of the elements of this stream with
     * any of the given {@link EquipmentItem#slot()}s
     */
    public EquipmentItemStream withSlot(int... slots) {
        return filter(o -> Arrays.stream(slots).anyMatch(slot -> o.slot() == slot));
    }

    public void unequip() {
        forEach(item -> item.interact("Remove"));
    }
}
