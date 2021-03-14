package net.runelite.client.plugins.iutils.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scene.GameObjectStream;
import net.runelite.client.plugins.iutils.scene.Position;

@Slf4j
@Singleton
public class Bot
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private iUtils utils;

	iTile[][][] tiles = new iTile[4][104][104];
	Position base;

	public Client client()
	{
		return client;
	}

	public ClientThread clientThread()
	{
		return clientThread;
	}

	public <T> T getFromClientThread(Supplier<T> supplier) {
		CompletableFuture<T> future = new CompletableFuture<>();

		clientThread().invoke(() -> {
			future.complete(supplier.get());
		});

		return future.join();
	}

	public Player localPlayer() {
		System.out.println("Local: " + client.getLocalPlayer().getWorldLocation().toString());
		return client.getLocalPlayer();
	}

	public Position base()
	{
		return base;
	}

	public iTile tile(Position position)
	{
		int plane = position.z;
		int x = position.x - base.x;
		int y = position.y - base.y;

		if (plane < 0 || plane >= 4)
		{
			return null;
		}
		if (x < 0 || x >= 104)
		{
			return null;
		}
		if (y < 0 || y >= 104)
		{
			return null;
		}

		return tiles[plane][x][y];
	}

	public Stream<iTile> tiles()
	{
		return Arrays.stream(client.getScene().getTiles())
			.flatMap(Arrays::stream)
			.flatMap(Arrays::stream)
			.filter(Objects::nonNull)
			.map(to -> new iTile(client(), new Position(to.getWorldLocation())));
	}

	public GameObjectStream objects()
	{
		Collection<TileObject> allObjects = new ArrayList<>();
		Tile[][][] tiles = client().getScene().getTiles();
		int plane = client().getPlane();

		for (int j = 0; j < tiles[plane].length; j++)
		{
			for (int k = 0; k < tiles[plane][j].length; k++)
			{
				GameObject[] go = tiles[plane][j][k].getGameObjects();
				for (int g = 0; g < go.length; g++)
				{
					if (go[g] != null)
					{
						allObjects.add(go[g]);
					}
				}

				if (tiles[plane][j][k].getWallObject() != null)
				{
					allObjects.add(tiles[plane][j][k].getWallObject());
				}

				if (tiles[plane][j][k].getGroundObject() != null)
				{
					allObjects.add(tiles[plane][j][k].getGroundObject());
				}

				if (tiles[plane][j][k].getDecorativeObject() != null)
				{
					allObjects.add(tiles[plane][j][k].getDecorativeObject());
				}
			}
		}

		return getFromClientThread(() -> new GameObjectStream(
			allObjects.stream()
				.map(to -> new iObject(client(), to.getId(), to.getName(), to.getActions(), to.getWorldLocation()))
				.collect(Collectors.toList())
				.stream()
		));
	}
}
