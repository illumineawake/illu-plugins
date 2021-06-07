package net.runelite.client.plugins.iherbcleaner.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iherbcleaner.Task;
import net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin;

import static net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin.beforeLoc;
import static net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin.timeout;

@Slf4j
public class MovingTask extends Task {

    @Override
    public boolean validate() {
        return playerUtils.isMoving(beforeLoc);
    }

    @Override
    public String getTaskDescription() {
        return iHerbCleanerPlugin.status;
    }

    @Override
    public void onGameTick(GameTick event) {
        Player player = client.getLocalPlayer();
        if (player != null) {
            playerUtils.handleRun(20, 30);
            timeout = tickDelay();
        }
    }
}