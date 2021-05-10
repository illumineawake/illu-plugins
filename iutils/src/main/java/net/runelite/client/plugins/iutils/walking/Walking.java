package net.runelite.client.plugins.iutils.walking;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.iTile;
import net.runelite.client.plugins.iutils.scene.Area;
import net.runelite.client.plugins.iutils.scene.ObjectCategory;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.scene.RectangularArea;
import net.runelite.client.plugins.iutils.ui.Chatbox;
import net.runelite.client.plugins.iutils.util.Util;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public static final ExecutorService PATHFINDING_EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
        var thread = Executors.defaultThreadFactory().newThread(r);
        thread.setDaemon(true);
        return thread;
    });

    private final Bot bot;
    private final Chatbox chatbox;

    private int minEnergy = new Random().nextInt(MAX_MIN_ENERGY - MIN_ENERGY + 1) + MIN_ENERGY;

    static {
        try {
            map = new CollisionMap(Util.ungzip(Walking.class.getResourceAsStream("/collision-map").readAllBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Inject
    public Walking(Bot bot) {
        this.bot = bot;
        chatbox = new Chatbox(bot);
    }

    public void walkTo(Area target) {
        if (target.contains(bot.localPlayer().templatePosition())) {
            return;
        }

        if (DEATHS_OFFICE.contains(bot.localPlayer().templatePosition())) {
            if (chatbox.chatState() != Chatbox.ChatState.NPC_CHAT) {
                bot.npcs().withName("Death").nearest().interact("Talk-to");
                bot.waitUntil(() -> chatbox.chatState() == Chatbox.ChatState.NPC_CHAT);
            }

            chatbox.chat(
                    "How do I pay a gravestone fee?",
                    "How long do I have to return to my gravestone?",
                    "How do I know what will happen to my items when I die?",
                    "I think I'm done here."
            );

            bot.objects().withName("Portal").nearest().interact("Use");
            bot.waitUntil(() -> !DEATHS_OFFICE.contains(bot.localPlayer().templatePosition()));
        }

        System.out.println("[Walking] Pathfinding " + bot.localPlayer().templatePosition() + " -> " + target);
        var transports = new HashMap<Position, List<Transport>>();
        var transportPositions = new HashMap<Position, List<Position>>();

        for (var transport : TransportLoader.buildTransports(bot)) {
            transports.computeIfAbsent(transport.source, k -> new ArrayList<>()).add(transport);
            transportPositions.computeIfAbsent(transport.source, k -> new ArrayList<>()).add(transport.target);
        }

        var teleports = new LinkedHashMap<Position, Teleport>();

        for (var teleport : new TeleportLoader(bot).buildTeleports()) {
            teleports.putIfAbsent(teleport.target, teleport);
        }

        var starts = new ArrayList<>(teleports.keySet());
        starts.add(bot.localPlayer().templatePosition());
        var path = pathfind(starts, target, transportPositions);

        if (path == null) {
            throw new IllegalStateException("couldn't pathfind " + bot.localPlayer().templatePosition() + " -> " + target);
        }

        System.out.println("[Walking] Done pathfinding");

        var startPosition = path.get(0);
        var teleport = teleports.get(startPosition);

        if (teleport != null) {
            System.out.println("[Walking] Teleporting to path start");
            teleport.handler.run();
            bot.waitUntil(() -> bot.localPlayer().templatePosition().distanceTo(teleport.target) <= teleport.radius);
        }

        walkAlong(path, transports);
    }

    private List<Position> pathfind(ArrayList<Position> start, Area target, Map<Position, List<Position>> tranports) {
        var result = PATHFINDING_EXECUTOR.submit(() -> new Pathfinder(map, tranports, start, target::contains).find());

        while (!result.isDone()) {
            bot.tick();
        }

        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void walkAlong(List<Position> path, Map<Position, List<Transport>> transports) {
        var target = path.get(path.size() - 1);

        var fails = 0;

        while (bot.localPlayer().templatePosition().distanceTo(target) > 0) {
            var remainingPath = remainingPath(path);
            var start = path.get(0);
            var current = bot.localPlayer().templatePosition();
            var end = path.get(path.size() - 1);
            var progress = path.size() - remainingPath.size();
            System.out.println("[Walking] " + start + " -> " + current + " -> " + end + ": " + progress + " / " + path.size());

            if (handleBreak(remainingPath, transports)) {
                continue;
            }

            if (!stepAlong(remainingPath)) {
                if (fails++ == 5) {
                    throw new IllegalStateException("stuck in path at " + bot.localPlayer().templatePosition());
                }
            } else {
                fails = 0;
            }
        }

        System.out.println("[Walking] Path end reached");
    }

    /**
     * Remaining tiles in a path, including the tile the player is on.
     */
    private List<Position> remainingPath(List<Position> path) {
        var nearest = path.stream()
                .min(Comparator.comparing(p -> bot.localPlayer().templatePosition().distanceTo(p)))
                .orElseThrow(() -> new IllegalArgumentException("empty path"));

        var remainingPath = path.subList(path.indexOf(nearest), path.size());

        if (remainingPath.isEmpty()) {
            throw new IllegalStateException("too far from path " + bot.localPlayer().templatePosition() + " -> " + nearest);
        }

        return remainingPath;
    }

    private boolean handleBreak(List<Position> path, Map<Position, List<Transport>> transports) {
        for (var i = 0; i < MAX_INTERACT_DISTANCE; i++) {
            if (i + 1 >= path.size()) {
                break;
            }

            var a = path.get(i);
            var b = path.get(i + 1);
            var tileA = tile(a);
            var tileB = tile(b);

            if (tileA == null) {
                return false;
            }

            var transportTargets = transports.get(a);
            var transport = transportTargets == null ? null : transportTargets.stream().filter(t -> t.target.equals(b)).findFirst().orElse(null);

            if (transport != null && bot.localPlayer().templatePosition().distanceTo(transport.source) <= transport.sourceRadius) {
                handleTransport(transport);
                return true;
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
        var wall = tile.object(ObjectCategory.WALL);
        return wall != null && wall.actions().contains("Open");
    }

    private boolean hasDiagonalDoor(iTile tile) {
        var wall = tile.object(ObjectCategory.REGULAR);
        return wall != null && wall.actions().contains("Open");
    }

    private boolean isWallBlocking(Position a, Position b) {
        switch (tile(a).object(ObjectCategory.WALL).orientation()) {
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
        tile(position).object(ObjectCategory.WALL).interact("Open");
        bot.waitUntil(this::isStill);
        bot.tick();
        return true;
    }


    private boolean openDiagonalDoor(Position position) {
        tile(position).object(ObjectCategory.REGULAR).interact("Open");
        bot.waitUntil(this::isStill);
        return true;
    }

    private void handleTransport(Transport transport) {
        System.out.println("[Walking] Handling transport " + transport.source + " -> " + transport.target);
        transport.handler.accept(bot);

        // TODO: if the player isn't on the transport source tile, interacting with the transport may cause the
        //   player to walk to a different source tile for the same transport, which has a different destination
        bot.waitUntil(() -> bot.localPlayer().templatePosition().distanceTo(transport.target) <= transport.targetRadius, 10);
        bot.tick(2);
    }

    private boolean stepAlong(List<Position> path) {
        path = reachablePath(path);
        if (path == null) return false;

        if (path.size() - 1 <= MIN_TILES_WALKED_IN_STEP) {
            return step(path.get(path.size() - 1), Integer.MAX_VALUE);
        }

        var targetDistance = MIN_TILES_WALKED_IN_STEP + RANDOM.nextInt(path.size() - MIN_TILES_WALKED_IN_STEP);
        var rechooseDistance = rechooseDistance(targetDistance);

        return step(path.get(targetDistance), rechooseDistance);
    }

    private int rechooseDistance(int targetDistance) {
        var rechoose = MIN_TILES_WALKED_BEFORE_RECHOOSE + RANDOM.nextInt(targetDistance - MIN_TILES_WALKED_BEFORE_RECHOOSE + 1);
        rechoose = Math.min(rechoose, targetDistance - MIN_TILES_LEFT_BEFORE_RECHOOSE); // don't get too near the end of the path, to avoid stopping
        return rechoose;
    }

    /**
     * Interacts with the target tile to walk to it, and waits for the player to either
     * reach it, or walk {@code tiles} tiles towards it before returning.
     *
     * @return
     */
    private boolean step(Position target, int tiles) {
        if (bot.inInstance()) {
            tile(bot.instancePositions(target).get(0)).walkTo();
        } else {
            tile(target).walkTo();
        }
        var ticksStill = 0;

        for (var tilesWalked = 0; tilesWalked < tiles; tilesWalked += isRunning() ? 2 : 1) {
            if (bot.localPlayer().templatePosition().equals(target)) {
                return false;
            }

            var oldPosition = bot.localPlayer().templatePosition();
            bot.tick();

            if (bot.localPlayer().templatePosition().equals(oldPosition)) {
                if (++ticksStill == 5) {
                    return false;
                }
            } else {
                ticksStill = 0;
            }

            if (!isRunning() && bot.energy() > minEnergy) {
                minEnergy = new Random().nextInt(MAX_MIN_ENERGY - MIN_ENERGY + 1) + MIN_ENERGY;
                System.out.println("[Walking] Enabling run, next minimum run energy: " + minEnergy);
                setRun(true);
            }

            if (bot.inventory().withNamePart("Stamina potion").exists() && bot.energy() < 70 && (bot.varb(25) == 0 || bot.energy() <= 4)) {
                var staminaPotion = bot.inventory().withNamePart("Stamina potion").first();
                staminaPotion.interact("Drink");
                bot.waitUntil(() -> bot.varb(25) == 1 && bot.energy() >= 20);
            }

            if (bot.varb(25) == 1) {
                setRun(true); // don't waste stamina effect
            }
        }

        return true;
    }

    /**
     * Tiles in a remaining path which can be walked to (including the tile the
     * player is currently on).
     */
    private List<Position> reachablePath(List<Position> remainingPath) {
        var reachable = new ArrayList<Position>();

        for (var position : remainingPath) {
            if (bot.tile(position) == null || position.distanceTo(bot.localPlayer().templatePosition()) >= MAX_INTERACT_DISTANCE) {
                break;
            }

            reachable.add(position);
        }

        if (reachable.isEmpty() || reachable.size() == 1 && reachable.get(0).equals(bot.localPlayer().templatePosition())) {
//            throw new IllegalStateException("no tiles in the path are reachable");
            return null;
        }

        return reachable;
    }

    private iTile tile(Position position) {
        if (bot.inInstance()) {
            var instancePositions = bot.instancePositions(position);

            if (instancePositions.isEmpty()) {
                return null;
            }

            return bot.tile(instancePositions.get(0));
        } else {
            return bot.tile(position);
        }
    }

    public void setRun(boolean run) {
        if (isRunning() != run) {
            bot.widget(160, 22).interact(0);
            bot.waitUntil(() -> isRunning() == run);
        }
    }

    public boolean isRunning() {
        return bot.varp(173) == 1;
    }

    public boolean reachable(Area target) {
        if (bot.localPlayer().templatePosition().equals(target)) {
            return true;
        }

        var path = new Pathfinder(map, Collections.emptyMap(), List.of(bot.localPlayer().templatePosition()), target::contains).find();

        if (path == null) {
            return false;
        }

        for (var position : path) {
            var wallObject = tile(position).object(ObjectCategory.WALL);

            if (wallObject != null && wallObject.actions().contains("Open")) {
                return false;
            }
        }

        return true;
    }

    public boolean isStill() {
        var position = bot.localPlayer().templatePosition();
        bot.tick();
        return bot.localPlayer().templatePosition().equals(position);
    }
}
