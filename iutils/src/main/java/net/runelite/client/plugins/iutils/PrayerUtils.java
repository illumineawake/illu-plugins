package net.runelite.client.plugins.iutils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class PrayerUtils {
    @Inject
    private Client client;

    @Inject
    private ActionQueue action;

    @Inject
    private MouseUtils mouse;

    @Inject
    private MenuUtils menu;

    public int getPoints() {
        return client.getBoostedSkillLevel(Skill.PRAYER);
    }

    public boolean isActive(Prayer prayer) {
        return client.getVar(prayer.getVarbit()) == 1;
    }

    public void toggle(Prayer prayer, long timeToDelay) {
        Widget widget = client.getWidget(prayer.getWidgetInfo());
        Point p = mouse.getClickPoint(widget.getBounds());
        LegacyMenuEntry toggle = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
        Runnable runnable = () -> {
            menu.setEntry(toggle);
            mouse.handleMouseClick(p);
        };
        action.delayTime(timeToDelay, runnable);
    }

    public boolean isQuickPrayerActive() {
        return client.getVar(Varbits.QUICK_PRAYER) == 1;
    }

    public void toggleQuickPrayer(boolean enabled, long timeToDelay) {
        Widget widget = client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
        if (widget != null) {
            Point p = mouse.getClickPoint(widget.getBounds());
            LegacyMenuEntry activate = new LegacyMenuEntry("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
            LegacyMenuEntry deactivate = new LegacyMenuEntry("Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
            Runnable runnable = () -> {
                if (enabled) {
                    menu.setEntry(activate);
                } else {
                    menu.setEntry(deactivate);
                }
                mouse.handleMouseClick(p);
            };
            action.delayTime(timeToDelay, runnable);
        }
    }
}
