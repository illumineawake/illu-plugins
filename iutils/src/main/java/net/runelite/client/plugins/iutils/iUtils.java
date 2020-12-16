/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iutils;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.http.api.ge.GrandExchangeClient;
import net.runelite.http.api.osbuddy.OSBGrandExchangeClient;
import net.runelite.http.api.osbuddy.OSBGrandExchangeResult;
import okhttp3.OkHttpClient;
import org.pf4j.Extension;

/**
 *
 */
@Extension
@PluginDescriptor(
	name = "iUtils",
	type = PluginType.UTILITY,
	description = "Illumine plugin utilities",
	hidden = false
)
@Slf4j
@SuppressWarnings("unused")
@Singleton
public class iUtils extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private iUtilsConfig config;

	@Inject
	private MouseUtils mouse;

	@Inject
	private ActionQueue action;

	@Inject
	private MenuUtils menu;

	@Inject
	private WalkUtils walk;

	@Inject
	private CalculationUtils calc;

	@Inject
	private InterfaceUtils interfaceUtils;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	ExecutorService executorService;

	@Inject
	private OSBGrandExchangeClient osbGrandExchangeClient;

	private OSBGrandExchangeResult osbGrandExchangeResult;

	public boolean randomEvent;
	public static boolean iterating;
	private final List<ActionQueue.DelayedAction> delayedActions = new ArrayList<>();
	private int clientTick = 0;
	private int gameTick = 0;
	int tickActions;

	@Provides
	OSBGrandExchangeClient provideOsbGrandExchangeClient(OkHttpClient okHttpClient)
	{
		return new OSBGrandExchangeClient(okHttpClient);
	}

	@Provides
	GrandExchangeClient provideGrandExchangeClient(OkHttpClient okHttpClient)
	{
		return new GrandExchangeClient(okHttpClient);
	}

	@Provides
	iUtilsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(iUtilsConfig.class);
	}

	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{

	}

	//Use with caution, does not pair with mouse click and is potentially detectable
	public void doInvokeClientTick(MenuEntry entry, long ticksToDelay)
	{
		Runnable runnable = () -> client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
			entry.getOpcode(), entry.getParam0(), entry.getParam1());
		action.delayClientTicks(ticksToDelay, runnable);
	}

	public void doActionClientTick(MenuEntry entry, Rectangle rect, long ticksToDelay)
	{
		Point point = mouse.getClickPoint(rect);
		doActionClientTick(entry, point, ticksToDelay);
	}

	public void doActionClientTick(MenuEntry entry, Point point, long ticksToDelay)
	{
		Runnable runnable = () -> {
			menu.setEntry(entry);
			mouse.handleMouseClick(point);
		};

		action.delayClientTicks(ticksToDelay, runnable);
	}

	public void doGameObjectActionClientTick(GameObject object, int menuOpcodeID, long ticksToDelay)
	{
		if (object == null)
		{
			return;
		}
		Rectangle rectangle = (object.getConvexHull().getBounds() != null) ? object.getConvexHull().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", object.getId(), menuOpcodeID, object.getSceneMinLocation().getX(),
			object.getSceneMinLocation().getY(), false);
		doActionClientTick(entry, rectangle, ticksToDelay);
	}

	public void doTileObjectActionClientTick(TileObject object, int menuOpcodeID, long ticksToDelay)
	{
		if (object == null)
		{
			return;
		}
		Rectangle rectangle = (object.getCanvasTilePoly().getBounds() != null) ? object.getCanvasTilePoly().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", object.getId(), menuOpcodeID, object.getLocalLocation().getSceneX(),
			object.getLocalLocation().getSceneY(), false);
		doActionClientTick(entry, rectangle, ticksToDelay);
	}

	public void doNpcActionClientTick(NPC npc, int menuOpcodeID, long ticksToDelay)
	{
		if (npc == null)
		{
			return;
		}
		Rectangle rectangle = (npc.getConvexHull().getBounds() != null) ? npc.getConvexHull().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", npc.getIndex(), menuOpcodeID, 0, 0, false);
		doActionClientTick(entry, rectangle, ticksToDelay);
	}

	public void doItemActionClientTick(WidgetItem item, int menuOpcodeID, int menuParam1ID, long ticksToDelay)
	{
		if (item == null)
		{
			return;
		}
		MenuEntry entry = new MenuEntry("", "", item.getId(), menuOpcodeID,
			item.getIndex(), menuParam1ID, true);
		doActionClientTick(entry, item.getCanvasBounds().getBounds(), ticksToDelay);
	}

	//Use with caution, does not pair with mouse click and is potentially detectable
	public void doInvokeGameTick(MenuEntry entry, long ticksToDelay)
	{
		Runnable runnable = () -> client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
			entry.getOpcode(), entry.getParam0(), entry.getParam1());
		action.delayGameTicks(ticksToDelay, runnable);
	}

	public void doActionGameTick(MenuEntry entry, Rectangle rect, long ticksToDelay)
	{
		Point point = mouse.getClickPoint(rect);
		doActionGameTick(entry, point, ticksToDelay);
	}

	public void doActionGameTick(MenuEntry entry, Point point, long ticksToDelay)
	{

		Runnable runnable = () -> {
			menu.setEntry(entry);
			mouse.handleMouseClick(point);
		};

		action.delayGameTicks(ticksToDelay, runnable);
	}

	public void doGameObjectActionGameTick(GameObject object, int menuOpcodeID, long ticksToDelay)
	{
		if (object == null)
		{
			return;
		}
		Rectangle rectangle = (object.getConvexHull().getBounds() != null) ? object.getConvexHull().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", object.getId(), menuOpcodeID, object.getSceneMinLocation().getX(),
			object.getSceneMinLocation().getY(), false);
		doActionGameTick(entry, rectangle, ticksToDelay);
	}

	public void doTileObjectActionGameTick(TileObject object, int menuOpcodeID, long ticksToDelay)
	{
		if (object == null)
		{
			return;
		}
		Rectangle rectangle = (object.getCanvasTilePoly().getBounds() != null) ? object.getCanvasTilePoly().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", object.getId(), menuOpcodeID, object.getLocalLocation().getSceneX(),
			object.getLocalLocation().getSceneY(), false);
		doActionGameTick(entry, rectangle, ticksToDelay);
	}

	public void doNpcActionGameTick(NPC npc, int menuOpcodeID, long ticksToDelay)
	{
		if (npc == null)
		{
			return;
		}
		Rectangle rectangle = (npc.getConvexHull().getBounds() != null) ? npc.getConvexHull().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", npc.getIndex(), menuOpcodeID, 0, 0, false);
		doActionGameTick(entry, rectangle, ticksToDelay);
	}

	public void doItemActionGameTick(WidgetItem item, int menuOpcodeID, int menuParam1ID, long ticksToDelay)
	{
		if (item == null)
		{
			return;
		}
		MenuEntry entry = new MenuEntry("", "", item.getId(), menuOpcodeID,
			item.getIndex(), menuParam1ID, true);
		doActionGameTick(entry, item.getCanvasBounds().getBounds(), ticksToDelay);
	}

	//Use with caution, does not pair with mouse click and is potentially detectable
	public void doInvokeMsTime(MenuEntry entry, long timeToDelay)
	{
		Runnable runnable = () -> client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
			entry.getOpcode(), entry.getParam0(), entry.getParam1());
		action.delayTime(timeToDelay, runnable);
	}

	public void doActionMsTime(MenuEntry entry, Rectangle rect, long timeToDelay)
	{
		Point point = mouse.getClickPoint(rect);
		doActionMsTime(entry, point, timeToDelay);
	}

	public void doActionMsTime(MenuEntry entry, Point point, long timeToDelay)
	{
		Runnable runnable = () -> {
			menu.setEntry(entry);
			mouse.handleMouseClick(point);
		};

		action.delayTime(timeToDelay, runnable);
	}

	public void doGameObjectActionMsTime(GameObject object, int menuOpcodeID, long timeToDelay)
	{
		if (object == null)
		{
			return;
		}
		Rectangle rectangle = (object.getConvexHull().getBounds() != null) ? object.getConvexHull().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", object.getId(), menuOpcodeID, object.getSceneMinLocation().getX(),
			object.getSceneMinLocation().getY(), false);
		doActionMsTime(entry, rectangle, timeToDelay);
	}

	public void doTileObjectActionMsTime(TileObject object, int menuOpcodeID, long timeToDelay)
	{
		if (object == null)
		{
			return;
		}
		Rectangle rectangle = (object.getCanvasTilePoly().getBounds() != null) ? object.getCanvasTilePoly().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", object.getId(), menuOpcodeID, object.getLocalLocation().getSceneX(),
			object.getLocalLocation().getSceneY(), false);
		doActionMsTime(entry, rectangle, timeToDelay);
	}

	public void doNpcActionMsTime(NPC npc, int menuOpcodeID, long timeToDelay)
	{
		if (npc == null)
		{
			return;
		}
		Rectangle rectangle = (npc.getConvexHull().getBounds() != null) ? npc.getConvexHull().getBounds() :
			new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
		MenuEntry entry = new MenuEntry("", "", npc.getIndex(), menuOpcodeID, 0, 0, false);
		doActionMsTime(entry, rectangle, timeToDelay);
	}

	public void doItemActionMsTime(WidgetItem item, int menuOpcodeID, int menuParam1ID, long timeToDelay)
	{
		if (item == null)
		{
			return;
		}
		MenuEntry entry = new MenuEntry("", "", item.getId(), menuOpcodeID,
			item.getIndex(), menuParam1ID, true);
		doActionMsTime(entry, item.getCanvasBounds().getBounds(), timeToDelay);
	}

	public void doModifiedActionGameTick(MenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, Rectangle rect, long ticksToDelay)
	{
		Point point = mouse.getClickPoint(rect);
		doModifiedActionGameTick(entry, modifiedID, modifiedIndex, modifiedOpcode, point, ticksToDelay);
	}

	public void doModifiedActionGameTick(MenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, Point point, long ticksToDelay)
	{
		Runnable runnable = () -> {
			menu.setModifiedEntry(entry, modifiedID, modifiedIndex, modifiedOpcode);
			mouse.handleMouseClick(point);
		};

		action.delayGameTicks(ticksToDelay, runnable);
	}

	//Use with caution, does not pair with mouse click and is potentially detectable
	public void doModifiedInvokeGameTick(MenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, long ticksToDelay)
	{
		Runnable runnable = () -> {
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(modifiedIndex);
			client.setSelectedItemID(modifiedID);
			client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
				modifiedOpcode, entry.getParam0(), entry.getParam1());
		};

		action.delayGameTicks(ticksToDelay, runnable);
	}

	public void doModifiedActionMsTime(MenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, Rectangle rect, long timeToDelay)
	{
		Point point = mouse.getClickPoint(rect);
		doModifiedActionMsTime(entry, modifiedID, modifiedIndex, modifiedOpcode, point, timeToDelay);
	}

	public void doModifiedActionMsTime(MenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, Point point, long timeToDelay)
	{
		Runnable runnable = () -> {
			menu.setModifiedEntry(entry, modifiedID, modifiedIndex, modifiedOpcode);
			mouse.handleMouseClick(point);
		};

		action.delayTime(timeToDelay, runnable);
	}

	//Use with caution, does not pair with mouse click and is potentially detectable
	public void doModifiedInvokeMsTime(MenuEntry entry, int modifiedID, int modifiedIndex, int modifiedOpcode, long timeToDelay)
	{
		Runnable runnable = () -> {
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(modifiedIndex);
			client.setSelectedItemID(modifiedID);
			client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(),
				modifiedOpcode, entry.getParam0(), entry.getParam1());
		};

		action.delayTime(timeToDelay, runnable);
	}

	public void oneClickCastSpell(WidgetInfo spellWidget, MenuEntry targetMenu, long sleepLength)
	{
		menu.setEntry(targetMenu, true);
		mouse.delayMouseClick(new Rectangle(0, 0, 100, 100), sleepLength);
		menu.setSelectedSpell(spellWidget);
		mouse.delayMouseClick(new Rectangle(0, 0, 100, 100), calc.getRandomIntBetweenRange(20, 60));
	}

	public void oneClickCastSpell(WidgetInfo spellWidget, MenuEntry targetMenu, Rectangle targetBounds, long sleepLength)
	{
		menu.setEntry(targetMenu, false);
		menu.setSelectedSpell(spellWidget);
		mouse.delayMouseClick(targetBounds, sleepLength);
	}

	public void setCombatStyle(int index)
	{
		MenuEntry entry = interfaceUtils.getAttackStyleMenuEntry(index);
		doActionClientTick(entry, new Point(0, 0), 0);
	}

	public void sendGameMessage(String message)
	{
		String chatMessage = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(message)
			.build();

		chatMessageManager
			.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build());
	}

	public OSBGrandExchangeResult getOSBItem(int itemId)
	{
		log.debug("Looking up OSB item price {}", itemId);
		osbGrandExchangeClient.lookupItem(itemId)
			.subscribe(
				(osbresult) ->
				{
					if (osbresult != null && osbresult.getOverall_average() > 0)
					{
						osbGrandExchangeResult = osbresult;
					}
				},
				(e) -> log.debug("Error getting price of item {}", itemId, e)
			);
		if (osbGrandExchangeResult != null)
		{
			return osbGrandExchangeResult;
		}
		else
		{
			return null;
		}
	}

	//Ganom's
	public int[] stringToIntArray(String string)
	{
		return Arrays.stream(string.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
	}

	public List<Integer> stringToIntList(String string)
	{
		return (string == null || string.trim().equals("")) ? List.of(0) :
			Arrays.stream(string.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
	}

	public boolean pointOnScreen(Point check)
	{
		int x = check.getX(), y = check.getY();
		return x > client.getViewportXOffset() && x < client.getViewportWidth()
			&& y > client.getViewportYOffset() && y < client.getViewportHeight();
	}


	/**
	 * RANDOM EVENT FUNCTIONS
	 */
	public void setRandomEvent(boolean random)
	{
		randomEvent = random;
	}

	public boolean getRandomEvent()
	{
		return randomEvent;
	}

	/**
	 * Pauses execution for a random amount of time between two values.
	 *
	 * @param minSleep The minimum time to sleep.
	 * @param maxSleep The maximum time to sleep.
	 * @see #sleep(int)
	 */

	public static void sleep(int minSleep, int maxSleep)
	{
		sleep(CalculationUtils.random(minSleep, maxSleep));
	}

	/**
	 * Pauses execution for a given number of milliseconds.
	 *
	 * @param toSleep The time to sleep in milliseconds.
	 */
	public static void sleep(int toSleep)
	{
		try
		{
			long start = System.currentTimeMillis();
			Thread.sleep(toSleep);

			// Guarantee minimum sleep
			long now;
			while (start + toSleep > (now = System.currentTimeMillis()))
			{
				Thread.sleep(start + toSleep - now);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static void sleep(long toSleep)
	{
		try
		{
			long start = System.currentTimeMillis();
			Thread.sleep(toSleep);

			// Guarantee minimum sleep
			long now;
			while (start + toSleep > (now = System.currentTimeMillis()))
			{
				Thread.sleep(start + toSleep - now);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		action.onClientTick(event);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		tickActions = 0;
		action.onGameTick(event);
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getOpcode() == MenuOpcode.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
			event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
		{
			return;
		}
		if (menu.entry != null)
		{
			client.setLeftClickMenuEntry(menu.entry);
			if (menu.modifiedMenu)
			{
				event.setModified();
			}
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getOpcode() == MenuOpcode.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
			event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
		{
			//Either logging out or world-hopping which is handled by 3rd party plugins so let them have priority
			log.info("Received world-hop/login related click. Giving them priority");
			menu.entry = null;
			return;
		}
		if (menu.entry != null)
		{
			tickActions++;
			event.consume();
			log.debug("Actions this game tick: {}", tickActions);
			if (menu.consumeClick)
			{
				log.info("Consuming a click and not sending anything else");
				menu.consumeClick = false;
				return;
			}
			if (menu.entry.getOption().equals("Walk here"))
			{
				log.info("Walk action: {} {}", walk.coordX, walk.coordY);
				walk.walkTile(walk.coordX, walk.coordY);
				walk.walkAction = false;
				menu.entry = null;
				return;
			}
			if (menu.modifiedMenu)
			{
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(menu.modifiedItemIndex);
				client.setSelectedItemID(menu.modifiedItemID);
				log.debug("doing a Modified MOC, mod ID: {}, mod index: {}, param1: {}", menu.modifiedItemID,
					menu.modifiedItemIndex, menu.entry.getParam1());
				client.invokeMenuAction(menu.entry.getOption(), menu.entry.getTarget(), menu.entry.getIdentifier(),
					menu.modifiedOpCode, menu.entry.getParam0(), menu.entry.getParam1());
				menu.modifiedMenu = false;
			}
			else
			{
				client.invokeMenuAction(menu.entry.getOption(), menu.entry.getTarget(), menu.entry.getIdentifier(),
					menu.entry.getOpcode(), menu.entry.getParam0(), menu.entry.getParam1());
			}
			menu.entry = null;
		}
		else
		{
			if (!event.isConsumed() && !action.delayedActions.isEmpty() && event.getOption().equals("Walk here"))
			{
				log.info("Consuming a NULL MOC event");
				event.consume();
			}
		}
	}
}
