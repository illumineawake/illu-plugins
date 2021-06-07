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
package net.runelite.client.plugins.icombinationrunecrafter;

import net.runelite.client.config.*;

@ConfigGroup("iCombinationRunecrafter")
public interface iCombinationRunecrafterConfig extends Config {

    @ConfigTitle(
            keyName = "delayConfig",
            name = "Sleep Delay Configuration",
            description = "Configure how the bot handles sleep delays",
            position = 0
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
            position = 1,
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
            position = 2,
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
            position = 3,
            section = "delayConfig"
    )
    default int sleepTarget() {
        return 120;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepDeviation",
            name = "Sleep Deviation",
            description = "",
            position = 4,
            section = "delayConfig"
    )
    default int sleepDeviation() {
        return 150;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 5,
            section = "delayConfig"
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

    @ConfigTitle(
            keyName = "delayTickConfig",
            name = "Game Tick Configuration",
            description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
            position = 10
    )
    String delayTickConfig = "delayTickConfig";

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayMin",
            name = "Game Tick Min",
            description = "",
            position = 11,
            section = "delayTickConfig"
    )
    default int tickDelayMin() {
        return 0;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayMax",
            name = "Game Tick Max",
            description = "",
            position = 12,
            section = "delayTickConfig"
    )
    default int tickDelayMax() {
        return 2;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayTarget",
            name = "Game Tick Target",
            description = "",
            position = 13,
            section = "delayTickConfig"
    )
    default int tickDelayTarget() {
        return 0;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayDeviation",
            name = "Game Tick Deviation",
            description = "",
            position = 14,
            section = "delayTickConfig"
    )
    default int tickDelayDeviation() {
        return 2;
    }

    @ConfigItem(
            keyName = "tickDelayWeightedDistribution",
            name = "Game Tick Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 15,
            section = "delayTickConfig"
    )
    default boolean tickDelayWeightedDistribution() {
        return false;
    }

    @ConfigTitle(
            keyName = "instructionsTitle",
            name = "Instructions",
            description = "",
            position = 16
    )
    String instructionsTitle = "instructionsTitle";

    @ConfigItem(
            keyName = "instruction",
            name = "",
            description = "Instructions. Don't enter anything into this field",
            position = 20,
            title = "instructionsTitle"
    )
    default String instruction() {
        return "Creates Lava, Steam or Smoke runes at Castle Wars/Fire Altar. Must have a Fire Tiara equipped. Requires Ring of Dueling, Talismans, Runes, Pure or Daeyalt Essence. " +
                "Stops and logs out (if enabled) when out of these materials. Does NOT support pouches or Magic Imbue currently.";
    }

    @ConfigItem(
            keyName = "getRunecraftingType",
            name = "Rune Type",
            description = "Choose which combination rune to craft",
            position = 24
    )

    default RunecraftingTypes getRunecraftingType() {
        return RunecraftingTypes.SMOKE_RUNES;
    }

    @ConfigItem(
            keyName = "getEssence",
            name = "Essence Type",
            description = "Choose your essence type",
            position = 24
    )

    default EssenceTypes getEssence() {
        return EssenceTypes.PURE_ESSENCE;
    }

    @ConfigItem(
            keyName = "staminaPotion",
            name = "Use Stamina Potions",
            description = "",
            position = 25
    )
    default boolean staminaPotion() {
        return false;
    }

    @ConfigItem(
            keyName = "stopStamina",
            name = "Stop when out of Stamina Potions",
            description = "",
            position = 26,
            hidden = true,
            unhide = "staminaPotion"
    )
    default boolean stopStamina() {
        return false;
    }

    @ConfigItem(
            keyName = "bindingNecklace",
            name = "Use Binding Necklaces",
            description = "",
            position = 30
    )
    default boolean bindingNecklace() {
        return false;
    }

    @ConfigItem(
            keyName = "stopNecklace",
            name = "Stop when out of necklaces",
            description = "",
            position = 31,
            hidden = true,
            unhide = "bindingNecklace"
    )
    default boolean stopNecklace() {
        return false;
    }

    @ConfigItem(
            keyName = "logout",
            name = "Log out when out of items",
            description = "",
            position = 40
    )
    default boolean logout() {
        return false;
    }

    @ConfigItem(
            keyName = "startButton",
            name = "Start/Stop",
            description = "Test button that changes variable value",
            position = 50
    )
    default Button startButton() {
        return new Button();
    }
}