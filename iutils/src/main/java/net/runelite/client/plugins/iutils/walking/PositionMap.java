package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.scene.Position;

import java.util.HashMap;
import java.util.Map;

public class PositionMap {
    public static final byte NONE = 0;
    public static final byte CUSTOM = 1;
    public static final byte N = 2;
    public static final byte NE = 3;
    public static final byte E = 4;
    public static final byte SE = 5;
    public static final byte S = 6;
    public static final byte SW = 7;
    public static final byte W = 8;
    public static final byte NW = 9;
    private final byte[][] regions = new byte[256 * 256][];
    private final Map<Position, Position> custom = new HashMap<>();

    public boolean containsKey(Position key) {
        return region(key)[index(key)] != 0;
    }

    public Position get(Position key) {
        var code = region(key)[index(key)];

        switch (code) {
            case NONE:
                return null;
            case CUSTOM:
                return custom.get(key);
            case N:
                return key.north();
            case NE:
                return key.north().east();
            case E:
                return key.east();
            case SE:
                return key.south().east();
            case S:
                return key.south();
            case SW:
                return key.south().west();
            case W:
                return key.west();
            case NW:
                return key.north().west();
            default:
                throw new AssertionError();
        }
    }

    public void put(Position key, Position value) {
        region(key)[index(key)] = CUSTOM;
        custom.put(key, value);
    }

    public void putN(Position key) {
        region(key)[index(key)] = N;
    }

    public void putNE(Position key) {
        region(key)[index(key)] = NE;
    }

    public void putE(Position key) {
        region(key)[index(key)] = E;
    }

    public void putSE(Position key) {
        region(key)[index(key)] = SE;
    }

    public void putS(Position key) {
        region(key)[index(key)] = S;
    }

    public void putSW(Position key) {
        region(key)[index(key)] = SW;
    }

    public void putW(Position key) {
        region(key)[index(key)] = W;
    }

    public void putNW(Position key) {
        region(key)[index(key)] = NW;
    }

    private int index(Position position) {
        return position.x % 64 + position.y % 64 * 64 + (position.z % 64) * 64 * 64;
    }

    private byte[] region(Position position) {
        var regionIndex = position.x / 64 * 256 + position.y / 64;

        var region = regions[regionIndex];

        if (region == null) {
            region = regions[regionIndex] = new byte[4 * 64 * 64];
        }

        return region;
    }
}
