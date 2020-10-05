package net.runelite.client.plugins.botutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.NPCQuery;

@Slf4j
@Singleton
public class NPCUtils
{
	@Inject
	private Client client;

	@Nullable
	public NPC findNearestNpc(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new NPCQuery()
				.idEquals(ids)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public NPC findNearestNpc(String... names)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new NPCQuery()
				.nameContains(names)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public NPC findNearestNpcWithin(WorldPoint worldPoint, int dist, Collection<Integer> ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new NPCQuery()
				.isWithinDistance(worldPoint, dist)
				.idEquals(ids)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}

	@Nullable
	public NPC findNearestAttackableNpcWithin(WorldPoint worldPoint, int dist, String name, boolean exactnpcname)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		if (exactnpcname)
		{
			return new NPCQuery()
					.isWithinDistance(worldPoint, dist)
					.filter(npc -> npc.getName() != null && npc.getName().toLowerCase().equals(name.toLowerCase()) && npc.getInteracting() == null && npc.getHealthRatio() != 0)
					.result(client)
					.nearestTo(client.getLocalPlayer());
		}
		else
		{
			return new NPCQuery()
					.isWithinDistance(worldPoint, dist)
					.filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(name.toLowerCase()) && npc.getInteracting() == null && npc.getHealthRatio() != 0)
					.result(client)
					.nearestTo(client.getLocalPlayer());
		}
	}

	@Nullable
	public NPC findNearestNpcTargetingLocal(String name, boolean exactnpcname)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		if (exactnpcname)
		{
			return new NPCQuery()
					.filter(npc -> npc.getName() != null && npc.getName().toLowerCase().equals(name.toLowerCase()) && npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)
					.result(client)
					.nearestTo(client.getLocalPlayer());
		}
		else
		{
			return new NPCQuery()
					.filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(name.toLowerCase()) && npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)
					.result(client)
					.nearestTo(client.getLocalPlayer());
		}

	}

	public List<NPC> getNPCs(int... ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new NPCQuery()
				.idEquals(ids)
				.result(client)
				.list;
	}

	public List<NPC> getNPCs(String... names)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return new ArrayList<>();
		}

		return new NPCQuery()
				.nameContains(names)
				.result(client)
				.list;
	}

	public NPC getFirstNPCWithLocalTarget()
	{
		assert client.isClientThread();

		List<NPC> npcs = client.getNpcs();
		for (NPC npc : npcs)
		{
			if (npc.getInteracting() == client.getLocalPlayer())
			{
				return npc;
			}
		}
		return null;
	}

}
