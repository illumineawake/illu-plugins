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
package net.runelite.client.plugins.rooftopagility;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.ItemDespawned;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import org.pf4j.Extension;
import static net.runelite.client.plugins.rooftopagility.RooftopAgilityState.*;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Rooftop Agility",
	enabledByDefault = false,
	description = "Illumine auto rooftop agility plugin",
	tags = {"agility"},
	type = PluginType.SKILLING
)
@Slf4j
public class RooftopAgilityPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BotUtils utils;

	@Inject
	private RooftopAgilityConfig config;

	@Inject
	PluginManager pluginManager;

	@Inject
	ClientToolbar clientToolbar;

	RooftopAgilityState state;
	RooftopAgilityPanel panel;
	private NavigationButton navButton;

	TileItem markOfGrace;
	Tile markOfGraceTile;
	MenuEntry targetMenu;
	LocalPoint beforeLoc = new LocalPoint(0, 0); //initiate to mitigate npe
	int timeout = 0;
	boolean startAgility;
	private final Set<Integer> REGION_IDS = Set.of(9781, 12853, 12597, 12084, 12339, 12338, 10806, 10297, 10553, 13358, 13878, 10547);
	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
		new ThreadPoolExecutor.DiscardPolicy());

	@Override
	protected void startUp()
	{
		/*panel = injector.getInstance(RooftopAgilityPanel.class);
		panel.init();
		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "panel_icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Rooftop Agility")
			.priority(5)
			.panel(panel)
			.icon(icon)
			.build();
		clientToolbar.addNavigation(navButton);*/
	}

	@Override
	protected void shutDown()
	{
		markOfGraceTile = null;
		markOfGrace = null;
		startAgility = false;
		//clientToolbar.removeNavigation(navButton);
	}

	private void sleepDelay()
	{
		long sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		log.info("Sleeping for {}ms", sleepLength);
		utils.sleep(sleepLength);
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.info("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	@Provides
	RooftopAgilityConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RooftopAgilityConfig.class);
	}

	private void findObstacle()
	{
		RooftopAgilityObstacles obstacle = RooftopAgilityObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
		if (obstacle != null)
		{
			log.info(String.valueOf(obstacle.getObstacleId()));

			if (obstacle.getObstacleType() == RooftopAgilityObstacleType.DECORATION)
			{
				DecorativeObject decObstacle = utils.findNearestDecorObject(obstacle.getObstacleId());
				if (decObstacle != null)
				{
					targetMenu = new MenuEntry("", "", decObstacle.getId(), 3, decObstacle.getLocalLocation().getSceneX(), decObstacle.getLocalLocation().getSceneY(), false);
					sleepDelay();
					utils.clickRandomPointCenter(-100, 100);
					return;
				}
			}
			if (obstacle.getObstacleType() == RooftopAgilityObstacleType.GROUND_OBJECT)
			{
				GroundObject groundObstacle = utils.findNearestGroundObject(obstacle.getObstacleId());
				if (groundObstacle != null)
				{
					targetMenu = new MenuEntry("", "", groundObstacle.getId(), 3, groundObstacle.getLocalLocation().getSceneX(), groundObstacle.getLocalLocation().getSceneY(), false);
					sleepDelay();
					utils.clickRandomPointCenter(-100, 100);
					return;
				}
			}
			GameObject objObstacle = utils.findNearestGameObject(obstacle.getObstacleId());
			if (objObstacle != null)
			{
				targetMenu = new MenuEntry("", "", objObstacle.getId(), 3, objObstacle.getSceneMinLocation().getX(), objObstacle.getSceneMinLocation().getY(), false);
				sleepDelay();
				utils.clickRandomPointCenter(-100, 100);
				return;
			}
		}
		else
		{
			log.info("Not in obstacle area");
		}
	}

	public RooftopAgilityState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}
		if (utils.isMoving(beforeLoc)) //could also test with just isMoving
		{
			timeout = tickDelay();
			return MOVING;
		}
		if (markOfGrace != null && markOfGraceTile != null && config.mogPickup())
		{
			RooftopAgilityObstacles currentObstacle = RooftopAgilityObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
			if (currentObstacle == null)
			{
				timeout = tickDelay();
				return MOVING;
			}
			if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0)
			{
				return MARK_OF_GRACE;
			}
		}
		if (!utils.isMoving(beforeLoc))
		{
			return FIND_OBSTACLE;
		}
		return ANIMATING; //need to determine an appropriate default
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (client != null && client.getLocalPlayer() != null && client.getGameState() == GameState.LOGGED_IN && startAgility)
		{
			if (!REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()))
			{
				log.info("not in agility course region");
				return;
			}
			utils.handleRun(40, 20);
			state = getState();
			//this seems shit
			beforeLoc = client.getLocalPlayer().getLocalLocation();
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					return;
				case MARK_OF_GRACE:
					log.info("Picking up mark of grace");
					targetMenu = new MenuEntry("", "", ItemID.MARK_OF_GRACE, 20, markOfGraceTile.getSceneLocation().getX(), markOfGraceTile.getSceneLocation().getY(), false);
					sleepDelay();
					utils.clickRandomPointCenter(-100, 100);
					return;
				case FIND_OBSTACLE:
					findObstacle();
					return;
				case MOVING:
					break;
				default:
					return;
			}
		}
		else
		{
			//log.info("client/ player is null or bot isn't started");
			return;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!startAgility || targetMenu == null)
		{
			return;
		}
		log.debug("MenuEntry string event: " + targetMenu.toString());
		event.setMenuEntry(targetMenu);
		timeout = tickDelay();
		targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) || !config.mogPickup())
		{
			return;
		}

		TileItem item = event.getItem();
		Tile tile = event.getTile();

		if (item.getId() == ItemID.MARK_OF_GRACE)
		{
			log.info("Mark of grace spawned");
			markOfGrace = item;
			markOfGraceTile = tile;
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event)
	{
		if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) || !config.mogPickup())
		{
			return;
		}

		TileItem item = event.getItem();

		if (item.getId() == ItemID.MARK_OF_GRACE)
		{
			log.info("Mark of grace despawned");
			markOfGrace = null;
		}
	}
}