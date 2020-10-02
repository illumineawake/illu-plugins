package net.runelite.client.plugins.botutils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Singleton
public class PlayerUtils
{
	@Inject
	private Client client;

	@Inject
	private BotUtils utils;

	@Inject
	private MouseUtils mouse;

	@Inject
	private ExecutorService executorService;

	private int nextRunEnergy;

	//Not very accurate, recommend using isMoving(LocalPoint lastTickLocalPoint)
	public boolean isMoving()
	{
		int camX = client.getCameraX2();
		int camY = client.getCameraY2();
		utils.sleep(25);
		return (camX != client.getCameraX() || camY != client.getCameraY()) && client.getLocalDestinationLocation() != null;
	}

	public boolean isMoving(LocalPoint lastTickLocalPoint)
	{
		return !client.getLocalPlayer().getLocalLocation().equals(lastTickLocalPoint);
	}

	public boolean isInteracting()
	{
		utils.sleep(25);
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
		assert client.isClientThread();
		if (nextRunEnergy < minEnergy || nextRunEnergy > minEnergy + randMax)
		{
			nextRunEnergy = utils.getRandomIntBetweenRange(minEnergy, minEnergy + utils.getRandomIntBetweenRange(0, randMax));
		}
		if (client.getEnergy() > nextRunEnergy ||
				client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0)
		{
			if (drinkStamPot(15 + utils.getRandomIntBetweenRange(0, 30)))
			{
				return;
			}
			if (!isRunEnabled())
			{
				nextRunEnergy = 0;
				Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);
				if (runOrb != null)
				{
					enableRun(runOrb.getBounds());
				}
			}
		}
	}

	public void handleRun(int minEnergy, int randMax, int potEnergy)
	{
		assert client.isClientThread();
		if (nextRunEnergy < minEnergy || nextRunEnergy > minEnergy + randMax)
		{
			nextRunEnergy = utils.getRandomIntBetweenRange(minEnergy, minEnergy + utils.getRandomIntBetweenRange(0, randMax));
		}
		if (client.getEnergy() > (minEnergy + utils.getRandomIntBetweenRange(0, randMax)) ||
				client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0)
		{
			if (drinkStamPot(potEnergy))
			{
				return;
			}
			if (!isRunEnabled())
			{
				nextRunEnergy = 0;
				Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);
				if (runOrb != null)
				{
					enableRun(runOrb.getBounds());
				}
			}
		}
	}

	public void enableRun(Rectangle runOrbBounds)
	{
		log.info("enabling run");
		executorService.submit(() ->
		{
			utils.targetMenu = new MenuEntry("Toggle Run", "", 1, 57, -1, 10485782, false);
			mouse.delayMouseClick(runOrbBounds, utils.getRandomIntBetweenRange(10, 250));
		});
	}

	//Checks if Stamina enhancement is active and if stamina potion is in inventory
	public WidgetItem shouldStamPot(int energy)
	{
		if (!getInventoryItems(java.util.List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4)).isEmpty()
				&& client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0 && client.getEnergy() < energy && !utils.isBankOpen())
		{
			return getInventoryWidgetItem(List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3,
					ItemID.STAMINA_POTION4, ItemID.ENERGY_POTION1, ItemID.ENERGY_POTION2, ItemID.ENERGY_POTION3, ItemID.ENERGY_POTION4));
		}
		else
		{
			return null;
		}
	}

	public boolean drinkStamPot(int energy)
	{
		WidgetItem staminaPotion = shouldStamPot(energy);
		if (staminaPotion != null)
		{
			log.info("using stamina potion");
			utils.targetMenu = new MenuEntry("", "", staminaPotion.getId(), MenuOpcode.ITEM_FIRST_OPTION.getId(), staminaPotion.getIndex(), 9764864, false);
			mouse.delayMouseClick(staminaPotion.getCanvasBounds(), utils.getRandomIntBetweenRange(5, 200));
			return true;
		}
		return false;
	}
}
