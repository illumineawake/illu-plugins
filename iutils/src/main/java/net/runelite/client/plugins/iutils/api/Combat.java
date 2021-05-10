package net.runelite.client.plugins.iutils.api;

import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.iNPC;
import net.runelite.client.plugins.iutils.ui.Prayer;

import javax.inject.Inject;

public class Combat {
    private final Bot bot;
    private final Prayers prayers;

    @Inject
    public Combat(Bot bot) {
        this.bot = bot;
        this.prayers = new Prayers(bot);
    }

    public void kill(iNPC npc, Prayer... prayers) {
        for (var prayer : prayers) {
            this.prayers.setEnabled(prayer, true);
        }

        npc.interact("Attack");

        try {
            while (bot.npcs().withIndex(npc.index()).withAction("Attack").exists()) {
                heal();
                restorePrayer();
                restoreStats();
                attack(npc);
                bot.tick();
            }
        } finally {
            for (var prayer : prayers) {
                this.prayers.setEnabled(prayer, false);
            }
        }
    }

    private void attack(iNPC npc) {
        // todo: target doen't necessarily mean in combat (could be chat)
        iNPC target = (iNPC)bot.localPlayer().target();

        if (target == null || target.index() != npc.index()) {
            if (bot.localPlayer().target() != null)
                System.out.println("Target doesn't equal npc: " + npc.toString() + " my target: " + target.toString());
            npc.interact("Attack");
        }
    }

    private boolean needsStatRestore() {
        var matters = new Skill[]{Skill.ATTACK, Skill.DEFENCE, Skill.STRENGTH};
        for (var skill : matters) {
            if (bot.modifiedLevel(skill) < bot.baseLevel(skill)) {
                return true;
            }
        }
        return false;
    }

    private void restoreStats() {
        if (bot.inventory().withNamePart("restore").exists() && needsStatRestore()) {
            bot.inventory().withNamePart("restore").first().interact("Drink");
        }
    }

    private void restorePrayer() {
        if (bot.modifiedLevel(Skill.PRAYER) < bot.baseLevel(Skill.PRAYER) / 2) {
            //todo add super restores?
            if (bot.inventory().withNamePart("Prayer potion(").exists()) {
                bot.inventory().withNamePart("Prayer potion(").first().interact("Drink");
            }
        }
    }

    public void heal() {
        if (bot.modifiedLevel(Skill.HITPOINTS) < bot.baseLevel(Skill.HITPOINTS) / 2) {
            var food = bot.inventory().withAction("Eat").first();
            if (food != null) {
                food.interact("Eat");
                bot.tick();
            }
        }
    }

    public void setAutoRetaliate(boolean autoRetaliate) {
        if (autoRetaliate() != autoRetaliate) {
            bot.widget(593, 30).interact("Auto retaliate");
            bot.waitUntil(() -> autoRetaliate() == autoRetaliate);
        }
    }

    public boolean autoRetaliate() {
        return bot.varp(172) == 0;
    }
}
