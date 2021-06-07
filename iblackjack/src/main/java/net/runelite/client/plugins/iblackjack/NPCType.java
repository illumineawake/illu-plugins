package net.runelite.client.plugins.iblackjack;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;

public enum NPCType {
    BANDIT_LVL_41(NpcID.BANDIT_737, ObjectID.LADDER_6261, new WorldPoint(3369, 2991, 0)),
    BANDIT_LVL_56(NpcID.BANDIT_735, ObjectID.LADDER_6261, new WorldPoint(3369, 2991, 0)),
    THUG(NpcID.MENAPHITE_THUG_3550, ObjectID.STAIRCASE_6242, new WorldPoint(3353, 2960, 0));

    @Getter(AccessLevel.PACKAGE)
    public final int npcid;

    @Getter(AccessLevel.PACKAGE)
    public final int escapeObjID;

    @Getter(AccessLevel.PACKAGE)
    public WorldPoint escapeLocation;

    NPCType(int npcid, int escapeObjID, WorldPoint escapeLocation) {
        this.npcid = npcid;
        this.escapeObjID = escapeObjID;
        this.escapeLocation = escapeLocation;
    }
}
