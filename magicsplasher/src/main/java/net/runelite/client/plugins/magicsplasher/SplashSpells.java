/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.magicsplasher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.SpriteID;
import net.runelite.api.widgets.WidgetInfo;

@Getter
@AllArgsConstructor
public enum SplashSpells
{
	WIND_STRIKE("Air Strike", WidgetInfo.SPELL_WIND_STRIKE, SpriteID.SPELL_WIND_STRIKE),
	WATER_STRIKE("Water Strike", WidgetInfo.SPELL_WATER_STRIKE, SpriteID.SPELL_WATER_STRIKE),
	EARTH_STRIKE("Earth Strike", WidgetInfo.SPELL_EARTH_STRIKE, SpriteID.SPELL_EARTH_STRIKE),
	FIRE_STRIKE("Fire Strike", WidgetInfo.SPELL_FIRE_STRIKE, SpriteID.SPELL_FIRE_STRIKE);

	private final String name;
	private final WidgetInfo info;
	private final int spellSpriteID;
}
