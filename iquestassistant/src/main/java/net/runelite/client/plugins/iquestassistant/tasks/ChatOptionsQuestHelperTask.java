package net.runelite.client.plugins.iquestassistant.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iquestassistant.Task;
import net.runelite.client.plugins.iutils.ui.Chatbox;

@Slf4j
public class ChatOptionsQuestHelperTask extends Task {

    @Override
    public boolean validate() {
        if (config.questHelper() && game.widget(219, 1) != null && chatbox.chatState() == Chatbox.ChatState.OPTIONS_CHAT) {
            var options = chatbox.getOptions();
            for (var option : options) {
                if (option.startsWith("[")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getTaskDescription() {
        return "Quest Helper dialogue";
    }

    @Override
    public void run() {
        chatbox.chat("[");
    }
}