package net.runelite.client.plugins.iutils.bot;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.iutils.api.Interactable;
import net.runelite.client.plugins.iutils.scene.Locatable;
import net.runelite.client.plugins.iutils.scene.Position;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class iObject implements Locatable, Interactable {

    private final Bot bot;
    private final TileObject tileObject;
    private final ObjectComposition definition;

    public iObject(Bot bot, TileObject tileObject, ObjectComposition definition) {
        this.bot = bot;
        this.tileObject = tileObject;
        this.definition = definition;
    }

    //	@Override
    public Bot bot() {
        return bot;
    }

    public Client client() {
        return bot.client;
    }

    @Override
    public Position position() {
        return new Position(tileObject.getWorldLocation());
    }

    public LocalPoint localPoint() {
        return tileObject.getLocalLocation();
    }

    public int id() {
        return tileObject.getId();
    }

    public String name() {
        return definition.getName();
    }

    public int orientation() { //TODO untested
        if (tileObject instanceof WallObject) {
            int orientation = ((WallObject) tileObject).getOrientationA();
            if (orientation == 1) return 0;
            if (orientation == 2) return 1;
            if (orientation == 4) return 2;
            if (orientation == 8) return 3;
            throw new AssertionError();
        }

        if (tileObject instanceof DecorativeObject)
            return ((DecorativeObject) tileObject).getOrientation();

        return -1;
    }

    public List<String> actions() {
        return Arrays.stream(definition().getActions())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public ObjectComposition definition() {
        return definition;
    }

    public Point menuPoint() {
        if (tileObject instanceof GameObject) {
            System.out.println("Is GO");
            GameObject temp = (GameObject) tileObject;
            return temp.getSceneMinLocation();
        }
        return new Point(localPoint().getSceneX(), localPoint().getSceneY());
    }

    @Override
    public void interact(String action) {
        for (int i = 0; i < actions().size(); i++) {
            if (action.equalsIgnoreCase(actions().get(i))) {
                interact(i);
                return;
            }
        }
        throw new IllegalArgumentException("no action \"" + action + "\" on object " + id());
    }

    public void interact(int action) {
        bot().clientThread.invoke(() -> {
            int menuAction;

            switch (action) {
                case 0:
                    menuAction = MenuAction.GAME_OBJECT_FIRST_OPTION.getId();
                    break;
                case 1:
                    menuAction = MenuAction.GAME_OBJECT_SECOND_OPTION.getId();
                    break;
                case 2:
                    menuAction = MenuAction.GAME_OBJECT_THIRD_OPTION.getId();
                    break;
                case 3:
                    menuAction = MenuAction.GAME_OBJECT_FOURTH_OPTION.getId();
                    break;
                case 4:
                    menuAction = MenuAction.GAME_OBJECT_FIFTH_OPTION.getId();
                    break;
                default:
                    throw new IllegalArgumentException("action = " + action);
            }

            client().invokeMenuAction("",
                    "",
                    id(),
                    menuAction,
                    menuPoint().getX(),
                    menuPoint().getY()
            );
        });
    }
}
