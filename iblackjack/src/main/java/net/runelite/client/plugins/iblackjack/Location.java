package net.runelite.client.plugins.iblackjack;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

public enum Location {
    THUG_ROOM_ONE(new WorldArea(new WorldPoint(3347, 2952, 0), new WorldPoint(3352, 2957, 0)), new WorldPoint(3350, 2957, 0)),
    THUG_ROOM_TWO(new WorldArea(new WorldPoint(3339, 2952, 0), new WorldPoint(3345, 2957, 0)), new WorldPoint(3345, 2955, 0)),
    BANDIT_ROOM_ONE(new WorldArea(new WorldPoint(3362, 3000, 0), new WorldPoint(3366, 3004, 0)), new WorldPoint(3364, 2999, 0), new WorldPoint(3364, 3003, 0)),
    BANDIT_ROOM_TWO(new WorldArea(new WorldPoint(3355, 3000, 0), new WorldPoint(3360, 3005, 0)), new WorldPoint(3358, 2999, 0)),
    BANDIT_ROOM_THREE(new WorldArea(new WorldPoint(3356, 2990, 0), new WorldPoint(3361, 2996, 0)), new WorldPoint(3361, 2993, 0));

    @Getter(AccessLevel.PACKAGE)
    public final WorldArea room;

    @Getter(AccessLevel.PACKAGE)
    public final WorldPoint curtainLocation;

    @Getter(AccessLevel.PACKAGE)
    public WorldPoint escapeLocation;

    Location(WorldArea room, WorldPoint curtainLocation) {
        this.room = room;
        this.curtainLocation = curtainLocation;
    }

    Location(WorldArea room, WorldPoint curtainLocation, WorldPoint escapeLocation) {
        this.room = room;
        this.curtainLocation = curtainLocation;
        this.escapeLocation = escapeLocation;
    }

    public static Location getRoom(WorldPoint worldPoint) {
        for (Location room : values()) {
            if (room.getRoom().distanceTo(worldPoint) == 0) {
                return room;
            }
        }
        return null;
    }

    public static Location getExitLocation(WorldPoint worldPoint) {
        for (Location room : values()) {
            if (room.getCurtainLocation().distanceTo(worldPoint) == 0) {
                return room;
            }
        }
        return null;
    }
}
