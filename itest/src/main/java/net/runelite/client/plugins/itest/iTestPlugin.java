/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.itest;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.itest.tasks.MovingTask;
import net.runelite.client.plugins.itest.tasks.TimeoutTask;
import net.runelite.client.plugins.iutils.WalkUtils;
import net.runelite.client.plugins.iutils.game.Bot;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scene.RectangularArea;
import net.runelite.client.plugins.iutils.walking.Walking;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "iTest",
	enabledByDefault = false,
	description = "Illumine - Test plugin",
	tags = {"illumine", "test", "bot"}
)
@Slf4j
public class iTestPlugin extends Plugin implements Runnable
{
	@Inject
	private Injector injector;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ExecutorService executorService;

	@Inject
	private iTestConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private iTestOverlay overlay;

	@Inject
	private iUtils utils;

	@Inject
	private WalkUtils walkUtils;

	@Inject
	private Bot bot;

	@Inject
	public ChinBreakHandler chinBreakHandler;

	@Inject
	private ConfigManager configManager;

	private TaskSet tasks = new TaskSet();
	public static LocalPoint beforeLoc = new LocalPoint(0, 0);

	MenuEntry targetMenu;
	Instant botTimer;
	Player player;

	public static boolean startBot;
	public static long sleepLength;
	public static int tickLength;
	public static int timeout;
	public static String status = "starting...";

	@Provides
	iTestConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(iTestConfig.class);
	}

	@Override
	protected void startUp()
	{
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown()
	{
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}

	@Override
	public void run() {
		if (client != null && client.getLocalPlayer() != null) {
			log.info("reloaded");
			long start = System.currentTimeMillis();
//			bot.inventory().withName("Tinderbox").first().useOn(bot.inventory().withName("Logs").first());
			Walking walking = new Walking(bot);
			walking.walkTo(new RectangularArea(3220, 3222, 3224, 3215)); //Lumbridge
//			walking.walkTo(new RectangularArea(1763, 3666, 1780, 3661)); //Port Pisc
//			walking.walkTo(new RectangularArea(3073, 3290, 3084, 3283)); //Draynorish
//			log.info("{}",bot.objects().withAction("Open").nearest().orientation());
//			GrandExchange grandExchange = new GrandExchange(bot);
//			grandExchange.buy(361, 5, 2, 60000);
//			grandExchange.sell(bot.inventory().withName("Tuna").first().id(), 85 );
//			clientThread.invoke(() -> client.runScript(112,84,'\n',"5")); //112
//			Chatbox chatbox = new Chatbox(bot);
//			chatbox.chat("access my bank");
//			log.info("{}", client.isResized());
//			List<iWidget> widgets = bot.widget(219, 1).items();
//			for (iWidget widget : widgets) {
//				log.info("{}", widget.text());
//			}
//			client.invokeMenuAction("","", 1, 57, -1, 10485782);
//			log.info("{}",bot.getFromClientThread(bank::isOpen));
//			Bank bank = new Bank(bot);
//			log.info("{}", bank.isOpen());
//			bank.withdraw(361, 20, true);
//			log.info("{}",bank.quantity(361));
//			bot.objects2().withName("Tree").nearest().interact("Chop down");
//			bot.objects().withName("Tree").nearest().interact("Chop down");
			log.info("finish: {}", System.currentTimeMillis() - start);
//			bank.withdraw(361, 5, true);
		}
	}

	private void loadTasks()
	{
		tasks.clear();
		tasks.addAll(
			injector.getInstance(TimeoutTask.class),
			injector.getInstance(MovingTask.class)
		);
	}

	public void resetVals()
	{
		log.debug("stopping Task Template plugin");
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
		startBot = false;
		botTimer = null;
		tasks.clear();
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("iTest"))
		{
			return;
		}
		executorService.submit(this::run);
	}

	public void updateStats()
	{
		//templatePH = (int) getPerHour(totalBraceletCount);
		//coinsPH = (int) getPerHour(totalCoins - ((totalCoins / BRACELET_HA_VAL) * (unchargedBraceletCost + revEtherCost + natureRuneCost)));
	}

	public long getPerHour(int quantity)
	{
		Duration timeSinceStart = Duration.between(botTimer, Instant.now());
		if (!timeSinceStart.isZero())
		{
			return (int) ((double) quantity * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
		}
		return 0;
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
//		long start = System.currentTimeMillis();
//		Bank bank = new Bank(bot);
//		log.info("{}",bot.getFromClientThread(bank::isOpen));
//		log.info("finish: {}", System.currentTimeMillis() - start);

		if (!startBot || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			if (chinBreakHandler.shouldBreak(this))
			{
				status = "Taking a break";
				chinBreakHandler.startBreak(this);
				timeout = 5;
			}

			Task task = tasks.getValidTask();
			if (task != null)
			{
				status = task.getTaskDescription();
				task.onGameTick(event);
			}
			else
			{
				status = "Task not found";
				log.debug(status);
			}
			beforeLoc = player.getLocalLocation();
		}
	}
}