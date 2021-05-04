/*
 * Copyright (c) 2018 gazivodag <https://github.com/gazivodag>
 * Copyright (c) 2019 lucwousin <https://github.com/lucwousin>
 * Copyright (c) 2019 infinitay <https://github.com/Infinitay>
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
package net.runelite.client.plugins.blackjackillumine;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.menus.AbstractComparableEntry;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.util.HotkeyListener;
import org.apache.commons.lang3.RandomUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Authors gazivodag longstreet
 */
@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "BlackjackIllumine",
	enabledByDefault = false,
	description = "Allows for one-click blackjacking, both knocking out and pickpocketing",
	tags = {"blackjack", "thieving"},
	type = PluginType.SKILLING
)
@Slf4j
public class BlackjackIllumine extends Plugin
{
	private static final int POLLNIVNEACH_REGION = 13358;

	private static final String SUCCESS_BLACKJACK = "You smack the bandit over the head and render them unconscious.";
	private static final String FAILED_BLACKJACK = "Your blow only glances off the bandit's head.";
	private static final String COMBAT_BLACKJACK = "You can't do this during combat.";
	private static final String SEEN_BLACKJACK = "Perhaps I shouldn't do this here, I think another Menaphite will see me.";

	private static final String PICKPOCKET = "Pickpocket";
	private static final String KNOCK_OUT = "Knock-out";
	private static final String BANDIT = "Bandit";
	private static final String MENAPHITE = "Menaphite Thug";

	private static final AbstractComparableEntry PICKPOCKET_BANDIT = new BJComparableEntry(BANDIT, true);
	private static final AbstractComparableEntry KNOCKOUT_BANDIT = new BJComparableEntry(BANDIT, false);
	private static final AbstractComparableEntry PICKPOCKET_MENAPHITE = new BJComparableEntry(MENAPHITE, true);
	private static final AbstractComparableEntry KNOCKOUT_MENAPHITE = new BJComparableEntry(MENAPHITE, false);

	@Inject
	private Client client;

	@Inject
	private BlackjackIllumineConfig config;

	@Inject
	private EventBus eventBus;

	@Inject
	private MenuManager menuManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private iUtils extUtils;

	@Inject
	private Notifier notifier;

	private long nextKnockOutTick = 0;
	private int timeout = 0;
	private static final int BLACKJACK_ID = 3550;
	private List<NPC> npcList = new ArrayList<>();
	private NPC closestNpc;
	private Point point;
	private Rectangle npcRect;
	private Rectangle foodBounds;
	private Point bounds;
	private boolean run;
	private boolean doDoubleClick;
	private ExecutorService executorService;

	@Provides
	BlackjackIllumineConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BlackjackIllumineConfig.class);
	}

	@Override
	protected void startUp()
	{
		keyManager.registerKeyListener(hotkeyListener);
		menuManager.addPriorityEntry(KNOCKOUT_BANDIT);
		menuManager.addPriorityEntry(KNOCKOUT_MENAPHITE);
		executorService = Executors.newSingleThreadExecutor();
	}

	@Override
	protected void shutDown()
	{
		menuManager.removePriorityEntry(PICKPOCKET_BANDIT);
		menuManager.removePriorityEntry(PICKPOCKET_MENAPHITE);
		menuManager.removePriorityEntry(KNOCKOUT_BANDIT);
		menuManager.removePriorityEntry(KNOCKOUT_MENAPHITE);
		keyManager.unregisterKeyListener(hotkeyListener);
		executorService.shutdown();
	}

	private HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggle())
	{
		@Override
		public void hotkeyPressed()
		{
			if (run)
			{
				log.info("pausing...");
				run = false;
			}
			else
			{
				log.info("resuming...");
				run = true;
			}
		}
	};

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
		{
			log.info("not logged in.");
			return;
		}
		else
		{
			if (client.getTickCount() >= nextKnockOutTick)
			{
				menuManager.removePriorityEntry(PICKPOCKET_BANDIT);
				menuManager.removePriorityEntry(PICKPOCKET_MENAPHITE);
				menuManager.addPriorityEntry(KNOCKOUT_BANDIT);
				menuManager.addPriorityEntry(KNOCKOUT_MENAPHITE);
			}
			if (getFood().isEmpty())
			{
				log.info("We're out of wine");
				run = false;
			}
			else
			{
				if (checkHitpoints())
				{
					drinkWine();
					return;
				}
			}
			if (run)
			{
				if (timeout > 0)
				{ //currently not being used
					log.info("wait tick: " + timeout);
					timeout--;
				}
				else
				{
					handleBlackjack();
				}
			}
			else
			{
				return;
			}
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		final String msg = event.getMessage();

		if (event.getType() == ChatMessageType.SPAM && (msg.equals(SUCCESS_BLACKJACK) || (msg.equals(FAILED_BLACKJACK) && config.pickpocketOnAggro())))
		{
			menuManager.removePriorityEntry(KNOCKOUT_BANDIT);
			menuManager.removePriorityEntry(KNOCKOUT_MENAPHITE);
			menuManager.addPriorityEntry(PICKPOCKET_BANDIT);
			menuManager.addPriorityEntry(PICKPOCKET_MENAPHITE);
			final int ticks = config.random() ? RandomUtils.nextInt(3, 4) : 4;
			nextKnockOutTick = client.getTickCount() + ticks;
		}
		else if ((msg.equals(COMBAT_BLACKJACK)) || (msg.equals(SEEN_BLACKJACK)))
		{
			log.info("we're in combat or we've been seen!");
			run = false;
			notifier.notify("we're in combat or we've been seen!", TrayIcon.MessageType.WARNING);
		}
	}

	private static class BJComparableEntry extends AbstractComparableEntry
	{
		private BJComparableEntry(final String npc, final boolean pickpocket)
		{
			if (!BANDIT.equals(npc) && !MENAPHITE.equals(npc))
			{
				throw new IllegalArgumentException("Only bandits or menaphites are valid");
			}

			this.setTarget(npc.toLowerCase());
			this.setOption(pickpocket ? PICKPOCKET : KNOCK_OUT);
			this.setPriority(100);
		}

		@Override
		public boolean matches(MenuEntry entry)
		{
			return entry.getOption().equalsIgnoreCase(this.getOption()) &&
				Text.removeTags(entry.getTarget(), true).equalsIgnoreCase(this.getTarget());
		}
	}

	private void drinkWine()
	{
		if (config.flash())
		{
			//setFlash(true);
		}

		if (getFood().isEmpty())
		{
			log.info("We're out of wine");
			run = false;
		}
		else
		{
			log.info("lets eat: " + getFood().get(0).getCanvasBounds().toString());
			//extUtils.moveClick(getFood().get(0).getCanvasBounds());
			foodBounds = getFood().get(0).getCanvasBounds();
			bounds = new Point((int) Math.round(foodBounds.getCenterX()) + (extUtils.getRandomIntBetweenRange(-15, 15)), ((int) Math.round(foodBounds.getCenterY()) + (extUtils.getRandomIntBetweenRange(-15, 15))));
			singleClick();
			doDoubleClick = true;
			timeout = 0; //this is not doing anything
			/*if (npcRect != null) {
				log.info("moving mouse");
				extUtils.moveMouseEvent(npcRect); //Test to see if moving mouse after eating stops the walk click
			}*/
		}
	}

	private void handleBlackjack()
	{
		log.info("handle blackjack");
		//wait(randomDelay(50,100));
		closestNpc = extUtils.findNearestNpc(BLACKJACK_ID);
		if (closestNpc != null)
		{
			npcRect = closestNpc.getConvexHull().getBounds();
			bounds = new Point((int) Math.round(npcRect.getCenterX()) + (extUtils.getRandomIntBetweenRange(-2, 2)), ((int) Math.round(npcRect.getCenterY()) + (extUtils.getRandomIntBetweenRange(-2, 2))));
			if (bounds != null)
			{
				if (!doDoubleClick)
				{
					singleClick();
				}
				else
				{
					log.info("trying double click");
					doubleClick();
					doDoubleClick = false;
				}
			}
		}
	}

	private boolean checkHitpoints()
	{
		return client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.hpThreshold();
	}

	private List<WidgetItem> getFood()
	{
		return extUtils.getItems(List.of(config.foodToEat()));
	}

	private void doubleClick()
	{
		delayFirstClick();
		delaySecondClick();
	}

	private void singleClick()
	{
		delayFirstClick();
	}

	private void delayFirstClick()
	{
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(this::simLeftClick, randomDelay(50, 100), TimeUnit.MILLISECONDS);
		service.shutdown();
	}

	private void delaySecondClick()
	{
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(this::simLeftClick, randomDelay(70, 80), TimeUnit.MILLISECONDS);
		service.shutdown();
	}

	private void simLeftClick()
	{
		extUtils.moveClick(bounds);
		return;
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
