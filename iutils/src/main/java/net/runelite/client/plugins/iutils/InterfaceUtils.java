package net.runelite.client.plugins.iutils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.iutils.api.Spells;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;

@Slf4j
@Singleton
public class InterfaceUtils {
    @Inject
    private Client client;

    @Inject
    private MouseUtils mouse;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    /**
     * Opens the container interface using the given index.
     *
     * @param index the index of the interface to open. Interface index's are in order of how they appear in game by default
     *              e.g. inventory is 3, logout is 10
     */
    public void openInterface(int index) {
        if (client == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        client.runScript(915, index); //open inventory
    }

    /**
     * @param index is position based on order including styles that might not be visible. Starting from 0.
     *              e.g. Stab = 0, Lunge = 1, Slash 2, Block = 3
     */
    public LegacyMenuEntry getAttackStyleLegacyMenuEntry(int index) {
        if (client == null || client.getGameState() != GameState.LOGGED_IN || index < 0) {
            return null;
        }
        final int BASE_PARAM = 38862852;
        final int INCREMENT = 4;
        int styleParam = BASE_PARAM + (index * INCREMENT);

        return new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, styleParam, false);
    }

    public void logout() {
        int param1 = (client.getWidget(WidgetInfo.LOGOUT_BUTTON) != null) ? 11927560 : 4522007;
        menu.setEntry(new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, param1, false));
        Widget logoutWidget = client.getWidget(WidgetInfo.LOGOUT_BUTTON);
        if (logoutWidget != null) {
            mouse.delayMouseClick(logoutWidget.getBounds(), calc.getRandomIntBetweenRange(5, 200));
        } else {
            mouse.delayMouseClick(new Point(0, 0), calc.getRandomIntBetweenRange(5, 200));
        }
    }

    public static void resumePauseWidget(int widgetId, int arg) {
        final int garbageValue = 1292618906;
        final String className = "ln";
        final String methodName = "hs";

        try {

            Class clazz = Class.forName(className);
            Method method = clazz.getDeclaredMethod(methodName, int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(null, widgetId, arg, garbageValue);
        } catch (Exception ignored) {
            return;
        }
    }

    public WidgetInfo getSpellWidgetInfo(String spell) {
        assert client.isClientThread();
        return Spells.getWidget(spell);
    }

    public WidgetInfo getPrayerWidgetInfo(String spell) {
        assert client.isClientThread();
        return PrayerMap.getWidget(spell);
    }

    public Widget getSpellWidget(String spell) {
        return client.getWidget(Spells.getWidget(spell));
    }

    public Widget getPrayerWidget(String spell) {
        assert client.isClientThread();
        return client.getWidget(PrayerMap.getWidget(spell));
    }

    public int getTabHotkey(Tab tab) {
        assert client.isClientThread();

        final int var = client.getVarbitValue(client.getVarps(), tab.getVarbit());
        final int offset = 111;

        switch (var) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return var + offset;
            case 13:
                return 27;
            default:
                return -1;
        }
    }

}
