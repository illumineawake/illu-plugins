package net.runelite.client.plugins.iutils.walking;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class CollisionMap {
    private final BitSet4D[] regions = new BitSet4D[256 * 256];

    public CollisionMap() {

    }

    public CollisionMap(byte[] data) {
        var buffer = ByteBuffer.wrap(data);

        while (buffer.hasRemaining()) {
            var region = buffer.getShort() & 0xffff;
            regions[region] = new BitSet4D(buffer, 64, 64, 4, 2);
        }
    }

    public byte[] toBytes() {
        var regionCount = (int) Arrays.stream(regions).filter(Objects::nonNull).count();
        var buffer = ByteBuffer.allocate(regionCount * (2 + 64 * 64 * 4 * 2 / 8));

        for (var i = 0; i < regions.length; i++) {
            if (regions[i] != null) {
                buffer.putShort((short) i);
                regions[i].write(buffer);
            }
        }

        return buffer.array();
    }

    public void set(int x, int y, int z, int w, boolean value) {
        var region = regions[x / 64 * 256 + y / 64];

        if (region == null) {
            return;
        }

        region.set(x % 64, y % 64, z, w, value);
    }

    public boolean get(int x, int y, int z, int w) {
        var region = regions[x / 64 * 256 + y / 64];

        if (region == null) {
            return false;
        }

        return region.get(x % 64, y % 64, z, w);
    }

    public void createRegion(int region) {
        regions[region] = new BitSet4D(64, 64, 4, 2);
        regions[region].setAll(true);
    }

    public boolean n(int x, int y, int z) {
        return get(x, y, z, 0);
    }

    public boolean s(int x, int y, int z) {
        return n(x, y - 1, z);
    }

    public boolean e(int x, int y, int z) {
        return get(x, y, z, 1);
    }

    public boolean w(int x, int y, int z) {
        return e(x - 1, y, z);
    }

    public boolean ne(int x, int y, int z) {
        return n(x, y, z) && e(x, y + 1, z) && e(x, y, z) && n(x + 1, y, z);
    }

    public boolean nw(int x, int y, int z) {
        return n(x, y, z) && w(x, y + 1, z) && w(x, y, z) && n(x - 1, y, z);
    }

    public boolean se(int x, int y, int z) {
        return s(x, y, z) && e(x, y - 1, z) && e(x, y, z) && s(x + 1, y, z);
    }

    public boolean sw(int x, int y, int z) {
        return s(x, y, z) && w(x, y - 1, z) && w(x, y, z) && s(x - 1, y, z);
    }
}
