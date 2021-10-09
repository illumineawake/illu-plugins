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
package net.runelite.client.plugins.iworldwalker;

import net.runelite.client.config.*;
import net.runelite.client.plugins.iworldwalker.farming.*;

@ConfigGroup("iWorldWalker")
public interface iWorldWalkerConfig extends Config {

    @ConfigTitle(
            keyName = "delayConfig",
            name = "Sleep Delay Configuration",
            description = "Configure how the bot handles sleep delays",
            position = 1
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
            position = 2,
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
            position = 3,
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
            position = 4,
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
            position = 5,
            section = "delayConfig"
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 6,
            section = "delayConfig"
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

    @ConfigTitle(
            keyName = "delayTickConfig",
            name = "Game Tick Configuration",
            description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
            position = 7
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
            position = 8,
            section = "delayTickConfig"
    )
    default int tickDelayMin() {
        return 1;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayMax",
            name = "Game Tick Max",
            description = "",
            position = 9,
            section = "delayTickConfig"
    )
    default int tickDelayMax() {
        return 3;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayTarget",
            name = "Game Tick Target",
            description = "",
            position = 10,
            section = "delayTickConfig"
    )
    default int tickDelayTarget() {
        return 2;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayDeviation",
            name = "Game Tick Deviation",
            description = "",
            position = 11,
            section = "delayTickConfig"
    )
    default int tickDelayDeviation() {
        return 1;
    }

    @ConfigItem(
            keyName = "tickDelayWeightedDistribution",
            name = "Game Tick Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 12,
            section = "delayTickConfig"
    )
    default boolean tickDelayWeightedDistribution() {
        return false;
    }

    @ConfigTitle(
            keyName = "instructionsTitle",
            name = "Instructions",
            description = "Instructions Title",
            position = 15
    )
    String instructionsTitle = "instructionsTitle";

    @ConfigItem(
            keyName = "instructions",
            name = "",
            description = "Instructions. Don't enter anything into this field",
            position = 18,
            section = "instructionsTitle"
    )
    default String instructions() {
        return "Select your location from the drop-down or enter a custom location using x,y,z format. Use Location/Tile Location in Developer Tools to obtain a custom coordinate.";
    }

    @ConfigTitle(
            keyName = "notesTitle",
            name = "Custom Notes",
            description = "Notes Title",
            position = 29
    )
    String notesTitle = "notesTitle";

    @ConfigItem(
            keyName = "notepad",
            name = "",
            description = "Paste custom coords that you want to save for frequent use",
            section = "notesTitle",
            position = 30
    )
    default String notepad() {
        return "Paste custom co-ords that you want to save for frequent use";
    }

    @ConfigTitle(
            keyName = "showQuestNotes",
            name = "Show Quest Notes",
            description = "Unhide the quest notes section, containing notes on supported quests",
            position = 31
    )
    String showQuestNotes = "showQuestNotes";

    @ConfigItem(
            keyName = "supportedQuests",
            name = "Quests",
            description = "Dropdown of supported quests",
            position = 32,
            section = "showQuestNotes"
    )
    default Quest quest() {
        return Quest.CLIENT_OF_KOUREND;
    }

    @ConfigItem(
            keyName = "questNotesCOK",
            name = "Quest Notes",
            description = "Notes for supported quests",
            position = 33,
            hidden = true,
            unhide = "supportedQuests",
            unhideValue = "CLIENT_OF_KOUREND",
            section = "showQuestNotes"
    )
    default String questNotesCOK() {
        return Quest.CLIENT_OF_KOUREND.getNotes();
    }

    @ConfigItem(
            keyName = "questNotesBIO",
            name = "Quest Notes",
            description = "Notes for supported quests",
            position = 34,
            hidden = true,
            unhide = "supportedQuests",
            unhideValue = "BIOHAZARD",
            section = "showQuestNotes"
    )
    default String questNotesBIO() {
        return Quest.BIOHAZARD.getNotes();
    }

    @ConfigItem(
            keyName = "questNotesGCat",
            name = "Quest Notes",
            description = "Notes for supported quests",
            position = 35,
            hidden = true,
            unhide = "supportedQuests",
            unhideValue = "GERTRUDES_CAT",
            section = "showQuestNotes"
    )
    default String questNotesGCat() {
        return Quest.GERTRUDES_CAT.getNotes();
    }

    @ConfigItem(
            keyName = "category",
            name = "Category",
            description = "Select the category of destinations",
            position = 100
    )
    default Category category() {
        return Category.NONE;
    }

    @ConfigItem(
            keyName = "catBanks",
            name = "Location",
            description = "Select the location to walk to",
            position = 101,
            hidden = true,
            unhide = "category",
            unhideValue = "BANKS"
    )
    default Banks catBanks() {
        return Banks.NONE;
    }

    @ConfigItem(
            keyName = "catBarcrawl",
            name = "Location",
            description = "Select the location to walk to",
            position = 102,
            hidden = true,
            unhide = "category",
            unhideValue = "BARCRAWL"
    )
    default Barcrawl catBarcrawl() {
        return Barcrawl.NONE;
    }

    @ConfigItem(
            keyName = "catCities",
            name = "Location",
            description = "Select the location to walk to",
            position = 103,
            hidden = true,
            unhide = "category",
            unhideValue = "CITIES"
    )
    default Cities catCities() {
        return Cities.NONE;
    }

    @ConfigItem(
            keyName = "catFarming",
            name = "Patch Type",
            description = "Select the Farming category you want",
            position = 110,
            hidden = true,
            unhide = "category",
            unhideValue = "FARMING"
    )
    default Farming catFarming() {
        return Farming.NONE;
    }

    @ConfigItem(
            keyName = "catFarmAllotments",
            name = "Patch",
            description = "Select the location to walk to",
            position = 111,
            hidden = true,
            unhide = "catFarming",
            unhideValue = "ALLOTMENTS"
    )
    default Allotments catFarmAllotments() {
        return Allotments.NONE;
    }

    @ConfigItem(
            keyName = "catFarmBushes",
            name = "Patch",
            description = "Select the location to walk to",
            position = 112,
            hidden = true,
            unhide = "catFarming",
            unhideValue = "BUSHES"
    )
    default Bushes catFarmBushes() {
        return Bushes.NONE;
    }

    @ConfigItem(
            keyName = "catFarmFruitTrees",
            name = "Patch",
            description = "Select the location to walk to",
            position = 113,
            hidden = true,
            unhide = "catFarming",
            unhideValue = "FRUIT_TREES"
    )
    default FruitTrees catFarmFruitTrees() {
        return FruitTrees.NONE;
    }

    @ConfigItem(
            keyName = "catFarmHerbs",
            name = "Patch",
            description = "Select the location to walk to",
            position = 114,
            hidden = true,
            unhide = "catFarming",
            unhideValue = "HERBS"
    )
    default Herbs catFarmHerbs() {
        return Herbs.NONE;
    }

    @ConfigItem(
            keyName = "catFarmHops",
            name = "Patch",
            description = "Select the location to walk to",
            position = 115,
            hidden = true,
            unhide = "catFarming",
            unhideValue = "HOPS"
    )
    default Hops catFarmHops() {
        return Hops.NONE;
    }

    @ConfigItem(
            keyName = "catFarmTrees",
            name = "Patch",
            description = "Select the location to walk to",
            position = 116,
            hidden = true,
            unhide = "catFarming",
            unhideValue = "TREES"
    )
    default Trees catFarmTrees() {
        return Trees.NONE;
    }

    @ConfigItem(
            keyName = "catGuilds",
            name = "Location",
            description = "Select the location to walk to",
            position = 103,
            hidden = true,
            unhide = "category",
            unhideValue = "GUILDS"
    )
    default Guilds catGuilds() {
        return Guilds.NONE;
    }

    @ConfigItem(
            keyName = "catSkilling",
            name = "Location",
            description = "Select the location to walk to",
            position = 104,
            hidden = true,
            unhide = "category",
            unhideValue = "SKILLING"
    )
    default Skilling catSkilling() {
        return Skilling.NONE;
    }

    @ConfigItem(
            keyName = "catSlayer",
            name = "Location",
            description = "Select the location to walk to",
            position = 105,
            hidden = true,
            unhide = "category",
            unhideValue = "SLAYER"
    )
    default Slayer catSlayer() {
        return Slayer.NONE;
    }

    @ConfigItem(
            keyName = "catMisc",
            name = "Location",
            description = "Select the location to walk to",
            position = 106,
            hidden = true,
            unhide = "category",
            unhideValue = "MISC"
    )
    default Misc catMisc() {
        return Misc.NONE;
    }

    @ConfigItem(
            keyName = "customLocation",
            name = "Custom Location",
            description = "Enter a Coordinate to walk to. Co-ordinate format should be x,y,z. Turn on Location or Tile Location in Developer Tools to obtain coordinates.",
            position = 135,
            hidden = true,
            unhide = "category",
            unhideValue = "CUSTOM"
    )
    default String customLocation() {
        return "0,0,0";
    }

    @ConfigItem(
            keyName = "rand",
            name = "Random Tile radius",
            description = "A random radius value applied to tiles",
            position = 140
    )
    default int rand() {
        return 3;
    }

    @ConfigItem(
            keyName = "disableRun",
            name = "Disable Running",
            description = "Disable running to arrive at your destination with 100% energy.",
            position = 145
    )
    default boolean disableRun() {
        return false;
    }

    @ConfigItem(
            keyName = "sendMsg",
            name = "Send message on destination",
            description = "Enables or Disables the message when you arrive at your destination",
            position = 145
    )
    default boolean sendMsg() {
        return true;
    }

    @ConfigItem(
            keyName = "closeMap",
            name = "Close Map",
            description = "Enable to close the world map after selecting your destination",
            position = 147
    )
    default boolean closeMap() {
        return true;
    }

    @ConfigItem(
            keyName = "enableUI",
            name = "Enable UI",
            description = "Enable to turn on in game UI",
            position = 150
    )
    default boolean enableUI() {
        return true;
    }

    @ConfigItem(
            keyName = "startButton",
            name = "Start/Stop",
            description = "Start/Stop plugin",
            position = 151
    )
    default Button startButton() {
        return new Button();
    }
}
