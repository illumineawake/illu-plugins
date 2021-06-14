package net.runelite.client.plugins.iquesterfree.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iquesterfree.Task;
import net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.scene.RectangularArea;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BuyStartItems extends Task {
    private static boolean startItemsCollected = false; //TODO might be false on each loop?
    private static final RectangularArea COOKS_KITCHEN = new RectangularArea(3206, 3215, 3210, 3213);

    @Override
    public boolean validate() {
        return iQuesterFreePlugin.taskConfig.buyStart() && !startItemsCollected;
    }

    @Override
    public String getTaskDescription() {
        return "Starting...";
    }

    @Override
    public List<ItemQuantity> requiredItems() {
        return new ArrayList<>();
    }

    @Override
    public void run() {
        iQuesterFreePlugin.status = "Buying required starting quest items";
        bank().depositInventory();
        obtainBank(getTaskItems());
        startItemsCollected = true;
    }

    private ItemQuantity[] getTaskItems() {
        List<ItemQuantity> items = new ArrayList<>();
        for (Task task : iQuesterFreePlugin.tasks.getAllValidTasks()) { //TODO untested
            items.addAll(task.requiredItems());
        }
        log.info("Obtaining start quest items: {}", items.toString());
        return items.toArray(ItemQuantity[]::new);
    }
}