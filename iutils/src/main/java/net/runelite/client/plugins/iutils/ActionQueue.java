package net.runelite.client.plugins.iutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import javax.inject.Singleton;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import javax.inject.Inject;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;

@Slf4j
@Singleton
public class ActionQueue
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	public final List<DelayedAction> delayedActions = new ArrayList<>();
	private int clientTick = 0;
	private int gameTick = 0;

	public void runDelayedActions()
	{
		Iterator<DelayedAction> it = delayedActions.iterator();
		while (it.hasNext())
		{
			//log.info("Action found");
			DelayedAction action = it.next();
			if (action.shouldRun.get())
			{
				log.info("Action running");
				action.runnable.run();
				it.remove();
			}
		}
	}

	public void onClientTick(ClientTick e)
	{
		clientTick++;
		runDelayedActions();
	}

	public void onGameTick(GameTick e)
	{
		//log.info("Game tick");
		gameTick++;
		runDelayedActions();
	}

	public void runLater(Supplier<Boolean> condition, Runnable runnable)
	{
		clientThread.invoke(() -> {
			if (condition.get())
			{
				runnable.run();
			}
			else
			{
				delayedActions.add(new DelayedAction(condition, runnable));
			}
			return true;
		});
	}

	public void delayGameTicks(long delay, Runnable runnable)
	{
		long when = gameTick + delay;
		log.info("Received runnable, sending at {} game ticks. Current game ticks: {}", when, gameTick);
		runLater(() -> gameTick >= when, runnable);
	}

	public void delayClientTicks(long delay, Runnable runnable)
	{
		long when = clientTick + delay;
		runLater(() -> clientTick >= when, runnable);
	}

	public void delayTime(long delay, Runnable runnable)
	{
		long when = System.currentTimeMillis() + delay;
		runLater(() -> System.currentTimeMillis() >= when, runnable);
	}

	@Value
	public static class DelayedAction
	{
		Supplier<Boolean> shouldRun;
		Runnable runnable;
	}
}
