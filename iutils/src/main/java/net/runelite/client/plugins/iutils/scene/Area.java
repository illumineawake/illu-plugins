package net.runelite.client.plugins.iutils.scene;

import net.runelite.api.coords.WorldPoint;

import java.util.Arrays;

public interface Area {
    boolean contains(Position position);

//    boolean contains(WorldPoint worldPoint);

    static Area union(Area... areas) {
        return position -> Arrays.stream(areas).anyMatch(a -> a.contains(position));
    }

    static Area intersection(Area... areas) {
        return position -> Arrays.stream(areas).allMatch(a -> a.contains(position));
    }

    default Area minus(Area other) {
        return position -> Area.this.contains(position) && !other.contains(position);
    }
}
