package net.runelite.client.plugins.itasktemplate.tasks;

import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.itasktemplate.Task;
import static net.runelite.client.plugins.itasktemplate.iTaskTemplatePlugin.timeout;

public class TimeoutTask extends Task
{
	@Override
	public boolean validate() { return timeout > 0;	}

	@Override
	public String getTaskDescription()
	{
		return "Timeout: " + timeout;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		timeout--;
	}
}