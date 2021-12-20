package net.runelite.client.plugins.iutils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.*;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.runelite.client.plugins.iutils.Banks.ALL_BANKS;

@Slf4j
@Singleton
public class ObjectUtils {
    @Inject
    private Client client;

    @Nullable
    public GameObject findNearestGameObject(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist, int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }
        return new GameObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GameObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist, Collection<Integer> ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GameObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GameObject findNearestGameObjectWithin(LocalPoint localPoint, int dist, int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }
        WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);
        return findNearestGameObjectWithin(worldPoint, dist, ids);
    }

    @Nullable
    public GameObject findNearestGameObjectWithin(LocalPoint localPoint, int dist, Collection<Integer> ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }
        WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);
        return findNearestGameObjectWithin(worldPoint, dist, ids);
    }

    @Nullable
    public GameObject findNearestGameObjectMenuWithin(WorldPoint worldPoint, int dist, String menuAction) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GameObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .filter(w -> ArrayUtils.contains(client.getObjectDefinition(w.getId()).getActions(), menuAction))
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public List<WallObject> getWallObjects(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return new ArrayList<>();
        }

        return new WallObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public List<DecorativeObject> getDecorObjects(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return new ArrayList<>();
        }

        return new DecorativeObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public List<GroundObject> getGroundObjects(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return new ArrayList<>();
        }

        return new GroundObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public TileItem getGroundItem(int id) {
        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();

        int z = client.getPlane();

        for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                Tile tile = tiles[z][x][y];

                if (tile == null) {
                    continue;
                }
                Player player = client.getLocalPlayer();
                if (player == null) {
                    continue;
                }
                TileItem tileItem = findItemAtTile(tile, id);
                if (tileItem != null) {
                    return tileItem;
                }
            }
        }
        return null;
    }

    private TileItem findItemAtTile(Tile tile, int id) {
        ItemLayer tileItemPile = tile.getItemLayer();
        if (tileItemPile != null) {
            TileItem tileItem = (TileItem) tileItemPile.getBottom();
            if (tileItem.getId() == id) {
                return tileItem;
            }
        }
        return null;
    }

    @Nullable
    public TileObject findNearestObject(int... ids) {
        GameObject gameObject = findNearestGameObject(ids);

        if (gameObject != null) {
            return gameObject;
        }

        WallObject wallObject = findNearestWallObject(ids);

        if (wallObject != null) {
            return wallObject;
        }
        DecorativeObject decorativeObject = findNearestDecorObject(ids);

        if (decorativeObject != null) {
            return decorativeObject;
        }

        return findNearestGroundObject(ids);
    }

    @Nullable
    public TileObject findNearestObjectWithin(WorldPoint worldPoint, int dist, int... ids) {
        GameObject gameObject = findNearestGameObjectWithin(worldPoint, dist, ids);

        if (gameObject != null) {
            return gameObject;
        }

        WallObject wallObject = findWallObjectWithin(worldPoint, dist, ids);

        if (wallObject != null) {
            return wallObject;
        }
        DecorativeObject decorativeObject = findNearestDecorObjectWithin(worldPoint, dist, ids);

        if (decorativeObject != null) {
            return decorativeObject;
        }

        return findNearestGroundObjectWithin(worldPoint, dist, ids);
    }

    @Nullable
    public TileObject findNearestObjectWithin(WorldPoint worldPoint, int dist) {
        GameObject gameObject = findNearestGameObjectWithin(worldPoint, dist);

        if (gameObject != null) {
            return gameObject;
        }

        WallObject wallObject = findWallObjectWithin(worldPoint, dist);

        if (wallObject != null) {
            return wallObject;
        }
        DecorativeObject decorativeObject = findNearestDecorObjectWithin(worldPoint, dist);

        if (decorativeObject != null) {
            return decorativeObject;
        }

        return findNearestGroundObjectWithin(worldPoint, dist);
    }

    @Nullable
    public TileObject findNearestObjectMenuWithin(WorldPoint worldPoint, int dist, String menuAction) {
        GameObject gameObject = findNearestGameObjectMenuWithin(worldPoint, dist, menuAction);

        if (gameObject != null) {
            return gameObject;
        }

        WallObject wallObject = findNearestWallObjectMenuWithin(worldPoint, dist, menuAction);

        if (wallObject != null) {
            return wallObject;
        }
        DecorativeObject decorativeObject = findNearestDecorObjectMenuWithin(worldPoint, dist, menuAction);

        if (decorativeObject != null) {
            return decorativeObject;
        }

        return findNearestGroundObjectMenuWithin(worldPoint, dist, menuAction);
    }

//    @Deprecated
//    @Nullable
//    public List<TileItem> getTileItemsWithin(int distance) {
//        assert client.isClientThread();
//
//        if (client.getLocalPlayer() == null) {
//            return new ArrayList<>();
//        }
//        return new TileQuery()
//                .isWithinDistance(client.getLocalPlayer().getWorldLocation(), distance)
//                .result(client)
//                .first()
//                .getGroundItems();
//    }
//
//    @Deprecated
//    @Nullable
//    public List<TileItem> getTileItemsAtTile(Tile tile) {
//        assert client.isClientThread();
//
//        if (client.getLocalPlayer() == null) {
//            return new ArrayList<>();
//        }
//        return new TileQuery()
//                .atWorldLocation(tile.getWorldLocation())
//                .result(client)
//                .first()
//                .getGroundItems();
//    }

    @Nullable
    public WallObject findNearestWallObject(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new WallObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public WallObject findWallObjectWithin(WorldPoint worldPoint, int radius, int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new WallObjectQuery()
                .isWithinDistance(worldPoint, radius)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public WallObject findWallObjectWithin(WorldPoint worldPoint, int radius) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new WallObjectQuery()
                .isWithinDistance(worldPoint, radius)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public WallObject findWallObjectWithin(WorldPoint worldPoint, int radius, Collection<Integer> ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new WallObjectQuery()
                .isWithinDistance(worldPoint, radius)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public WallObject findNearestWallObjectMenuWithin(WorldPoint worldPoint, int dist, String menuAction) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new WallObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .filter(w -> ArrayUtils.contains(client.getObjectDefinition(w.getId()).getActions(), menuAction))
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public DecorativeObject findNearestDecorObject(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new DecorativeObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public DecorativeObject findNearestDecorObjectWithin(WorldPoint worldPoint, int dist, int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new DecorativeObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public DecorativeObject findNearestDecorObjectWithin(WorldPoint worldPoint, int dist) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new DecorativeObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public DecorativeObject findNearestDecorObjectMenuWithin(WorldPoint worldPoint, int dist, String menuAction) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new DecorativeObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .filter(w -> ArrayUtils.contains(client.getObjectDefinition(w.getId()).getActions(), menuAction))
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GroundObject findNearestGroundObject(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GroundObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GroundObject findNearestGroundObjectWithin(WorldPoint worldPoint, int dist, int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GroundObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GroundObject findNearestGroundObjectWithin(WorldPoint worldPoint, int dist) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GroundObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GroundObject findNearestGroundObjectMenuWithin(WorldPoint worldPoint, int dist, String menuAction) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GroundObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .filter(w -> ArrayUtils.contains(client.getObjectDefinition(w.getId()).getActions(), menuAction))
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public List<GameObject> getGameObjects(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return new ArrayList<>();
        }

        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public List<GameObject> getLocalGameObjects(int distanceAway, int... ids) {
        if (client.getLocalPlayer() == null) {
            return new ArrayList<>();
        }
        List<GameObject> localGameObjects = new ArrayList<>();
        for (GameObject gameObject : getGameObjects(ids)) {
            if (gameObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < distanceAway) {
                localGameObjects.add(gameObject);
            }
        }
        return localGameObjects;
    }

    public GameObject getGameObjectAtWorldPoint(WorldPoint worldPoint) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GameObjectQuery()
                .atWorldLocation(worldPoint)
                .result(client).first();
    }

    public GameObject getGameObjectAtLocalPoint(LocalPoint localPoint) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GameObjectQuery()
                .atLocalLocation(localPoint)
                .result(client).first();
    }

    @Nullable
    public GameObject findNearestBank() {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new GameObjectQuery()
                .idEquals(ALL_BANKS)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
}
