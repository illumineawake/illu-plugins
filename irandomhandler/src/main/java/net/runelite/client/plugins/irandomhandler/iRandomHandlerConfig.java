/*
 *  Copyright (c) 2018, trimbe <github.com/trimbe>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.irandomhandler;

import net.runelite.client.config.*;

@ConfigGroup("irandomhandler")
public interface iRandomHandlerConfig extends Config {
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
            position = 4,
            section = "delayConfig"
    )
    default int sleepDeviation() {
        return 10;
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
        return 1;
    }

    @Range(
            min = 0,
            max = 15
    )
    @ConfigItem(
            keyName = "tickDelayMax",
            name = "Game Tick Max",
            description = "",
            position = 12,
            section = "delayTickConfig"
    )
    default int tickDelayMax() {
        return 3;
    }

    @Range(
            min = 0,
            max = 15
    )
    @ConfigItem(
            keyName = "tickDelayTarget",
            name = "Game Tick Target",
            description = "",
            position = 13,
            section = "delayTickConfig"
    )
    default int tickDelayTarget() {
        return 2;
    }

    @Range(
            min = 0,
            max = 15
    )
    @ConfigItem(
            keyName = "tickDelayDeviation",
            name = "Game Tick Deviation",
            description = "",
            position = 14,
            section = "delayTickConfig"
    )
    default int tickDelayDeviation() {
        return 1;
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

    @ConfigItem(
            keyName = "dismissAllEvents",
            name = "handle/dismiss all events",
            description = "Enable to handle and dismiss all random event types. Includes handling genie for XP lamp.",
            position = -2
    )
    default boolean dismissAllEvents() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissDunce",
            name = "dismiss on Surprise Exam",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissDunce() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissDwarf",
            name = "dismiss on Drunken Dwarf",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissDwarf() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissGenie",
            name = "Handle Genie",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissGenie() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissDemon",
            name = "dismiss on Drill Demon",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissDemon() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissForester",
            name = "dismiss on Freaky Forester",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissForester() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissFrog",
            name = "dismiss on Kiss the Frog",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissFrog() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissGravedigger",
            name = "dismiss on Gravedigger",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissGravedigger() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissMoM",
            name = "dismiss on Mysterious Old Man",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissMoM() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissBob",
            name = "dismiss on Evil Bob",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissBob() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissQuiz",
            name = "dismiss on Quiz Master",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissQuiz() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissJekyll",
            name = "dismiss on Jekyll & Hyde",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissJekyll() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissBeekeeper",
            name = "dismiss on Beekeeper",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissBeekeeper() {
        return false;
    }

    @ConfigItem(
            keyName = "dismissSandwich",
            name = "dismiss on Sandwich Lady",
            description = "",
            hide = "dismissAllEvents"
    )
    default boolean dismissSandwich() {
        return false;
    }
}
