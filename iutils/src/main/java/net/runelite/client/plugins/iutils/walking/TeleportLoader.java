package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class TeleportLoader {
    private final Bot bot;

    public TeleportLoader(Bot bot) {
        this.bot = bot;
    }

    public List<Teleport> buildTeleports() {
        ArrayList<Teleport> teleports = new ArrayList<>();

//        if (ringOfDueling() != null) {
//            teleports.add(new Teleport(new Position(3315, 3235, 0), 2, () -> ringAction(ringOfDueling(), "Duel Arena")));
//            teleports.add(new Teleport(new Position(2440, 3090, 0), 2, () -> ringAction(ringOfDueling(), "Castle Wars")));
//            teleports.add(new Teleport(new Position(3151, 3635, 0), 2, () -> ringAction(ringOfDueling(), "Ferox Enclave")));
//        }
//
//        if (botsNecklace() != null) {
//            teleports.add(new Teleport(new Position(2898, 3553, 0), 2, () -> ringAction(botsNecklace(), "Burtrope")));
//            teleports.add(new Teleport(new Position(2520, 3571, 0), 2, () -> ringAction(botsNecklace(), "Barbarian Outpost")));
//        }

        return teleports;
    }

    private void ringAction(InventoryItem ring, String target) {

    }

    private void necklaceAction(InventoryItem necklace, String target) {

    }

    private InventoryItem ringOfDueling() {
        return bot.inventory().withId(2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566).first();
    }

    private InventoryItem botsNecklace() {
        return bot.inventory().withId(3863, 3855, 3857, 3859, 3861, 3863, 3865, 3867).first();
    }

    private InventoryItem combatBracelet() {
        return bot.inventory().withId(11972, 11974, 11118, 11120, 11122, 11124).first();
    }

    private InventoryItem ringOfWealth() {
        return bot.inventory().withId(11980, 11982, 11984, 11986, 11988).first();
    }

    private InventoryItem amuletOfGlory() {
        return bot.inventory().withId(1706, 1708, 1710, 1712, 11976, 11978).first();
    }

    private InventoryItem necklaceOfPassage() {
        return bot.inventory().withId(21146, 21149, 21151, 21153, 21155).first();
    }

    private InventoryItem burningAmulet() {
        return bot.inventory().withId(21166, 21171, 21173, 21175, 21167).first();
    }

    private InventoryItem xericsTalisman() {
        return bot.inventory().withId(13393).first();
    }

    private InventoryItem slayerRing() {
        return bot.inventory().withId(11866, 11867, 11868, 11869, 11870, 11871, 11872, 11873, 21268).first();
    }

    private InventoryItem digsitePendant() {
        return bot.inventory().withId(11190, 11191, 11192, 11193, 11194).first();
    }

    private InventoryItem drakansMedallion() {
        return bot.inventory().withId(22400).first();
    }
}
