/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.autoclickillumine;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "Auto Clicker Illumine",
	enabledByDefault = false,
	type = PluginType.UTILITY
)
@Slf4j
public class AutoClickIllumine extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private AutoClickIllumineConfig config;

	@Inject
	private AutoClickIllumineOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private iUtils extUtils;

	private ExecutorService executorService;
	private Point point;
	private Point randPoint;
	private Random random;
	private NPC npcTest;
	private boolean run;
	private ItemManager itemManager;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private boolean flash;

	@Provides
	AutoClickIllumineConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AutoClickIllumineConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		keyManager.registerKeyListener(hotkeyListener);
		executorService = Executors.newSingleThreadExecutor();
		random = new Random();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		keyManager.unregisterKeyListener(hotkeyListener);
		executorService.shutdown();
		random = null;
	}

	private List<WidgetItem> getFish() //Inventory fish items
	{
		return getItems(11328, 11330, 11332, 15491, 15492, 15493);
	}

	private List<WidgetItem> getDrop()
	{
		return getItems(11328, 11330, 11332, 15491, 15492, 15493, 13648, 13649, 13650, 13651, 15484, 15485, 15486, 15487, 23129, 23130);
	}

	private List<WidgetItem> getWine()
	{
		return getItems(1993);
	}

	public List<WidgetItem> getInventoryItems(int... itemIds)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		ArrayList<Integer> itemIDs = new ArrayList<>();
		List<WidgetItem> list = new ArrayList<>();

		for (int i : itemIds)
		{
			itemIDs.add(i);
		}

		for (WidgetItem i : inventoryWidget.getWidgetItems())
		{
			if (itemIDs.contains(i.getId()))
			{
				list.add(i);
			}

		}

		return list;
	}

	public List<WidgetItem> getAllInventoryItems()
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		ArrayList<Integer> itemIDs = new ArrayList<>();
		List<WidgetItem> list = new ArrayList<>();

		for (WidgetItem item : inventoryWidget.getWidgetItems())
		{
			log.info(String.valueOf(item.getId()));
			list.add(item);
		}
		return list;
	}

	public void clickInventoryItems(List<WidgetItem> list)
	{
		if (list.isEmpty())
		{
			return;
		}

		for (WidgetItem item : list)
		{
			clickInventoryItem(item);
		}
	}

	public void clickInventoryItem(WidgetItem item)
	{
		if (client.getWidget(WidgetInfo.INVENTORY).isHidden())
		{
			//this.keyPress(tabUtils.getTabHotkey(Tab.INVENTORY)); not sure how to do this at the moment
			log.debug("Inventory is not open");
		}
		if (item != null)
		{
			String name = Integer.toString(item.getId());
			if (itemManager.getItemDefinition(item.getId()) != null)
			{
				name = itemManager.getItemDefinition((item.getId())).getName();

			}

			log.debug("Grabbing getCanvasBounds of " + name);

			if (item.getCanvasBounds() != null)
			{
				clickWidgetItem(item);
			}
			else
			{
				log.debug("Could not find getCanvasBounds of " + name);
			}
		}
	}

	public void clickWidgetItem(WidgetItem item)
	{
		//clickPoint(getWidgetItemClickPoint(item)); flexo runelite-bot driven
		extUtils.click(item.getCanvasBounds());
	}

	private List<WidgetItem> getItems(int... itemIds)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		ArrayList<Integer> itemIDs = new ArrayList<>();
		for (int i : itemIds)
		{
			itemIDs.add(i);
		}

		List<WidgetItem> listToReturn = new ArrayList<>();

		for (WidgetItem item : inventoryWidget.getWidgetItems())
		{
			if (itemIDs.contains(item.getId()))
			{
				listToReturn.add(item);
			}
		}

		return listToReturn;
	}


	private HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggle())
	{
		@Override
		public void hotkeyPressed()
		{
			run = !run;

			if (!run)
			{
				return;
			}

			point = client.getMouseCanvasPosition();
			randPoint = new Point(point.getX() + extUtils.getRandomIntBetweenRange(-3, 3), point.getY() + extUtils.getRandomIntBetweenRange(-5, 5));
			log.info(getWine().get(0).toString());

			executorService.submit(() ->
			{
				while (run)
				{
					if (client.getGameState() != GameState.LOGGED_IN)
					{
						run = false;
						break;
					}

					if (checkHitpoints() || checkInventory())
					{
						//run = false;
						if (config.flash())
						{
							setFlash(true);
						}
						try
						{
							Thread.sleep(extUtils.getRandomIntBetweenRange(208, 501));
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						if (getWine().isEmpty())
						{
							log.info("We're out of wine");
							break;
						}
						else
						{
							log.info("lets eat");
							extUtils.moveClick(getWine().get(0).getCanvasBounds());
							randPoint = new Point(point.getX() + extUtils.getRandomIntBetweenRange(-10, 10), point.getY() + extUtils.getRandomIntBetweenRange(-10, 20)); // generate a slightly randomized clickpoint
							log.info("new click point: " + randPoint.toString());
						}
						try
						{
							Thread.sleep(extUtils.getRandomIntBetweenRange(600, 1500));
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						//break;
					}

					extUtils.moveClick(randPoint);

					try
					{
						Thread.sleep(randomDelay());
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	};

	private long randomDelay()
	{
		if (config.weightedDistribution())
		{
			/* generate a gaussian random (average at 0.0, std dev of 1.0)
			 * take the absolute value of it (if we don't, every negative value will be clamped at the minimum value)
			 * get the log base e of it to make it shifted towards the right side
			 * invert it to shift the distribution to the other end
			 * clamp it to min max, any values outside of range are set to min or max */
			return (long) clamp(
				(-Math.log(Math.abs(random.nextGaussian()))) * config.deviation() + config.target()
			);
		}
		else
		{
			/* generate a normal even distribution random */
			return (long) clamp(
				Math.round(random.nextGaussian() * config.deviation() + config.target())
			);
		}
	}

	private double clamp(double val)
	{
		return Math.max(config.min(), Math.min(config.max(), val));
	}

	private boolean checkHitpoints()
	{
		if (!config.autoDisableHp())
		{
			return false;
		}
		return client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.hpThreshold();
	}

	private boolean checkInventory()
	{
		if (!config.autoDisableInv())
		{
			return false;
		}
		final Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		return inventoryWidget.getWidgetItems().size() == 28;
	}

	private static int randomDelay(int min, int max)
	{
		Random rand = new Random();
		int n = rand.nextInt(max) + 1;
		if (n < min)
		{
			n += min;
		}
		return n;
	}
}
