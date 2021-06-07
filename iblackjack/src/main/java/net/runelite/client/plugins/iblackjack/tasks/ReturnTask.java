package net.runelite.client.plugins.iblackjack.tasks;

import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iblackjack.iBlackjackPlugin;

public class ReturnTask extends Task {
    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public String getTaskDescription() {
        return "Timeout: " + iBlackjackPlugin.timeout;
    }

    @Override
    public void onGameTick(GameTick event) {
        iBlackjackPlugin.timeout--;
    }
}