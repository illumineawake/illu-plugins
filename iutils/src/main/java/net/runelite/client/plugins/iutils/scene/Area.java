package net.runelite.client.plugins.iutils.scene;

import java.util.Arrays;

public abstract class Area {
    public abstract boolean contains(Position position);

    public static Area union(Area... areas) {
        return new Area() {
            @Override
            public boolean contains(Position position) {
                return Arrays.stream(areas).anyMatch(a -> a.contains(position));
            }
        };
    }

    public static Area intersection(Area... areas) {
        return new Area() {
            @Override
            public boolean contains(Position position) {
                return Arrays.stream(areas).allMatch(a -> a.contains(position));
            }
        };
    }

    public Area minus(Area other) {
        return new Area() {
            @Override
            public boolean contains(Position position) {
                return Area.this.contains(position) && !other.contains(position);
            }
        };
    }
}
