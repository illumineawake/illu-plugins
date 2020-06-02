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
package net.runelite.client.plugins.varrockrooftopagility;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import org.pf4j.Extension;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static net.runelite.client.plugins.varrockrooftopagility.VarrockAgilityState.*;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Varrock Agility",
	enabledByDefault = false,
	description = "Illumine Varrock rooftop agility plugin",
	tags = {"agility"},
	type = PluginType.SKILLING
)
@Slf4j
public class VarrockAgilityPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private VarrockAgilityConfiguration config;

	@Inject
	private BotUtils utils;

	@Inject
	private ConfigManager configManager;

	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
		new ThreadPoolExecutor.DiscardPolicy());

	VarrockAgilityState state;
	VarrockAgilityObstacles obstacles;
	GameObject targetObject;
	GameObject nextTree;
	MenuEntry targetMenu;
	int timeout = 0;

	private final Set<Integer> itemIds = new HashSet<>();
	private final Set<Integer> gameObjIds = new HashSet<>();


	@Provides
	VarrockAgilityConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VarrockAgilityConfiguration.class);
	}

	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{
		configManager.setConfiguration("VarrockAgility", "startBot", false);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("VarrockAgility"))
		{
			return;
		}
		getConfigValues();
		if (event.getKey().equals("startBot"))
		{
			if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN))
			{
				if (config.startBot())
				{
					//skillLocation = client.getLocalPlayer().getWorldLocation();
					getConfigValues();
					//log.info("Starting power-skiller at location: " + skillLocation);
				}
			}
			else
			{
				if (config.startBot())
				{
					log.info("Stopping bot");
					configManager.setConfiguration("VarrockAgility", "startBot", false);
				}
			}
		}
	}

	private void getConfigValues()
	{
		gameObjIds.clear();

		for (int i : utils.stringToIntArray(config.gameObjects()))
		{
			gameObjIds.add(i);
		}

		itemIds.clear();

		for (int i : utils.stringToIntArray(config.items()))
		{
			itemIds.add(i);
		}
	}

	//enables run if below given minimum energy with random positive variation
	private void handleRun(int minEnergy, int randMax)
	{
		if (utils.isRunEnabled())
		{
			return;
		}
		else if (client.getEnergy() > (minEnergy + utils.getRandomIntBetweenRange(0, randMax)))
		{
			log.info("enabling run");
			targetMenu = new MenuEntry("Toggle Run", "", 1, 57, -1, 10485782, false);
			utils.clickRandomPoint(0, 200);
		}
	}

	/*private void interactTree()
	{
		nextTree = utils.findNearestGameObjectWithin(skillLocation, config.locationRadius(), gameObjIds);
		if (nextTree != null)
		{
			targetObject = nextTree;
			targetMenu = new MenuEntry("", "", nextTree.getId(), 3, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
			utils.clickRandomPoint(0, 200);
		}
		else
		{
			log.info("tree is null");
		}
	}*/

	private void dropInventory()
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			if (!items.isEmpty())
			{
				log.info("dropping " + items.size() + " items.");
				utils.sendGameMessage("dropping " + items.size() + " items.");
				state = ITERATING;
				executorService.submit(() ->
				{
					items.stream()
						.filter(item -> itemIds.contains(item.getId()))
						.forEach((item) -> {
							targetMenu = new MenuEntry("", "", item.getId(), 37, item.getIndex(), 9764864, false);
							utils.clickRandomPoint(0, 200);
							try
							{
								Thread.sleep(utils.getRandomIntBetweenRange(config.randLow(), config.randHigh()));
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						});
					state = ANIMATING; //failsafe so it doesn't get stuck looping. I should probs handle this better
				});
			}
			else
			{
				log.info("inventory list is empty");
			}
		}
		else
		{
			log.info("inventory container is null");
		}
	}

	public VarrockAgilityState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}
		if (state == ITERATING && !utils.inventoryEmpty())
		{
			return ITERATING;
		}
		if (utils.inventoryFull())
		{
			return DROPPING;
		}
		if (utils.isMoving())
		{
			timeout = 2;
			return MOVING;
		}
		if (!utils.isInteracting() && !utils.inventoryFull())
		{
			return FIND_OBJECT;
		}
		return ANIMATING; //need to determine an appropriate default
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		//if (client != null && client.getLocalPlayer() != null && config.startBot())
		if (client != null && client.getLocalPlayer() != null && config.startBot())
		{
			handleRun(40, 20);

			VarrockAgilityObstacles varObstacle = VarrockAgilityObstacles.getArea(client.getLocalPlayer().getWorldLocation());
			if(varObstacle != null)
			{
				log.info(String.valueOf(varObstacle.getObstacleId()));
			}
			else
			{
				log.info("enum is null");
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
		if (config.startBot())
		{
			if (targetMenu == null)
			{
				log.info("Modified MenuEntry is null");
				return;
			}
			else
			{
				//log.info("MenuEntry string event: " + targetMenu.toString());
				event.setMenuEntry(targetMenu);
				if (state != ITERATING)
				{
					timeout = 2;
				}
				targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
			}
		}
		else
		{
			//TODO: capture object clicks
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (nextTree == null || event.getGameObject() != nextTree) {
			return;
		} else {
			if (client.getLocalDestinationLocation() != null) {
				//interactTree(); //This is a failsafe, Player can get stuck with a destination on object despawn and be "forever moving".
			}
		}
	}
}