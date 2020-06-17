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

import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_ENTER;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import static net.runelite.api.ObjectID.*;
import static net.runelite.api.Varbits.BLAST_FURNACE_COFFER;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;

import net.runelite.api.geometry.Shapes;
import net.runelite.api.queries.ActorQuery;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.InventoryItemQuery;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.api.queries.TileQuery;
import net.runelite.api.queries.WidgetItemQuery;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import net.runelite.api.queries.NPCQuery;

import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.InventoryComparableEntry;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
/*import net.runelite.client.rsb.methods.Tiles;
import net.runelite.client.rsb.wrappers.RSArea;
import net.runelite.client.rsb.wrappers.RSTile;*/
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.List;

/*import net.runelite.client.rsb.methods.*;
import net.runelite.client.rsb.botLauncher.*;*/


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Test",
	enabledByDefault = false,
	description = "Illumine test plugin",
	tags = {"tick"},
	type = PluginType.UTILITY
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

	@Inject
	private ItemManager itemManager;

	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
		new ThreadPoolExecutor.DiscardPolicy());

	Point point = new Point(10, 10);
	GameObject object;
	int timeout = 0;
	int itemCount = 0;
	public static final MenuEntry BANK_MENU = new MenuEntry("Bank", "<col=ffff>Bank booth", 10355, 4, 56, 48, true);
	public LocalPoint localPoint;
	MenuEntry testMenu;
	private Tile[][][] areaTile = new Tile[3187][3230][0];
	List<WorldPoint> worldPointList = new ArrayList<>();

	LocalPoint beforeLoc;
	WorldPoint outsideWorldPoint = new WorldPoint(2500, 2500, 0);
	WorldPoint swWorldPoint = new WorldPoint(3160, 3208, 0);
	WorldPoint neWorldPoint = new WorldPoint(3197, 3241, 0);
	//WorldArea worldAreaTest = new WorldArea(swWorldPoint,20,10);
	WorldArea worldAreaTest = new WorldArea(new WorldPoint(3160, 3208, 0), new WorldPoint(3160, 3208, 0));
	WorldArea worldAreaCustom = new WorldArea(swWorldPoint, neWorldPoint);
	private final int VARROCK_REGION_ID = 12853;


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

		if (event.getKey().equals("volume"))
		{
			//placeholder
		}
	}

	public Widget getBankItemWidgetAnyOf(int... ids)
	{
		if (!utils.isBankOpen())
		{
			return null;
		}

		WidgetItem bankItem = new BankItemQuery().idEquals(ids).result(client).first();
		if (bankItem != null)
			return bankItem.getWidget();
		else
			return null;
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		List<Integer> inventorySetup = List.of(ItemID.COAL_BAG_12019, ItemID.SMALL_FISHING_NET);
		//object = new GameObjectQuery().idEquals(TREE, TREE_1277, TREE_1278, TREE_1279, TREE_1280).filter(o -> rsAreaOutsideTest.contains(o.getWorldLocation())).result(client).nearestTo(client.getLocalPlayer());
		if (client != null && client.getLocalPlayer() != null)
		{
			if(!utils.iterating)
			{
				//Collection<WidgetItem> items = getAllInventoryItems();
				//MenuEntry test = utils.getInventoryItemMenu(itemManager, "Check", 35);
				log.info(String.valueOf(getBankItemWidgetAnyOf(ItemID.COAL_BAG_12019, ItemID.SWORDFISH)));
				//handleRun(20,20);
				//utils.depositAllExcept(ItemID.COAL_BAG_12019, ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4);
				//utils.depositAllExcept(inventorySetup);
				//arrayParamTest(inventorySetup);
			}
			//final int coffer = client.getVar(BLAST_FURNACE_COFFER);
			//log.info("Coffer value: " + coffer);
			//log.info(String.valueOf(client.getItemContainer(InventoryID.BANK) == null));
			//log.info(String.valueOf(bankWidget != null));)
			//log.info("Free inv spaces: " + new InventoryItemQuery(InventoryID.INVENTORY).idEquals(-1).result(client).size());
			/*1for (Item item : client.getItemContainer(InventoryID.INVENTORY).getItems())
			{
				if (item.getId() != -1)
				{
					itemCount++;
				}
			}
			log.info("Items in inventory: " + itemCount);
			itemCount = 0;*/

			/*MenuEntry[] menuEntries = client.getMenuEntries();
			if (menuEntries != null)
			{
				for (MenuEntry entry : menuEntries)
				{
					if (entry.getOption().equals("Eat"))
					{
						log.info(entry.toString());
						break;
					}
				}
			}*/
			/*if (beforeLoc != null)
			{
				log.info("Current Loc value: " + client.getLocalPlayer().getLocalLocation() + "before Loc value " + beforeLoc);
				log.info("Do they equal: " + String.valueOf(client.getLocalPlayer().getLocalLocation().equals(beforeLoc)));
			}
			beforeLoc = client.getLocalPlayer().getLocalLocation();*/
			//int camX = client.getCameraX();
			//int camY = client.getCameraY();

			//log.info("local destination value: " + String.valueOf(client.getLocalDestinationLocation() != null));
			//DecorativeObject decObject = utils.findNearestDecorObject(ROUGH_WALL_14412);
			//log.info(String.valueOf(decObject.getLocalLocation().getSceneX()));
			//log.info(String.valueOf(worldAreaTest.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0));
			//NPC npc = new NPCQuery().idEquals(512).result(client).nearestTo(client.getLocalPlayer());
			//if (npc != null)
			//	log.info("NPC interacting status: " + npc.getInteracting());


			//log.info(String.valueOf(client.getItemContainer(InventoryID.INVENTORY).getItems().length));
			//ArrayList<Item> items = utils.getWidgetItems(utils.stringToIntArray("1511,1522"));
			//log.info(String.valueOf(items.size()));


		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		log.info(event.toString());
		event.getGroupId();
		Widget optionWidget = client.getWidget(219,1);
		if (optionWidget != null)
		{
			log.info("our widget loaded");
		}
	}

	/*@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		if(client.getLocalPlayer().getWorldLocation().getRegionID() != VARROCK_REGION_ID)
		{
			return;
		}

		TileItem item = event.getItem();
		Tile tile = event.getTile();

		if(item.getId() == ItemID.MARK_OF_GRACE)
		{
			utils.sendGameMessage("Mark of grace spawned");
			testMenu = new MenuEntry("","", ItemID.MARK_OF_GRACE,20,tile.getSceneLocation().getX(),tile.getSceneLocation().getY(),false);
			utils.clickRandomPoint(200,400);
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event)
	{
		if(client.getLocalPlayer().getWorldLocation().getRegionID() != VARROCK_REGION_ID)
		{
			return;
		}

		TileItem item = event.getItem();

		if(item.getId() == ItemID.MARK_OF_GRACE)
		{
			utils.sendGameMessage("Mark of grace despawned");
		}
	}*/

	/*@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		event.getType();
	}*/

	/*@Subscribe
	private void onWidget(WidgetLoaded event)
	{
		log.info(event.toString());
		event.getGroupId(
	}*/

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		log.info("Test menu, before hook: " + event.toString());
		if (testMenu != null)
		{
			event.setMenuEntry(testMenu);
			log.info("Test menu, after hook: " + testMenu.toString());
			testMenu = null;
		}
	}

	/*@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		log.info("Test event to string: " + event.toString());
		MenuEntry dismissMenu = new MenuEntry("", "", 1875, MenuOpcode.EXAMINE_NPC.getId(),0,0, false);
		event.setMenuEntry(dismissMenu);
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