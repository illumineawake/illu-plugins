package net.runelite.client.plugins.iutils.walking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.iutils.Spells;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.InventoryItem;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.ui.Chatbox;
import net.runelite.client.plugins.iutils.ui.StandardSpellbook;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static net.runelite.client.plugins.iutils.walking.TeleportSpell.VARROCK_TELEPORT;

@Slf4j
public class TeleportLoader {
    public static final int[] RING_OF_DUELING = {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566};
    public static final int[] GAMES_NECKLACE = {3853, 3863, 3855, 3857, 3859, 3861, 3863, 3865, 3867};
    public static final int[] COMBAT_BRACELET = {11972, 11974, 11118, 11120, 11122, 11124};
    public static final int[] RING_OF_WEALTH = {11980, 11982, 11984, 11986, 11988};
    public static final int[] AMULET_OF_GLORY = {1706, 1708, 1710, 1712, 11976, 11978};
    public static final int[] NECKLACE_OF_PASSAGE = {21146, 21149, 21151, 21153, 21155};
    public static final int[] BURNING_AMULET = {21166, 21171, 21173, 21175, 21167};
    public static final int XERICS_TALISMAN = 13393;
    public static final int[] SLAYER_RING = {11866, 11867, 11868, 11869, 11870, 11871, 11872, 11873, 21268};
    public static final int[] DIGSITE_PENDANT = {11190, 11191, 11192, 11193, 11194};
    public static final int DRAKANS_MEDALLION = 22400;
    public static final int[] SKILLS_NECKLACE = {11111, 11109, 11107, 11105, 11970, 11968};
    private final Game game;
    private final Chatbox chatbox;
    protected StandardSpellbook standardSpellbook;

    public TeleportLoader(Game game) {
        this.game = game;
        this.chatbox = new Chatbox(game);
        this.standardSpellbook = new StandardSpellbook(game);
    }

    public List<Teleport> buildTeleports() {
        var teleports = new ArrayList<Teleport>();
        var playerPosition = game.localPlayer().position();

        if (game.membersWorld()) {
            if (ringOfDueling() != null) {
                teleports.add(new Teleport(new Position(3315, 3235, 0), 6, () -> jewelleryAction(ringOfDueling(), "Duel Arena")));
                teleports.add(new Teleport(new Position(2440, 3090, 0), 2, () -> jewelleryAction(ringOfDueling(), "Castle Wars")));
                teleports.add(new Teleport(new Position(3151, 3635, 0), 2, () -> jewelleryAction(ringOfDueling(), "Ferox Enclave")));
            }

            if (gamesNecklace() != null) {
                teleports.add(new Teleport(new Position(2898, 3553, 0), 2, () -> jewelleryAction(gamesNecklace(), "Burthorpe")));
                teleports.add(new Teleport(new Position(2520, 3571, 0), 6, () -> jewelleryAction(gamesNecklace(), "Barbarian Outpost")));
                teleports.add(new Teleport(new Position(2964, 4382, 2), 2, () -> jewelleryAction(gamesNecklace(), "Corporeal Beast")));
                teleports.add(new Teleport(new Position(3244, 9501, 2), 2, () -> jewelleryAction(gamesNecklace(), "Tears of Guthix")));
                teleports.add(new Teleport(new Position(1624, 3938, 0), 1, () -> jewelleryAction(gamesNecklace(), "Wintertodt Camp")));
            }

            if (combatBracelet() != null) {
                teleports.add(new Teleport(new Position(2882, 3548, 0), 2, () -> jewelleryAction(combatBracelet(), "Warriors' Guild")));
                teleports.add(new Teleport(new Position(3191, 3367, 0), 2, () -> jewelleryAction(combatBracelet(), "Champions' Guild")));
                teleports.add(new Teleport(new Position(3052, 3488, 0), 2, () -> jewelleryAction(combatBracelet(), "Monastery")));
                teleports.add(new Teleport(new Position(2655, 3441, 0), 2, () -> jewelleryAction(combatBracelet(), "Ranging Guild")));
            }

            if (skillsNecklace() != null) {
                teleports.add(new Teleport(new Position(2611, 3390, 0), 6, () -> jewelleryAction(skillsNecklace(), "Fishing Guild")));
                teleports.add(new Teleport(new Position(3050, 9763, 0), 6, () -> jewelleryAction(skillsNecklace(), "Mining Guild")));
                teleports.add(new Teleport(new Position(2933, 3295, 0), 6, () -> jewelleryAction(skillsNecklace(), "Crafting Guild")));
                teleports.add(new Teleport(new Position(3143, 3440, 0), 6, () -> jewelleryAction(skillsNecklace(), "Cooking Guild")));
                teleports.add(new Teleport(new Position(1662, 3505, 0), 6, () -> jewelleryAction(skillsNecklace(), "Woodcutting Guild")));
                teleports.add(new Teleport(new Position(1249, 3718, 0), 6, () -> jewelleryAction(skillsNecklace(), "Farming Guild")));
            }

            if (ringOfWealth() != null) {
                teleports.add(new Teleport(new Position(3163, 3478, 0), 2, () -> jewelleryAction(ringOfWealth(), "Grand Exchange")));
                //teleports.add(new Teleport(new Position(2996, 3375, 0), 2, () -> jewleryAction(ringOfWealth(), "Falador")));
//            teleports.add(new Teleport(new Position, 2, () -> jewleryAction(ringOfWealth(), "Miscellania")));
                teleports.add(new Teleport(new Position(2829, 10167, 0), 2, () -> jewelleryAction(ringOfWealth(), "Dondakan")));
            }

            if (amuletOfGlory() != null) {
                teleports.add(new Teleport(new Position(3087, 3496, 0), 0, () -> jewelleryAction(amuletOfGlory(), "Edgeville")));
                teleports.add(new Teleport(new Position(2918, 3176, 0), 0, () -> jewelleryAction(amuletOfGlory(), "Karamja")));
                teleports.add(new Teleport(new Position(3105, 3251, 0), 0, () -> jewelleryAction(amuletOfGlory(), "Draynor Village")));
                teleports.add(new Teleport(new Position(3293, 3163, 0), 0, () -> jewelleryAction(amuletOfGlory(), "Al Kharid")));
            }

            if (necklaceOfPassage() != null) {
                teleports.add(new Teleport(new Position(3114, 3179, 0), 2, () -> jewelleryAction(necklaceOfPassage(), "Wizards' Tower")));
                teleports.add(new Teleport(new Position(2430, 3348, 0), 2, () -> jewelleryAction(necklaceOfPassage(), "The Outpost")));
                teleports.add(new Teleport(new Position(3405, 3157, 0), 2, () -> jewelleryAction(necklaceOfPassage(), "Eagles' Eyrie")));
            }

            if (burningAmulet() != null) {
                teleports.add(new Teleport(new Position(3235, 3636, 0), 2, () -> jewelleryAction(burningAmulet(), "Chaos Temple")));
                teleports.add(new Teleport(new Position(3038, 3651, 0), 2, () -> jewelleryAction(burningAmulet(), "Bandit Camp")));
                teleports.add(new Teleport(new Position(3028, 3842, 0), 2, () -> jewelleryAction(burningAmulet(), "Lava Maze")));
            }

            if (slayerRing() != null) {
                teleports.add(new Teleport(new Position(2432, 3423, 0), 2, () -> jewelleryAction(slayerRing(), "Stronghold Slayer Cave")));
                teleports.add(new Teleport(new Position(3422, 3537, 0), 2, () -> jewelleryAction(slayerRing(), "Slayer Tower")));
                teleports.add(new Teleport(new Position(2802, 10000, 0), 2, () -> jewelleryAction(slayerRing(), "Fremennik Slayer Dungeon")));
                teleports.add(new Teleport(new Position(3185, 4601, 0), 2, () -> jewelleryAction(slayerRing(), "Tarn's Lair")));
                teleports.add(new Teleport(new Position(2028, 4636, 0), 2, () -> jewelleryAction(slayerRing(), "Dark Beasts")));
            }

            if (digsitePendant() != null) {
                teleports.add(new Teleport(new Position(3341, 3445, 0), 2, () -> jewelleryAction(digsitePendant(), "Digsite")));
//            teleports.add(new Teleport(new Position, 2, () -> jewleryAction(digsitePendant(), "Fossil Island")));
                teleports.add(new Teleport(new Position(3549, 10456, 0), 2, () -> jewelleryAction(digsitePendant(), "Lithkren")));
            }

//        if (drakansMedallion() != null) {
//            teleports.add(new Teleport(new Position(3649, 3230, 0), 0, () -> jewleryAction(drakansMedallion(), "Ver Sinhaza")));
//            teleports.add(new Teleport(new Position(3592, 3337, 0), 0, () -> jewleryAction(drakansMedallion(), "Darkmeyer")));
//        }

            for (TeleportTab teleportTab : TeleportTab.values()) {
                if (teleportTab.canUse(game)) {
                    log.info("Adding teleport tab: {}", teleportTab.getTabletName());
                    teleports.add(new Teleport(teleportTab.getLocation(), 5, () -> inventoryAction(teleportTab.getTabletName(), "Break")));
                }
            }
        }

        for (TeleportSpell teleportSpell : TeleportSpell.values()) {
            if (!teleportSpell.canUse(game)) continue;
            if (teleportSpell.getLocation().distanceTo(playerPosition) > 20) {
                log.info("Adding teleport method: " + teleportSpell.getSpellName());
                teleports.add(new Teleport(teleportSpell.getLocation(), 5, () -> cast(teleportSpell.getSpellName())));
            }
        }

        return teleports;
    }

    //Jewellery
    private void jewelleryAction(InventoryItem item, String target) { // TODO
        item.interact("Rub");
        chatbox.chat(target);
    }

    private InventoryItem ringOfDueling() {
        return game.inventory().withId(RING_OF_DUELING).first();
    }

    private InventoryItem gamesNecklace() {
        return game.inventory().withId(GAMES_NECKLACE).first();
    }

    private InventoryItem combatBracelet() {
        return game.inventory().withId(COMBAT_BRACELET).first();
    }

    private InventoryItem skillsNecklace() {
        return game.inventory().withId(SKILLS_NECKLACE).first();
    }

    private InventoryItem ringOfWealth() {
        return game.inventory().withId(RING_OF_WEALTH).first();
    }

    private InventoryItem amuletOfGlory() {
        return game.inventory().withId(AMULET_OF_GLORY).first();
    }

    private InventoryItem necklaceOfPassage() {
        return game.inventory().withId(NECKLACE_OF_PASSAGE).first();
    }

    private InventoryItem burningAmulet() {
        return game.inventory().withId(BURNING_AMULET).first();
    }

    private InventoryItem xericsTalisman() {
        return game.inventory().withId(XERICS_TALISMAN).first();
    }

    private InventoryItem slayerRing() {
        return game.inventory().withId(SLAYER_RING).first();
    }

    private InventoryItem digsitePendant() {
        return game.inventory().withId(DIGSITE_PENDANT).first();
    }

    private InventoryItem drakansMedallion() {
        return game.inventory().withId(DRAKANS_MEDALLION).first();
    }

    //Magic
    private void cast(String spellName) { // TODO
        log.info("Casting teleport - {}", spellName);
        standardSpellbook.castSpell(Spells.getWidget(spellName));
    }

    private void inventoryAction(String itemName, String action) {
        game.inventory().withName(itemName).withAction(action).first().interact(action);
    }

}
