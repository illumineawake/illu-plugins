/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Dalton <delps1001@gmail.com>
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
package net.runelite.client.plugins.autoworldhop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;
import net.runelite.client.config.Title;

@ConfigGroup("autoworldhop")
public interface AutoHopConfig extends Config
{
	@ConfigTitleSection(
		keyName = "hopTitle",
		name = "Hop",
		description = "",
		position = 1
	)
	default Title hopTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "cmbBracket",
		name = "Within combat bracket",
		description = "Only hop if the player is within your combat bracket",
		titleSection = "hopTitle",
		position = 2
	)
	default boolean cmbBracket()
	{
		return true;
	}

	@ConfigItem(
		keyName = "alwaysHop",
		name = "Hop on player spawn",
		description = "Hop when a player  spawns",
		titleSection = "hopTitle",
		position = 3
	)
	default boolean alwaysHop()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chatHop",
		name = "Hop on chat message",
		description = "Hop whenever any message is entered into chat",
		titleSection = "hopTitle",
		position = 4
	)
	default boolean chatHop()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hopRadius",
		name = "Hop radius",
		description = "Hop only when another player enters radius",
		titleSection = "hopTitle",
		position = 5
	)
	default boolean hopRadius()
	{
		return false;
	}

	@ConfigItem(
		keyName = "playerRadius",
		name = "Player radius",
		description = "Radius (tiles) for player to be within to trigger hop",
		titleSection = "hopTitle",
		position = 6,
		hidden = true,
		unhide = "hopRadius"
	)
	default int playerRadius()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "skulledHop",
		name = "Skulled",
		description = "Hop when a player within your combat bracket spawns that has a skull",
		titleSection = "hopTitle",
		position = 7,
		hide = "alwaysHop"
	)
	default boolean skulledHop()
	{
		return true;
	}

	@ConfigItem(
		keyName = "underHop",
		name = "Log under",
		description = "Hop when a player within your combat bracket spawns underneath you",
		titleSection = "hopTitle",
		position = 8,
		hide = "alwaysHop"
	)
	default boolean underHop()
	{
		return true;
	}

	@ConfigItem(
		keyName = "bankIgnore",
		name = "Ignore near Bank",
		description = "Don't hop if within 15 tiles of a bank, to avoid causing your character to hop if you walk into Bank/GE with this plugin turned on",
		titleSection = "hopTitle",
		position = 9
	)
	default boolean bankIgnore()
	{
		return true;
	}

	@ConfigTitleSection(
		keyName = "worldsTitle",
		name = "Worlds",
		description = "",
		position = 10
	)
	default Title worldsTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "american",
		name = "American",
		description = "Allow hopping to American worlds",
		titleSection = "worldsTitle",
		position = 11
	)
	default boolean american()
	{
		return true;
	}

	@ConfigItem(
		keyName = "unitedkingdom",
		name = "UK",
		description = "Allow hopping to UK worlds",
		titleSection = "worldsTitle",
		position = 12
	)
	default boolean unitedkingdom()
	{
		return true;
	}

	@ConfigItem(
		keyName = "germany",
		name = "German",
		description = "Allow hopping to German worlds",
		titleSection = "worldsTitle",
		position = 13
	)
	default boolean germany()
	{
		return true;
	}

	@ConfigItem(
		keyName = "australia",
		name = "Australian",
		description = "Allow hopping to Australian worlds",
		titleSection = "worldsTitle",
		position = 14
	)
	default boolean australia()
	{
		return true;
	}

	@ConfigTitleSection(
		keyName = "ignoresTitle",
		name = "Ignore",
		description = "",
		position = 15
	)
	default Title ignoresTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "friends",
		name = "Friends",
		description = "Don't hop when the player spawned is on your friend list",
		titleSection = "ignoresTitle",
		position = 16
	)
	default boolean friends()
	{
		return true;
	}

	@ConfigItem(
		keyName = "clanmembers",
		name = "Clan members",
		description = "Don't hop when the player spawned is in your clan chat",
		titleSection = "ignoresTitle",
		position = 17
	)
	default boolean clanmember()
	{
		return true;
	}
}