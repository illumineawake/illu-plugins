package net.runelite.client.plugins.iutils.bot;

import net.runelite.api.*;
import net.runelite.client.plugins.iutils.api.Interactable;
import net.runelite.client.plugins.iutils.scene.Locatable;
import net.runelite.client.plugins.iutils.scene.Position;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class iGroundItem implements Locatable, Interactable {
    private final Bot bot;
    private final TileItem tileItem;
    private final ItemComposition definition;

    public iGroundItem(Bot bot, TileItem tileItem, ItemComposition definition) {
        this.bot = bot;
        this.tileItem = tileItem;
        this.definition = definition;
    }

    public Bot bot() {
        return bot;
    }

    public Client client() {
        return bot.client;
    }

    @Override
    public Position position() {
        return new Position(tileItem.getTile().getWorldLocation());
    }

    public int id() {
        return tileItem.getId();
    }

    public int quantity() {
        return tileItem.getQuantity();
    }

    public String name() {
        return definition.getName();
    }

    @Override
    public List<String> actions() {
        return Arrays.stream(definition.getGroundActions())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void interact(String action) {
        System.out.println(actions().toString());
        for (int i = 0; i < actions().size(); i++) {
            if (action.equalsIgnoreCase(actions().get(i))) {
                interact(i);
                return;
            }
        }
        throw new IllegalArgumentException("no action \"" + action + "\" on ground item " + id());
    }

    private int getActionId(int action) {
        switch (action) {
            case 0:
                return MenuAction.GROUND_ITEM_FIRST_OPTION.getId();
            case 1:
                return MenuAction.GROUND_ITEM_SECOND_OPTION.getId();
            case 2:
                return MenuAction.GROUND_ITEM_THIRD_OPTION.getId();
            case 3:
                return MenuAction.GROUND_ITEM_FOURTH_OPTION.getId();
            case 4:
                return MenuAction.GROUND_ITEM_FIFTH_OPTION.getId();
            default:
                throw new IllegalArgumentException("action = " + action);
        }
    }

    public void interact(int action) {
        bot().clientThread.invoke(() -> {
            client().invokeMenuAction("",
                    "",
                    id(),
                    MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), //TODO configure for other menu actions for ground items
                    tileItem.getTile().getSceneLocation().getX(),
                    tileItem.getTile().getSceneLocation().getY()
            );
        });
    }

    public String toString() {
        return name() + " (" + id() + ")" + (quantity() == 1 ? "" : " x" + quantity());
    }
}
