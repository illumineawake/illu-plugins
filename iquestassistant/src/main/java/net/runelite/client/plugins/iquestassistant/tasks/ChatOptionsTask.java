package net.runelite.client.plugins.iquestassistant.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iquestassistant.Dialogue;
import net.runelite.client.plugins.iquestassistant.Task;
import net.runelite.client.plugins.iutils.ui.Chatbox;

@Slf4j
public class ChatOptionsTask extends Task {

    @Override
    public boolean validate() {
        if (config.supportedQuests() && game.widget(219, 1) != null && chatbox.chatState() == Chatbox.ChatState.OPTIONS_CHAT) {
            return !Dialogue.getDialogue(game, chatbox.getOptions()).equals("");
        }
        return false;
    }

    @Override
    public String getTaskDescription() {
        return "Options chat";
    }

    @Override
    public void run() {
        String matchingOption = Dialogue.getDialogue(game, chatbox.getOptions());
        if (!matchingOption.equals("")) {
            log.info("Found matching option: {}", matchingOption);
            chatbox.chooseOption(matchingOption);
        }
    }
}