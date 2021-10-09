package net.runelite.client.plugins.iutils.api;

import net.runelite.api.widgets.WidgetInfo;

public enum EquipmentSlot {
    HEAD(0, 387, 15, WidgetInfo.EQUIPMENT_HELMET),
    CAPE(1, 387, 16, WidgetInfo.EQUIPMENT_CAPE),
    NECK(2, 387, 17, WidgetInfo.EQUIPMENT_AMULET),
    WEAPON(3, 387, 18, WidgetInfo.EQUIPMENT_WEAPON),
    TORSO(4, 387, 19, WidgetInfo.EQUIPMENT_BODY),
    SHIELD(5, 387, 20, WidgetInfo.EQUIPMENT_SHIELD),
    LEGS(7, 387, 21, WidgetInfo.EQUIPMENT_LEGS),
    HAND(9, 387, 22, WidgetInfo.EQUIPMENT_GLOVES),
    FEET(10, 387, 23, WidgetInfo.EQUIPMENT_BOOTS),
    RING(12, 387, 24, WidgetInfo.EQUIPMENT_RING),
    AMMO(13, 387, 25, WidgetInfo.EQUIPMENT_AMMO);

    public final int index;
    public final int widgetID;
    public final int widgetChild;
    public final WidgetInfo widgetInfo;

    EquipmentSlot(int index, int widgetID, int widgetChild, WidgetInfo widgetInfo) {
        this.index = index;
        this.widgetID = widgetID;
        this.widgetChild = widgetChild;
        this.widgetInfo = widgetInfo;
    }

}
