/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.botutils;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigPanelItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "BotUtils",
	type = PluginType.UTILITY,
	hidden = true
)
@Slf4j
@SuppressWarnings("unused")
@Singleton
public class BotUtils extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ItemManager itemManager;

	protected static final java.util.Random random = new java.util.Random();


	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
		new ThreadPoolExecutor.DiscardPolicy());

	public boolean randomEvent;

	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{

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

	public int[] stringToIntArray(String string)
	{
		return Arrays.stream(string.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
	}

	@Nullable
	public GameObject findNearestGameObject(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist, int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.idEquals(ids)
			.isWithinDistance(worldPoint, dist)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist, Set<Integer> ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.idEquals(ids)
			.isWithinDistance(worldPoint, dist)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public NPC findNearestNpc(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new NPCQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public WallObject findNearestWallObject(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new WallObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public DecorativeObject findNearestDecorObject(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new DecorativeObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public GroundObject findNearestGroundObject(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GroundObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	public List<GameObject> getGameObjects(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new GameObjectQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	public List<NPC> getNPCs(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new NPCQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	public List<WallObject> getWallObjects(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new WallObjectQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	public List<DecorativeObject> getDecorObjects(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new DecorativeObjectQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	public List<GroundObject> getGroundObjects(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new GroundObjectQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	@Nullable
	public TileObject findNearestObject(int... ids)
	{
		GameObject gameObject = findNearestGameObject(ids);

		if (gameObject != null)
		{
			return gameObject;
		}

		WallObject wallObject = findNearestWallObject(ids);

		if (wallObject != null)
		{
			return wallObject;
		}
		DecorativeObject decorativeObject = findNearestDecorObject(ids);

		if (decorativeObject != null)
		{
			return decorativeObject;
		}

		return findNearestGroundObject(ids);
	}

	public List<Widget> getEquippedItems(int[] itemIds)
	{
		assert client.isClientThread();

		Widget equipmentWidget = client.getWidget(WidgetInfo.EQUIPMENT);

		List<Integer> equippedIds = new ArrayList<>();

		for (int i : itemIds)
		{
			equippedIds.add(i);
		}

		List<Widget> equipped = new ArrayList<>();

		if (equipmentWidget.getStaticChildren() != null)
		{
			for (Widget widgets : equipmentWidget.getStaticChildren())
			{
				for (Widget items : widgets.getDynamicChildren())
				{
					if (equippedIds.contains(items.getItemId()))
					{
						equipped.add(items);
					}
				}
			}
		}
		else
		{
			log.error("Children is Null!");
		}

		return equipped;
	}

	public int getTabHotkey(Tab tab)
	{
		assert client.isClientThread();

		final int var = client.getVarbitValue(client.getVarps(), tab.getVarbit());
		final int offset = 111;

		switch (var)
		{
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
				return var + offset;
			case 13:
				return 27;
			default:
				return -1;
		}
	}

	public WidgetInfo getSpellWidgetInfo(String spell)
	{
		assert client.isClientThread();
		return Spells.getWidget(spell);
	}

	public WidgetInfo getPrayerWidgetInfo(String spell)
	{
		assert client.isClientThread();
		return PrayerMap.getWidget(spell);
	}

	public Widget getSpellWidget(String spell)
	{
		assert client.isClientThread();
		return client.getWidget(Spells.getWidget(spell));
	}

	public Widget getPrayerWidget(String spell)
	{
		assert client.isClientThread();
		return client.getWidget(PrayerMap.getWidget(spell));
	}

	public boolean pointOnScreen(Point check)
	{
		int x = check.getX(), y = check.getY();
		return x > client.getViewportXOffset() && x < client.getViewportWidth()
			&& y > client.getViewportYOffset() && y < client.getViewportHeight();
	}

	/**
	 * This method must be called on a new
	 * thread, if you try to call it on
	 * {@link net.runelite.client.callback.ClientThread}
	 * it will result in a crash/desynced thread.
	 */
	public void typeString(String string)
	{
		assert !client.isClientThread();

		for (char c : string.toCharArray())
		{
			pressKey(c);
		}
	}

	public void pressKey(char key)
	{
		keyEvent(401, key);
		keyEvent(402, key);
		keyEvent(400, key);
	}

	public void pressKey(int key)
	{
		keyEvent(401, key);
		keyEvent(402, key);
		keyEvent(400, key);
	}

	private void keyEvent(int id, char key)
	{
		KeyEvent e = new KeyEvent(
			client.getCanvas(), id, System.currentTimeMillis(),
			0, KeyEvent.VK_UNDEFINED, key
		);

		client.getCanvas().dispatchEvent(e);
	}

	private void keyEvent(int id, int key)
	{
		KeyEvent e = new KeyEvent(
				client.getCanvas(), id, System.currentTimeMillis(),
				0, key, KeyEvent.CHAR_UNDEFINED
		);

		client.getCanvas().dispatchEvent(e);
	}

	/*
	public void sendKeyEvent(KeyEvent key)
	{
		KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER);
		this.client.getCanvas().dispatchEvent(keyPress);
		KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER);
		this.client.getCanvas().dispatchEvent(keyRelease);
		KeyEvent keyTyped = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER);
		this.client.getCanvas().dispatchEvent(keyTyped);
	}*/

	/**
	 * This method must be called on a new
	 * thread, if you try to call it on
	 * {@link net.runelite.client.callback.ClientThread}
	 * it will result in a crash/desynced thread.
	 */
	public void click(Rectangle rectangle)
	{
		assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		click(point);
	}

	public void click(Point p)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
			mouseEvent(501, point);
			mouseEvent(502, point);
			mouseEvent(500, point);
			return;
		}
		mouseEvent(501, p);
		mouseEvent(502, p);
		mouseEvent(500, p);
	}

	public void moveClick(Rectangle rectangle)
	{
		assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		moveClick(point);
	}

	public void moveClick(Point p)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
			mouseEvent(504, point);
			mouseEvent(505, point);
			mouseEvent(503, point);
			mouseEvent(501, point);
			mouseEvent(502, point);
			mouseEvent(500, point);
			return;
		}
		mouseEvent(504, p);
		mouseEvent(505, p);
		mouseEvent(503, p);
		mouseEvent(501, p);
		mouseEvent(502, p);
		mouseEvent(500, p);
	}

	public Point getClickPoint(@NotNull Rectangle rect)
	{
		final int x = (int) (rect.getX() + getRandomIntBetweenRange((int) rect.getWidth() / 6 * -1, (int) rect.getWidth() / 6) + rect.getWidth() / 2);
		final int y = (int) (rect.getY() + getRandomIntBetweenRange((int) rect.getHeight() / 6 * -1, (int) rect.getHeight() / 6) + rect.getHeight() / 2);

		return new Point(x, y);
	}

	public void moveMouseEvent(Rectangle rectangle)
	{
		assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		moveClick(point);
	}

	public void moveMouseEvent(Point p)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
			mouseEvent(504, point);
			mouseEvent(505, point);
			mouseEvent(503, point);
			return;
		}
		mouseEvent(504, p);
		mouseEvent(505, p);
		mouseEvent(503, p);
	}

	public int getRandomIntBetweenRange(int min, int max)
	{
		return (int) ((Math.random() * ((max - min) + 1)) + min);
	}

	private void mouseEvent(int id, @NotNull Point point)
	{
		MouseEvent e = new MouseEvent(
			client.getCanvas(), id,
			System.currentTimeMillis(),
			0, point.getX(), point.getY(),
			1, false, 1
		);

		client.getCanvas().dispatchEvent(e);
	}

	public void clickRandomPoint(int min, int max)
	{
		Point point = new Point(getRandomIntBetweenRange(min, max), getRandomIntBetweenRange(min, max));
		click(point);
	}

	public void clickRandomPointCenter(int min, int max)
	{
		Point point = new Point(client.getCenterX() + getRandomIntBetweenRange(min, max), client.getCenterY() + getRandomIntBetweenRange(min, max));
		click(point);
	}

	/**
	 *
	 *  PLAYER FUNCTIONS
	 *
	 */

	//Not very accurate, recommend using isMovingTick()
	public boolean isMoving()
	{
		int camX = client.getCameraX2();
		int camY = client.getCameraY2();
		sleep(25);
		//should this be all OR's instead of AND?
		//log.info("CamX2 (before): " + camX + " CamX after: " + client.getCameraX() + "CamY2 (before): " + camY + " CamY after: " + client.getCameraY());
		return (camX != client.getCameraX() || camY != client.getCameraY()) && client.getLocalDestinationLocation() != null;
	}

	public boolean isMoving(LocalPoint lastTickLocalPoint)
	{
		return !client.getLocalPlayer().getLocalLocation().equals(lastTickLocalPoint);
	}

	public boolean isInteracting()
	{
		sleep(25);
		return isMoving() || client.getLocalPlayer().getAnimation() != -1;
	}

	public boolean isRunEnabled()
	{
		return client.getVarpValue(173) == 1;
	}

	/**
	 *
	 *  INVENTORY FUNCTIONS
	 *
	 */

	public boolean inventoryFull()
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			return inventoryWidget.getWidgetItems().size() >= 28;
		}
		else
		{
			return false;
		}
	}

	public boolean inventoryEmpty()
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			return inventoryWidget.getWidgetItems().size() <= 0;
		}
		else
		{
			return false;
		}
	}

	public int getInventorySpace()
	{
		if (client.getItemContainer(InventoryID.INVENTORY) == null)
		{
			return 0;
		}
		return new InventoryItemQuery(InventoryID.INVENTORY)
			.idEquals(-1)
			.result(client)
			.size();
	}

	public List<WidgetItem> getItems(int... itemIDs)
	{
		assert client.isClientThread();

		return new InventoryWidgetItemQuery()
				.idEquals(itemIDs)
				.result(client)
				.list;
	}

	public List<WidgetItem> getItems(Set<Integer> itemIDs)
	{
		assert client.isClientThread();

		return new InventoryWidgetItemQuery()
				.idEquals(itemIDs)
				.result(client)
				.list;
	}

	public WidgetItem getInventoryWidgetItem(int itemID)
	{
		assert client.isClientThread();

		return new InventoryWidgetItemQuery()
				.idEquals(itemID)
				.result(client)
				.first();
	}

	public MenuEntry getInventoryItemMenu(ItemManager itemManager, String menuOption, int opcode)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				String[] menuActions = itemManager.getItemDefinition(item.getId()).getInventoryActions();
				for (String action : menuActions)
				{
					if (action != null && action.equals(menuOption))
					{
						MenuEntry menuEntry = new MenuEntry("", "", item.getId(), opcode, item.getIndex(), 9764864, false);
						return menuEntry;
					}
				}
			}
		}
		return null;
	}

	//untested
	public boolean inventoryContains(int itemID)
	{
		if (client.getItemContainer(InventoryID.INVENTORY) == null)
		{
			return false;
		}
		return new InventoryItemQuery(InventoryID.INVENTORY)
				.idEquals(itemID)
				.result(client)
				.size() >= 1;
	}

	public boolean inventoryContains(int itemID, int minStackAmount)
	{
		if (client.getItemContainer(InventoryID.INVENTORY) == null)
		{
			return false;
		}
		Item item =  new InventoryItemQuery(InventoryID.INVENTORY)
				.idEquals(itemID)
				.result(client)
				.first();

		return item != null && item.getQuantity() >= minStackAmount;
	}

	/**
	 *
	 *  BANKING FUNCTIONS
	 *
	 */
	public boolean isBankOpen()
	{
		return client.getItemContainer(InventoryID.BANK) != null;
	}

	public boolean bankContains(String itemName)
	{
		if (isBankOpen())
		{
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

			for (Item item: bankItemContainer.getItems())
			{
				if (itemManager.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName))
					return true;
			}
		}
		return false;
	}

	//This is untested
	public boolean bankContains(int... ids)
	{
		if (isBankOpen())
		{
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

			return new BankItemQuery().idEquals(ids).result(client).isEmpty();
		}
		return false;
	}

	public boolean bankContains(String itemName, int minStackAmount)
	{
		if (isBankOpen())
		{
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

			for (Item item: bankItemContainer.getItems())
			{
				if (itemManager.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName) && item.getQuantity() >= minStackAmount)
					return true;
			}
		}
		return false;
	}

	public boolean bankContains(int itemID, int minStackAmount)
	{
		if (isBankOpen())
		{
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);
			WidgetItem bankItem = new BankItemQuery().idEquals(itemID).result(client).first();

			return bankItem != null && bankItem.getQuantity() > minStackAmount;
		}
		return false;
	}

	public Widget getBankItemWidget(int... ids)
	{
		ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);
		if (!isBankOpen() || bankItemContainer == null)
		{
			return null;
		}

		return new BankItemQuery()
				.idEquals(ids)
				.result(client)
				.first().getWidget();
	}

	/**
	 *
	 *  RANDOM EVENT FUNCTIONS
	 *
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
	 *
	 *  UTILITY FUNCTIONS
	 *
	 */

	/**
	 * Pauses execution for a random amount of time between two values.
	 *
	 * @param minSleep The minimum time to sleep.
	 * @param maxSleep The maximum time to sleep.
	 * @see #sleep(int)
	 * @see #random(int, int)
	 */
	public void sleep(int minSleep, int maxSleep)
	{
		sleep(random(minSleep, maxSleep));
	}

	/**
	 * Pauses execution for a given number of milliseconds.
	 *
	 * @param toSleep The time to sleep in milliseconds.
	 */
	public void sleep(int toSleep)
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
	/*public void sleep(int toSleep)
	{
		executorService.submit(() ->
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
		});
	}*/

	/**
	 * Returns a random double with min as the inclusive lower bound and max as
	 * the exclusive upper bound.
	 *
	 * @param min The inclusive lower bound.
	 * @param max The exclusive upper bound.
	 * @return Random double min <= n < max.
	 */
	public static double random(double min, double max)
	{
		return Math.min(min, max) + random.nextDouble() * Math.abs(max - min);
	}

	/**
	 * Returns a random integer with min as the inclusive lower bound and max as
	 * the exclusive upper bound.
	 *
	 * @param min The inclusive lower bound.
	 * @param max The exclusive upper bound.
	 * @return Random integer min <= n < max.
	 */
	public static int random(int min, int max)
	{
		int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
	}

}
