package net.runelite.client.plugins.iutils.api;

public enum EquipmentSlot {
    HEAD(0, 387, 15),
    CAPE(1, 387, 16),
    NECK(2, 387, 17),
    WEAPON(3, 387, 18),
    TORSO(4, 387, 19),
    SHIELD(5, 387, 20),
    LEGS(7, 387, 21),
    HAND(9, 387, 22),
    FEET(10, 387, 23),
    RING(12, 387, 24),
    AMMO(13, 387, 25);

    public final int index;
    public final int widgetID;
    public final int widgetChild;

    EquipmentSlot(int index, int widgetID, int widgetChild) {
        this.index = index;
        this.widgetID = widgetID;
        this.widgetChild = widgetChild;
    }

}
