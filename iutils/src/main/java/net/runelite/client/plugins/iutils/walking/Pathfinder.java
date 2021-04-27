package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.scene.Position;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Pathfinder {
    private final CollisionMap map;
    private final List<Node> starts;
    private final Predicate<Position> target;
    private final List<Node> boundary = new LinkedList<>();
    private final PositionSet visited = new PositionSet();
    private final Map<Position, List<Position>> transports;

    public Pathfinder(CollisionMap map, Map<Position, List<Position>> transports, List<Position> starts, Predicate<Position> target) {
        this.map = map;
        this.transports = transports;
        this.target = target;
        this.starts = starts.stream().map(s -> new Node(s, null)).collect(Collectors.toList());
    }

    public List<Position> find() {
        boundary.addAll(starts);

        while (!boundary.isEmpty()) {
            var node = boundary.remove(0);

            if (target.test(node.position)) {
                return node.path();
            }

            addNeighbors(node);
        }

        return null;
    }

    private void addNeighbors(Node node) {
        // Prefer taking transports as early as possible, to avoid causing problems with ladders which can be taken
        // from several source positions.
        for (var transport : transports.getOrDefault(node.position, new ArrayList<>())) {
            addNeighbor(node, transport);
        }

        if (map.w(node.position.x, node.position.y, node.position.z)) {
            addNeighbor(node, new Position(node.position.x - 1, node.position.y, node.position.z));
        }

        if (map.e(node.position.x, node.position.y, node.position.z)) {
            addNeighbor(node, new Position(node.position.x + 1, node.position.y, node.position.z));
        }

        if (map.s(node.position.x, node.position.y, node.position.z)) {
            addNeighbor(node, new Position(node.position.x, node.position.y - 1, node.position.z));
        }

        if (map.n(node.position.x, node.position.y, node.position.z)) {
            addNeighbor(node, new Position(node.position.x, node.position.y + 1, node.position.z));
        }

        if (map.sw(node.position.x, node.position.y, node.position.z)) {
            addNeighbor(node, new Position(node.position.x - 1, node.position.y - 1, node.position.z));
        }

        if (map.se(node.position.x, node.position.y, node.position.z)) {
            addNeighbor(node, new Position(node.position.x + 1, node.position.y - 1, node.position.z));
        }

        if (map.nw(node.position.x, node.position.y, node.position.z)) {
            addNeighbor(node, new Position(node.position.x - 1, node.position.y + 1, node.position.z));
        }

        if (map.ne(node.position.x, node.position.y, node.position.z)) {
            addNeighbor(node, new Position(node.position.x + 1, node.position.y + 1, node.position.z));
        }
    }

    private void addNeighbor(Node node, Position neighbor) {
        if (visited.contains(neighbor.x, neighbor.y, neighbor.z)) {
            return;
        }

        visited.add(neighbor.x, neighbor.y, neighbor.z);
        boundary.add(new Node(neighbor, node));
    }

    private static class Node {
        public final Position position;
        public final Node previous;

        public Node(Position position, Node previous) {
            this.position = position;
            this.previous = previous;
        }

        public List<Position> path() {
            List<Position> path = new LinkedList<>();
            var node = this;

            while (node != null) {
                path.add(0, node.position);
                node = node.previous;
            }

            return new ArrayList<>(path);
        }
    }
}
