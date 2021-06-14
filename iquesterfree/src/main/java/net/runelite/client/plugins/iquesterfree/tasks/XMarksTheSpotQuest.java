package net.runelite.client.plugins.iquesterfree.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iquesterfree.Task;
import net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.scene.RectangularArea;

import java.util.ArrayList;
import java.util.List;

import static net.runelite.api.ItemID.*;
import static net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin.questName;
import static net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin.taskConfig;

@Slf4j
public class XMarksTheSpotQuest extends Task
{

    private static final RectangularArea PUB = new RectangularArea(3226, 3242, 3233, 3236);
    private static final RectangularArea DIG_SPOT_1 = new RectangularArea(3230, 3209, 3230, 3209);
    private static final RectangularArea DIG_SPOT_2 = new RectangularArea(3203, 3212, 3203, 3212);
    private static final RectangularArea DIG_SPOT_3 = new RectangularArea(3109, 3264, 3109, 3264);
    private static final RectangularArea DIG_SPOT_4 = new RectangularArea(3078, 3259, 3078, 3259);
    private static final RectangularArea PORT_SARIM_DOCKS = new RectangularArea(3051, 3248, 3055, 3246);

    @Override
    public boolean validate()
    {
        return taskConfig.xMarksTheSpot() && questProgress() < 7;
    }

    @Override
    public String getTaskDescription()
    {
        questName = "X Marks The Spot";
        return "Starting " + questName;
    }

    @Override
    public List<ItemQuantity> requiredItems()
    {
        List<ItemQuantity> items = new ArrayList<>();
        if (questProgress() == 0)
        {
            items.add(new ItemQuantity(SPADE, 1));
        }

        return items;
    }

    @Override
    public void run()
    {
        while(questProgress() < 7)
        {
            log.info("Doing quest step: {} {}", questName, questProgress());
            game.tick();
            switch(questProgress())
            {
                case 0:
                    iQuesterFreePlugin.status = "Obtaining items";
                    obtain(requiredItems());
                    iQuesterFreePlugin.status = "Talking to Veos";

                    chatOptionalNpc(PUB, "Veos", "I'm looking for a quest.",
                            "Sounds good, what do I do?", "Can I help?", "Yes.");
                    game.tick();
                    break;
                case 2:
                    iQuesterFreePlugin.status = "Dig spot # 1";

                    if(!game.localPlayer().position().inside(DIG_SPOT_1))
                    {
                        walking.walkTo(DIG_SPOT_1);
                        game.tick();
                    } else
                    {
                        if(game.inventory().withName("Spade").exists())
                        {
                            game.inventory().withName("Spade").first().interact("Dig");
                            game.tick(3);
                        }
                    }
                    chatbox.continueChats();
                    break;
                case 3:
                    iQuesterFreePlugin.status = "Dig spot # 2";
                    if(!game.localPlayer().position().inside(DIG_SPOT_2))
                    {
                        walking.walkTo(DIG_SPOT_2);
                        game.tick();
                    } else
                    {
                        if(game.inventory().withName("Spade").exists())
                        {
                            game.inventory().withName("Spade").first().interact("Dig");
                            game.tick(3);
                        }
                    }
                    chatbox.continueChats();
                    break;
                case 4:
                    iQuesterFreePlugin.status = "Dig spot # 3";
                    if(!game.localPlayer().position().inside(DIG_SPOT_3))
                    {
                        walking.walkTo(DIG_SPOT_3);
                        game.tick();
                    } else
                    {
                        if(game.inventory().withName("Spade").exists())
                        {
                            game.inventory().withName("Spade").first().interact("Dig");
                            game.tick(3);
                        }
                    }
                    chatbox.continueChats();
                    break;
                case 5:
                    iQuesterFreePlugin.status = "Dig spot # 4";
                    if(!game.localPlayer().position().inside(DIG_SPOT_4))
                    {
                        walking.walkTo(DIG_SPOT_4);
                        game.tick();
                    } else
                    {
                        if(game.inventory().withName("Spade").exists())
                        {
                            game.inventory().withName("Spade").first().interact("Dig");
                            game.tick(3);
                        }
                    }
                    chatbox.continueChats();
                    break;
                case 6:
                    iQuesterFreePlugin.status = "Talking to Veos to finish quest";
                    chatNpc(PORT_SARIM_DOCKS, "Veos");
                    game.tick();
                    handleCompletion();
                    break;
            }
        }
    }

    private int questProgress()
    {
        return game.varb(8063);
    }
}