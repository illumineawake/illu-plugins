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
package net.runelite.client.plugins.powerskiller;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.sound.sampled.Clip;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.api.coords.WorldArea;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
//import net.runelite.client.rsb.wrappers.RSTile;
import org.pf4j.Extension;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.runelite.api.ObjectID.*;
import static net.runelite.client.plugins.powerskiller.PowerSkillerState.*;


@Extension
@PluginDescriptor(
	name = "PowerSkiller",
	enabledByDefault = false,
	description = "Illumine auto power-skill plugin",
	tags = {"tick"},
	type = PluginType.UTILITY
)
@Slf4j
public class PowerSkillerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PowerSkillerConfiguration config;

	@Inject
	private BotUtils utils;

	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
			new ThreadPoolExecutor.DiscardPolicy());

	PowerSkillerState state;
	GameObject targetObject;
	GameObject nextTree;
	MenuEntry targetMenu;
	Point point;
	int timeout = 0;

	private final Set<Integer> ids = new HashSet<>();
	private final List<WidgetItem> items = new ArrayList<>();
	WorldPoint treeWorldPoint = new WorldPoint(3187,3230,0);
	WorldPoint treeWorldPoint2 = new WorldPoint(3187,3235,0);

	WorldPoint swWorldPoint = new WorldPoint(3160, 3208, 0);
	WorldPoint neWorldPoint = new WorldPoint(3197, 3241, 0);
	//WorldArea worldAreaTest = new WorldArea(swWorldPoint, neWorldPoint);

	@Provides
	PowerSkillerConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PowerSkillerConfiguration.class);
	}



	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{

	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("PowerSkiller"))
		{
			return;
		}

		if (event.getKey().equals("volume")) {
			//placeholder
		}
	}

	//enables run if below given minimum energy with random variation
	private void handleRun(int minEnergy, int randMax) {
		if (utils.isRunEnabled()) {
			return;
		} else if (client.getEnergy() > (minEnergy + utils.getRandomIntBetweenRange(0, randMax))) {
			log.info("enabling run");
			targetMenu = new MenuEntry("Toggle Run","",1,57,-1,10485782,false);
			utils.clickRandomPoint(0,200);
		}
	}

	private void interactTree(){
		//treeArea = new WorldArea(treeWorldPoint, treeWorldPoint2);
		nextTree = utils.findNearestGameObjectWithin(treeWorldPoint, 20, TREE, TREE_1277, TREE_1278, TREE_1279, TREE_1280);
		if (nextTree != null) {
			targetObject = nextTree;
			//targetMenu = new MenuEntry("Chop down", "<col=ffff>Tree", nextTree.getId(), 3, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
			targetMenu = new MenuEntry("", "", nextTree.getId(), 3, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
			utils.clickRandomPoint(0,200);
		} else {
			log.info("tree is null");
		}
	}

	private void dropInventory() {
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null) {
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			if (!items.isEmpty()) {
				log.info("dropping this many items: " + items.size());
				state = ITERATING;
				executorService.submit(() ->
				{
					for (WidgetItem item : items)
					{
						//targetMenu = new MenuEntry("Drop", "Drop", item.getId(), 37, item.getIndex(), 9764864, false);
						targetMenu = new MenuEntry("", "", item.getId(), 37, item.getIndex(), 9764864, false);
						utils.clickRandomPoint(0,200);
						try
						{
							Thread.sleep(utils.getRandomIntBetweenRange(25,300));
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					state = CHOPPING; //failsafe so it doesn't get stuck looping. I should probs handle this better
				});
			} else {
				log.info("inventory list is empty");
				//timeout = 0;
			}
		} else {
			log.info("inventory container is null");
		}
	}

	public PowerSkillerState getState() {
		if (timeout > 0) {
			return TIMEOUT;
		}
		if (state == ITERATING && !utils.inventoryEmpty()) {
			return ITERATING;
		}
		if (utils.inventoryFull()) {
			return DROPPING;
		}
		if (utils.isMoving()) {
			timeout = 2;
			return MOVING;
		}
		if (!utils.isInteracting() && !utils.inventoryFull()) {
			return FIND_TREE;
		}
		return CHOPPING; //need to determine an appropriate default
	}

	@Subscribe
	private void onGameTick(GameTick tick) {
		if (client != null && client.getLocalPlayer() != null) {
			handleRun(40, 20);
			state = getState();
			log.info("Current state is: " + state.toString());
			switch (state) {
				case TIMEOUT:
					timeout--;
					return;
				case DROPPING:
					dropInventory();
					return;
				case FIND_TREE:
					interactTree();
					return;
				case CHOPPING:
				case ITERATING:
				case MOVING:
					return; //not sure yet
			}
		} else {
			log.info("client or player is null");
			return;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (targetMenu == null){
			log.info("Modified MenuEntry is null");
			return;
		} else {
			//log.info("MenuEntry string event: " + targetMenu.toString());
			event.setMenuEntry(targetMenu);
			if (state != ITERATING) {
				timeout = 2;
			}
			targetMenu = null; //this allow the player to interact with the client without their clicks being overriden
		}
	}

	/*@Subscribe
	private void onItemContainerChanged(ItemContainerChanged event) {
		ItemContainer itemContainer = event.getItemContainer();
		if(itemContainer != client.getItemContainer(InventoryID.INVENTORY)){
			return;
		}
		if(isInventoryFull()) {
			dropInventory();
		}
	}*/

	/*@Subscribe
	public void onAnimationChanged(AnimationChanged event) {
		if (event.getActor() == client.getLocalPlayer()){
			log.info("animation changed to: " + event.getActor().getAnimation());
		}
	}*/

	/*@Subscribe
	public void onInteractingChanged(InteractingChanged event) {
		if(event.getSource() == client.getLocalPlayer()) {
			log.info("interact changed: " + event.toString());
		}
	}*/



	/*private String playerReturner() {
		if (client == null || client.getLocalPlayer() == null) {
			return "something is null";
		}
		if (logbalanceArea.contains(client.getLocalPlayer().getWorldLocation())) {
			return "we are IN the area";
		} else {
			return "we are NOT in the area";
		}
	}*/
}