package net.runelite.client.plugins.iutils.ui;

import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.iWidget;

import javax.inject.Inject;
import java.util.List;

public class Chatbox {
    private final Bot bot;

    @Inject
    public Chatbox(Bot bot) {
        this.bot = bot;
    }

    public ChatState chatState() {
        switch (bot.widget(162, 562).nestedInterface()) {
            case -1:
                return ChatState.CLOSED;
            case 11:
                return ChatState.ITEM_CHAT;
            case 217:
                return ChatState.PLAYER_CHAT;
            case 231:
                return ChatState.NPC_CHAT;
            case 219:
                return ChatState.OPTIONS_CHAT;
            case 193:
                return ChatState.SPECIAL;
            case 229:
                return ChatState.MODEL;
            case 633:
                return ChatState.SPRITE;
            case 233:
                return ChatState.LEVEL_UP;
            case 270:
                return ChatState.MAKE;
            default:
                throw new IllegalStateException("unknown chat child " + bot.widget(162, 562).nestedInterface());
        }
    }

    public void chat(String... options) {
        bot.waitUntil(() -> chatState() != ChatState.CLOSED);

        for (String option : options) {
            continueChats();
            chooseOption(option);
        }

        continueChats();
    }

    public void continueChats() {
        while (chatState() != ChatState.CLOSED && chatState() != ChatState.OPTIONS_CHAT && chatState() != ChatState.MAKE) {
            continueChat();
            bot.tick();
        }
    }

    public void chooseOption(String part) {
        bot.tick();

        if (chatState() != ChatState.OPTIONS_CHAT) {
            throw new IllegalStateException("not an options chat");
        }
        List<iWidget> widgets = bot.widget(219, 1).items();
        for (iWidget widget : widgets) {
            if (widget.text() != null && widget.text().contains(part)) {
                widget.select();
                bot.tick(2);
                return; // todo: wait
            }
        }

        throw new IllegalStateException("no option " + part + " found");
    }

    public void continueChat() {
        switch (chatState()) {
            case CLOSED:
                throw new IllegalStateException("there's no chat");
            case OPTIONS_CHAT:
                throw new IllegalStateException("can't continue, this is an options chat");
            case PLAYER_CHAT:
                bot.widget(217, 3).select();
                break;
            case NPC_CHAT:
                bot.widget(231, 3).select();
                break;
            case ITEM_CHAT:
                bot.widget(11, 4).select();
                break;
            case SPECIAL:
                bot.widget(193, 0, 1).select();
                break;
            case MODEL:
                bot.widget(229, 2).select();
                break;
            case SPRITE:
                bot.widget(633, 0, 1).select();
                break;
        }
    }

    public void selectMenu(String option) { //TODO untested
        bot.waitUntil(() -> bot.screenContainer().nestedInterface() == 187);

        for (var child : bot.widget(187, 3).children()) {
            if (child.text() != null && child.text().contains(option)) {
                child.select();
                return;
            }
        }

        throw new IllegalArgumentException("no option '" + option + "' found");
    }

    public void selectExperienceItemSkill(Skill skill) {
        bot.waitUntil(() -> bot.screenContainer().nestedInterface() == 240);

        switch (skill) {
            case ATTACK:
                bot.widget(240, 0, 0).select();
                break;
            case STRENGTH:
                bot.widget(240, 0, 1).select();
                break;
            case RANGED:
                bot.widget(240, 0, 2).select();
                break;
            case MAGIC:
                bot.widget(240, 0, 3).select();
                break;
            case DEFENCE:
                bot.widget(240, 0, 4).select();
                break;
            case HITPOINTS:
                bot.widget(240, 0, 5).select();
                break;
            case PRAYER:
                bot.widget(240, 0, 6).select();
                break;
            case AGILITY:
                bot.widget(240, 0, 7).select();
                break;
            case HERBLORE:
                bot.widget(240, 0, 8).select();
                break;
            case THIEVING:
                bot.widget(240, 0, 9).select();
                break;
            case CRAFTING:
                bot.widget(240, 0, 10).select();
                break;
            case RUNECRAFT:
                bot.widget(240, 0, 11).select();
                break;
            case SLAYER:
                bot.widget(240, 0, 12).select();
                break;
            case FARMING:
                bot.widget(240, 0, 13).select();
                break;
            case MINING:
                bot.widget(240, 0, 14).select();
                break;
            case SMITHING:
                bot.widget(240, 0, 15).select();
                break;
            case FISHING:
                bot.widget(240, 0, 16).select();
                break;
            case COOKING:
                bot.widget(240, 0, 17).select();
                break;
            case FIREMAKING:
                bot.widget(240, 0, 18).select();
                break;
            case WOODCUTTING:
                bot.widget(240, 0, 19).select();
                break;
            case FLETCHING:
                bot.widget(240, 0, 20).select();
                break;
            case CONSTRUCTION:
                bot.widget(240, 0, 21).select();
                break;
            case HUNTER:
                bot.widget(240, 0, 22).select();
                break;
        }

        bot.waitUntil(() -> bot.screenContainer().nestedInterface() != 240);
    }

    public void make(int index, int quantity) {
        bot.waitUntil(() -> chatState() == ChatState.MAKE);
        bot.widget(270, 14 + index, quantity).select();
    }

    public enum ChatState {
        CLOSED,
        PLAYER_CHAT,
        NPC_CHAT,
        ITEM_CHAT,
        OPTIONS_CHAT,
        SPECIAL,
        MODEL,
        SPRITE,
        LEVEL_UP,
        MAKE
    }
}
