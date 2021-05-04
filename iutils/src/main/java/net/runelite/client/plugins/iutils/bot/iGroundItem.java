package net.runelite.client.plugins.iutils.bot;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.api.TileItem;
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
    private final ObjectComposition definition;

    public iGroundItem(Bot bot, TileItem tileItem, ObjectComposition definition) {
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
        return Arrays.stream(definition.getActions())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void interact(String action) {
        for (int i = 0; i < actions().size(); i++) {
            if (action.equalsIgnoreCase(actions().get(i))) {
                interact(i);
                return;
            }
        }
        throw new IllegalArgumentException("no action \"" + action + "\" on ground item " + id());
    }

    public void interact(int action) {
        bot().clientThread.invoke(() -> {
            int menuAction;

            switch (action) {
                case 0:
                    menuAction = MenuAction.GROUND_ITEM_FIRST_OPTION.getId();
                    break;
                case 1:
                    menuAction = MenuAction.GROUND_ITEM_SECOND_OPTION.getId();
                    break;
                case 2:
                    menuAction = MenuAction.GROUND_ITEM_THIRD_OPTION.getId();
                    break;
                case 3:
                    menuAction = MenuAction.GROUND_ITEM_FOURTH_OPTION.getId();
                    break;
                case 4:
                    menuAction = MenuAction.GROUND_ITEM_FIFTH_OPTION.getId();
                    break;
                default:
                    throw new IllegalArgumentException("action = " + action);
            }

            client().invokeMenuAction("",
                    "",
                    id(),
                    menuAction,
                    tileItem.getTile().getSceneLocation().getX(),
                    tileItem.getTile().getSceneLocation().getY()
            );
        });
    }

    public String toString() {
        return name() + " (" + id() + ")" + (quantity() == 1 ? "" : " x" + quantity());
    }
}
