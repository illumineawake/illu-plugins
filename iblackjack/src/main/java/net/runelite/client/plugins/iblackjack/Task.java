package net.runelite.client.plugins.iblackjack;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.iutils.*;

import javax.inject.Inject;

@Slf4j
public abstract class Task {
    public Task() {
    }

    @Inject
    public Client client;

    @Inject
    public iBlackjackConfig config;

    @Inject
    public iUtils utils;

    @Inject
    public MenuUtils menu;

    @Inject
    public MouseUtils mouse;

    @Inject
    public CalculationUtils calc;

    @Inject
    public PlayerUtils playerUtils;

    @Inject
    public InventoryUtils inventory;

    @Inject
    public NPCUtils npc;

    @Inject
    public ObjectUtils object;

    public LegacyMenuEntry entry;
    public static String status = "";
    public static boolean shouldHop;
    public final WorldArea MENAPHITE_ROOM = new WorldArea(new WorldPoint(3347, 2952, 0), new WorldPoint(3352, 2957, 0));
    public final WorldPoint MENAPHITE_POINT = new WorldPoint(3349, 2955, 0);
    public final WorldPoint shopPoint = new WorldPoint(3360, 2955, 0);
    private final WorldArea SHOP_AREA = new WorldArea(new WorldPoint(3353, 2953, 0), new WorldPoint(3364, 2961, 0));

    public abstract boolean validate();

    public long sleepDelay() {
        iBlackjackPlugin.sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return iBlackjackPlugin.sleepLength;
    }

    public int tickDelay() {
        iBlackjackPlugin.tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        return iBlackjackPlugin.tickLength;
    }

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }

    public void onGameTick(GameTick event) {
        return;
    }

    public boolean inShopArea() {
        return client.getLocalPlayer().getWorldArea().intersectsWith(SHOP_AREA);
    }

    public boolean isShopOpen() {
        Widget shop = client.getWidget(300, 0);
        return (shop != null && !shop.isHidden());
    }
}
