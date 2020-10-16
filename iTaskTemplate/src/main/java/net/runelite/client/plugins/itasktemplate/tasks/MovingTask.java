package net.runelite.client.plugins.itasktemplate.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.itasktemplate.Task;
import static net.runelite.client.plugins.itasktemplate.iTaskTemplatePlugin.beforeLoc;
import static net.runelite.client.plugins.itasktemplate.iTaskTemplatePlugin.timeout;

@Slf4j
public class MovingTask extends Task
{

	@Override
	public boolean validate()
	{
		return playerUtils.isMoving(beforeLoc);
	}

	@Override
	public String getTaskDescription()
	{
		return status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			playerUtils.handleRun(20, 30);
			timeout = tickDelay();
		}
	}
}