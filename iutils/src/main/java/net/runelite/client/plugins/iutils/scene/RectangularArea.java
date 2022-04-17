package net.runelite.client.plugins.iutils.scene;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.CalculationUtils;

@Slf4j
public class RectangularArea implements Area {
    public final int minX;
    public final int minY;
    public final int maxX;
    public final int maxY;
    public final int minZ;
    public final int maxZ;

    public RectangularArea(int x1, int y1, int x2, int y2) {
        this(x1, y1, 0, x2, y2, 3);
    }

    public RectangularArea(int x1, int y1, int x2, int y2, int z) {
        this(x1, y1, z, x2, y2, z);
    }

    public RectangularArea(int x1, int y1, int z1, int x2, int y2, int z2) {
        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        maxX = Math.max(x1, x2);
        maxY = Math.max(y1, y2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
    }

    public RectangularArea(Position start, Position end) {
        this(start.x, start.y, start.z, end.x, end.y, end.z);
    }

    @Override
    public boolean contains(Position position) {
        return position.x >= minX && position.x <= maxX &&
                position.y >= minY && position.y <= maxY &&
                position.z >= minZ && position.z <= maxZ;
    }

    public double distanceTo(Position other) {
        return distanceTo(new RectangularArea(other, other));
    }

    public double distanceTo(RectangularArea other) {
        Position p1 = nearestPosition(other);
        Position p2 = other.nearestPosition(this);
        return p1.distanceTo(p2);
    }

    public boolean interects(RectangularArea other) {
        return distanceTo(other) == 0;
    }

    public Position nearestPosition(RectangularArea other) {
        int x, y;
        if (other.minX <= minX) {
            x = minX;
        } else if (other.minX >= maxX) {
            x = maxX;
        } else {
            x = other.minX;
        }
        if (other.minY <= minY) {
            y = minY;
        } else if (other.minY >= maxY) {
            y = maxY;
        } else {
            y = other.minY;
        }
        return new Position(x, y, 0);
    }

    public Position randomPosition() {
        var x = CalculationUtils.random(minX, maxX);
        var y = CalculationUtils.random(minY, maxY);
        var z = minZ == 0 ? minZ : CalculationUtils.random(minZ, maxZ);

        var position = new Position(x, y, z);
        log.info("Random Position: {} found from Rectangular Area: {}", position, this);

        return position;
    }

        public String toString() {
        return "[" + minX + ", " + maxX + "] x [" + minY + ", " + maxY + "] x [" + minZ + ", " + maxZ + "]";
    }
}
