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
package net.runelite.client.plugins.imenudebugger;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.PlayerUtils;
import net.runelite.client.plugins.iutils.iUtils;
import static net.runelite.client.plugins.iutils.iUtils.iterating;
import org.pf4j.Extension;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "iMenu Debugger Plugin",
	enabledByDefault = false,
	description = "Illumine - Menu Debugger plugin. Has no function other than debugging",
	tags = {"illumine", "menu", "debug", "bot"},
	type = PluginType.UTILITY
)
@Slf4j
public class iMenuDebuggerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private iMenuDebuggerConfig config;

	@Inject
	private iUtils utils;

	@Inject
	private PlayerUtils playerUtils;

	@Inject
	private ConfigManager configManager;


	MenuEntry testMenu;
	Player player;
	LocalPoint beforeLoc;

	int timeout;

	@Provides
	iMenuDebuggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(iMenuDebuggerConfig.class);
	}

	@Override
	protected void startUp() {

	}

	@Override
	protected void shutDown() {

	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			if (timeout > 0)
			{
				timeout--;
				return;
			}
			if (!iterating)
			{
				if (!playerUtils.isMoving())
				{
					timeout = 10;
				}
			}
			beforeLoc = player.getLocalLocation();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		log.info("Menu Entry before override: {}", event.toString());
		if (config.printChat())
		{
			utils.sendGameMessage("Option value: " + event.getOption());
			utils.sendGameMessage("Target value: " + event.getTarget());
			utils.sendGameMessage("Identifier value: " + event.getIdentifier());
			utils.sendGameMessage("Opcode value: " + event.getOpcode());
			utils.sendGameMessage("Param0 value: " + event.getParam0());
			utils.sendGameMessage("Param1 value: " + event.getParam1());
			utils.sendGameMessage("mouseButton value: " + event.getMouseButton());
		}
		if (testMenu == null)
		{
			return;
		}
		if (utils.getRandomEvent()) //for random events
		{
			log.debug("Test plugin not overriding due to random event");
			return;
		}
		else
		{
			event.setMenuEntry(testMenu);
			testMenu = null; //this allow the player to interact with the client without their clicks being overridden
		}
	}
}