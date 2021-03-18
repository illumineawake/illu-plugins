package net.runelite.client.plugins.iutils.bot;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.iutils.actor.NpcStream;
import net.runelite.client.plugins.iutils.actor.PlayerStream;
import net.runelite.client.plugins.iutils.scene.GameObjectStream;
import net.runelite.client.plugins.iutils.scene.ObjectCategory;
import net.runelite.client.plugins.iutils.scene.Position;

@Slf4j
@Singleton
public class Bot {

    @Inject public Client client;
    @Inject public ClientThread clientThread;

    iTile[][][] tiles = new iTile[4][104][104];
    Position base;

    public Client client() {
        return client;
    }

    public ClientThread clientThread() {
        return clientThread;
    }

    public <T> T getFromClientThread(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();

        clientThread().invoke(() -> {
            future.complete(supplier.get());
        });

        return future.join();
    }

    public iPlayer localPlayer() {
        return new iPlayer(this, client.getLocalPlayer(), client.getLocalPlayer().getPlayerComposition());
    }

    public Position base() {
        return base;
    }

    public iTile tile(Position position) {
        int plane = position.z;
        int x = position.x - base.x;
        int y = position.y - base.y;

        if (plane < 0 || plane >= 4) {
            return null;
        }
        if (x < 0 || x >= 104) {
            return null;
        }
        if (y < 0 || y >= 104) {
            return null;
        }

        return tiles[plane][x][y];
    }

    public Stream<iTile> tiles() {
        return Arrays.stream(client().getScene().getTiles())
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .map(to -> new iTile(this, client(), new Position(to.getWorldLocation())));
    }

    public GameObjectStream objects() {
        Collection<BaseObject> baseObjects = new ArrayList<>();
        Tile[][][] tiles = client().getScene().getTiles();
        int plane = client().getPlane();

        for (int j = 0; j < tiles[plane].length; j++) {
            for (int k = 0; k < tiles[plane][j].length; k++) {
                GameObject[] go = tiles[plane][j][k].getGameObjects();
                for (GameObject gameObject : go) {
                    if (gameObject != null) {
                        baseObjects.add(new BaseObject(gameObject, ObjectCategory.REGULAR));
                    }
                }
                WallObject wallObject = tiles[plane][j][k].getWallObject();
                if (wallObject != null) {
                    baseObjects.add(new BaseObject(wallObject, ObjectCategory.WALL));
                }

                GroundObject groundObject = tiles[plane][j][k].getGroundObject();
                if (groundObject!= null) {
                    baseObjects.add(new BaseObject(groundObject, ObjectCategory.FLOOR_DECORATION));
                }

                DecorativeObject decorativeObject = tiles[plane][j][k].getDecorativeObject();
                if (decorativeObject != null) {
                    baseObjects.add(new BaseObject(decorativeObject, ObjectCategory.WALL_DECORATION));
                }
            }
        }
        return getFromClientThread(() -> new GameObjectStream(baseObjects.stream()
                .map(o -> new iObject(
                        this,
                        o.tileObject,
                        o.objectCategory(),
                        client().getObjectDefinition(o.tileObject.getId())
                        ))
                .collect(Collectors.toList())
                .stream())
        );
    }

    public NpcStream npcs() {
        return getFromClientThread(() -> new NpcStream(client().getNpcs().stream()
                .map(npc -> new iNPC(this, npc, client().getNpcDefinition(npc.getId())))
                .collect(Collectors.toList())
                .stream())
        );
    }

    public PlayerStream players() {
        return getFromClientThread(() -> new PlayerStream(client().getPlayers().stream()
                .map(p -> new iPlayer(this, p, p.getPlayerComposition()))
                .collect(Collectors.toList())
                .stream())
        );
    }

    public static class BaseObject {
        private final TileObject tileObject;
        private final ObjectCategory objectCategory;

        public BaseObject(TileObject tileObject, ObjectCategory objectCategory) {
            this.tileObject = tileObject;
            this.objectCategory = objectCategory;
        }

        public TileObject tileObject() {
            return tileObject;
        }

        public ObjectCategory objectCategory() {
            return objectCategory;
        }
    }
}
