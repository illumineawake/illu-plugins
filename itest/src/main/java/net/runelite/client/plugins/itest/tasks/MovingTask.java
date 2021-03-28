package net.runelite.client.plugins.itest.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.itest.Task;
import net.runelite.client.plugins.itest.iTestPlugin;

@Slf4j
public class MovingTask extends Task
{

	@Override
	public boolean validate()
	{
		return playerUtils.isMoving(iTestPlugin.beforeLoc);
	}

	@Override
	public String getTaskDescription()
	{
		return iTestPlugin.status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			playerUtils.handleRun(20, 30);
			iTestPlugin.timeout = tickDelay();
		}
	}
}