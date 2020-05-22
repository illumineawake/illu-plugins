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
package net.runelite.client.plugins.test;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.sound.sampled.Clip;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.geometry.Shapes;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.TileQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.WorldLocation;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
/*import net.runelite.client.rsb.methods.Tiles;
import net.runelite.client.rsb.wrappers.RSArea;
import net.runelite.client.rsb.wrappers.RSTile;*/
import org.pf4j.Extension;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import static net.runelite.api.ObjectID.*;

/*import net.runelite.client.rsb.methods.*;
import net.runelite.client.rsb.botLauncher.*;*/


@Extension
@PluginDescriptor(
	name = "Test",
	enabledByDefault = false,
	description = "Illumine test plugin",
	tags = {"tick"},
	type = PluginType.MISCELLANEOUS
)
@Slf4j
public class TestPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TestPluginConfiguration config;

	@Inject
	private BotUtils utils;

	Point point = new Point(10,10);
	GameObject object;
	int timeout = 0;
	public static final MenuEntry BANK_MENU = new MenuEntry("Bank","<col=ffff>Bank booth",10355, 4, 56, 48, true);
	public LocalPoint localPoint;
	MenuEntry BankMenu;
	private Tile[][][] areaTile = new Tile[3187][3230][0];
	List<WorldPoint> worldPointList = new ArrayList<>();
	WorldPoint outsideWorldPoint = new WorldPoint(2500,2500,0);
	WorldPoint swWorldPoint = new WorldPoint(3160, 3208, 0);
	WorldPoint neWorldPoint = new WorldPoint(3197, 3241, 0);
	WorldArea worldAreaTest = new WorldArea(swWorldPoint,20,10);
	WorldArea worldAreaCustom = new WorldArea(swWorldPoint,neWorldPoint);

	/*MethodContext ctx;
	RuneLite bot;*/
	/*RSArea rsAreaTest = new RSArea(new RSTile(3160, 3208, 0), new RSTile(3197, 3241, 0));
	RSArea rsAreaOutsideTest = new RSArea(new RSTile(3092, 3295, 0), new RSTile(3135, 3263, 0));
	RSTile screenTile = new RSTile(2655, 3286, 0);*/

	@Provides
	TestPluginConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TestPluginConfiguration.class);
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
		if (!event.getGroup().equals("Test"))
		{
			return;
		}

		if (event.getKey().equals("volume")) {
			//placeholder
		}
	}

	@Subscribe
	private void onGameTick(GameTick tick) {
		//object = new GameObjectQuery().idEquals(TREE, TREE_1277, TREE_1278, TREE_1279, TREE_1280).filter(o -> rsAreaOutsideTest.contains(o.getWorldLocation())).result(client).nearestTo(client.getLocalPlayer());
		object = utils.findNearestGameObject(TREE, TREE_1277, TREE_1278, TREE_1279, TREE_1280);
		if (object != null) {
			log.info("object found: " + object.getId());
		}
		else
		{
			log.info("object NOT FOUND");
		}
		//log.info(String.valueOf(worldAreaTest.toWorldPointList().toString()));
		worldPointList.clear();
		//worldPointList.addAll(worldAreaCustom.toWorldPointList());
		//log.info(String.valueOf(worldPointList.contains(client.getLocalPlayer().getWorldLocation())));
		//log.info(String.valueOf(rsAreaTest.contains(outsideWorldPoint)));
		//log.info("********world point list: ********** " + worldPointList.toString());
		//LocalPoint lp = client.getLocalPlayer().getLocalLocation();
		//Tile playerTile = client.getScene().getTiles()[client.getPlane()][lp.getSceneX()][lp.getSceneY()];
		//Tile playerTile = client.getLocalPlayer().getLocalLocation();
		//log.info(playerTile.getSceneLocation().toString());
		//utils.findNearestGameObjectWithin(lp, 20, TREE, TREE_1277, TREE_1278, TREE_1279, TREE_1280);
		/*if (client != null && client.getLocalPlayer() != null) {
			if(utils.isMoving()) {
				log.info("we are moving");
				timeout = 2; //there is always a 2 tick delay between players localdest = null and players animation changing from -1
				return;
			}

			if(timeout > 0) {
				timeout--;
				log.info("timeout: " + timeout);
				return;
			} else {
				if (!utils.isInteracting()) {
					GameObject nextTree;
					nextTree = utils.findNearestGameObject(TREE, TREE_1277, TREE_1278, TREE_1279, TREE_1280);
					if (nextTree != null) {
						int distanceTest = nextTree.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation());
						log.info("not interacting, finding new tree: " + nextTree.getId());
					} else {
						log.info("tree is null");
					}
				} else {
					log.info("we're interacting");
				}
			}

		//log.info("game tick");
		//object.getEntity().
		//log.info(playerReturner());
		//log.info(client.getLocalPlayer().getWorldLocation().toString());



		} else {
			log.info("everything is null");
			return;
		}*/

		/*if (client.getLocalPlayer() != null && client.getLocalDestinationLocation() != null) {
			log.info("player loc: " + client.getLocalPlayer().getWorldLocation());
			log.info(String.valueOf("local dest: " + client.getLocalDestinationLocation()));
			if (utils.isMoving()) {
				log.info("we're moving");
			}
		}*/

		//log.info("Animation: " + String.valueOf(client.getLocalPlayer().getAnimation()));
	}

	/*@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		log.info("Test event to string: " + event.toString());
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