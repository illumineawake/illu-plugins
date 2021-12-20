package net.runelite.client.plugins.iblackjack.tasks;

import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;

import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.*;

public class PickpocketTask extends Task {
    NPC bandit;

    @Override
    public boolean validate() {
        if (selectedNPCIndex == 0) {
            return false;
        }
        bandit = npc.findNearestNpcIndex(selectedNPCIndex, config.npcType().npcid);
        return !inCombat && client.getTickCount() < nextKnockoutTick && bandit != null;
    }

    @Override
    public String getTaskDescription() {
        return "Pickpocket bandit";
    }

    @Override
    public void onGameTick(GameTick event) {
        entry = new LegacyMenuEntry("", "", selectedNPCIndex, MenuAction.NPC_THIRD_OPTION.getId(), 0, 0, false);
        utils.doActionMsTime(entry, bandit.getConvexHull().getBounds(), sleepDelay());
        if (config.random() && calc.getRandomIntBetweenRange(0, 10) == 0) {
            //timeout = calc.getRandomIntBetweenRange(1,2);
            timeout = tickDelay();
            utils.sendGameMessage("Resting for " + timeout + " tick(s)");
        }
    }
}