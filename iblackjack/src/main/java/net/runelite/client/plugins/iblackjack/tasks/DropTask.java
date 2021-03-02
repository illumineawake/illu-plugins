package net.runelite.client.plugins.iblackjack.tasks;

import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.iblackjack.Task;
import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.inCombat;
import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.nextKnockoutTick;
import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.selectedNPCIndex;
import net.runelite.client.plugins.iutils.ActionQueue;

@Slf4j
public class DropTask extends Task
{
	@Inject
	private ActionQueue action;

	@Override
	public boolean validate()
	{
		return !inCombat && !isShopOpen() && !inventory.containsItem(ItemID.JUG_OF_WINE) &&
			inventory.containsItem(ItemID.JUG) && action.delayedActions.isEmpty();
	}

	@Override
	public String getTaskDescription()
	{
		return "Dropping Jugs";
	}

	@Override
	public void onGameTick(GameTick event)
	{
		log.info("Entering drop task");
		selectedNPCIndex = 0;
		nextKnockoutTick = 0;

		List<WidgetItem> jugs = inventory.getItems(List.of(ItemID.JUG));
		long sleep = 0;
		for (WidgetItem jug : jugs)
		{
			entry = new MenuEntry("", "", jug.getId(), MenuAction.ITEM_FIFTH_OPTION.getId(), jug.getIndex(),
				WidgetInfo.INVENTORY.getId(), false);
			sleep += sleepDelay();
			log.info("Adding jug: {}, delay time: {}", jug.getIndex(), sleep);
			utils.doActionMsTime(entry, jug.getCanvasBounds(), sleep);
		}
	}
}