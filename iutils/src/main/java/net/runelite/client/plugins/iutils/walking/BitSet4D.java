package net.runelite.client.plugins.iutils.walking;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class BitSet4D {
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final int sizeW;
    private final BitSet bits;

    public BitSet4D(int sizeX, int sizeY, int sizeZ, int sizeW) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeW = sizeW;
        bits = new BitSet(sizeX * sizeY * sizeZ * sizeW);
    }

    public BitSet4D(ByteBuffer buffer, int sizeX, int sizeY, int sizeZ, int sizeW) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeW = sizeW;

        var oldLimit = buffer.limit();
        buffer.limit(buffer.position() + (sizeX * sizeY * sizeZ * sizeW + 7) / 8);
        bits = BitSet.valueOf(buffer);
        buffer.position(buffer.limit());
        buffer.limit(oldLimit);
    }

    public void write(ByteBuffer buffer) {
        var startPos = buffer.position();
        buffer.put(bits.toByteArray());
        buffer.position(startPos + (sizeX * sizeY * sizeZ * sizeW + 7) / 8);
    }

    public boolean get(int x, int y, int z, int w) {
        return bits.get(index(x, y, z, w));
    }

    public void set(int x, int y, int z, int flag, boolean value) {
        bits.set(index(x, y, z, flag), value);
    }

    public void setAll(boolean value) {
        bits.set(0, bits.size(), value);
    }

    private int index(int x, int y, int z, int w) {
        if (x < 0 || y < 0 || z < 0 || w < 0 || x >= sizeX || y >= sizeY || z >= sizeZ || w >= sizeW) {
            throw new IndexOutOfBoundsException("(" + x + ", " + y + ", " + z + ", " + w + ")");
        }

        var index = z;
        index = index * sizeY + y;
        index = index * sizeX + x;
        index = index * sizeW + w;
        return index;
    }
}
