package net.runelite.client.plugins.iherbcleaner.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.iherbcleaner.Task;
import net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.InventoryUtils;
import net.runelite.client.plugins.iutils.game.Game;

import javax.inject.Inject;
import java.util.List;

import static net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin.status;

@Slf4j
public class CleanHerbTask extends Task {

    @Inject
    ActionQueue action;

    @Inject
    InventoryUtils inventory;

    @Override
    public boolean validate() {
        return /*action.delayedActions.isEmpty()*/ !Game.isBusy() && inventory.containsItem(config.herbID());
    }

    @Override
    public String getTaskDescription() {
        return iHerbCleanerPlugin.status;
    }

    @Override
    public void onGameTick(GameTick event) {
        status = "Starting herb cleaning";
        var herbs = game.inventory().withId(config.herbID()).withAction("Clean").all();
        game.executorService.submit(() -> {
            herbs.forEach(h -> {
                h.interact("Clean");
                game.sleepExact(sleepDelay());
            });
        });
        log.info(status);
    }
}