/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.magicsplasher;

import lombok.Getter;

@Getter
public enum SplashSpells
{

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
