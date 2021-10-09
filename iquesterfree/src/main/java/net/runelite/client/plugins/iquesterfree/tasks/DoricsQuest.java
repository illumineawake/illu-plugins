package net.runelite.client.plugins.iquesterfree.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iquesterfree.Task;
import net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin;
import net.runelite.client.plugins.iutils.api.TeleportLocation;
import net.runelite.client.plugins.iutils.api.TeleportMethod;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.scene.RectangularArea;

import java.util.ArrayList;
import java.util.List;

import static net.runelite.api.ItemID.*;

@Slf4j
public class DoricsQuest extends Task {
    private static final RectangularArea DORICS_HUT = new RectangularArea(2950, 3452, 2953, 3449);

    @Override
    public boolean validate() {
        return iQuesterFreePlugin.taskConfig.dorics() && questProgress() < 10;
    }

    @Override
    public String getTaskDescription() {
        iQuesterFreePlugin.questName = "Doric's Quest";
        return "Starting " + iQuesterFreePlugin.questName;
    }

    @Override
    public List<ItemQuantity> requiredItems() {
        List<ItemQuantity> items = new ArrayList<>();

        if (questProgress() == 0) {
            items.add(new ItemQuantity(CLAY, 6));
            items.add(new ItemQuantity(COPPER_ORE, 4));
            items.add(new ItemQuantity(IRON_ORE, 2));
            items.addAll(new TeleportMethod(game, TeleportLocation.FALADOR, 1).getItems());
        }

        return items;
    }

    @Override
    public void run() {
        while (questProgress() < 10) {
            log.info("Doing quest step: {} {}", iQuesterFreePlugin.questName, questProgress());
            game.tick();
            iQuesterFreePlugin.status = "Obtaining items";
            obtain(requiredItems());
            iQuesterFreePlugin.status = "Walking to Doric";
            chatNpc(DORICS_HUT, "Doric", "I wanted to use your anvils.", "Yes, I will get you the materials.");
            handleCompletion();
        }
    }

    private int questProgress() {
        return game.varp(31);
    }
}