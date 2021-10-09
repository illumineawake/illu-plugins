package net.runelite.client.plugins.iutils.walking;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.api.SpellBook;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.scene.Position;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public enum TeleportSpell {

    VARROCK_TELEPORT(SpellBook.Type.STANDARD, 25, new Position(3212, 3424, 0), false, "Varrock Teleport", new RuneRequirement(1, RuneElement.LAW), new RuneRequirement(3, RuneElement.AIR), new RuneRequirement(1, RuneElement.FIRE)),
    LUMBRIDGE_TELEPORT(SpellBook.Type.STANDARD, 31, new Position(3225, 3219, 0), false, "Lumbridge Teleport", new RuneRequirement(1, RuneElement.LAW), new RuneRequirement(3, RuneElement.AIR), new RuneRequirement(1, RuneElement.EARTH)),
    FALADOR_TELEPORT(SpellBook.Type.STANDARD, 37, new Position(2966, 3379, 0), false, "Falador Teleport", new RuneRequirement(1, RuneElement.LAW), new RuneRequirement(3, RuneElement.AIR), new RuneRequirement(1, RuneElement.WATER)),
    CAMELOT_TELEPORT(SpellBook.Type.STANDARD, 45, new Position(2757, 3479, 0), true, "Camelot Teleport", new RuneRequirement(1, RuneElement.LAW), new RuneRequirement(5, RuneElement.AIR)),
    ARDOUGNE_TELEPORT(SpellBook.Type.STANDARD, 51, new Position(2661, 3300, 0), true, "Ardougne Teleport", new RuneRequirement(2, RuneElement.LAW), new RuneRequirement(2, RuneElement.WATER)),
//TODO    KOUREND_TELEPORT(SpellBook.Type.STANDARD, 69, "Kourend Castle Teleport", new RuneRequirement(2, RuneElement.LAW), new RuneRequirement(2, RuneElement.SOUL), new RuneRequirement(4, RuneElement.WATER), new RuneRequirement(5, RuneElement.FIRE)),

    ;

    private SpellBook.Type spellBookType;
    private int requiredLevel;
    private Position location;
    private boolean members;
    private String spellName;
    private RuneRequirement[] recipe;

    TeleportSpell(SpellBook.Type spellBookType, int level, Position location, boolean members, String spellName, RuneRequirement... recipe) {
        this.spellBookType = spellBookType;
        this.requiredLevel = level;
        this.location = location;
        this.members = members;
        this.spellName = spellName;
        this.recipe = recipe;
    }

    public RuneRequirement[] getRecipe() {
        return recipe;
    }

    public String getSpellName() {
        return spellName;
    }

    public Position getLocation() {
        return location;
    }

    public List<ItemQuantity> recipe(Game game) {
        List<ItemQuantity> items = new ArrayList<>();

        for (RuneRequirement pair : recipe) {
            int amountRequiredForSpell = pair.getFirst();
            RuneElement runeElement = pair.getSecond();
            int amount = amountRequiredForSpell - runeElement.getCount(game);
            if (amount > 0) {
                items.add(new ItemQuantity(pair.getSecond().getRuneId(), amount));
            }
        }
        return items;
    }

    public boolean hasRequirements(Game game) {
        if (SpellBook.getCurrentSpellBook(game) != spellBookType) {
            return false;
        }
        if (this.members && !game.membersWorld()) {
            return false;
        }
        if (requiredLevel > game.modifiedLevel(Skill.MAGIC)) {
            return false;
        }
        if (this == ARDOUGNE_TELEPORT && game.varp(165) < 30) { //TODO may cause issues
            return false;
        }

        return true;
    }

    public boolean canUse(Game game) {
        if (!hasRequirements(game)) {
            return false;
        }

        for (RuneRequirement pair : recipe) {
            int amountRequiredForSpell = pair.getFirst();
            RuneElement runeElement = pair.getSecond();
            if (runeElement.getCount(game) < amountRequiredForSpell) {
                return false;
            }
        }
        return true;
    }

}