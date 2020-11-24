package net.runelite.client.plugins.iutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileItemPile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.DecorativeObjectQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.GroundObjectQuery;
import net.runelite.api.queries.TileQuery;
import net.runelite.api.queries.WallObjectQuery;
import static net.runelite.client.plugins.iutils.Banks.ALL_BANKS;

@Slf4j
@Singleton
public class ObjectUtils
{
	@Inject
	private Client client;

	@Nullable
	public GameObject findNearestGameObject(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist, int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.isWithinDistance(worldPoint, dist)
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist, Collection<Integer> ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.isWithinDistance(worldPoint, dist)
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public GameObject findNearestGameObjectWithin(LocalPoint localPoint, int dist, int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}
		WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);
		return findNearestGameObjectWithin(worldPoint, dist, ids);
	}

	@Nullable
	public GameObject findNearestGameObjectWithin(LocalPoint localPoint, int dist, Collection<Integer> ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}
		WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);
		return findNearestGameObjectWithin(worldPoint, dist, ids);
	}

	public List<WallObject> getWallObjects(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new WallObjectQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	public List<DecorativeObject> getDecorObjects(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new DecorativeObjectQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	public List<GroundObject> getGroundObjects(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new GroundObjectQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	public TileItem getGroundItem(int id)
	{
		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		int z = client.getPlane();

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[z][x][y];

				if (tile == null)
				{
					continue;
				}
				Player player = client.getLocalPlayer();
				if (player == null)
				{
					continue;
				}
				TileItem tileItem = findItemAtTile(tile, id);
				if (tileItem != null)
				{
					return tileItem;
				}
			}
		}
		return null;
	}

	private TileItem findItemAtTile(Tile tile, int id)
	{
		TileItemPile tileItemPile = tile.getItemLayer();
		if (tileItemPile != null)
		{
			TileItem tileItem = (TileItem) tileItemPile.getBottom();
			if (tileItem.getId() == id)
			{
				return tileItem;
			}
		}
		return null;
	}

	@Nullable
	public TileObject findNearestObject(int... ids)
	{
		GameObject gameObject = findNearestGameObject(ids);

		if (gameObject != null)
		{
			return gameObject;
		}

		WallObject wallObject = findNearestWallObject(ids);

		if (wallObject != null)
		{
			return wallObject;
		}
		DecorativeObject decorativeObject = findNearestDecorObject(ids);

		if (decorativeObject != null)
		{
			return decorativeObject;
		}

		return findNearestGroundObject(ids);
	}

	@Nullable
	public List<TileItem> getTileItemsWithin(int distance)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}
		return new TileQuery()
			.isWithinDistance(client.getLocalPlayer().getWorldLocation(), distance)
			.result(client)
			.first()
			.getGroundItems();
	}

	@Nullable
	public List<TileItem> getTileItemsAtTile(Tile tile)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}
		return new TileQuery()
			.atWorldLocation(tile.getWorldLocation())
			.result(client)
			.first()
			.getGroundItems();
	}

	@Nullable
	public WallObject findNearestWallObject(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new WallObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public WallObject findWallObjectWithin(WorldPoint worldPoint, int radius, int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new WallObjectQuery()
			.isWithinDistance(worldPoint, radius)
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public WallObject findWallObjectWithin(WorldPoint worldPoint, int radius, Collection<Integer> ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new WallObjectQuery()
			.isWithinDistance(worldPoint, radius)
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public DecorativeObject findNearestDecorObject(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new DecorativeObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public GroundObject findNearestGroundObject(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GroundObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}

	public List<GameObject> getGameObjects(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new GameObjectQuery()
			.idEquals(ids)
			.result(client)
			.list;
	}

	public List<GameObject> getLocalGameObjects(int distanceAway, int... ids)
	{
		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}
		List<GameObject> localGameObjects = new ArrayList<>();
		for (GameObject gameObject : getGameObjects(ids))
		{
			if (gameObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < distanceAway)
			{
				localGameObjects.add(gameObject);
			}
		}
		return localGameObjects;
	}

	public GameObject getGameObjectAtWorldPoint(WorldPoint worldPoint)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.atWorldLocation(worldPoint)
			.result(client).first();
	}

	public GameObject getGameObjectAtLocalPoint(LocalPoint localPoint)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.atLocalLocation(localPoint)
			.result(client).first();
	}

	@Nullable
	public GameObject findNearestBank()
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
			.idEquals(ALL_BANKS)
			.result(client)
			.nearestTo(client.getLocalPlayer());
	}
}
