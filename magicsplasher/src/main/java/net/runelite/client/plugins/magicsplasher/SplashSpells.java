/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.magicsplasher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.MenuOpcode;
import net.runelite.api.SpriteID;
import net.runelite.api.widgets.WidgetInfo;

@Getter
public enum SplashSpells
{
	/*WIND_STRIKE("Air Strike", WidgetInfo.SPELL_WIND_STRIKE, SpriteID.SPELL_WIND_STRIKE),
	WATER_STRIKE("Water Strike", WidgetInfo.SPELL_WATER_STRIKE, SpriteID.SPELL_WATER_STRIKE),
	EARTH_STRIKE("Earth Strike", WidgetInfo.SPELL_EARTH_STRIKE, SpriteID.SPELL_EARTH_STRIKE),
	FIRE_STRIKE("Fire Strike", WidgetInfo.SPELL_FIRE_STRIKE, SpriteID.SPELL_FIRE_STRIKE),
	CURSE("Curse", WidgetInfo.SPELL_CURSE, SpriteID.SPELL_CURSE, "Cast");*/

	AUTO_CAST("Auto-cast"),
	SINGLE_CAST("Single cast", "Cast"),
	HIGH_ALCHEMY("High Alchemy", "Cast");

	private final String name;
	private String menuOption = "";

	SplashSpells(String name)
	{
		this.name = name;
	}

	SplashSpells(String name, String menuOption)
	{
		this.name = name;
		this.menuOption = menuOption;
	}
}
