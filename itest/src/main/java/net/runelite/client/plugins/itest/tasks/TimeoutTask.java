package net.runelite.client.plugins.itest.tasks;

import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.itest.Task;
import net.runelite.client.plugins.itest.iTestPlugin;

public class TimeoutTask extends Task
{
	@Override
	public boolean validate()
	{
		return iTestPlugin.timeout > 0;
	}

	@Override
	public String getTaskDescription()
	{
		return "Timeout: " + iTestPlugin.timeout;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		iTestPlugin.timeout--;
	}
}