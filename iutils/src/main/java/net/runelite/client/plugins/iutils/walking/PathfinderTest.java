package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.util.Util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

public class PathfinderTest {
    private static final CollisionMap map;
    public static final Position START = new Position(3209, 3220, 2);
    public static final Position END = new Position(3224, 3219, 0);

    static {
        try {
            map = new CollisionMap(Util.ungzip(Walking.class.getResourceAsStream("/collision-map").readAllBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("Position[] path = {");

        for (var position : new Pathfinder(map, Map.of(), List.of(START), p -> p.equals(END)).find()) {
            System.out.print("    new Position");
            System.out.print(position);
            System.out.println(",");
        }

        while (true) {
            var s = System.nanoTime();
            new Pathfinder(map, Map.of(), List.of(START), p -> p.equals(END)).find();
            System.out.println((System.nanoTime() - s) / 1000000. + "ms");
        }
    }
}
