package net.runelite.client.plugins.iquesterfree.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iquesterfree.Task;
import net.runelite.client.plugins.iquesterfree.iQuesterFreePlugin;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.scene.RectangularArea;
import net.runelite.client.plugins.iutils.walking.TeleportLoader;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class BuyStartItems extends Task {
    private static boolean startItemsCollected = false;
    private static final List<Integer> GE_JEWELLERY_IDS = List.of(2552, 3853, 11118, 11980, 1712, 21146, 21166, 13393, 11866, 11190, 22400, 11105);

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
        getTaskItems();
//        obtainBank(getTaskItems());
        startItemsCollected = true;
    }

    private ItemQuantity[] getTaskItems() {
        List<ItemQuantity> items = new ArrayList<>();

        for (Task task : iQuesterFreePlugin.tasks.getAllValidTasks()) {
            List<ItemQuantity> taskItems = task.requiredItems();
            for (ItemQuantity item : taskItems) {
                if (GE_JEWELLERY_IDS.contains(item.id)) {
                    log.info("Found a GE item: {}", item.id);
                    if (items.stream().anyMatch(i -> i.id == item.id)) {
                        log.info("Found a duplicate item, removing: {}", item.id);
                        taskItems.remove(item);
                    }
                }
            }
            items.addAll(taskItems);
        }

        log.info("Obtaining start quest items: {}", items.toString());
        return items.toArray(ItemQuantity[]::new);
    }
}