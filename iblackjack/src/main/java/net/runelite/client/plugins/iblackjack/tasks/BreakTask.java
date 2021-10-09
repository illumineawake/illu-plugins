package net.runelite.client.plugins.iblackjack.tasks;

import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iblackjack.iBlackjackPlugin;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;

import javax.inject.Inject;

import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.timeout;

public class BreakTask extends Task {
    @Inject
    private iBlackjackPlugin plugin;

    @Inject
    public ReflectBreakHandler chinBreakHandler;

    @Override
    public boolean validate() {
        return iBlackjackPlugin.timeout > 0;
    }

    @Override
    public String getTaskDescription() {
        return "Timeout: " + iBlackjackPlugin.timeout;
    }

    @Override
    public void onGameTick(GameTick event) {
        if (chinBreakHandler.shouldBreak(plugin)) {
            status = "Taking a break";
            chinBreakHandler.startBreak(plugin);
            timeout = 3;
        }
    }
}