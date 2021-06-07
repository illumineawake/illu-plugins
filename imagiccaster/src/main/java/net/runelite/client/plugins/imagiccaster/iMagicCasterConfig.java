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
package net.runelite.client.plugins.imagiccaster;

import net.runelite.client.config.*;


@ConfigGroup("iMagicCaster")
public interface iMagicCasterConfig extends Config {

    @ConfigItem(
            keyName = "getSpellType",
            name = "Spell type",
            description = "Select the type of spell.",
            position = 0
    )
    default CastType getSpellType() {
        return CastType.AUTO_CAST;
    }

    @ConfigItem(
            keyName = "getSpell",
            name = "Spell",
            description = "Choose a spell, only required for single casting",
            position = 5,
            hidden = true,
            unhide = "getSpellType",
            unhideValue = "SINGLE_CAST"
    )
    default Spells getSpell() {
        return Spells.CURSE;
    }

    @ConfigItem(
            keyName = "npcID",
            name = "NPC ID",
            description = "Provide ID of the NPC to target",
            position = 10
    )
    default int npcID() {
        return 0;
    }

    @ConfigItem(
            keyName = "itemID",
            name = "Item ID",
            description = "Provide ID of the item to High Alc",
            position = 15,
            hidden = true,
            unhide = "getSpellType",
            unhideValue = "HIGH_ALCHEMY"
    )
    default int itemID() {
        return 0;
    }

    @ConfigItem(
            keyName = "groundItemID",
            name = "Ground Item ID",
            description = "Provide ID of the Ground Item to Tele grab",
            position = 20,
            hidden = true,
            unhide = "getSpellType",
            unhideValue = "TELE_GRAB"

    )
    default int groundItemID() {
        return 0;
    }

    @ConfigItem(
            keyName = "moveCast",
            name = "Cast while moving",
            description = "Enable to allow casting while moving",
            position = 40
    )
    default boolean moveCast() {
        return false;
    }

    @ConfigItem(
            keyName = "logout",
            name = "Logout when out of runes",
            description = "Enable to logout when out of runes. Won't work if you are attacking an npc that attacks you",
            position = 45
    )
    default boolean logout() {
        return true;
    }

    @ConfigTitle(
            keyName = "delayConfig",
            name = "Sleep Delay Configuration",
            description = "Configure how the bot handles sleep delays",
            position = 5
    )
    String delayConfig = "delayConfig";

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMin",
            name = "Sleep Min",
            description = "",
            position = 6,
            section = "delayConfig"
    )
    default int sleepMin() {
        return 60;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMax",
            name = "Sleep Max",
            description = "",
            position = 7,
            section = "delayConfig"
    )
    default int sleepMax() {
        return 350;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepTarget",
            name = "Sleep Target",
            description = "",
            position = 8,
            section = "delayConfig"
    )
    default int sleepTarget() {
        return 100;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepDeviation",
            name = "Sleep Deviation",
            description = "",
            position = 9,
            section = "delayConfig"
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 10,
            section = "delayConfig"
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

    @ConfigTitle(
            keyName = "delayTickConfig",
            name = "Game Tick Configuration",
            description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
            position = 11
    )
    String delayTickConfig = "delayTickConfig";

    @Range(
            min = 0,
            max = 25
    )
    @ConfigItem(
            keyName = "tickDelayMin",
            name = "Game Tick Min",
            description = "",
            position = 12,
            section = "delayTickConfig"
    )
    default int tickDelayMin() {
        return 1;
    }

    @Range(
            min = 0,
            max = 30
    )
    @ConfigItem(
            keyName = "tickDelayMax",
            name = "Game Tick Max",
            description = "",
            position = 13,
            section = "delayTickConfig"
    )
    default int tickDelayMax() {
        return 10;
    }

    @Range(
            min = 0,
            max = 30
    )
    @ConfigItem(
            keyName = "tickDelayTarget",
            name = "Game Tick Target",
            description = "",
            position = 114,
            section = "delayTickConfig"
    )
    default int tickDelayTarget() {
        return 5;
    }

    @Range(
            min = 0,
            max = 30
    )
    @ConfigItem(
            keyName = "tickDelayDeviation",
            name = "Game Tick Deviation",
            description = "",
            position = 15,
            section = "delayTickConfig"
    )
    default int tickDelayDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "tickDelayWeightedDistribution",
            name = "Game Tick Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 16,
            section = "delayTickConfig"
    )
    default boolean tickDelayWeightedDistribution() {
        return false;
    }

    @ConfigItem(
            keyName = "enableRun",
            name = "Automatically enable run (energy)",
            description = "If enabled, this will toggle your run on when you're above 40%",
            position = 17
    )
    default boolean enableRun() {
        return true;
    }

    @ConfigItem(
            keyName = "enableUI",
            name = "Enable UI",
            description = "Enable to turn on in game UI",
            position = 25
    )
    default boolean enableUI() {
        return true;
    }

    @ConfigItem(
            keyName = "startButton",
            name = "Start/Stop",
            description = "Start or stop the bot",
            position = 30
    )
    default Button startButton() {
        return new Button();
    }
}
