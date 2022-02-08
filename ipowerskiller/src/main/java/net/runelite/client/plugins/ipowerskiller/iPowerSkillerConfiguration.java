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
package net.runelite.client.plugins.ipowerskiller;

import net.runelite.client.config.*;

@ConfigGroup("iPowerSkiller")
public interface iPowerSkillerConfiguration extends Config {

    @ConfigSection(
            keyName = "delayConfig",
            name = "Sleep Delay Configuration",
            description = "Configure how the bot handles sleep delays",
            position = 2,
            closedByDefault = true
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
            position = 3,
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
            position = 4,
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
            position = 5,
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
            position = 6,
            section = "delayConfig"
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 7,
            section = "delayConfig"
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

    @ConfigSection(
            keyName = "delayTickConfig",
            name = "Game Tick Configuration",
            description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
            position = 8,
            closedByDefault = true
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
            position = 9,
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
            position = 10,
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
            position = 11,
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
            position = 12,
            section = "delayTickConfig"
    )
    default int tickDelayDeviation() {
        return 1;
    }

    @ConfigItem(
            keyName = "tickDelayWeightedDistribution",
            name = "Game Tick Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 13,
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
            keyName = "instructions",
            name = "",
            description = "Instructions. Don't enter anything into this field",
            position = 20,
            title = "instructionsTitle"
    )
    default String instructions() {
        return "Use Developer Tools to determine the Type and ID of the object you want to power-skill on. " +
                "Typically in-game objects that have blue hover text are Game Objects (trees, rocks etc.) and objects that have yellow text are NPCs (fishing spots etc.)";
    }

    @ConfigSection(
            keyName = "skillerConfig",
            name = "Power Skiller Configuration",
            description = "",
            position = 60
    )
    String skillerConfig = "skillerConfig";

    @ConfigItem(
            keyName = "type",
            name = "Object Type",
            description = "Type of Object. Typically in-game objects that have blue hover text are Game Objects (trees, rocks etc.) " +
                    "and objects that have yellow text are NPCs (e.g. fishing spots). Use Developer Tools to determine Object Type and ID.",
            position = 70,
            section = "skillerConfig"
    )
    default iPowerSkillerType type() {
        return iPowerSkillerType.GAME_OBJECT;
    }

    @ConfigItem(
            keyName = "objectIds",
            name = "IDs to power-skill",
            description = "Separate with comma",
            position = 80,
            section = "skillerConfig"
    )
    default String objectIds() {
        return "";
    }

    @Range(
            min = 1,
            max = 60
    )
    @ConfigItem(
            keyName = "locationRadius",
            name = "Location Radius",
            description = "Radius to search for GameObjects.",
            position = 81,
            section = "skillerConfig"
    )
    default int locationRadius() {
        return 10;
    }

    @ConfigItem(
            keyName = "drawLocationRadius",
            name = "Draw Location Radius",
            description = "Draw location Radius on screen.",
            position = 82,
            section = "skillerConfig"
    )
    default boolean drawlocationRadius() {
        return false;
    }

    @ConfigItem(
            keyName = "safeSpot",
            name = "Safe spot",
            description = "Safe spot will force your character to always return to the tile you started the plugin on",
            position = 83,
            section = "skillerConfig"
    )
    default boolean safeSpot() {
        return false;
    }

    @ConfigItem(
            keyName = "safeSpotRadius",
            name = "Safe spot radius",
            description = "Radius of the safe spot to return to. 0 will always return to the same tile, 1 will return to a 1 tile radius of safespot",
            position = 84,
            hidden = true,
            unhide = "safeSpot",
            section = "skillerConfig"
    )
    default int safeSpotRadius() {
        return 1;
    }

    @ConfigItem(
            keyName = "enableUI",
            name = "Enable UI",
            description = "Enable to turn on in game UI",
            position = 85,
            section = "skillerConfig"
    )
    default boolean enableUI() {
        return true;
    }

    @ConfigSection(
            keyName = "opcodeConfig",
            name = "Menu Opcodes",
            description = "",
            position = 86,
            closedByDefault = true
    )
    String opcodeConfig = "opcodeConfig";

    @ConfigItem(
            keyName = "customOpcode",
            name = "Use custom Menu Opcode",
            description = "Enable to use a custom Menu Opcode. Use this in scenarios where the default Menu Opcode isn't working." +
                    "Example default NPC Opcode works for fishing with lobster pots but not harpooning. To harpoon set an opcode of 10.",
            position = 87,
            section = "opcodeConfig"
    )
    default boolean customOpcode() {
        return false;
    }

    @ConfigItem(
            keyName = "printOpcode",
            name = "Print Opcode in Game Chat",
            description = "Enable to Print the Opcode of the action you want in Game Chat when you click it. " +
                    "Use this if you're unsure what Opcode to use",
            position = 88,
            hidden = true,
            unhide = "customOpcode",
            section = "opcodeConfig"
    )
    default boolean printOpcode() {
        return false;
    }

    @ConfigItem(
            keyName = "objectOpcode",
            name = "Object Menu Opcode",
            description = "Enable to use a custom Menu Opcode for interacting with an object." +
                    "Example default NPC Opcode works for fishing with lobster pots but not harpooning. To harpoon set an opcode of 10. Use for pickpocketing etc.",
            position = 89,
            section = "opcodeConfig",
            hidden = true,
            unhide = "customOpcode"

    )
    default boolean objectOpcode() {
        return false;
    }

    @ConfigItem(
            keyName = "objectOpcodeValue",
            name = "Object Opcode Value",
            description = "Input custom Opcode value" +
                    "Example default NPC Opcode works for fishing with lobster pots but not harpooning. To harpoon set an opcode of 10.",
            position = 90,
            hidden = true,
            unhide = "objectOpcode",
            section = "opcodeConfig"
    )
    default int objectOpcodeValue() {
        return 10;
    }

    @ConfigItem(
            keyName = "inventoryMenu",
            name = "Inventory Custom Menu",
            description = "Enable to use a custom Menu for interacting with an inventory item." +
                    "Example emptying jars, combining items etc.",
            position = 91,
            section = "opcodeConfig",
            hidden = true,
            unhide = "customOpcode"

    )
    default boolean inventoryMenu() {
        return false;
    }

    @ConfigItem(
            keyName = "combineItems",
            name = "Combine Inventory Items",
            description = "Enable to combine 2 items in inventory together" +
                    "Example cutting fish",
            position = 92,
            section = "opcodeConfig",
            hidden = true,
            unhide = "inventoryMenu"

    )
    default boolean combineItems() {
        return false;
    }

    @ConfigItem(
            keyName = "toolId",
            name = "Tool ID",
            description = "Inventory ID of the tool you want to use for combining, e.g. knife, tinderbox etc.",
            position = 93,
            hidden = true,
            unhide = "combineItems",
            section = "opcodeConfig"
    )
    default int toolId() {
        return 0;
    }

    @ConfigItem(
            keyName = "inventoryOpcodeValue",
            name = "Inventory Opcode Value",
            description = "Input custom Opcode value. If you are combining items this is the opcode when you click on the tool, if you are not combining items it is the opcode of the action you're performing, e.g. emptying jars",
            position = 94,
            hidden = true,
            unhide = "inventoryMenu",
            section = "opcodeConfig"
    )
    default int inventoryOpcodeValue() {
        return 0;
    }

    @ConfigSection(
            keyName = "dropConfig",
            name = "Dropping & Banking",
            description = "",
            position = 100
    )
    String dropConfig = "dropConfig";

    @ConfigItem(
            keyName = "bankItems",
            name = "Bank gathered items (Beta)",
            description = "Enable to bank your items instead of drop",
            position = 101,
            section = "dropConfig"
    )
    default boolean bankItems() {
        return false;
    }

    @ConfigItem(
            keyName = "dropInventory",
            name = "Drop/Bank entire inventory",
            description = "Enable to drop your entire inventory",
            position = 102,
            section = "dropConfig"
    )
    default boolean dropInventory() {
        return false;
    }

    @ConfigItem(
            keyName = "requiredItems",
            name = "Required inventory item IDs",
            description = "Separate with comma. Bot will stop if required items are not in inventory, e.g. fishing bait. Leave at 0 if there are none.",
            position = 103,
            hide = "dropInventory",
            section = "dropConfig"
    )
    default String requiredItems() {
        return "";
    }

    @ConfigItem(
            keyName = "logout",
            name = "Logout when out of required IDs",
            description = "Bot will logout if required items are not in inventory, e.g. fishing bait.",
            position = 104,
            hide = "dropInventory",
            section = "dropConfig"
    )
    default boolean logout() {
        return true;
    }

    @ConfigItem(
            keyName = "items",
            name = "Item IDs to drop/not drop or Bank",
            description = "Separate with comma, enable below option to not drop/bank these IDs.",
            position = 110,
            hide = "dropInventory",
            section = "dropConfig"
    )
    default String items() {
        return "";
    }

    @ConfigItem(
            keyName = "dropExcept",
            name = "Drop/Bank all except above IDs",
            description = "Enable to drop/Bank all items except the given IDs",
            position = 120,
            hide = "dropInventory",
            section = "dropConfig"
    )
    default boolean dropExcept() {
        return true;
    }

    @ConfigItem(
            keyName = "dropOne",
            name = "Get 1 Drop 1",
            description = "Tick manipulation",
            position = 121,
            section = "dropConfig"
    )
    default boolean dropOne() {
        return false;
    }

    @ConfigItem(
            keyName = "startButton",
            name = "Start/Stop",
            description = "Test button that changes variable value",
            position = 150
    )
    default Button startButton() {
        return new Button();
    }
}
