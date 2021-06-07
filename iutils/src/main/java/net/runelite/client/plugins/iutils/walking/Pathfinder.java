package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.scene.Position;

import java.util.*;
import java.util.function.Predicate;

public class Pathfinder {
    private final CollisionMap map;
    private final List<Position> starts;
    private final Predicate<Position> target;
    private final Deque<Position> boundary = new ArrayDeque<>();
    private final PositionMap predecessors = new PositionMap();
    private final Map<Position, List<Position>> transports;

    public Pathfinder(CollisionMap map, Map<Position, List<Position>> transports, List<Position> starts, Predicate<Position> target) {
        this.map = map;
        this.transports = transports;
        this.target = target;
        this.starts = starts;
    }

    public List<Position> find() {
        boundary.addAll(starts);

        for (var start : starts) {
            predecessors.put(start, null);
        }

        while (!boundary.isEmpty()) {
            var node = boundary.removeFirst();

            if (target.test(node)) {
                List<Position> result = new LinkedList<>();

                while (node != null) {
                    result.add(0, node);
                    node = predecessors.get(node);
                }

                return result;
            }

            addNeighbors(node);
        }

        return null;
    }

    private void addNeighbors(Position position) {
        // Prefer taking transports as early as possible, to avoid causing problems with ladders which can be taken
        // from several source positions.
        for (var transport : transports.getOrDefault(position, new ArrayList<>())) {
            if (predecessors.containsKey(transport)) {
                continue;
            }

            predecessors.put(transport, position);
            boundary.addLast(transport);
        }

        if (map.w(position.x, position.y, position.z)) {
            var neighbor = new Position(position.x - 1, position.y, position.z);
            if (!predecessors.containsKey(neighbor)) {
                predecessors.putE(neighbor);
                boundary.addLast(neighbor);
            }
        }

        if (map.e(position.x, position.y, position.z)) {
            var neighbor = new Position(position.x + 1, position.y, position.z);
            if (!predecessors.containsKey(neighbor)) {
                predecessors.putW(neighbor);
                boundary.addLast(neighbor);
            }
        }

        if (map.s(position.x, position.y, position.z)) {
            var neighbor = new Position(position.x, position.y - 1, position.z);
            if (!predecessors.containsKey(neighbor)) {
                predecessors.putN(neighbor);
                boundary.addLast(neighbor);
            }
        }

        if (map.n(position.x, position.y, position.z)) {
            var neighbor = new Position(position.x, position.y + 1, position.z);
            if (!predecessors.containsKey(neighbor)) {
                predecessors.putS(neighbor);
                boundary.addLast(neighbor);
            }
        }

        if (map.sw(position.x, position.y, position.z)) {
            var neighbor = new Position(position.x - 1, position.y - 1, position.z);
            if (!predecessors.containsKey(neighbor)) {
                predecessors.putNE(neighbor);
                boundary.addLast(neighbor);
            }
        }

        if (map.se(position.x, position.y, position.z)) {
            var neighbor = new Position(position.x + 1, position.y - 1, position.z);
            if (!predecessors.containsKey(neighbor)) {
                predecessors.putNW(neighbor);
                boundary.addLast(neighbor);
            }
        }

        if (map.nw(position.x, position.y, position.z)) {
            var neighbor = new Position(position.x - 1, position.y + 1, position.z);
            if (!predecessors.containsKey(neighbor)) {
                predecessors.putSE(neighbor);
                boundary.addLast(neighbor);
            }
        }

        if (map.ne(position.x, position.y, position.z)) {
            var neighbor = new Position(position.x + 1, position.y + 1, position.z);
            if (!predecessors.containsKey(neighbor)) {
                predecessors.putSW(neighbor);
                boundary.addLast(neighbor);
            }
        }
    }
}
