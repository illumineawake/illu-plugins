package net.runelite.client.plugins.iblackjack.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iblackjack.Location;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;
import net.runelite.client.plugins.iutils.WalkUtils;

import javax.inject.Inject;

import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.inCombat;
import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.timeout;

@Slf4j
public class LeaveRoomTask extends Task {
    Location currentRoom;

    @Inject
    private WalkUtils walk;

    @Override
    public boolean validate() {
        if (inCombat || !inventory.containsItem(ItemID.JUG_OF_WINE) && !inventory.containsItem(ItemID.JUG)) {
            currentRoom = Location.getRoom(client.getLocalPlayer().getWorldLocation());
            return currentRoom != null;
        }
        return false;
    }

    @Override
    public String getTaskDescription() {
        return status;
    }

    @Override
    public void onGameTick(GameTick event) {
        log.info("LeaveRoom");
        if (currentRoom.escapeLocation != null && inCombat) {
            status = "climbing ladder in room";
            log.info(status);
            GameObject ladder = object.findNearestGameObjectWithin(currentRoom.escapeLocation, 1, config.npcType().escapeObjID);
            if (ladder != null) {
                entry = new LegacyMenuEntry("", "", ladder.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                        ladder.getSceneMinLocation().getX(), ladder.getSceneMinLocation().getY(), false);
                utils.doActionMsTime(entry, ladder.getConvexHull().getBounds(), sleepDelay());
                timeout = tickDelay();
            }
        } else {
            WallObject closedCurtain = object.findWallObjectWithin(currentRoom.curtainLocation, 1, ObjectID.CURTAIN_1533);
            if (closedCurtain != null) {
                status = "Opening curtain";
                entry = new LegacyMenuEntry("", "", closedCurtain.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                        closedCurtain.getLocalLocation().getSceneX(), closedCurtain.getLocalLocation().getSceneY(), false);
                utils.doActionMsTime(entry, closedCurtain.getConvexHull().getBounds(), sleepDelay());
                log.debug(status);
                timeout = tickDelay();
            } else {
                status = "Exiting room";
                walk.sceneWalk(currentRoom.curtainLocation, 0, sleepDelay());
            }
        }
        log.info(status);
    }
}