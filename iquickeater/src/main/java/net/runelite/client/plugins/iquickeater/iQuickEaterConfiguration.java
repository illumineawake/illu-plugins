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
package net.runelite.client.plugins.iquickeater;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("iQuickEater")
public interface iQuickEaterConfiguration extends Config {
    @Range(min = 1, max = 99)
    @ConfigItem(
            keyName = "minEatHP",
            name = "Minimum Eat HP",
            description = "Minimum HP to eat at. i.e. will always eat",
            position = 0
    )
    default int minEatHP() {
        return 10;
    }

    @Range(min = 1, max = 100)
    @ConfigItem(
            keyName = "maxEatHP",
            name = "Maximum Eat HP",
            description = "Highest HP to consider eating. Value MUST be higher than minimum HP config. If HP drops below this value bot may randomly decide to eat.",
            position = 1
    )
    default int maxEatHP() {
        return 20;
    }

    @ConfigItem(
            keyName = "usePercent",
            name = "Use percentage instead of flat values",
            description = "This will take the value of minimum and maximum eat HP to be a percentage instead.",
            position = 2
    )
    default boolean usePercent() {
        return false;
    }

    @ConfigItem(
            keyName = "drinkAntiPoison",
            name = "Drink Anti-Poison/Dote Potions",
            description = "Enable to drink Anti-Poisons or Antidotes when poisoned",
            position = 4
    )
    default boolean drinkAntiPoison() {
        return true;
    }

    @ConfigItem(
            keyName = "drinkPrayer",
            name = "Drink Prayer restoration Potions",
            description = "Enable to drink Prayer/Super Restore pots below given Prayer levels",
            position = 5
    )
    default boolean drinkPrayer() {
        return false;
    }

    @ConfigItem(
            keyName = "minPrayerPoints",
            name = "Minimum Prayer Points",
            description = "Minimum Prayer points to drink at. i.e. will always drink",
            hidden = true,
            unhide = "drinkPrayer",
            position = 6
    )
    default int minPrayerPoints() {
        return 10;
    }

    @ConfigItem(
            keyName = "maxPrayerPoints",
            name = "Maximum Prayer Points",
            description = "Highest Prayer points to consider drinking. Value MUST be higher than minimum Prayer config. If Prayer drops below this value bot may randomly decide to eat.",
            hidden = true,
            unhide = "drinkPrayer",
            position = 7
    )
    default int maxPrayerPoints() {
        return 20;
    }

    @ConfigItem(
            keyName = "drinkStrength",
            name = "Enable Drink Strength Pots",
            description = "Enable to drink pots to restore strength",
            position = 8
    )
    default boolean drinkStrength() {
        return false;
    }

    @ConfigItem(
            keyName = "strengthPoints",
            name = "Strength Points",
            description = "Drink strength boosting pot below this level",
            position = 9,
            hidden = true,
            unhide = "drinkStrength"
    )
    default int strengthPoints() {
        return 100;
    }

    @ConfigItem(
            keyName = "drinkAttack",
            name = "Enable Drink Attack Pots",
            description = "Enable to drink pots to restore attack",
            position = 10
    )
    default boolean drinkAttack() {
        return false;
    }

    @ConfigItem(
            keyName = "attackPoints",
            name = "Attack Points",
            description = "Drink attack boosting pot below this level",
            position = 11,
            hidden = true,
            unhide = "drinkAttack"
    )
    default int attackPoints() {
        return 100;
    }

    @ConfigItem(
            keyName = "drinkDefence",
            name = "Enable Drink Defence Pots",
            description = "Enable to drink pots to restore defence",
            position = 12
    )
    default boolean drinkDefence() {
        return false;
    }

    @ConfigItem(
            keyName = "defencePoints",
            name = "Defence Points",
            description = "Drink defence boosting pot below this level",
            position = 13,
            hidden = true,
            unhide = "drinkDefence"
    )
    default int defencePoints() {
        return 100;
    }

    @ConfigItem(
            keyName = "drinkRanged",
            name = "Enable Drink Ranged Pots",
            description = "Enable to drink pots to restore ranged",
            position = 14
    )
    default boolean drinkRanged() {
        return false;
    }

    @ConfigItem(
            keyName = "rangedPoints",
            name = "Ranged Points",
            description = "Drink ranged boosting pot below this level",
            position = 15,
            hidden = true,
            unhide = "drinkRanged"
    )
    default int rangedPoints() {
        return 100;
    }

    @ConfigItem(
            keyName = "drinkMagic",
            name = "Enable Drink Magic Pots",
            description = "Enable to drink pots to restore magic",
            position = 16
    )
    default boolean drinkMagic() {
        return false;
    }

    @ConfigItem(
            keyName = "magicPoints",
            name = "Magic Points",
            description = "Drink magic boosting pot below this level",
            position = 17,
            hidden = true,
            unhide = "drinkMagic"
    )
    default int magicPoints() {
        return 100;
    }

    @ConfigItem(
            keyName = "drinkStamina",
            name = "Drink Stamina Potions",
            description = "Enable to drink Stamina Potions below given energy level",
            position = 18
    )
    default boolean drinkStamina() {
        return false;
    }

    @ConfigItem(
            keyName = "maxDrinkEnergy",
            name = "Drink stamina below energy",
            description = "This is the maximum energy amount",
            position = 20,
            hidden = true,
            unhide = "drinkStamina"
    )
    default int maxDrinkEnergy() {
        return 60;
    }

    @ConfigItem(
            keyName = "randEnergy",
            name = "random variation for drink energy (subtracted from max)",
            description = "A random value that is subtracted from max drink energy. E.g. a random value of '20' with a max drink energy of 60 would " +
                    "cause stamina pot to be drunk at a random value between 40 and 60",
            position = 30,
            hidden = true,
            unhide = "drinkStamina"
    )
    default int randEnergy() {
        return 20;
    }

    @ConfigItem(
            keyName = "drinkAntiFire",
            name = "Drink Anti-Fire Potions",
            description = "Enable to drink Anti-Fire when burnt",
            position = 3
    )
    default boolean drinkAntiFire() {
        return true;
    }

    @ConfigItem(
            keyName = "keepPNeckEquipped",
            name = "Keep Phoenix Neck Equipped",
            description = "This will keep a phoenix necklace equipped.",
            position = 40
    )
    default boolean keepPNeckEquipped() {
        return false;
    }

    @ConfigItem(
            keyName = "activateImbHeart",
            name = "Reactivate Imbued Heart",
            description = "Enable to automatically reactivate the imbued heart - activate heart once manually. ",
            position = 41
    )
    default boolean activateImbHeart() {
        return false;
    }
    
    @ConfigItem(
            keyName = "useInvokes",
            name = "Use Invokes",
            description = "WARNING: This is potentially detectable. This will use items without sending any click data" +
                    "<br>Use this if iquickeater is conflicting with another plugin",
            position = 51
    )
    default boolean useInvokes() {
        return false;
    }
}
