package net.runelite.client.plugins.iutils.scene;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.iutils.walking.Pathfinder;
import net.runelite.client.plugins.iutils.walking.Walking;

import javax.inject.Inject;
import java.util.*;

public class Position implements Area {
    @Inject
    private Client client;
    public final int x;
    public final int y;
    public final int z;

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(WorldPoint worldPoint) {
        this.x = worldPoint.getX();
        this.y = worldPoint.getY();
        this.z = worldPoint.getPlane();
    }

    public static Position unpack(int packed) {
        return new Position(packed >> 14 & 0x3fff, packed & 0x3fff, packed >> 28);
    }

    public Position north() {
        return new Position(x, y + 1, z);
    }

    public Position south() {
        return new Position(x, y - 1, z);
    }

    public Position east() {
        return new Position(x + 1, y, z);
    }

    public Position west() {
        return new Position(x - 1, y, z);
    }

    public Position add(int x, int y, int z) {
        return new Position(this.x + x, this.y + y, this.z + z);
    }

    public double distanceTo(Position other) {
        if (z != other.z) {
            return Double.MAX_VALUE;
        }

        return Math.hypot((other.x - x), (other.y - y));
    }

    public double distanceTo(WorldPoint other) {
        if (z != other.getPlane()) {
            return Double.MAX_VALUE;
        }

        return Math.hypot((other.getX() - x), (other.getY() - y));
    }

    public int pathLength(WorldPoint other) {
        return pathLength(new Position(other));
    }

    public int pathLength(Position other) {
        if (z != other.z) {
            return Integer.MAX_VALUE;
        }

        var path = new Pathfinder(Walking.map, Collections.emptyMap(), List.of(other), this::contains).find();

        if (path == null) {
            return Integer.MAX_VALUE;
        }
        return path.size();
    }

    public double distanceTo(Area other) {
        if (other instanceof RectangularArea) {
            return ((RectangularArea) other).distanceTo(this);
        }

        if (other instanceof PolygonalArea) {
            return ((PolygonalArea) other).points.get(0).distanceTo(this);
        }

        if (other instanceof Position) {
            return ((Position) other).distanceTo(this);
        }

        return Integer.MAX_VALUE;
    }

    public boolean inside(Area area) {
        return area.contains(this);
    }

    public int regionID() {
        return x >> 6 << 8 | y >> 6;
    }

    public Position groundLevel() {
        return new Position(x, y, 0);
    }

    public int packed() {
        return (z << 28) + (x << 14) + y;
    }

    public Area areaWithin(int distance) {
        return new RectangularArea(x - distance, y - distance, x + distance, y + distance, z);
    }

    @Override
    public boolean contains(Position position) {
        return Objects.equals(position, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y && z == position.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public boolean reachable(WorldPoint other) {
        return reachable(new Position(other));
    }

    public boolean reachable(Position other) {
        return pathLength(other) < Integer.MAX_VALUE;
    }

    public Position nearestReachable(WorldPoint other) {
        return nearestReachable(new Position(other));
    }

    public Position nearestReachable(Position other) {
        if (reachable(other))
            return this;

        List<Position> positions = nearestReachable(other, 1, 6);

        if (positions == null || positions.isEmpty()) {
            return null;
        }

        Position closest_pos = null;
        double closest_pos_dist = Double.MAX_VALUE;
        for (Position pos : positions) {
            double current_pos_dist = pos.distanceTo(other);
            if (closest_pos_dist > current_pos_dist) {
                closest_pos_dist = current_pos_dist;
                closest_pos = pos;
            }
        }

        return closest_pos;
    }

    public List<Position> nearestReachable(Position other, int depth, int max_depth) {
        List<Position> positions = new ArrayList<>();

        if (depth > max_depth)
            return null;

        Position pos_n = north();
        for (int i = 1; depth > i; i++)
            pos_n = pos_n.north();
        if (pos_n.reachable(other))
            positions.add(pos_n);

        Position pos_e = east();
        for (int i = 1; depth > i; i++)
            pos_e = pos_e.east();
        if (pos_e.reachable(other))
            positions.add(pos_e);

        Position pos_s = south();
        for (int i = 1; depth > i; i++)
            pos_s = pos_s.south();
        if (pos_s.reachable(other))
            positions.add(pos_s);

        Position pos_w = west();
        for (int i = 1; depth > i; i++)
            pos_w = pos_w.west();
        if (pos_w.reachable(other))
            positions.add(pos_w);

        if (!positions.isEmpty())
            return positions;

        Position pos_nw = pos_n;
        for (int i = 0; depth > i; i++) {
            pos_nw = pos_nw.west();
            if (pos_nw.reachable(other))
                positions.add(pos_nw);
        }

        Position pos_ne = pos_n;
        for (int i = 0; depth > i; i++) {
            pos_ne = pos_ne.east();
            if (pos_ne.reachable(other))
                positions.add(pos_ne);
        }

        Position pos_sw = pos_s;
        for (int i = 0; depth > i; i++) {
            pos_sw = pos_sw.west();
            if (pos_sw.reachable(other))
                positions.add(pos_sw);
        }

        Position pos_se = pos_s;
        for (int i = 0; depth > i; i++) {
            pos_se = pos_se.east();
            if (pos_se.reachable(other))
                positions.add(pos_se);
        }

        if (!positions.isEmpty())
            return positions;
        return nearestReachable(other, ++depth, max_depth);
    }
}
