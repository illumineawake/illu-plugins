package net.runelite.client.plugins.iutils.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.game.Game;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class Chatbox {
    private final Game game;

    @Inject
    public Chatbox(Game game) {
        this.game = game;
    }

    public ChatState chatState() {
        switch (game.widget(162, 559).nestedInterface()) {
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
                throw new IllegalStateException("unknown chat child " + game.widget(162, 562).nestedInterface());
        }
    }

    public void chat() {
        game.waitUntil(() -> chatState() != ChatState.CLOSED, 100);
        continueChats();
    }

    /**
     * Choose chat option in order of given chat options. Will fail if chat option isn't found.
     */
    public void chat(String... options) {
        game.waitUntil(() -> chatState() != ChatState.CLOSED, 100);

        for (var option : options) {
            continueChats();
            chooseOption(option);
        }

        continueChats();
    }

    public void chat(int... options) {
        game.waitUntil(() -> chatState() != ChatState.CLOSED, 100);

        for (var option : options) {
            continueChats();
            chooseOption(option);
        }

        continueChats();
    }

    /**
     * Choose chat option from any given chat options
     */
    public void chats(Collection<String> options) {
        game.waitUntil(() -> chatState() != ChatState.CLOSED);

        while (chatState() != ChatState.CLOSED) {
            continueChats();
            chooseOptions(options);
        }
    }

    public void continueChats() {
        while (chatState() != ChatState.CLOSED && chatState() != ChatState.OPTIONS_CHAT && chatState() != ChatState.MAKE) {
            continueChat();
            game.tick(1,2);
        }
    }

    public void continueChat() {
        switch (chatState()) {
            case CLOSED:
                throw new IllegalStateException("there's no chat");
            case OPTIONS_CHAT:
                throw new IllegalStateException("can't continue, this is an options chat");
            case PLAYER_CHAT:
                game.widget(217, 4).select();
                break;
            case NPC_CHAT:
                game.widget(231, 4).select();
                break;
            case ITEM_CHAT:
                game.widget(11, 4).select();
                break;
            case SPECIAL:
                game.widget(193, 0, 1).select();
                break;
            case MODEL:
                game.widget(229, 2).select();
                break;
            case SPRITE:
                game.widget(633, 0, 1).select();
                break;
            case LEVEL_UP:
                game.widget(233, 3).select();
                break;
            default:
                throw new IllegalStateException("unknown continue chat " + chatState());
        }
    }

    public List<String> getOptions() {
        List<String> options = new ArrayList<>();

        if (chatState() == ChatState.CLOSED || chatState() != ChatState.OPTIONS_CHAT) {
//            throw new IllegalStateException("Not an options chat");
            return options;
        }


        for (var i = 0; i < game.widget(219, 1).items().size(); i++) {
            if (game.widget(219, 1, i).text() != null) {
                options.add(game.widget(219, 1, i).text());
            }
        }

        return options;
    }

    public void chooseOption(String part) {
        if (chatState() == ChatState.CLOSED) {
            throw new IllegalStateException("chat closed before option: " + part);
        }

        if (chatState() != ChatState.OPTIONS_CHAT) {
            throw new IllegalStateException("not an options chat, " + chatState());
        }

        for (var i = 0; i < game.widget(219, 1).items().size(); i++) {
            if (game.widget(219, 1, i).text() != null && game.widget(219, 1, i).text().contains(part)) {
                game.widget(219, 1, i).select();
                game.waitChange(this::chatState, 6);
                game.tick(2,3);
                return;
            }
        }

        throw new IllegalStateException("no option " + part + " found");
    }

    public void chooseOption(int option) {
        if (chatState() == ChatState.CLOSED) {
            throw new IllegalStateException("chat closed before option: " + option);
        }

        if (chatState() != ChatState.OPTIONS_CHAT) {
            throw new IllegalStateException("not an options chat, " + chatState());
        }

        if (game.widget(219, 1, option).text() != null) {
            log.info("Selecting chat option from integer: {}", game.widget(219, 1, option).text());
            game.widget(219, 1, option).select();
            game.waitChange(this::chatState, 6);
            game.tick(2,3);
            return;
        }

        throw new IllegalStateException("no option " + option + " found");
    }

    public void chooseOptions(Collection<String> options) {
        if (chatState() == ChatState.CLOSED || chatState() != ChatState.OPTIONS_CHAT) {
            return;
        }

        for (var i = 0; i < game.widget(219, 1).items().size(); i++) {
            if (game.widget(219, 1, i).text() != null && options.contains(game.widget(219, 1, i).text())) {
                game.widget(219, 1, i).select();
                game.waitChange(this::chatState, 6);
                game.tick(2,3);
                return;
            }
        }

        throw new IllegalStateException("no option found: " + options.toString());
    }

    public String findFromOptions(Collection<String> options) {
        if (chatState() == ChatState.CLOSED || chatState() != ChatState.OPTIONS_CHAT) {
            return "";
        }

        for (var i = 0; i < game.widget(219, 1).items().size(); i++) {
            if (game.widget(219, 1, i).text() != null && options.contains(game.widget(219, 1, i).text())) {
                return game.widget(219, 1, i).text();
            }
        }
        return "";
    }

    public void selectMenu(String option) { //TODO untested
        game.waitUntil(() -> game.screenContainer().nestedInterface() == 187);

        for (var child : game.widget(187, 3).items()) {
            if (child.text() != null && child.text().contains(option)) {
                child.select();
                return;
            }
        }

        throw new IllegalArgumentException("no option '" + option + "' found");
    }

    public void selectExperienceItemSkill(Skill skill) {
        game.waitUntil(() -> game.screenContainer().nestedInterface() == 240);

        switch (skill) {
            case ATTACK:
                game.widget(240, 0, 0).select();
                break;
            case STRENGTH:
                game.widget(240, 0, 1).select();
                break;
            case RANGED:
                game.widget(240, 0, 2).select();
                break;
            case MAGIC:
                game.widget(240, 0, 3).select();
                break;
            case DEFENCE:
                game.widget(240, 0, 4).select();
                break;
            case HITPOINTS:
                game.widget(240, 0, 5).select();
                break;
            case PRAYER:
                game.widget(240, 0, 6).select();
                break;
            case AGILITY:
                game.widget(240, 0, 7).select();
                break;
            case HERBLORE:
                game.widget(240, 0, 8).select();
                break;
            case THIEVING:
                game.widget(240, 0, 9).select();
                break;
            case CRAFTING:
                game.widget(240, 0, 10).select();
                break;
            case RUNECRAFT:
                game.widget(240, 0, 11).select();
                break;
            case SLAYER:
                game.widget(240, 0, 12).select();
                break;
            case FARMING:
                game.widget(240, 0, 13).select();
                break;
            case MINING:
                game.widget(240, 0, 14).select();
                break;
            case SMITHING:
                game.widget(240, 0, 15).select();
                break;
            case FISHING:
                game.widget(240, 0, 16).select();
                break;
            case COOKING:
                game.widget(240, 0, 17).select();
                break;
            case FIREMAKING:
                game.widget(240, 0, 18).select();
                break;
            case WOODCUTTING:
                game.widget(240, 0, 19).select();
                break;
            case FLETCHING:
                game.widget(240, 0, 20).select();
                break;
            case CONSTRUCTION:
                game.widget(240, 0, 21).select();
                break;
            case HUNTER:
                game.widget(240, 0, 22).select();
                break;
        }

        game.waitUntil(() -> game.screenContainer().nestedInterface() != 240, 10);
    }

    public void make(int index, int quantity) {
        game.waitUntil(() -> chatState() == ChatState.MAKE);
        game.widget(270, 14 + index, quantity).select();
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
