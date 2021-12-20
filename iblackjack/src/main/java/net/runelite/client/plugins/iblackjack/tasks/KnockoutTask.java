package net.runelite.client.plugins.iblackjack.tasks;

import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;

import java.util.Set;

import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.nextKnockoutTick;
import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.selectedNPCIndex;

public class KnockoutTask extends Task {
    Set<String> foodMenu = Set.of("Eat", "Drink");
    NPC bandit;

    @Override
    public boolean validate() {
        if (selectedNPCIndex == 0) {
            return false;
        }
        bandit = npc.findNearestNpcIndex(selectedNPCIndex, config.npcType().npcid);

        return client.getTickCount() >= nextKnockoutTick && bandit != null &&
                inventory.getItemMenu(foodMenu) != null;
    }

    @Override
    public String getTaskDescription() {
        return "Knockout bandit";
    }

    @Override
    public void onGameTick(GameTick event) {
        entry = new LegacyMenuEntry("", "", selectedNPCIndex, MenuAction.NPC_FIFTH_OPTION.getId(), 0, 0, false);
        utils.doActionMsTime(entry, bandit.getConvexHull().getBounds(), sleepDelay());
    }
}