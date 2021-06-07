package net.runelite.client.plugins.iquestassistant.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iquestassistant.Task;
import net.runelite.client.plugins.iutils.ui.Chatbox;

@Slf4j
public class ContinueChatTask extends Task {

    @Override
    public boolean validate() {
        return config.continueChat()
                && chatbox.chatState() != Chatbox.ChatState.CLOSED && chatbox.chatState() != Chatbox.ChatState.OPTIONS_CHAT;
    }

    @Override
    public String getTaskDescription() {
        return "Continuing chat";
    }

    @Override
    public void run() {
        chatbox.continueChat();
    }
}