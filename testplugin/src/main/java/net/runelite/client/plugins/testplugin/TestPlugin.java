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
package net.runelite.client.plugins.testplugin;

import com.google.inject.Provides;
import java.util.*;
import java.util.concurrent.*;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.CalculationUtils;
import net.runelite.client.plugins.iutils.InventoryUtils;
import net.runelite.client.plugins.iutils.MouseUtils;
import net.runelite.client.plugins.iutils.iUtils;
import org.pf4j.Extension;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "Test Plugin",
	enabledByDefault = false,
	description = "Illumine - Test plugin",
	tags = {"illumine", "test", "bot"},
	type = PluginType.UTILITY
)
@Slf4j
public class TestPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private TestConfig config;

	@Inject
	private iUtils utils;

	@Inject
	private ActionQueue action;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private CalculationUtils calc;

	@Inject
	private MouseUtils mouse;

	@Inject
	private ConfigManager configManager;

	MenuEntry testMenu;
	MenuEntry testMenu2;
	Player player;
	GameObject testGameObject;
	LocalPoint beforeLoc;

	int timeout;
	int tickCount;
	boolean done;
	Timer timer;

	@Provides
	TestConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TestConfig.class);
	}

	@Override
	protected void startUp()
	{
		timeout = 2;
	}

	@Override
	protected void shutDown()
	{
		timeout = 0;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("Test"))
		{
			return;
		}
		log.debug("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey())
		{
			case "startButton":
				log.info("button clicked");
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client != null && client.getLocalPlayer() != null && client.getGameState() == GameState.LOGGED_IN)
		{

		}
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
			if (!iUtils.iterating)
			{
				if (action.delayedActions.isEmpty() && !inventory.isEmpty())
				{
					log.info("Executing on game tick");
					Collection<WidgetItem> inventoryItems = inventory.getAllItems();
					if (!inventoryItems.isEmpty())
					{
						int i = 1;
						for (WidgetItem item : inventoryItems)
						{
							MenuEntry entry = new MenuEntry("", "", item.getId(), MenuOpcode.ITEM_DROP.getId(), item.getIndex(),
								WidgetInfo.INVENTORY.getId(), false);
							utils.doActionClientTick(entry, item.getCanvasBounds(), (3 * i) + calc.getRandomIntBetweenRange(0, 2));
							i++;
						}
						done = true;
					}
				}
			}
			beforeLoc = player.getLocalLocation();
		}
	}

	/*@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		log.info("message type {}, message {}", event.getType(), event.getMessage());
	}*/

	/*@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		log.info("Menu Entry before override: {}", event.toString());
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
	}*/
}