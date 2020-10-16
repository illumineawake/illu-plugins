/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.iworldwalker;

import lombok.Getter;

@Getter
public enum Quest
{
	CLIENT_OF_KOUREND("Client of Kourend", "1825,3690,0 - Veos on Pisc dock\n1807,3726,0 - Leenz at Pisc general store\n1772,3588,0 - Horace at Hosidius general store\n1545,3630,0 - Jennifer at Shayzien general store\n1551,3719,0 - Munty at Lovakengj general store\n1720,3724,0 - Regath at Arceuus general store\n1713,3883,0 - Dark Altar\n\nBring 1 feather");

	private final String name;
	private final String notes;

	Quest(String name, String notes)
	{
		this.name = name;
		this.notes = notes;
	}
}
