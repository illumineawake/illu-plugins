package net.runelite.client.plugins.iquesterfree.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iquesterfree.Task;
import net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin;
import net.runelite.client.plugins.iutils.api.TeleportLocation;
import net.runelite.client.plugins.iutils.api.TeleportMethod;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.scene.RectangularArea;

import java.util.ArrayList;
import java.util.List;

import static net.runelite.api.ItemID.SPADE;
import static net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin.questName;
import static net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin.taskConfig;

@Slf4j
public class XMarksTheSpotQuest extends Task {
    private static final RectangularArea LUMBRIDGE_BAR = new RectangularArea(3226, 3241, 3230, 3239);
    private static final RectangularArea VEOS_SARIM = new RectangularArea(3054, 3245, 3056, 3246);
    private static final Position BOB_DIG = new Position(3230, 3209, 0);
    private static final Position CASTLE_DIG = new Position(3203, 3212, 0);
    private static final Position DRAYNOR_DIG = new Position(3109, 3264, 0);
    private static final Position MARTIN_DIG = new Position(3078, 3259, 0);

    @Override
    public boolean validate() {
        log.info("{}", taskConfig.xMarksTheSpot());
        return taskConfig.xMarksTheSpot() && questProgress() < 8;
    }

    @Override
    public String getTaskDescription() {
        questName = "X Marks the Spot";
        return "Starting " + questName;
    }

    @Override
    public List<ItemQuantity> requiredItems() {
        List<ItemQuantity> items = new ArrayList<>();

        if (questProgress() == 0) {
            items.add(new ItemQuantity(SPADE, 1));
            items.addAll(new TeleportMethod(game, TeleportLocation.LUMBRIDGE, 1).getItems());
        }
        return items;
    }

    @Override
    public void run() {
        while (questProgress() < 8) {
            log.info("Doing quest step: {} {}", questName, questProgress());
            game.tick();
            switch (questProgress()) {
                case 0:
                case 1:
                    iQuesterFreePlugin.status = "Obtaining items";
                    obtain(requiredItems());
                    teleportToLumbridge();
                    walking.walkTo(LUMBRIDGE_BAR);
                    game.npcs().withName("Veos").nearest().interact("Talk-to");
                    chatbox.chat("I'm looking for a quest.", "Yes");
                    break;
                case 2:
                    digAt(BOB_DIG);
                    break;
                case 3:
                    digAt(CASTLE_DIG);
                    break;
                case 4:
                    digAt(DRAYNOR_DIG);
                    break;
                case 5:
                    digAt(MARTIN_DIG);
                    break;
                case 6:
                    iQuesterFreePlugin.status = "Talking to Veos";
                    chatNpc(VEOS_SARIM, "Veos");
                    game.tick(2);
                    handleCompletion();
                    break;
                case 7:
                case 8: {
                    if (game.inventory().withName("Antique lamp").exists()) {
                        game.inventory().withName("Antique lamp").first().interact("Rub");
                        game.tick();
                        chatbox.selectExperienceItemSkill(taskConfig.xSkill());
                    }
                    return;
                }
            }
        }
        if (game.inventory().withName("Antique lamp").exists()) {
            game.inventory().withName("Antique lamp").first().interact("Rub");
            game.tick();
            chatbox.selectExperienceItemSkill(taskConfig.xSkill());
        }
    }

    private void digAt(Position position) {
        obtain(new ItemQuantity(SPADE, 1));
        iQuesterFreePlugin.status = "Digging at " + position.toString();
        walking.walkTo(position);
        game.inventory().withName("Spade").first().interact("Dig");
        game.tick(4);
    }

    private int questProgress() {
        return game.varb(8063);
    }
}