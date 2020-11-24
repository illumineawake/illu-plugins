package net.runelite.client.plugins.iblackjack.tasks;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.ObjectID;
import net.runelite.api.WallObject;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iblackjack.Location;
import net.runelite.client.plugins.iblackjack.Task;
import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.inCombat;
import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.timeout;
import net.runelite.client.plugins.iutils.WalkUtils;

@Slf4j
public class LeaveRoomTask extends Task
{
	Location currentRoom;

	@Inject
	private WalkUtils walk;

	@Override
	public boolean validate()
	{
		if (inCombat || !inventory.containsItem(ItemID.JUG_OF_WINE) && !inventory.containsItem(ItemID.JUG))
		{
			currentRoom = Location.getRoom(client.getLocalPlayer().getWorldLocation());
			return currentRoom != null;
		}
		return false;
	}

	@Override
	public String getTaskDescription()
	{
		return status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		log.info("LeaveRoom");
		if (currentRoom.escapeLocation != null && inCombat)
		{
			status = "climbing ladder in room";
			log.info(status);
			GameObject ladder = object.findNearestGameObjectWithin(currentRoom.escapeLocation, 1, config.npcType().escapeObjID);
			if (ladder != null)
			{
				entry = new MenuEntry("", "", ladder.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(),
					ladder.getSceneMinLocation().getX(), ladder.getSceneMinLocation().getY(), false);
				utils.doActionMsTime(entry, ladder.getConvexHull().getBounds(), sleepDelay());
				timeout = tickDelay();
			}
		}
		else
		{
			WallObject closedCurtain = object.findWallObjectWithin(currentRoom.curtainLocation, 1, ObjectID.CURTAIN_1533);
			if (closedCurtain != null)
			{
				status = "Opening curtain";
				entry = new MenuEntry("", "", closedCurtain.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(),
					closedCurtain.getLocalLocation().getSceneX(), closedCurtain.getLocalLocation().getSceneY(), false);
				utils.doActionMsTime(entry, closedCurtain.getConvexHull().getBounds(), sleepDelay());
				log.debug(status);
				timeout = tickDelay();
			}
			else
			{
				status = "Exiting room";
				walk.sceneWalk(currentRoom.curtainLocation, 0, sleepDelay());
			}
		}
		log.info(status);
	}
}