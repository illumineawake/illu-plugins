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
package net.runelite.client.plugins.magiccaster;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.GameState;
import net.runelite.api.ChatMessageType;
import net.runelite.api.AnimationID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import static net.runelite.client.plugins.magiccaster.MagicCasterState.*;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Magic Caster",
	enabledByDefault = false,
	description = "Illumine automated magic caster",
	tags = {"Magic", "Splashing", "Profit", "Casting"},
	type = PluginType.SKILLING
)
@Slf4j
public class MagicCasterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BotUtils utils;

	@Inject
	private MagicCasterConfig config;

	@Inject
	PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	MagicCasterOverlay overlay;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	CastType castType;
	Spells selectedSpell;
	MagicCasterState state;
	Instant botTimer;
	MenuEntry targetMenu;
	LocalPoint beforeLoc = new LocalPoint(0, 0); //initiate to mitigate npe
	Player player;
	NPC targetNPC;
	WidgetItem targetItem;

	int npcID = -1;
	int itemID = -1;
	int timeout = 0;
	int failureCount = 0;
	long sleepLength = 0;
	boolean startBot;
	private static final String OUT_OF_RUNES_MSG = "You do not have enough";
	private static final String UNREACHABLE_MSG = "I can't reach that";
	private final int MAX_FAILURE = 10;

	@Override
	protected void startUp()
	{
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown()
	{
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}

	@Provides
	MagicCasterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MagicCasterConfig.class);
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("MagicCaster"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey())
		{
			case "startButton":
				if (!startBot)
				{
					startBot = true;
					chinBreakHandler.startPlugin(this);
					botTimer = Instant.now();
					state = null;
					targetMenu = null;
					timeout = 0;
					botTimer = Instant.now();
					initVals();
					overlayManager.add(overlay);
				}
				else
				{
					resetVals();
				}
				break;
		}
	}

	public void initVals()
	{
		castType = config.getSpellType();
		selectedSpell = config.getSpell();
		npcID = config.npcID();
		itemID = config.itemID();
	}

	public void resetVals()
	{
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
		startBot = false;
		castType = null;
		selectedSpell = null;
		botTimer = null;
		failureCount = 0;
		npcID = -1;
		itemID = -1;
		timeout = 0;
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup() != "MagicCaster")
		{
			return;
		}
		switch (event.getKey())
		{
			case "npcID":
				npcID = config.npcID();
				log.debug("NPC ID set to {}", npcID);
				break;
			case "itemID":
				itemID = config.itemID();
				log.debug("Item ID set to {}", itemID);
				break;
			case "getSpellType":
				castType = config.getSpellType();
				log.debug("Spell cast type set to {}", castType.getName());
				break;
			case "getSpell":
				selectedSpell = config.getSpell();
				log.debug("Spell set to {}", selectedSpell.getName());
				break;
		}
	}

	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private NPC findNPC()
	{
		log.debug("looking for NPC");
		NPC npc = new NPCQuery().idEquals(npcID).filter(n -> n.getInteracting() == client.getLocalPlayer()).result(client).nearestTo(player);
		if (npc != null)
		{
			return npc;
		}
		return new NPCQuery().idEquals(npcID).filter(n -> n.getInteracting() == null || n.getInteracting() == client.getLocalPlayer()).result(client).nearestTo(player);
	}

	private WidgetItem getItem()
	{
		log.debug("finding item");
		return utils.getInventoryWidgetItem(itemID);
	}

	private void castSpell()
	{
		switch (castType.getName())
		{
			case "Single cast":
				targetMenu = new MenuEntry("Cast", "", targetNPC.getIndex(), MenuOpcode.SPELL_CAST_ON_NPC.getId(),
						0, 0, false);
				utils.oneClickCastSpell(selectedSpell.getSpell(), targetMenu, targetNPC.getConvexHull().getBounds(), sleepDelay());
				timeout = 4 + tickDelay();
				return;
			case "Auto-cast":
				targetMenu = new MenuEntry("", "", targetNPC.getIndex(), MenuOpcode.NPC_SECOND_OPTION.getId(), 0, 0, false);
				utils.setMenuEntry(targetMenu);
				utils.delayMouseClick(targetNPC.getConvexHull().getBounds(), sleepDelay());
				timeout = 10 + tickDelay();
				return;
			case "High Alchemy":
				targetMenu = new MenuEntry("Cast", "", targetItem.getId(), MenuOpcode.ITEM_USE_ON_WIDGET.getId(), targetItem.getIndex(), 9764864, true);
				timeout = 5 + tickDelay();
				utils.oneClickCastSpell(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY, targetMenu, targetItem.getCanvasBounds().getBounds(), sleepDelay());
				return;
		}
	}

	public MagicCasterState getState()
	{
		if (timeout > 0)
		{
			return IDLING;
		}
		if (utils.isMoving(beforeLoc)) //could also test with just isMoving
		{
			return MOVING;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			return HANDLE_BREAK;
		}
		if (castType.getName().equals("High Alchemy"))
		{
			targetItem = getItem();
			return (targetItem != null && targetItem.getQuantity() > 0) ? FIND_ITEM : ITEM_NOT_FOUND;
		}
		targetNPC = findNPC();
		return (targetNPC != null) ? FIND_NPC : NPC_NOT_FOUND;
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (!startBot || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			if (!client.isResized())
			{
				utils.sendGameMessage("illu - client must be set to resizable");
				startBot = false;
				return;
			}
			utils.handleRun(40, 20);
			state = getState();
			beforeLoc = player.getLocalLocation();
			switch (state)
			{
				case IDLING:
					timeout--;
					return;
				case MOVING:
					timeout = tickDelay();
					break;
				case NPC_NOT_FOUND:
					log.debug("NPC not found");
					utils.sendGameMessage("NPC not found");
					timeout = tickDelay();
					break;
				case ITEM_NOT_FOUND:
					log.info("Item not found, config: {}", config.itemID());
					utils.sendGameMessage("Item not found");
					if (config.logout())
					{
						utils.logout();
						resetVals();
					}
					else
					{
						timeout = tickDelay();
					}
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					timeout = 10;
					break;
				case FIND_NPC:
				case FIND_ITEM:
					castSpell();
					break;
			}
		}
		else
		{
			log.debug("client/player is null or bot isn't started");
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!startBot || event.getActor() != player)
		{
			return;
		}
		log.debug("Animation ID changed to {}, resetting timeout", event.getActor().getAnimation());
		if (event.getActor().getAnimation() == AnimationID.LOW_LEVEL_MAGIC_ATTACK)
		{
			timeout = 10 + tickDelay();
			failureCount = 0;
			return;
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (!startBot || event.getType() != ChatMessageType.GAMEMESSAGE &&
			event.getType() != ChatMessageType.ENGINE)
		{
			return;
		}
		if (event.getMessage().contains(OUT_OF_RUNES_MSG))
		{
			log.debug("Out of runes!");
			utils.sendGameMessage("Out of runes!");
			startBot = false;
			if (config.logout())
			{
				utils.logout();
			}
			return;
		}
		if (event.getMessage().contains(UNREACHABLE_MSG))
		{
			log.debug("unreachable message, fail count: " + failureCount);
			if (failureCount >= MAX_FAILURE)
			{
				utils.sendGameMessage("failed to reach NPC too many times, stopping");
				startBot = false;
				if (config.logout())
				{
					utils.logout();
					resetVals();
				}
				return;
			}
			failureCount++;
			timeout = tickDelay();
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (!startBot)
		{
			return;
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			state = IDLING;
			timeout = 2;
		}
	}
}