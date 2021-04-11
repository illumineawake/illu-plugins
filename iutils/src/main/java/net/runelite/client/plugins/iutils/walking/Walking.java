package net.runelite.client.plugins.iutils.walking;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.WalkUtils;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.iObject;
import net.runelite.client.plugins.iutils.bot.iTile;
import net.runelite.client.plugins.iutils.scene.Area;
import net.runelite.client.plugins.iutils.scene.ObjectCategory;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.scene.RectangularArea;
import net.runelite.client.plugins.iutils.ui.Chatbox;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class Walking {
    private static final CollisionMap map;
    private static final int MAX_INTERACT_DISTANCE = 20;
    private static final int MIN_TILES_WALKED_IN_STEP = 10;
    private static final int MIN_TILES_WALKED_BEFORE_RECHOOSE = 10; // < MIN_TILES_WALKED_IN_STEP
    private static final int MIN_TILES_LEFT_BEFORE_RECHOOSE = 3; // < MIN_TILES_WALKED_IN_STEP
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
        Position target = path.get(path.size() - 1);

        while (bot.localPlayer().position().distanceTo(target) > 0) {
            List<Position> remainingPath = remainingPath(path);
            Position start = path.get(0);
            Position current = bot.localPlayer().position();
            Position end = path.get(path.size() - 1);
            int progress = path.size() - remainingPath.size();
            System.out.println("[Walking] " + start + " -> " + current + " -> " + end + ": " + progress + " / " + path.size());

            if (handleBreak(remainingPath, transports)) {
                continue;
            }

            stepAlong(remainingPath);
        }

        System.out.println("[Walking] Path end reached");
    }

    /**
     * Remaining tiles in a path, including the tile the player is on.
     */
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
        for (int i = 0; i < MAX_INTERACT_DISTANCE; i++) {
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
                if (handleTransport(transport)) {
                    if (transport.source.regionID() != bot.localPlayer().position().regionID()) {
                        bot.tick(5);
                    }
                    return true;
                }
                return false;
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

//        if (bot.screenContainer().nestedInterface() == 580) { TODO: Handle Mort Myre Gate
//            bot.widget(580, 20).interact("Off/On");
//            bot.sleepApproximately(1000);
//            bot.widget(580, 17).interact("Yes");
//            bot.waitUntil(() -> bot.screenContainer().nestedInterface() == -1);
//            bot.waitUntil(this::isStill);
//        }

        bot.waitUntil(this::isStill);
        log.info("Waiting 3 ticks for door");
        bot.tick(3);
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

    private void stepAlong(List<Position> path) {
        path = reachablePath(path);

        if (path.size() - 1 <= MIN_TILES_WALKED_IN_STEP) {
            step(path.get(path.size() - 1), Integer.MAX_VALUE);
            return;
        }

        int targetDistance = MIN_TILES_WALKED_IN_STEP + RANDOM.nextInt(path.size() - MIN_TILES_WALKED_IN_STEP);
        int rechooseDistance = rechooseDistance(targetDistance);

        step(path.get(targetDistance), rechooseDistance);
    }

    private int rechooseDistance(int targetDistance) {
        int rechoose = MIN_TILES_WALKED_BEFORE_RECHOOSE + RANDOM.nextInt(targetDistance - MIN_TILES_WALKED_BEFORE_RECHOOSE + 1);
        rechoose = Math.min(rechoose, targetDistance - MIN_TILES_LEFT_BEFORE_RECHOOSE); // don't get too near the end of the path, to avoid stopping
        return rechoose;
    }

    /**
     * Interacts with the target tile to walk to it, and waits for the player to either
     * reach it, or walk {@code tiles} tiles towards it before returning.
     */
    private void step(Position target, int tiles) {
        bot.tile(target).walkTo();
        int ticksStill = 0;

        for (int tilesWalked = 0; tilesWalked < tiles; tilesWalked += isRunning() ? 2 : 1) {
            if (bot.localPlayer().position().equals(target)) {
                return;
            }

            Position oldPosition = bot.localPlayer().position();
            bot.tick();

            if (bot.localPlayer().position().equals(oldPosition)) {
                if (++ticksStill == 5) {
                    throw new IllegalStateException("stuck in path at " + bot.localPlayer().position());
                }
            } else {
                ticksStill = 0;
            }

            if (!isRunning() && bot.energy() > minEnergy) {
                minEnergy = new Random().nextInt(MAX_MIN_ENERGY - MIN_ENERGY + 1) + MIN_ENERGY;
                System.out.println("[Walking] Enabling run, next minimum run energy: " + minEnergy);
                setRun(true);
            }
        }
    }

    /**
     * Tiles in a remaining path which can be walked to (including the tile the
     * player is currently on).
     */
    private List<Position> reachablePath(List<Position> remainingPath) {
        List<Position> reachable = new ArrayList<>();

        for (Position position : remainingPath) {
            if (bot.tile(position) == null || position.distanceTo(bot.localPlayer().position()) >= MAX_INTERACT_DISTANCE) {
                break;
            }

            reachable.add(position);
        }

        if (reachable.isEmpty() || reachable.size() == 1 && reachable.get(0).equals(bot.localPlayer().position())) {
            throw new IllegalStateException("no tiles in the path are reachable");
        }

        return reachable;
    }

    public void setRun(boolean run) {
        if (isRunning() != run) {
            bot.widget(160, 22).interact(0);
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
