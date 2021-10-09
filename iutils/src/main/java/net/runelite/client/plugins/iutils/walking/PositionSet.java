package net.runelite.client.plugins.iutils.walking;

public class PositionSet {
    protected final BitSet4D[] regions = new BitSet4D[256 * 256];

    public boolean contains(int x, int y, int z) {
        var region = regions[x / 64 * 256 + y / 64];

        if (region == null) {
            return false;
        }

        return region.get(x % 64, y % 64, z, 0);
    }

    public void add(int x, int y, int z) {
        var region = regions[x / 64 * 256 + y / 64];

        if (region == null) {
            region = regions[x / 64 * 256 + y / 64] = new BitSet4D(64, 64, 4, 1);
        }

        region.set(x % 64, y % 64, z, 0, true);
    }
}
