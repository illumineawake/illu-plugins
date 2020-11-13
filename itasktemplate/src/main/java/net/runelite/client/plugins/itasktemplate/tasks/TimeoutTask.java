package net.runelite.client.plugins.itasktemplate.tasks;

import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.itasktemplate.Task;
import net.runelite.client.plugins.itasktemplate.iTaskTemplatePlugin;

public class TimeoutTask extends Task
{
	@Override
	public boolean validate() { return iTaskTemplatePlugin.timeout > 0;	}

	@Override
	public String getTaskDescription()
	{
		return "Timeout: " + iTaskTemplatePlugin.timeout;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		iTaskTemplatePlugin.timeout--;
	}
}