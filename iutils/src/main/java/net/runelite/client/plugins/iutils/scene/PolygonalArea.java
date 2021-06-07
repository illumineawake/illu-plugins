package net.runelite.client.plugins.iutils.scene;

import java.util.*;
import java.util.stream.Collectors;

public class PolygonalArea implements Area {
    public final List<Position> points;

    public PolygonalArea(List<Position> points) {
        this.points = Collections.unmodifiableList(points);
    }

    @Override
    public boolean contains(Position position) {
        List<Position> slice = slice(position.x);
        boolean inside = false;

        for (Position point : slice) {
            if (point.equals(position.groundLevel())) {
                return true;
            }

            if (point.x > position.x) {
                break;
            }

            inside = !inside;
        }

        return inside;
    }

    public List<Position> slice(int x) {
        return edges().stream()
                .flatMap(e -> e.slice(x).stream())
                .sorted(Comparator.comparing(p -> p.x))
                .collect(Collectors.toList());
    }

    public List<Edge> edges() {
        List<Edge> result = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            Position a = points.get(i);
            Position b = points.get((i + 1) % points.size());
            result.add(new Edge(a, b));
        }

        return result;
    }

    public static class Edge {
        public final Position a;
        public final Position b;

        private Edge(Position a, Position b) {
            this.a = a;
            this.b = b;
        }

        public Optional<Position> slice(int x) {
            if (x < a.x || x > b.x) {
                return Optional.empty();
            }

            if (a.y == b.y) {
                return Optional.of(new Position(x, a.y, 0));
            }

            double slope = (double) (b.y - a.y) / (b.x - a.x);
            double y = a.y + ((x + 0.5) - a.x) * slope;
            return Optional.of(new Position(x, (int) y, 0));
        }
    }
}
