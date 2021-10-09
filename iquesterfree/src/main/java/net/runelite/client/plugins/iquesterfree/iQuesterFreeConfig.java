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
package net.runelite.client.plugins.iquesterfree;

import net.runelite.api.Skill;
import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("iQuesterFree")
public interface iQuesterFreeConfig extends Config {
    @ConfigSection(
            name = "Instructions",
            description = "",
            position = -4
    )
    String instructionsSection = "instructionsTitle";

    @ConfigItem(
            keyName = "instructions",
            name = "",
            description = "Instructions. Don't enter anything into this field",
            title = "instructionsTitle",
			section = "instructionsTitle",
            position = -3
    )
    default String instructions() {
        return "Select the quests to run, have roughly 50k+ gp and press Start.";
    }

    @ConfigSection(
            name = "Quests",
            description = "",
            position = -2
    )
    String f2pQuests = "f2pQuests";

    @ConfigItem(
            keyName = "dorics",
            name = "Dorics Quest",
            description = "",
            section = "f2pQuests"
    )
    default boolean dorics() {
        return true;
    }


    @ConfigItem(
            keyName = "goblinDiplomacy",
            name = "Goblin Diplomacy",
            description = "",
            section = "f2pQuests"
    )
    default boolean goblinDiplomacy() {
        return true;
    }

    @ConfigItem(
            keyName = "romeoAndJuliet",
            name = "Romeo and Juliet",
            description = "",
            section = "f2pQuests"
    )
    default boolean romeoAndJuliet() {
        return true;
    }

    @ConfigItem(
            keyName = "xMarksTheSpot",
            name = "X Marks the Spot",
            description = "",
            section = "f2pQuests"
    )
    default boolean xMarksTheSpot() {
        return true;
    }

    @ConfigItem(
            keyName = "xSkill",
            name = "X Marks Lamp Skill",
            description = "Choose a skill to use your lamp on",
            hidden = true,
            unhide = "xMarksTheSpot",
            section = "f2pQuests",
            position = 1
    )

    default Skill xSkill() {
        return Skill.PRAYER;
    }

	@ConfigItem(
		keyName = "buyStart",
		name = "Allow buying all items at start",
		description = "Enable to allow the plugin to buy all starting quest items for eligible quests at plugin start." +
			" You may not want to enable this if you are already partway through a quest.",
		position = 89
	)
	default boolean buyStart() {
		return true;
	}

    @ConfigItem(
            keyName = "enableUI",
            name = "Enable UI",
            description = "Enable to turn on in game UI",
            position = 90
    )
    default boolean enableUI() {
        return true;
    }

    @ConfigItem(
            keyName = "startButton",
            name = "Start/Stop",
            description = "Test button that changes variable value",
            position = 100
    )
    default Button startButton() {
        return new Button();
    }
}
