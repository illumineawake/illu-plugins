package net.runelite.client.plugins.iutils.ui;

import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.iWidget;

import java.util.List;

public class Chatbox {
    private final Bot bot;

    public Chatbox(Bot bot) {
        this.bot = bot;
    }

    public ChatState chatState() {
        return switch (bot.widget(162, 562).nestedInterface()) {
            case -1 -> ChatState.CLOSED;
            case 11 -> ChatState.ITEM_CHAT;
            case 217 -> ChatState.PLAYER_CHAT;
            case 231 -> ChatState.NPC_CHAT;
            case 219 -> ChatState.OPTIONS_CHAT;
            case 193 -> ChatState.SPECIAL;
            case 229 -> ChatState.MODEL;
            case 633 -> ChatState.SPRITE;
            default -> throw new IllegalStateException("unknown chat child " + bot.widget(162, 562).nestedInterface());
        };
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
        while (chatState() != ChatState.CLOSED && chatState() != ChatState.OPTIONS_CHAT) {
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
                widget.select(widgets.indexOf(widget));
                bot.tick();
                bot.tick();
                bot.tick();
                return; // todo: wait
            }
        }

        throw new IllegalStateException("no option " + part + " found");
    }

    public void continueChat() {
        switch (chatState()) {
            case CLOSED -> throw new IllegalStateException("there's no chat");
            case OPTIONS_CHAT -> throw new IllegalStateException("can't continue, this is an options chat");
            case PLAYER_CHAT -> bot.widget(217, 3).select();
            case NPC_CHAT -> bot.widget(231, 3).select();
            case ITEM_CHAT -> bot.widget(11, 4).select();
            case SPECIAL -> bot.widget(193, 0, 1).select();
            case MODEL -> bot.widget(229, 2).select();
            case SPRITE -> bot.widget(633, 0, 1).select();
        }
    }

    public void selectExperienceItemSkill(Skill skill) {
        bot.waitUntil(() -> bot.screenContainer().nestedInterface() == 240);

        switch (skill) {
            case ATTACK -> bot.widget(240, 0, 0).select();
            case STRENGTH -> bot.widget(240, 0, 1).select();
            case RANGED -> bot.widget(240, 0, 2).select();
            case MAGIC -> bot.widget(240, 0, 3).select();
            case DEFENCE -> bot.widget(240, 0, 4).select();
            case HITPOINTS -> bot.widget(240, 0, 5).select();
            case PRAYER -> bot.widget(240, 0, 6).select();
            case AGILITY -> bot.widget(240, 0, 7).select();
            case HERBLORE -> bot.widget(240, 0, 8).select();
            case THIEVING -> bot.widget(240, 0, 9).select();
            case CRAFTING -> bot.widget(240, 0, 10).select();
            case RUNECRAFT -> bot.widget(240, 0, 11).select();
            case SLAYER -> bot.widget(240, 0, 12).select();
            case FARMING -> bot.widget(240, 0, 13).select();
            case MINING -> bot.widget(240, 0, 14).select();
            case SMITHING -> bot.widget(240, 0, 15).select();
            case FISHING -> bot.widget(240, 0, 16).select();
            case COOKING -> bot.widget(240, 0, 17).select();
            case FIREMAKING -> bot.widget(240, 0, 18).select();
            case WOODCUTTING -> bot.widget(240, 0, 19).select();
            case FLETCHING -> bot.widget(240, 0, 20).select();
            case CONSTRUCTION -> bot.widget(240, 0, 21).select();
            case HUNTER -> bot.widget(240, 0, 22).select();
        }

        bot.waitUntil(() -> bot.screenContainer().nestedInterface() != 240);
    }

    public enum ChatState {
        CLOSED,
        PLAYER_CHAT,
        NPC_CHAT,
        ITEM_CHAT,
        OPTIONS_CHAT,
        SPECIAL,
        MODEL,
        SPRITE
    }
}
