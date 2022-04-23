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
import static net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin.questName;
import static net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin.taskConfig;

@Slf4j
public class GoblinDiplomacyQuest extends Task {
    private static final RectangularArea GOBLIN_VILLAGE = new RectangularArea(2956, 3512, 2960, 3510);

    @Override
    public boolean validate() {
        return taskConfig.goblinDiplomacy() && questProgress() < 6;
    }

    @Override
    public String getTaskDescription() {
        questName = "Goblin Diplomacy";
        return "Starting " + questName;
    }

    @Override
    public List<ItemQuantity> requiredItems() {
        List<ItemQuantity> items = new ArrayList<>();

        if (questProgress() == 0) {
            items.add(new ItemQuantity(BLUE_DYE, 1));
            items.add(new ItemQuantity(ORANGE_DYE, 1));
            items.add(new ItemQuantity(GOBLIN_MAIL, 3));
            items.addAll(new TeleportMethod(game, TeleportLocation.FALADOR, 1).getItems());
        }

        return items;
    }

    @Override
    public void run() {
        while (questProgress() < 6) {
            log.info("Doing quest step: {} {}", questName, questProgress());
            game.tick();
            switch (questProgress()) {
                case 0:
                    iQuesterFreePlugin.status = "Obtaining items";
                    obtain(requiredItems());
                    iQuesterFreePlugin.status = "Chatting with Goblin General";
                    chatOptionalNpc(GOBLIN_VILLAGE, "General Bentnoze", "Yes, Wartface looks fat",
                            "Do you want me to pick an armour colour for you?", "What about a different colour?");
                    game.tick(5);
                    break;

                case 3:
                    if (!hasItem("Blue goblin mail") && hasItem("Blue dye") && hasItem("Goblin mail")) {
                        iQuesterFreePlugin.status = "Dying Goblin mail blue";
                        useItemItem("Blue dye", "Goblin mail");
                        waitItem("Blue goblin mail");
                    }

                    if (!hasItem("Orange goblin mail") && hasItem("Orange dye") && hasItem("Goblin mail")) {
                        iQuesterFreePlugin.status = "Dying Goblin mail orange";
                        useItemItem("Orange dye", "Goblin mail");
                        waitItem("Orange goblin mail");
                    }
                    if (!game.inInstance()) {
                        iQuesterFreePlugin.status = "Chatting with Goblin General";
                        chatOptionalNpc(GOBLIN_VILLAGE, "General Bentnoze", "Yes, he looks fat", "I have some blue armour here", "I have some orange armour here", "I have some brown armour here");
                    }
                    chatbox.chat();
                    game.waitUntil(() -> !game.inInstance()); //TODO: illu this seems to hang - after the goblin tries the armour, then we exit the instanced scene, the bot doesn't continue. restarting the bot fixes it. -sox
                    break;

                case 4:
                    if (!game.inInstance()) {
                        iQuesterFreePlugin.status = "Chatting with Goblin General";
                        chatOptionalNpc(GOBLIN_VILLAGE, "General Bentnoze", "Yes, he looks fat", "I have some blue armour here", "I have some orange armour here", "I have some brown armour here");
                    }
                    chatbox.chat();
                    break;
                case 5:
                    if (!game.inInstance()) {
                        iQuesterFreePlugin.status = "Chatting with Goblin General";
                        chatOptionalNpc(GOBLIN_VILLAGE, "General Bentnoze", "Yes, he looks fat", "I have some blue armour here", "I have some orange armour here", "I have some brown armour here");
                    }
                    chatbox.chat();
                    handleCompletion();
                    break;
            }
        }
    }

    private int questProgress() {
        return game.varb(2378);
    }
}