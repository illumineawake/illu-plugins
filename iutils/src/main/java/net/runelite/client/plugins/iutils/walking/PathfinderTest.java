package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.util.Util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

public class PathfinderTest {
    private static final CollisionMap map;
    public static final Position START = new Position(3225, 3218, 0);
    public static final Position END = new Position(3166, 9999, 0);

    static {
        try {
            map = new CollisionMap(Util.ungzip(Walking.class.getResourceAsStream("/collision-map").readAllBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        while (true) {
            var s = System.nanoTime();
            new Pathfinder(map, Map.of(), List.of(START), p -> p.equals(END)).find();
            System.out.println((System.nanoTime() - s) / 1000000. + "ms");
        }
    }
}
