package net.runelite.client.plugins.iutils.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.plugins.iutils.scene.Locatable;
import net.runelite.client.plugins.iutils.scene.ObjectCategory;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.rs.api.RSClient;

@Slf4j
public class iTile implements Locatable {
    final Bot bot;
    final Tile tile;
    //    List<GroundItem> items = new ArrayList<>();
    iObject regularObject;
    iObject wall;
    iObject wallDecoration;
    iObject floorDecoration;

    iTile(Bot bot, Tile tile) {
        this.bot = bot;
        this.tile = tile;
        //tile.getGameObjects()).filter(Objects::nonNull).findFirst().orElse(null)
    }

    //    @Override
    public Bot bot() {
        return bot;
    }

    @Override
    public Client client() {
        return bot.client;
    }

    @Override
    public Position position() {
        return new Position(tile.getSceneLocation().getX(), tile.getSceneLocation().getY(), tile.getPlane());
    }

    public void walkTo() {
        bot.clientThread.invoke(() -> bot.walkUtils.sceneWalk(position(), 0, 0));
    }

//    public List<GroundItem> items() {
//        return items;
//    }

//    public List<iObject> objects() {
//        ArrayList<iObject> objects = new ArrayList<>(4);
//        if (regularObject != null) objects.add(regularObject);
//        if (wall != null) objects.add(wall);
//        if (wallDecoration != null) objects.add(wallDecoration);
//        if (floorDecoration != null) objects.add(floorDecoration);
//        return objects;
//    }

    public iObject object(ObjectCategory category) {
        switch (category) {
            case REGULAR:
                GameObject go = Arrays.stream(tile.getGameObjects()).filter(Objects::nonNull).findFirst().orElse(null);
                return (go == null) ? null : new iObject(bot, go,
                        ObjectCategory.REGULAR,
                        bot.getFromClientThread(() -> client().getObjectDefinition(go.getId()))
                );
            case WALL:
                WallObject wo = tile.getWallObject();
                return (wo == null) ? null :  new iObject(bot, wo, ObjectCategory.WALL, bot.getFromClientThread(() -> client().getObjectDefinition(wo.getId())));
            case WALL_DECORATION:
                DecorativeObject dec = tile.getDecorativeObject();
                return (dec == null) ? null :  new iObject(bot, dec, ObjectCategory.WALL_DECORATION, bot.getFromClientThread(() -> client().getObjectDefinition(dec.getId())));
            case FLOOR_DECORATION:
                GroundObject ground = tile.getGroundObject();
                return (ground == null) ? null :  new iObject(bot, ground, ObjectCategory.FLOOR_DECORATION, bot.getFromClientThread(() -> client().getObjectDefinition(ground.getId())));
            default:
                return null;
        }
    }

    /*public iObject object(ObjectCategory category) {
    	iObject result;
        return switch (category) {
            case REGULAR -> regularObject;
            case WALL -> wall;
            case WALL_DECORATION -> wallDecoration;
            case FLOOR_DECORATION -> floorDecoration;
        };
    }*/
}
