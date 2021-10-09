package net.runelite.client.plugins.iquesterfree;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.scripts.UtilsScript;
import net.runelite.client.plugins.iutils.ui.Chatbox;

import java.util.List;

@Slf4j
public abstract class Task extends UtilsScript {

    public Task() {
    }

    public abstract boolean validate();

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }

    public abstract List<ItemQuantity> requiredItems();

    public abstract void run();

    public void handleCompletion() {
        game.waitUntil(() -> game.screenContainer().nestedInterface() == 153, 30);
        iQuesterFreePlugin.status = "Completed";
        log.debug(iQuesterFreePlugin.questName + " completed");
        game.tick(3);
        game.widget(153, 16).interact(0); //close quest completion screen
        game.tick(3);
        if (chatbox.chatState() != Chatbox.ChatState.CLOSED) {
            chatbox.chat();
        }
    }
}
