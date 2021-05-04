package net.runelite.client.plugins.itasktemplate;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iutils.*;

import javax.inject.Inject;

@Slf4j
public abstract class Task
{
	public Task()
	{
	}

	@Inject
	public Client client;

	@Inject
	public iTaskTemplateConfig config;

	@Inject
	public iUtils utils;

	@Inject
	public MenuUtils menu;

	@Inject
	public MouseUtils mouse;

	@Inject
	public CalculationUtils calc;

	@Inject
	public PlayerUtils playerUtils;

	@Inject
	public ObjectUtils object;

	public MenuEntry entry;

	public abstract boolean validate();

	public long sleepDelay()
	{
		iTaskTemplatePlugin.sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return iTaskTemplatePlugin.sleepLength;
	}

	public int tickDelay()
	{
		iTaskTemplatePlugin.tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		return iTaskTemplatePlugin.tickLength;
	}

	public String getTaskDescription()
	{
		return this.getClass().getSimpleName();
	}

	public void onGameTick(GameTick event)
	{
		return;
	}

}
