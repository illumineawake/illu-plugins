package net.runelite.client.plugins.iutils.bot;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.iutils.api.Interactable;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scene.Locatable;
import net.runelite.client.plugins.iutils.scene.Position;

public class iObject implements Locatable, Interactable
{
	//	@Inject private Bot bot;
	@Inject
	private iUtils utils;
	private Client client;

	//	private final Bot bot;
//	public final Tile tile;
//	public final int id;
//	public int sequence = -1;
//	public final ObjectType type;
//	public TileObject tileObject;
//	public ObjectComposition ObjectComposition;
	public int id;
	public String name;
	public String[] actions;
	public WorldPoint worldPoint;

	public iObject(Client client, int id, String name, String[] actions, WorldPoint worldPoint)
	{
//		this.bot = bot;
		this.client = client;
//		this.tileObject = tileObject;
		this.id = id;
		this.worldPoint = worldPoint;
		this.name = name;
		this.actions = actions;
	}

//	@Override
//	public Bot bot() { return bot; }

	@Override
	public Client client()
	{
		return client;
	}

//	public int id() {
//		return id;
//	}

	@Override
	public Position position()
	{
		return new Position(worldPoint);
	}

//	/**
//	 * The {@link ObjectType} of the object.
//	 */
//	public ObjectType type() {
//		return type;
//	}

	public int id()
	{
		return id;
	}

	public String name() { return name;}

	public List<String> actions() {
			return Arrays.stream(actions)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		}

	/**
	 * The name of the object, or {@code null} if it has no name.
	 */
//	public String name() {
//		return definition().getName();
//	}
//	public String name()
//	{
//		System.out.println("Definition value: " + String.valueOf(definition()));
//		return definition().getName();
//	}

//	public ObjectComposition definition() {
//		AtomicReference<ObjectComposition> ref = new AtomicReference<>();
//
//		clientThread().invoke(() -> {
//			ref.set(client().getObjectDefinition(id()));
//		});
//
//		return ref.get();
//	}

	public ObjectComposition definition() {
		return client().getObjectDefinition(id());
	}

//	public ObjectComposition definition()
//	{
//		CompletableFuture<Object> completableFuture = new CompletableFuture<>();
//		if (client.isClientThread()) {
//			return client.getObjectDefinition(id());
//		} else {
//			clientThread.invoke(() -> {
//				completableFuture.complete(client.getObjectDefinition(id()));
//		});
//			return (ObjectComposition) completableFuture.get();
//	}

//	@Override
//	public List<String> actions() {
//		return Arrays.stream(definition().getActions())
//			.filter(Objects::nonNull)
//			.collect(Collectors.toList());
//	}

//	public List<String> actions()
//	{
//		return Arrays.stream(definition().getActions())
//			.filter(Objects::nonNull)
//			.collect(Collectors.toList());
//	}

	@Override
	public void interact(String action)
	{
		String[] actions = definition().getActions();

		for (int i = 0; i < actions.length; i++)
		{
			if (action.equalsIgnoreCase(actions[i]))
			{
				interact(i);
				return;
			}
		}

		throw new IllegalArgumentException("no action \"" + action + "\" on object " + id());
	}

	public void interact(int action)
	{
		//TODO: write interact method
	}
}
