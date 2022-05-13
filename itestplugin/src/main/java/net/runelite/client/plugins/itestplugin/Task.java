package net.runelite.client.plugins.itestplugin;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iutils.*;

import javax.inject.Inject;

import static net.runelite.client.plugins.itestplugin.iTestPlugin.sleepLength;
import static net.runelite.client.plugins.itestplugin.iTestPlugin.tickLength;

@Slf4j
public abstract class Task {
    public Task() {
    }

    @Inject
    public Client client;

    @Inject
    public iTestConfig config;

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
    public ObjectUtils object;

    public LegacyMenuEntry entry;

    public abstract boolean validate();

    public long sleepDelay() {
        sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return sleepLength;
    }

    public int tickDelay() {
        tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        return tickLength;
    }

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }

    public void onGameTick(GameTick event) {
        return;
    }

}
