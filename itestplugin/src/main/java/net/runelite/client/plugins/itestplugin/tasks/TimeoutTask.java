package net.runelite.client.plugins.itestplugin.tasks;

import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.itestplugin.Task;

import static net.runelite.client.plugins.itestplugin.iTestPlugin.timeout;

public class TimeoutTask extends Task {
    @Override
    public boolean validate() {
        return timeout > 0;
    }

    @Override
    public String getTaskDescription() {
        return "Timeout: " + timeout;
    }

    @Override
    public void onGameTick(GameTick event) {
        timeout--;
    }
}