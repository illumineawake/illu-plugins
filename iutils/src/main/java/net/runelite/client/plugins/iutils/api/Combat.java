package net.runelite.client.plugins.iutils.api;

import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.iNPC;
import net.runelite.client.plugins.iutils.ui.Prayer;

import javax.inject.Inject;

public class Combat {
    private final Game game;
    private final Prayers prayers;

    @Inject
    public Combat(Game game) {
        this.game = game;
        this.prayers = new Prayers(game);
    }

    public void kill(iNPC npc, Prayer... prayers) {
        for (var prayer : prayers) {
            this.prayers.setEnabled(prayer, true);
        }

        npc.interact("Attack");

        try {
            while (game.npcs().withIndex(npc.index()).withAction("Attack").exists()) {
                heal();
                restorePrayer();
                restoreStats();
                attack(npc);
                game.tick();
            }
        } finally {
            for (var prayer : prayers) {
                this.prayers.setEnabled(prayer, false);
            }
        }
    }

    public void attack(iNPC npc) {
        // todo: target doen't necessarily mean in combat (could be chat)
        iNPC target = (iNPC) game.localPlayer().target();

        if (target == null || target.index() != npc.index()) {
            if (game.localPlayer().target() != null)
                System.out.println("Target doesn't equal npc: " + npc.toString() + " my target: " + target.toString());
            npc.interact("Attack");
        }
    }

    private boolean needsStatRestore() {
        var matters = new Skill[]{Skill.ATTACK, Skill.DEFENCE, Skill.STRENGTH};
        for (var skill : matters) {
            if (game.modifiedLevel(skill) < game.baseLevel(skill)) {
                return true;
            }
        }
        return false;
    }

    public void restoreStats() {
        if (game.inventory().withNamePart("restore").exists() && needsStatRestore()) {
            game.inventory().withNamePart("restore").first().interact("Drink");
        }
    }

    public void restorePrayer() {
        if (game.modifiedLevel(Skill.PRAYER) < game.baseLevel(Skill.PRAYER) / 2) {
            //todo add super restores?
            if (game.inventory().withNamePart("Prayer potion(").exists()) {
                game.inventory().withNamePart("Prayer potion(").first().interact("Drink");
            }
        }
    }

    public void heal() {
        if (game.modifiedLevel(Skill.HITPOINTS) < game.baseLevel(Skill.HITPOINTS) / 2) {
            var food = game.inventory().withAction("Eat").first();
            if (food != null) {
                food.interact("Eat");
                game.tick();
            }
        }
    }

    public void setAutoRetaliate(boolean autoRetaliate) {
        if (autoRetaliate() != autoRetaliate) {
            game.widget(593, 30).interact("Auto retaliate");
            game.waitUntil(() -> autoRetaliate() == autoRetaliate);
        }
    }

    public boolean autoRetaliate() {
        return game.varp(172) == 0;
    }
}
