package net.runelite.client.plugins.iutils.scene;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import java.util.Objects;

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

    public int distanceTo(Position other) {
        if (z != other.z) {
            return Integer.MAX_VALUE;
        }

        return Math.max(Math.abs(other.x - x), Math.abs(other.y - y));
    }

    public int distanceTo(WorldPoint other) {
        if (z != other.getPlane()) {
            return Integer.MAX_VALUE;
        }

        return Math.max(Math.abs(other.getX() - x), Math.abs(other.getY() - y));
    }

    public int distanceTo(Area other) {
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

}
