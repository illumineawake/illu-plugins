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
package net.runelite.client.plugins.worldwalker;

import com.google.inject.Provides;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "World Walker Plugin",
	enabledByDefault = false,
	description = "Illumine - World Walker plugin",
	tags = {"illumine", "walk", "web", "travel", "bot"},
	type = PluginType.UTILITY
)
@Slf4j
public class WorldWalkerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private WorldWalkerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private WorldWalkerOverlay overlay;

	@Inject
	private BotUtils utils;

	@Inject
	private ConfigManager configManager;

	Instant botTimer;
	Player player;
	WorldWalkerState state;
	LocalPoint beforeLoc = new LocalPoint(0, 0);
	WorldPoint customLocation;

	boolean startBot;
	long sleepLength;
	int tickLength;
	int timeout;

	@Provides
	WorldWalkerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WorldWalkerConfig.class);
	}

	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{
		resetVals();
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("WorldWalker"))
		{
			return;
		}
		log.debug("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startBot)
			{
				player = client.getLocalPlayer();
				if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
				{
					log.debug("starting World Walker plugin");
					startBot = true;
					timeout = 0;
					state = null;
					botTimer = Instant.now();
					overlayManager.add(overlay);
					if (config.location().equals(Location.CUSTOM))
					{
						customLocation = getCustomLoc();
						if (customLocation != null)
						{
							log.info("Custom location set to: {}", customLocation);
						}
						else
						{
							utils.sendGameMessage("Invalid custom location provided: " + config.customLocation());
							log.info("Invalid custom location provided: {}", config.customLocation());
							resetVals();
						}
					}
				}
				else
				{
					log.info("Start World Walker logged in!");
					resetVals();
				}
			}
			else
			{
				resetVals();
			}
		}
	}

	private WorldPoint getCustomLoc()
	{
		if (config.location().equals(Location.CUSTOM))
		{
			int[] customTemp = utils.stringToIntArray(config.customLocation());
			if (customTemp.length != 3)
			{
				return null;
			}
			else
			{
				return new WorldPoint(customTemp[0], customTemp[1], customTemp[2]);
			}
		}
		return null;
	}

	@Subscribe
	private void onConfigChange(ConfigChanged event)
	{
		if (!event.getGroup().equals("WorldWalker"))
		{
			return;
		}
		switch (event.getKey())
		{
			case "location":
				if (config.location().equals(Location.CUSTOM))
				{
					customLocation = getCustomLoc();
					if (customLocation != null)
					{
						log.info("Custom location set to: {}", customLocation);
					}
					else
					{
						utils.sendGameMessage("Invalid custom location provided: " + config.customLocation());
						log.info("Invalid custom location provided: {}", config.customLocation());
						resetVals();
					}
				}
		}
	}

	private void resetVals()
	{
		log.debug("stopping World Walker plugin");
		overlayManager.remove(overlay);
		startBot = false;
		botTimer = null;
		customLocation = null;
		state = null;
	}

	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		return tickLength;
	}

	private WorldPoint getLocation()
	{
		return (config.location().equals(Location.CUSTOM)) ? customLocation : config.location().getWorldPoint();
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!startBot || config.location().equals(Location.NONE))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			utils.handleRun(20, 30);
			if (timeout > 0)
			{
				timeout--;
			}
			else
			{
				if (player.getWorldLocation().distanceTo(getLocation()) >= config.rand())
				{
					utils.webWalk(getLocation(), config.rand(), utils.isMoving(beforeLoc), sleepDelay());
					timeout = tickDelay();
				}
				else
				{
					utils.sendGameMessage("Arrived at " + config.location().getName()+ ", stopping World Walker");
					resetVals();
				}
			}
			beforeLoc = player.getLocalLocation();
		}
	}
}