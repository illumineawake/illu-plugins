package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.iObject;
import net.runelite.client.plugins.iutils.bot.iTile;
import net.runelite.client.plugins.iutils.scene.Area;
import net.runelite.client.plugins.iutils.scene.ObjectCategory;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.scene.RectangularArea;
import net.runelite.client.plugins.iutils.ui.Chatbox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Walking {
    private static final CollisionMap map;
    private static final int MAX_WALK_DISTANCE = 20;
    private static final Random RANDOM = new Random();
    private static final int MAX_MIN_ENERGY = 50;
    private static final int MIN_ENERGY = 15;
    private static final Area DEATHS_OFFICE = new RectangularArea(3167, 5733, 3184, 5720);

    private final Bot bot;
    private final Chatbox chatbox;

    private int minEnergy = new Random().nextInt(MAX_MIN_ENERGY - MIN_ENERGY + 1) + MIN_ENERGY;

    static {
        Map<SplitFlagMap.Position, byte[]> compressedRegions = new HashMap<>();

        try (ZipInputStream in = new ZipInputStream(Walking.class.getResourceAsStream("/collision-map.zip"))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String[] n = entry.getName().split("_");

                compressedRegions.put(
                        new SplitFlagMap.Position(Integer.parseInt(n[0]), Integer.parseInt(n[1])),
                        in.readAllBytes()
                );
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        map = new CollisionMap(64, compressedRegions);
    }

    public Walking(Bot bot) {
        this.bot = bot;
        chatbox = new Chatbox(bot);
    }

    public void walkTo(Area target) {
        if (target.contains(bot.localPlayer().position())) {
            return;
        }

//        if (DEATHS_OFFICE.contains(bot.localPlayer().templatePosition())) {
//            if (chatbox.chatState() != Chatbox.ChatState.NPC_CHAT) {
//                bot.npcs().withName("Death").nearest().interact("Talk-to");
//                bot.waitUntil(() -> chatbox.chatState() == Chatbox.ChatState.NPC_CHAT);
//            }
//
//            chatbox.chat(
//                    "How do I pay a gravestone fee?",
//                    "How long do I have to return to my gravestone?",
//                    "How do I know what will happen to my items when I die?",
//                    "I think I'm done here."
//            );
//
//            bot.objects().withName("Portal").nearest().interact("Use");
//            bot.waitUntil(() -> !DEATHS_OFFICE.contains(bot.localPlayer().templatePosition()));
//        }

        System.out.println("[Walking] Pathfinding " + bot.localPlayer().position() + " -> " + target);
        Map<Position, List<Transport>> transports = new HashMap<>();
        Map<Position, List<Position>> transportPositions = new HashMap<>();

        for (Transport transport : TransportLoader.buildTransports(bot)) {
            transports.computeIfAbsent(transport.source, k -> new ArrayList<>()).add(transport);
            transportPositions.computeIfAbsent(transport.source, k -> new ArrayList<>()).add(transport.target);
        }

        Map<Position, Teleport> teleports = new LinkedHashMap<>();

        for (Teleport teleport : new TeleportLoader(bot).buildTeleports()) {
            teleports.putIfAbsent(teleport.target, teleport);
        }

        ArrayList<Position> starts = new ArrayList<>(teleports.keySet());
        starts.add(bot.localPlayer().position());
        List<Position> path = new Pathfinder(map, transportPositions, starts, target::contains).find();

        if (path == null) {
            throw new IllegalStateException("couldn't pathfind " + bot.localPlayer().position() + " -> " + target);
        }

        System.out.println("[Walking] Done pathfinding");

        Position startPosition = path.get(0);
        Teleport teleport = teleports.get(startPosition);

        if (teleport != null) {
            System.out.println("[Walking] Teleporting to path start");
            teleport.handler.run();
            bot.waitUntil(() -> bot.localPlayer().position().distanceTo(teleport.target) <= teleport.radius);
        }

        walkAlong(path, transports);
    }

    private void walkAlong(List<Position> path, Map<Position, List<Transport>> transports) {
        int failed = 0;
        Position target = path.get(path.size() - 1);

        while (bot.localPlayer().position().distanceTo(target) > 0) {
            List<Position> remainingPath = remainingPath(path);

            System.out.println("[Walking] " + path.get(0) + " -> " + bot.localPlayer().position() + " -> " + path.get(path.size() - 1) + ": " + (path.size() - remainingPath.size()) + " / " + path.size());

            if (handleBreak(remainingPath, transports) || stepAlong(remainingPath)) {
                failed = 0;
            } else {
                failed++;
            }

            if (failed >= 10) {
                throw new IllegalStateException("stuck in path " + bot.localPlayer().position() + " -> " + target);
            }

            bot.tick();
        }

        System.out.println("[Walking] Path end reached");
    }

    private List<Position> remainingPath(List<Position> path) {
        Position nearest = path.stream()
                .min(Comparator.comparing(p -> bot.localPlayer().position().distanceTo(p)))
                .orElseThrow(() -> new IllegalArgumentException("empty path"));

        List<Position> remainingPath = path.subList(path.indexOf(nearest), path.size());

        if (remainingPath.isEmpty()) {
            throw new IllegalStateException("too far from path " + bot.localPlayer().position() + " -> " + nearest);
        }

        return remainingPath;
    }

    private boolean handleBreak(List<Position> path, Map<Position, List<Transport>> transports) {
        for (int i = 0; i < MAX_WALK_DISTANCE; i++) {
            if (i + 1 >= path.size()) {
                break;
            }

            Position a = path.get(i);
            Position b = path.get(i + 1);
            iTile tileA = bot.tile(a);
            iTile tileB = bot.tile(b);

            if (tileA == null) {
                return false;
            }

            List<Transport> transportTargets = transports.get(a);
            Transport transport = transportTargets == null ? null : transportTargets.stream().filter(t -> t.target.equals(b)).findFirst().orElse(null);

            if (transport != null && bot.localPlayer().position().distanceTo(transport.source) <= transport.sourceRadius) {
                return handleTransport(transport);
            }

            if (hasDiagonalDoor(tileA)) return openDiagonalDoor(a);

            if (tileB == null) {
                return false; // scene edge
            }

            if (hasDoor(tileA) && isWallBlocking(a, b)) return openDoor(a);
            if (hasDoor(tileB) && isWallBlocking(b, a)) return openDoor(b);
        }

        return false;
    }

    private boolean hasDoor(iTile tile) {
        iObject wall = tile.object(ObjectCategory.WALL);
        return wall != null && wall.actions().contains("Open");
    }

    private boolean hasDiagonalDoor(iTile tile) {
        iObject wall = tile.object(ObjectCategory.REGULAR);
        return wall != null && wall.actions().contains("Open");
    }

    private boolean isWallBlocking(Position a, Position b) {
        switch (bot.tile(a).object(ObjectCategory.WALL).orientation()) {
            case 0:
                return a.west().equals(b) || a.west().north().equals(b) || a.west().south().equals(b);
            case 1:
                return a.north().equals(b) || a.north().west().equals(b) || a.north().east().equals(b);
            case 2:
                return a.east().equals(b) || a.east().north().equals(b) || a.east().south().equals(b);
            case 3:
                return a.south().equals(b) || a.south().west().equals(b) || a.south().east().equals(b);
            default:
                throw new AssertionError();
        }
    }

    private boolean openDoor(Position position) {
        bot.tile(position).object(ObjectCategory.WALL).interact("Open");
        bot.waitUntil(this::isStill);
        return true;
    }

    private boolean openDiagonalDoor(Position position) {
        bot.tile(position).object(ObjectCategory.REGULAR).interact("Open");
        bot.waitUntil(this::isStill);
        return true;
    }

    private boolean handleTransport(Transport transport) {
        System.out.println("[Walking] Handling transport " + transport.source + " -> " + transport.target);
        transport.handler.accept(bot);
        return bot.waitUntil(() -> bot.localPlayer().position().distanceTo(transport.target) <= transport.targetRadius, 10000); // todo
    }

    private boolean stepAlong(List<Position> path) {
        List<Position> reachable = new ArrayList<>();

        for (Position position : path) {
            if (bot.tile(position) == null || position.distanceTo(bot.localPlayer().position()) >= MAX_WALK_DISTANCE) {
                System.out.println("null :" + (bot.tile(position) == null));
                System.out.println("distance: " + (position.distanceTo(bot.localPlayer().position()) >= MAX_WALK_DISTANCE));
                break;
            }

            reachable.add(position);
        }

        if (reachable.isEmpty()) {
            throw new IllegalStateException("no tiles in the path are reachable " + bot.localPlayer().position() + " " + path.get(0) + " -> " + path.get(path.size() - 1));
        }

        Position step = reachable.get(Math.min(new Random().nextInt(MAX_WALK_DISTANCE), reachable.size() - 1));
        int currentDistance = bot.localPlayer().position().distanceTo(step);
        int nextStepDistance = 1 + new Random().nextInt(1 + currentDistance / 3);
        bot.tile(step).walkTo();

        Position lastPosition = bot.localPlayer().position();
        while (bot.localPlayer().position().distanceTo(step) > nextStepDistance) {
            bot.tick();

            if (bot.energy() > minEnergy) {
                setRun(true);
            }

            if (lastPosition.equals(bot.localPlayer().position())) {
                System.err.println("[Walking] Path step stuck: " + bot.localPlayer().position() + " -> " + step);
                return false; // stuck, cancel this step
            }

            lastPosition = bot.localPlayer().position();
        }

        return true;
    }

    public void setRun(boolean run) {
        if (isRunning() != run) {
            bot.widget(160, 22).interact(0);
            minEnergy = new Random().nextInt(MAX_MIN_ENERGY - MIN_ENERGY + 1) + MIN_ENERGY;
            System.out.println("[Walking] Enabling run, next minimum run energy: " + minEnergy);
            bot.tick();
        }
    }

    public boolean isRunning() {
        return bot.varp(173) == 1;
    }

    public boolean reachable(Area target) {
        if (bot.localPlayer().position().equals(target)) {
            return true;
        }

        List<Position> path = new Pathfinder(map, Collections.emptyMap(), List.of(bot.localPlayer().position()), target::contains).find();

        if (path == null) {
            return false;
        }

        for (Position position : path) {
            iObject wallObject = bot.tile(position).object(ObjectCategory.WALL);

            if (wallObject != null && wallObject.actions().contains("Open")) {
                return false;
            }
        }

        return true;
    }

    public boolean isStill() {
        Position position = bot.localPlayer().position();
        bot.tick();
        return bot.localPlayer().position().equals(position);
    }
}
