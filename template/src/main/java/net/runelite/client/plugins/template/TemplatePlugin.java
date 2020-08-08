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
package net.runelite.client.plugins.template;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import static net.runelite.client.plugins.template.TemplateState.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Template Plugin",
	enabledByDefault = false,
	description = "Illumine - Template plugin",
	tags = {"illumine", "template", "bot"},
	type = PluginType.UTILITY
)
@Slf4j
public class TemplatePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TemplateConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TemplateOverlay overlay;

	@Inject
	private BotUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	TemplateState state;
	LocalPoint beforeLoc = new LocalPoint(0, 0);

	boolean startBot;
	long sleepLength;
	int tickLength;
	int timeout;
	int coinsPH;

	@Provides
	TemplateConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TemplateConfig.class);
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

	private void resetVals()
	{
		log.debug("stopping template plugin");
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
		startBot = false;
		botTimer = null;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("Template"))
		{
			return;
		}
		log.debug("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startBot)
			{
				log.debug("starting template plugin");
				startBot = true;
				timeout = 0;
				coinsPH = 0;
				state = null;
				targetMenu = null;
				botTimer = Instant.now();
				overlayManager.add(overlay);
				chinBreakHandler.startPlugin(this);
			}
			else
			{
				resetVals();
			}
		}
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

	private long sleepDelay()
	{
		return utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
	}

	private int tickDelay()
	{
		tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private TemplateState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}
		if (utils.iterating)
		{
			return ITERATING;
		}
		if (utils.isMoving(beforeLoc))
		{
			return MOVING;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			return HANDLE_BREAK;
		}
		return TIMEOUT;
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!startBot || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			state = getState();
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					break;
				case ITERATING:
					break;
				case MOVING:
					timeout = tickDelay();
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					break;
			}
			beforeLoc = player.getLocalLocation();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!startBot || targetMenu == null)
		{
			return;
		}
		if (utils.getRandomEvent()) //for random events
		{
			log.debug("Template plugin not overriding due to random event");
			return;
		}
		else
		{
			event.setMenuEntry(targetMenu);
			targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
		}
	}
}