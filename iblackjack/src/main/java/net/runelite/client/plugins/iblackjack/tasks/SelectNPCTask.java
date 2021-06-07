package net.runelite.client.plugins.iblackjack.tasks;

import net.runelite.client.plugins.iblackjack.Task;

import static net.runelite.client.plugins.iblackjack.iBlackjackPlugin.selectedNPCIndex;


public class SelectNPCTask extends Task {
    @Override
    public boolean validate() {
        return selectedNPCIndex == 0;
    }

    @Override
    public String getTaskDescription() {
        return "Knock-out bandit to begin";
    }

}