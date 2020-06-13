/*
 * Copyright (c) 2018, Seth <Sethtroll3@gmail.com>
 * Copyright (c) 2019, Brandon White <bmwqg@live.com>
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
package net.runelite.client.plugins.blastfurnacebot;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import static net.runelite.api.NullObjectID.NULL_9092;
import net.runelite.api.ObjectID;
import static net.runelite.api.ObjectID.CONVEYOR_BELT;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import static net.runelite.client.plugins.blastfurnacebot.BlastFurnaceState.*;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.pf4j.Extension;

@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Blast Furnace - Illumine",
	description = "Illumine bot for Blast Furnace minigame",
	tags = {"minigame", "overlay", "skilling", "smithing"},
	type = PluginType.MINIGAME
)
@Singleton
public class BlastFurnaceBotPlugin extends Plugin
{
	private static final int BAR_DISPENSER = NULL_9092;
	private static final String FOREMAN_PERMISSION_TEXT = "Okay, you can use the furnace for ten minutes. Remember, you only need half as much coal as with a regular furnace.";

	@Getter(AccessLevel.PACKAGE)
	private GameObject conveyorBelt;

	@Getter(AccessLevel.PACKAGE)
	private GameObject barDispenser;

	private ForemanTimer foremanTimer;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BlastFurnaceOverlay overlay;

	@Inject
	private BlastFurnaceCofferOverlay cofferOverlay;

	@Inject
	private BlastFurnaceClickBoxOverlay clickBoxOverlay;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private BlastFurnaceBotConfig config;

	@Inject
	private BotUtils utils;

	BlastFurnaceState state;
	MenuEntry targetMenu;

	private int timeout = 0;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(cofferOverlay);
		overlayManager.add(clickBoxOverlay);
	}

	@Override
	protected void shutDown()
	{
		infoBoxManager.removeIf(ForemanTimer.class::isInstance);
		overlayManager.remove(overlay);
		overlayManager.remove(cofferOverlay);
		overlayManager.remove(clickBoxOverlay);
		conveyorBelt = null;
		barDispenser = null;
		foremanTimer = null;
	}

	private void openBank() {
		GameObject bankObject = utils.findNearestGameObject(26707);
		if (bankObject != null)
		{
			targetMenu = new MenuEntry("", "", bankObject.getId(), 3, bankObject.getSceneMinLocation().getX(), bankObject.getSceneMinLocation().getY(), true);
			utils.clickRandomPointCenter(-100, 100);
			timeout = 2;
		}
	}

	private BlastFurnaceState getState()
	{
		if(conveyorBelt == null || barDispenser == null)
		{
			return OUT_OF_AREA;
		}
		if (timeout > 0)
		{
			return TIMEOUT;
		}
		if (utils.isMoving())
		{
			timeout = 2;
			return MOVING;
		}
		if (!utils.isBankOpen())
		{
			//handleRun()
			if(!utils.getItems(ItemID.RUNITE_ORE).isEmpty()) //will update botutils to take a String contains, so can search if inventory has any Bars
			{ //INVENTORY CONTAINS BARS
				openBank();
				return OPENING_BANK;
			}
			if(client.getVar(Varbits.BAR_DISPENSER) > 0) //BARS IN FURNACE
			{
				if (utils.getInventorySpace() < 26)
				{
					openBank();
					return OPENING_BANK;
				}
				targetMenu = new MenuEntry("","",barDispenser.getId(), 3, barDispenser.getSceneMinLocation().getX(), barDispenser.getSceneMinLocation().getY(), false);
				utils.clickRandomPointCenter(-100, 100);
				timeout = 2;
				//WIDGET GROUP 270 is the collection window for bars, need to add to WidgetLoaded event
				return COLLECTING_BARS;
			}
		}

		if (utils.inventoryFull())
		{
			return BELT;
		}
		return null;
	}

	@Provides
	BlastFurnaceBotConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BlastFurnaceBotConfig.class);
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();

		switch (gameObject.getId())
		{
			case CONVEYOR_BELT:
				conveyorBelt = gameObject;
				break;

			case BAR_DISPENSER:
				barDispenser = gameObject;
				break;
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();

		switch (gameObject.getId())
		{
			case CONVEYOR_BELT:
				conveyorBelt = null;
				break;

			case BAR_DISPENSER:
				barDispenser = null;
				break;
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			conveyorBelt = null;
			barDispenser = null;
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		getState();
		Widget npcDialog = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
		if (npcDialog == null)
		{
			return;
		}

		// blocking dialog check until 5 minutes needed to avoid re-adding while dialog message still displayed
		boolean shouldCheckForemanFee = client.getRealSkillLevel(Skill.SMITHING) < 60
			&& (foremanTimer == null || Duration.between(Instant.now(), foremanTimer.getEndTime()).toMinutes() <= 5);

		if (shouldCheckForemanFee)
		{
			String npcText = Text.sanitizeMultilineText(npcDialog.getText());

			if (npcText.equals(FOREMAN_PERMISSION_TEXT))
			{
				infoBoxManager.removeIf(ForemanTimer.class::isInstance);

				foremanTimer = new ForemanTimer(this, itemManager);
				infoBoxManager.addInfoBox(foremanTimer);
			}
		}
	}
}
