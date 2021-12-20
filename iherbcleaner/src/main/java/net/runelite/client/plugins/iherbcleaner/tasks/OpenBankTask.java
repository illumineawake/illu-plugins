package net.runelite.client.plugins.iherbcleaner.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iherbcleaner.Task;
import net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.BankUtils;
import net.runelite.client.plugins.iutils.InventoryUtils;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.iherbcleaner.iHerbCleanerPlugin.status;

@Slf4j
public class OpenBankTask extends Task {

    @Inject
    ActionQueue action;

    @Inject
    InventoryUtils inventory;

    @Inject
    BankUtils bank;

    @Override
    public boolean validate() {
        return action.delayedActions.isEmpty() && !inventory.containsItem(config.herbID()) &&
                !bank.isOpen();
    }

    @Override
    public String getTaskDescription() {
        return iHerbCleanerPlugin.status;
    }

    @Override
    public void onGameTick(GameTick event) {
        GameObject bank = object.findNearestGameObject(config.bankID());
        if (bank != null) {
            status = "Opening bank";
            entry = new LegacyMenuEntry("", "", bank.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId(),
                    bank.getSceneMinLocation().getX(), bank.getSceneMinLocation().getY(), false);
            Rectangle rectangle = (bank.getConvexHull() != null) ? bank.getConvexHull().getBounds() :
                    new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
            ;
            utils.doActionMsTime(entry, rectangle, sleepDelay());
        } else {
            status = "Bank not found";
        }
        log.info(status);
    }
}