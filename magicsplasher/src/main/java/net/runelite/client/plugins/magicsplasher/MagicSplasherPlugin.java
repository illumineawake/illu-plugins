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
package net.runelite.client.plugins.magicsplasher;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
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
import static net.runelite.client.plugins.magicsplasher.MagicSplasherState.*;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Magic Splasher",
	enabledByDefault = false,
	description = "Illumine automated magic splasher",
	tags = {"Magic", "Splashing"},
	type = PluginType.SKILLING
)
@Slf4j
public class MagicSplasherPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BotUtils utils;

	@Inject
	private MagicSplasherConfig config;

	@Inject
	PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	MagicSplasherOverlay overlay;

	SplashSpells selectedSpell;
	MagicSplasherState state;
	Instant botTimer;
	MenuEntry targetMenu;
	LocalPoint beforeLoc = new LocalPoint(0, 0); //initiate to mitigate npe
	Player player;
	NPC splashNPC;
	WidgetItem targetItem;
	ExecutorService executorService;

	int npcID = -1;
	int itemID = -1;
	int timeout = 0;
	int failureCount = 0;
	long sleepLength = 0;
	boolean startSplasher;
	private static final String OUT_OF_RUNES_MSG = "You do not have enough";
	private static final String UNREACHABLE_MSG = "I can't reach that";
	private final int MAX_FAILURE = 10;

	@Override
	protected void startUp()
	{
		initVals();
	}

	@Override
	protected void shutDown()
	{
		resetVals();
	}

	public void initVals()
	{
		executorService = Executors.newSingleThreadExecutor();
		overlayManager.add(overlay);
		selectedSpell = config.getSpells();
		npcID = config.npcID();
		itemID = config.itemID();
	}

	public void resetVals()
	{
		executorService.shutdown();
		overlayManager.remove(overlay);
		startSplasher = false;
		selectedSpell = null;
		botTimer = null;
		failureCount = 0;
		npcID = -1;
		itemID = -1;
		timeout = 0;
	}

	@Provides
	MagicSplasherConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MagicSplasherConfig.class);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup() != "MagicSplasher")
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
			case "getSpells":
				selectedSpell = config.getSpells();
				log.debug("Splashing spell set to {}", selectedSpell.getName());
				break;
		}
	}

	private void sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		log.debug("Sleeping for {}ms", sleepLength);
		utils.sleep(sleepLength);
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private void handleMouseClick()
	{
		executorService.submit(() ->
		{
			try
			{
				sleepDelay();
				utils.clickRandomPointCenter(-100, 100);
			}
			catch (RuntimeException e)
			{
				e.printStackTrace();
			}
		});
	}

	private void openSpellBook()
	{
		targetMenu = new MenuEntry("", "", 1, MenuOpcode.CC_OP.getId(), -1, 10551356, false); //open spellbook
		handleMouseClick();
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
		switch (selectedSpell.getName())
		{
			case "Single cast":
				targetMenu = new MenuEntry(selectedSpell.getMenuOption(), "", splashNPC.getIndex(), MenuOpcode.SPELL_CAST_ON_NPC.getId(), 0, 0, false);
				timeout = 4 + tickDelay();
				break;
			case "Auto-cast":
				targetMenu = new MenuEntry(selectedSpell.getMenuOption(), "", splashNPC.getIndex(), MenuOpcode.NPC_SECOND_OPTION.getId(), 0, 0, false);
				timeout = 10 + tickDelay();
				break;
			case "High Alchemy":
				targetMenu = new MenuEntry("Cast", "", targetItem.getId(), MenuOpcode.ITEM_USE_ON_WIDGET.getId(), targetItem.getIndex(), 9764864, true);
				timeout = 5 + tickDelay();
				break;
		}
		handleMouseClick();
	}

	public MagicSplasherState getState()
	{
		log.info(selectedSpell.getName());
		if (timeout > 0)
		{
			return IDLING;
		}
		if (utils.isMoving(beforeLoc)) //could also test with just isMoving
		{
			return MOVING;
		}
		if(selectedSpell.getName().equals("High Alchemy"))
		{
			targetItem = getItem();
			return (targetItem != null && targetItem.getQuantity() > 0) ? FIND_ITEM : ITEM_NOT_FOUND;
		}
		splashNPC = findNPC();
		return (splashNPC != null) ? FIND_NPC : NPC_NOT_FOUND;
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN && startSplasher)
		{
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
				case OPENING_SPELLBOOK:
					openSpellBook();
					break;
				case NPC_NOT_FOUND:
					log.debug("NPC not found");
					utils.sendGameMessage("NPC not found");
					timeout = tickDelay();
					break;
				case ITEM_NOT_FOUND:
					log.info("Item not found, config: {}, ID: {}, quantity {}", config.itemID(), targetItem.getId(), targetItem.getQuantity());
					utils.sendGameMessage("Item not found");
					if (config.logout())
						utils.logout();
					else
						timeout = tickDelay();
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
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!startSplasher || targetMenu == null)
		{
			return;
		}
		log.debug("MenuEntry string event: " + targetMenu.toString());
		event.setMenuEntry(targetMenu);
		targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!startSplasher || event.getActor() != player)
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
		if (event.getType() != ChatMessageType.GAMEMESSAGE &&
			event.getType() != ChatMessageType.ENGINE)
		{
			return;
		}
		if (event.getMessage().contains(OUT_OF_RUNES_MSG))
		{
			log.debug("Out of runes!");
			utils.sendGameMessage("Out of runes!");
			startSplasher = false;
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
				startSplasher = false;
				if (config.logout())
				{
					executorService.submit(() ->
					{
						utils.logout();
					});
				}
				return;
			}
			failureCount++;
			timeout = tickDelay();
		}
	}
}