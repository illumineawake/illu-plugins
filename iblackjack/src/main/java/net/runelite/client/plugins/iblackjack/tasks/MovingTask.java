package net.runelite.client.plugins.iblackjack.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iblackjack.Location;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iblackjack.iBlackjackPlugin;

@Slf4j
public class MovingTask extends Task {

    @Override
    public boolean validate() {
        Location currentRoom = Location.getRoom(client.getLocalPlayer().getWorldLocation());
        return playerUtils.isMoving(iBlackjackPlugin.beforeLoc) && currentRoom == null;
        //return false;
    }

    @Override
    public String getTaskDescription() {
        return status;
    }

    @Override
    public void onGameTick(GameTick event) {
        Player player = client.getLocalPlayer();
        if (player != null) {
            playerUtils.handleRun(20, 30);
            iBlackjackPlugin.timeout = tickDelay();
        }
    }
}