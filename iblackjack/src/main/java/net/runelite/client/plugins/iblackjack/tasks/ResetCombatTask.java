package net.runelite.client.plugins.iblackjack.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iblackjack.Location;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;
import net.runelite.client.plugins.iutils.WalkUtils;

import javax.inject.Inject;

import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.*;

@Slf4j
public class ResetCombatTask extends Task {
    Location currentRoom;

    @Inject
    private WalkUtils walk;

    @Override
    public boolean validate() {
        return inCombat || (!inventory.containsItem(ItemID.JUG_OF_WINE) && !inventory.containsItem(ItemID.JUG) && !inShopArea());
    }

    @Override
    public String getTaskDescription() {
        return status;
    }

    @Override
    public void onGameTick(GameTick event) {
        log.info("ResetCombat");
        if (client.getPlane() > 0) {
            inCombat = false;
            nextKnockoutTick = 0;
            selectedNPCIndex = 0;
            status = "waiting in safe area";
            return;
        }
        Location exitLocation = Location.getExitLocation(client.getLocalPlayer().getWorldLocation());
        if (exitLocation != null) {
            WallObject openCurtain = object.findWallObjectWithin(exitLocation.curtainLocation, 1, ObjectID.CURTAIN_1534);
            if (openCurtain != null) {
                status = "Closing curtain";
                entry = new LegacyMenuEntry("", "", openCurtain.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                        openCurtain.getLocalLocation().getSceneX(), openCurtain.getLocalLocation().getSceneY(), false);
                utils.doActionMsTime(entry, openCurtain.getConvexHull().getBounds(), sleepDelay());
                log.debug(status);
                timeout = tickDelay();
                return;
            }
        } else {
            log.info("Exit location is null");
        }
        if (inCombat) {
            GameObject staircase = object.findNearestGameObjectWithin(config.npcType().escapeLocation, 2, config.npcType().escapeObjID);
            if (staircase != null) {
                entry = new LegacyMenuEntry("", "", staircase.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                        staircase.getSceneMinLocation().getX(), staircase.getSceneMinLocation().getY(), false);
                utils.doActionMsTime(entry, staircase.getConvexHull().getBounds(), sleepDelay());
                status = "Climbing staircase";
            } else {
                walk.sceneWalk(shopPoint, 3, sleepDelay());
                status = "Escaping to shop";
            }
        } else {
            NPC barman = npc.findNearestNpc(NpcID.ALI_THE_BARMAN);
            if (barman != null) {
                status = "Opening shop";
                entry = new LegacyMenuEntry("", "", barman.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0, false);
                utils.doActionMsTime(entry, new Point(0, 0), sleepDelay());
            } else {
                //walk.sceneWalk(shopPoint, 3, sleepDelay());
                walk.webWalk(shopPoint, 5, playerUtils.isMoving(beforeLoc), sleepDelay());
                status = "Walking to shop";
            }
        }
        log.info(status);
    }
}