package net.runelite.client.plugins.iutils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.NPCQuery;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Singleton
public class NPCUtils {
    @Inject
    private Client client;

    @Nullable
    public NPC findNearestNpc(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new NPCQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public NPC findNearestNpc(String... names) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new NPCQuery()
                .nameContains(names)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public NPC findNearestNpcIndex(int index, int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        List<NPC> npcs = new ArrayList<>(new NPCQuery()
                .idEquals(ids)
                .result(client));

        for (NPC npc : npcs) {
            if (npc.getIndex() == index) {
                return npc;
            }
        }
        return null;
    }

    @Nullable
    public NPC findNearestNpcWithin(WorldPoint worldPoint, int dist, Collection<Integer> ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        return new NPCQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public NPC findNearestAttackableNpcWithin(WorldPoint worldPoint, int dist, String name, boolean exactnpcname) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }
        for (String npcName : name.split(",")) {
            NPC query;
            if (exactnpcname) {
                query = new NPCQuery()
                        .isWithinDistance(worldPoint, dist)
                        .filter(npc -> npc.getName() != null && npc.getName().toLowerCase().equals(npcName.toLowerCase()) && npc.getInteracting() == null && npc.getHealthRatio() != 0)
                        .result(client)
                        .nearestTo(client.getLocalPlayer());
            } else {
                query = new NPCQuery()
                        .isWithinDistance(worldPoint, dist)
                        .filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(npcName.toLowerCase()) && npc.getInteracting() == null && npc.getHealthRatio() != 0)
                        .result(client)
                        .nearestTo(client.getLocalPlayer());
            }
            if (query != null) {
                return query;
            }
        }
        return null;
    }

    @Nullable
    public NPC findNearestNpcTargetingLocal(String name, boolean exactnpcname) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return null;
        }

        for (String npcName : name.split(",")) {
            NPC query;
            if (exactnpcname) {
                query = new NPCQuery()
                        .filter(npc -> npc.getName() != null && npc.getName().toLowerCase().equals(npcName.toLowerCase()) && npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)
                        .result(client)
                        .nearestTo(client.getLocalPlayer());
            } else {
                query = new NPCQuery()
                        .filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(npcName.toLowerCase()) && npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)
                        .result(client)
                        .nearestTo(client.getLocalPlayer());
            }
            if (query != null) {
                return query;
            }
        }
        return null;
    }

    public List<NPC> getNPCs(int... ids) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return new ArrayList<>();
        }

        return new NPCQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public List<NPC> getNPCs(String... names) {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null) {
            return new ArrayList<>();
        }

        return new NPCQuery()
                .nameContains(names)
                .result(client)
                .list;
    }

    public NPC getFirstNPCWithLocalTarget() {
        assert client.isClientThread();

        List<NPC> npcs = client.getNpcs();
        for (NPC npc : npcs) {
            if (npc.getInteracting() == client.getLocalPlayer()) {
                return npc;
            }
        }
        return null;
    }

}
