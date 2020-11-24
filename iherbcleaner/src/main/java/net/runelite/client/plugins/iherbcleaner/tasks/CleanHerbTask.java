package net.runelite.client.plugins.iherbcleaner.tasks;

import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.iherbcleaner.Task;
import net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin;
import static net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin.status;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.InventoryUtils;

@Slf4j
public class CleanHerbTask extends Task
{

	@Inject
	ActionQueue action;

	@Inject
	InventoryUtils inventory;

	@Override
	public boolean validate()
	{
		return action.delayedActions.isEmpty() && inventory.containsItem(config.herbID());
	}

	@Override
	public String getTaskDescription()
	{
		return iHerbCleanerPlugin.status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		status = "Starting herb cleaning";
		List<WidgetItem> herbs = inventory.getItems(List.of(config.herbID()));
		long sleep = 0;
		for (WidgetItem herb : herbs)
		{
			log.info("Adding herb: {}, delay time: {}", herb.getIndex(), sleep);
			entry = new MenuEntry("", "", herb.getId(), MenuOpcode.ITEM_FIRST_OPTION.getId(),
				herb.getIndex(), WidgetInfo.INVENTORY.getId(), true);
			sleep += sleepDelay();
			herb.getCanvasBounds().getBounds();
			Rectangle rectangle = herb.getCanvasBounds().getBounds();
			utils.doActionMsTime(entry, rectangle, sleep);
		}
		log.info(status);
	}
}