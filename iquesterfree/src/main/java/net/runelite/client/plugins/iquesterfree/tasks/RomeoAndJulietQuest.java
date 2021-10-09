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

import static net.runelite.api.ItemID.CADAVA_BERRIES;
import static net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin.questName;
import static net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin.taskConfig;

@Slf4j
public class RomeoAndJulietQuest extends Task {
    private static final RectangularArea ROMEO = new RectangularArea(3207, 3427, 3221, 3422);
    private static final RectangularArea JULIET = new RectangularArea(3155, 3426, 3161, 3425, 1);
    private static final RectangularArea LAWRENCE = new RectangularArea(3253, 3483, 3257, 3477);
    private static final RectangularArea APOTHECARY = new RectangularArea(3192, 3405, 3198, 3402);

    @Override
    public boolean validate() {
        return taskConfig.romeoAndJuliet() && questProgress() < 100;
    }

    @Override
    public String getTaskDescription() {
        questName = "Romeo and Juliet";
        return "Starting " + questName;
    }

    @Override
    public List<ItemQuantity> requiredItems() {
        List<ItemQuantity> items = new ArrayList<>();

        if (questProgress() == 0) {
            items.add(new ItemQuantity(CADAVA_BERRIES, 1));
            items.addAll(new TeleportMethod(game, TeleportLocation.VARROCK_CENTRE, 2).getItems());
        }

        return items;
    }

    @Override
    public void run() {
        while (questProgress() < 100) {
            log.info("Doing quest step: {} {}", questName, questProgress());
            game.tick();
            switch (questProgress()) {
                case 0:
                    iQuesterFreePlugin.status = "Obtaining items";
                    obtain(requiredItems());
                    iQuesterFreePlugin.status = "Talking to Romeo";
                    chatNpc(ROMEO, "Romeo", "Yes, I have seen her actually!", "Yes.");
                    break;
                case 10:
                    iQuesterFreePlugin.status = "Talking to Juliet";
                    chatNpc(JULIET, "Juliet");
                    break;
                case 20:
                    iQuesterFreePlugin.status = "Giving letter to Romeo";
                    chatNpc(ROMEO, "Romeo");
                    break;
                case 30:
                    iQuesterFreePlugin.status = "Talking to Father Lawrence";
                    chatNpc(LAWRENCE, "Father Lawrence");
                    break;
                case 40:
                    iQuesterFreePlugin.status = "Talking to Apothecary";
                    chatNpc(APOTHECARY, "Apothecary", "Talk about something else.", "Talk about Romeo & Juliet.");
                    break;
                case 50:
                    iQuesterFreePlugin.status = "Giving potion to Juliet";
                    chatNpc(JULIET, "Juliet");
                    chat(4);
                    game.tick(4);
                    break;
                case 60:
                    iQuesterFreePlugin.status = "Talking to Romeo";
                    if (!game.inInstance()) {
                        chatNpc(ROMEO, "Romeo");
                    } else {
                        chat();
                        game.tick(5);
                    }
            }
        }
        if (questProgress() == 100) {
            handleCompletion();
        }
    }

    private int questProgress() {
        return game.varp(144);
    }
}