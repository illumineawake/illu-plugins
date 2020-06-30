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
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.http.api.ge.GrandExchangeClient;
import net.runelite.http.api.osbuddy.OSBGrandExchangeClient;
import net.runelite.http.api.osbuddy.OSBGrandExchangeResult;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "BotUtils",
	type = PluginType.UTILITY,
	description = "Illumine bot utilities",
	hidden = false
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

	@Inject
	private GrandExchangeClient grandExchangeClient;

	MenuEntry targetMenu;
	protected static final java.util.Random random = new java.util.Random();


	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
		new ThreadPoolExecutor.DiscardPolicy());
	private OSBGrandExchangeResult osbGrandExchangeResult;
	private static final OSBGrandExchangeClient OSBCLIENT = new OSBGrandExchangeClient();
	public boolean randomEvent;
	public boolean iterating;

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
		//keyEvent(400, key);
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
		//assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		click(point);
	}

	public void click(Point p)
	{
		//assert !client.isClientThread();

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
		//assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		moveClick(point);
	}

	public void moveClick(Point p)
	{
		//assert !client.isClientThread();

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
		//assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		moveClick(point);
	}

	public void moveMouseEvent(Point p)
	{
		//assert !client.isClientThread();

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
	 * PLAYER FUNCTIONS
	 */

	//Not very accurate, recommend using isMovingTick()
	public boolean isMoving()
	{
		int camX = client.getCameraX2();
		int camY = client.getCameraY2();
		sleep(25);
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

	public boolean isAnimating()
	{
		return client.getLocalPlayer().getAnimation() != -1;
	}

	public boolean isRunEnabled()
	{
		return client.getVarpValue(173) == 1;
	}

	//enables run if below given minimum energy with random positive variation
	public void handleRun(int minEnergy, int randMax)
	{
		if (client.getEnergy() > (minEnergy + getRandomIntBetweenRange(0, randMax)))
		{

			WidgetItem staminaPotion = shouldStamPot();
			if (staminaPotion != null)
			{
				log.info("using stamina potion");
				targetMenu = new MenuEntry("", "", staminaPotion.getId(), MenuOpcode.ITEM_FIRST_OPTION.getId(), staminaPotion.getIndex(), 9764864, false);
				clickRandomPointCenter(-100, 100);
				sleep(10, 50);
				return;
			}
			if (!isRunEnabled())
			{
				log.info("enabling run");
				targetMenu = new MenuEntry("Toggle Run", "", 1, 57, -1, 10485782, false);
				clickRandomPointCenter(-100, 100);
			}
		}
	}

	//Checks if Stamina enhancement is active and if stamina potion is in inventory
	public WidgetItem shouldStamPot()
	{
		if (!getItems(List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4)).isEmpty()
			&& client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0 && client.getEnergy() < 15 + getRandomIntBetweenRange(0, 30) && !isBankOpen())
		{
			return getInventoryWidgetItem(List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4));
		}
		else
		{
			return null;
		}
	}

	/**
	 * INVENTORY FUNCTIONS
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
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			return 28 - inventoryWidget.getWidgetItems().size();
		}
		else
		{
			return -1;
		}
	}

	//Requires inventory visible
	/*public List<WidgetItem> getItems(int... itemIDs)
	{
		assert client.isClientThread();

		return new InventoryWidgetItemQuery()
			.idEquals(itemIDs)
			.result(client)
			.list;
	}*/

	//Doesn't require a visible inventory
	public List<WidgetItem> getItems(Set<Integer> ids)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		List<WidgetItem> matchedItems = new ArrayList<>();

		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (ids.contains(item.getId()))
				{
					matchedItems.add(item);
				}
			}
			return matchedItems;
		}
		return null;
	}

	public List<WidgetItem> getItems(List<Integer> ids)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		List<WidgetItem> matchedItems = new ArrayList<>();

		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (ids.contains(item.getId()))
				{
					matchedItems.add(item);
				}
			}
			return matchedItems;
		}
		return null;
	}

	/*//Requires inventory visible
	public List<WidgetItem> getItems(Set<Integer> itemIDs)
	{
		assert client.isClientThread();

		return new InventoryWidgetItemQuery()
			.idEquals(itemIDs)
			.result(client)
			.list;
	}*/

	public Collection<WidgetItem> getAllInventoryItems()
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			return inventoryWidget.getWidgetItems();
		}
		return null;
	}

	//Requires inventory visible and returns null if not
	/*public WidgetItem getInventoryWidgetItem(int itemID)
	{
		assert client.isClientThread();

		return new InventoryWidgetItemQuery()
			.idEquals(itemID)
			.result(client)
			.first();
	}*/

	//Inventory doesn't need to be visible, will return null if not wrapped
	public WidgetItem getInventoryWidgetItem(int id)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (item.getId() == id)
				{
					return item;
				}
			}
		}
		return null;
	}

	//Inventory doesn't need to be visible, will return null if not wrapped - use List.of(Ids) in parameter
	public WidgetItem getInventoryWidgetItem(List<Integer> ids)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (ids.contains(item.getId()))
				{
					return item;
				}
			}
		}
		return null;
	}

	//Requires inventory visible and returns null if not
	/*public WidgetItem getInventoryWidgetItem(int... ids)
	{
		assert client.isClientThread();

		return new InventoryWidgetItemQuery()
			.idEquals(ids)
			.result(client)
			.first();
	}*/

	//Works without inventory being visible and doesn't NPE
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

	//Works without inventory being visible and doesn't NPE
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

	//UNTESTED
	public boolean inventoryContains(Set<Integer> itemIds)
	{
		if (client.getItemContainer(InventoryID.INVENTORY) == null)
		{
			return false;
		}
		return getItems(itemIds).size() > 0;
	}

	//Works without inventory being visible and doesn't NPE
	public boolean inventoryContains(int itemID, int minStackAmount)
	{
		if (client.getItemContainer(InventoryID.INVENTORY) == null)
		{
			return false;
		}
		Item item = new InventoryItemQuery(InventoryID.INVENTORY)
			.idEquals(itemID)
			.result(client)
			.first();

		return item != null && item.getQuantity() >= minStackAmount;
	}

	public void dropAll(List<Integer> ids)
	{
		if (isBankOpen() || isDepositBoxOpen())
		{
			log.info("can't drop item, bank is open");
			return;
		}
		Collection<WidgetItem> inventoryItems = getAllInventoryItems();
		executorService.submit(() ->
		{
			try
			{
				iterating = true;
				for (WidgetItem item : inventoryItems)
				{
					if (ids.contains(item.getId())) //6512 is empty widget slot
					{
						log.info("dropping item: " + item.getId());
						depositAllOfItem(item);
						sleep(80, 170);
						targetMenu = new MenuEntry("", "", item.getId(), MenuOpcode.ITEM_DROP.getId(), item.getIndex(), 9764864, false);
						clickRandomPointCenter(-100, 100);
					}
				}
				iterating = false;
			}
			catch (Exception e)
			{
				iterating = false;
				e.printStackTrace();
			}
		});
	}

	/**
	 * BANKING FUNCTIONS
	 */

	public boolean isDepositBoxOpen()
	{
		return client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER) != null;
	}

	public boolean isBankOpen()
	{
		return client.getItemContainer(InventoryID.BANK) != null;
	}

	public void closeBank()
	{
		if (!isBankOpen())
		{
			return;
		}
		targetMenu = new MenuEntry("", "", 1, MenuOpcode.CC_OP.getId(), 11, 786434, false); //close bank
		clickRandomPointCenter(-100, 100);
	}

	//doesn't NPE
	public boolean bankContains(String itemName)
	{
		if (isBankOpen())
		{
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

			for (Item item : bankItemContainer.getItems())
			{
				if (itemManager.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName))
				{
					return true;
				}
			}
		}
		return false;
	}

	//doesn't NPE
	public boolean bankContainsAnyOf(int... ids)
	{
		if (isBankOpen())
		{
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

			return new BankItemQuery().idEquals(ids).result(client).size() > 0;
		}
		return false;
	}

	//Placeholders count as being found
	public boolean bankContains(String itemName, int minStackAmount)
	{
		if (isBankOpen())
		{
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

			for (Item item : bankItemContainer.getItems())
			{
				if (itemManager.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName) && item.getQuantity() >= minStackAmount)
				{
					return true;
				}
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

			return bankItem != null && bankItem.getQuantity() >= minStackAmount;
		}
		return false;
	}

	//doesn't NPE
	public Widget getBankItemWidget(int id)
	{
		if (!isBankOpen())
		{
			return null;
		}

		WidgetItem bankItem = new BankItemQuery().idEquals(id).result(client).first();
		if (bankItem != null)
		{
			return bankItem.getWidget();
		}
		else
		{
			return null;
		}
	}

	//doesn't NPE
	public Widget getBankItemWidgetAnyOf(int... ids)
	{
		if (!isBankOpen())
		{
			return null;
		}

		WidgetItem bankItem = new BankItemQuery().idEquals(ids).result(client).first();
		if (bankItem != null)
		{
			return bankItem.getWidget();
		}
		else
		{
			return null;
		}
	}

	public void depositAll()
	{
		if (!isBankOpen() && !isDepositBoxOpen())
		{
			return;
		}
		if (isDepositBoxOpen())
		{
			targetMenu = new MenuEntry("", "", 1, MenuOpcode.CC_OP.getId(), -1, 12582916, false); //deposit all in bank interface
			clickRandomPointCenter(-100, 100);
			return;
		}
		else
		{
			targetMenu = new MenuEntry("", "", 1, MenuOpcode.CC_OP.getId(), -1, 786473, false); //deposit all in bank interface
			clickRandomPointCenter(-100, 100);
		}
	}

	public void depositAllExcept(List<Integer> ids)
	{
		if (!isBankOpen())
		{
			return;
		}
		Collection<WidgetItem> inventoryItems = getAllInventoryItems();
		List<Integer> depositedItems = new ArrayList<>();
		executorService.submit(() ->
		{
			try
			{
				iterating = true;
				for (WidgetItem item : inventoryItems)
				{
					if (!ids.contains(item.getId()) && item.getId() != 6512 && !depositedItems.contains(item.getId())) //6512 is empty widget slot
					{
						log.info("depositing item: " + item.getId());
						depositAllOfItem(item);
						sleep(80, 170);
						depositedItems.add(item.getId());
					}
				}
				iterating = false;
				depositedItems.clear();
			}
			catch (Exception e)
			{
				iterating = false;
				e.printStackTrace();
			}
		});
	}

	public void depositAllOfItem(WidgetItem itemWidget)
	{
		if (!isBankOpen())
		{
			return;
		}
		targetMenu = new MenuEntry("", "", 2, MenuOpcode.CC_OP.getId(), itemWidget.getIndex(), 983043, false);
		clickRandomPointCenter(-100, 100);
	}

	public void withdrawAllItem(Widget bankItemWidget)
	{
		targetMenu = new MenuEntry("Withdraw-All", "", 1, MenuOpcode.CC_OP.getId(), bankItemWidget.getIndex(), 786444, false);
		clickRandomPointCenter(-100, 100);
	}

	public void withdrawItem(Widget bankItemWidget)
	{
		targetMenu = new MenuEntry("", "", 2, MenuOpcode.CC_OP.getId(), bankItemWidget.getIndex(), 786444, false);
		clickRandomPointCenter(-100, 100);
		sleep(50, 250);
	}

	/**
	 * GRAND EXCHANGE FUNCTIONS
	 */
	/*public OSBGrandExchangeResult getOSBItem(int itemId){
		log.debug("Looking up OSB item price {}", itemId);
		executorService.submit(() ->
		{
			OSBCLIENT.lookupItem(itemId)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.single())
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
		});
		sleep(100);
		if (osbGrandExchangeResult != null)
			return osbGrandExchangeResult;
		else
			return null;
	}*/
	public OSBGrandExchangeResult getOSBItem(int itemId)
	{
		log.debug("Looking up OSB item price {}", itemId);
		OSBCLIENT.lookupItem(itemId)
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

	public void sleep(long toSleep)
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

	//Ganom's function, generates a random number allowing for curve and weight
	public long randomDelay(boolean weightedDistribution, int min, int max, int deviation, int target)
	{
		if (weightedDistribution)
		{
			/* generate a gaussian random (average at 0.0, std dev of 1.0)
			 * take the absolute value of it (if we don't, every negative value will be clamped at the minimum value)
			 * get the log base e of it to make it shifted towards the right side
			 * invert it to shift the distribution to the other end
			 * clamp it to min max, any values outside of range are set to min or max */
			return (long) clamp((-Math.log(Math.abs(random.nextGaussian()))) * deviation + target, min, max);
		}
		else
		{
			/* generate a normal even distribution random */
			return (long) clamp(Math.round(random.nextGaussian() * deviation + target), min, max);
		}
	}

	private double clamp(double val, int min, int max)
	{
		return Math.max(min, Math.min(max, val));
	}

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

   /*private MenuEntry handleMenuEntry(MenuEntry menuEntry) {
        if (targetMenu != null)
        {
            if (randomEvent)
            {
                return;
            }
        } else
        {
            targetMenu = menuEntry;
        }
    }*/

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		//sendGameMessage("do click here in event - utils");
		if (targetMenu != null)
		{
			event.setMenuEntry(targetMenu);
		}
		targetMenu = null;
	}

}