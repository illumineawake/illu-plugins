package net.runelite.client.plugins.iutils.game;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.geometry.Cuboid;
import net.runelite.api.vars.AccountType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.actor.NpcStream;
import net.runelite.client.plugins.iutils.actor.PlayerStream;
import net.runelite.client.plugins.iutils.api.EquipmentSlot;
import net.runelite.client.plugins.iutils.scene.GameObjectStream;
import net.runelite.client.plugins.iutils.scene.GroundItemStream;
import net.runelite.client.plugins.iutils.scene.ObjectCategory;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.ui.EquipmentItemStream;
import net.runelite.client.plugins.iutils.ui.InventoryItemStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.event.KeyEvent.VK_ENTER;
import static net.runelite.client.plugins.iutils.iUtils.sleep;

/**
 * OPRS compatible port of a comprehensive OSRS automation API developed by Runemoro.
 * Please check out his github at - https://github.com/Runemoro/
 * Whilst all credit should go to him for the massive work in building this API please do not bug him for any issues relating
 * to this OPRS ported version. All support/bug requests should be provided via my github - https://github.com/illumineawake/illu-plugins
 * or via the Illumine Plugins discord
 *
 * @author Runemoro - ported to OPRS by illumine
 */
@Slf4j
@Singleton
public class Game {

    @Inject
    public Client client;

    @Inject
    public ClientThread clientThread;

    @Inject
    public iUtils utils;

    @Inject
    public WalkUtils walkUtils;

    @Inject
    private CalculationUtils calc;

    @Inject
    private KeyboardUtils keyboard;

    public boolean closeWidget;

    iTile[][][] tiles = new iTile[4][104][104];
    Position base;
    private final InteractionManager interactionManager = new InteractionManager(this);

    public Client client() {
        return client;
    }

    public iUtilsConfig config() {
        return utils.config;
    }

    public ClientThread clientThread() {
        return clientThread;
    }

    public <T> T getFromClientThread(Supplier<T> supplier) {
        if (!client.isClientThread()) {
            CompletableFuture<T> future = new CompletableFuture<>();

            clientThread().invoke(() -> {
                future.complete(supplier.get());
            });
            return future.join();
        } else {
            return supplier.get();
        }
    }

    public int tick(int tickMin, int tickMax) {
        Random r = new Random();
        int result = r.nextInt((tickMax + 1) - tickMin) + tickMin;

        for (int i = 0; i < result; i++) {
            tick();
        }
        return result;
    }

    public void tick(int ticks) {
        for (int i = 0; i < ticks; i++) {
            tick();
        }
    }

    public void tick() {
        if (client.getGameState() == GameState.LOGIN_SCREEN || client.getGameState() == GameState.LOGIN_SCREEN_AUTHENTICATOR) {
            return;
        }

        long start = client().getTickCount();

        while (client.getTickCount() == start) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public long ticks() {
        return client.getTickCount();
    }

    public int tickDelay() {
        int tickLength = (int) calc.randomDelay(
                config().tickDelayWeightedDistribution(),
                config().tickDelayMin(),
                config().tickDelayMax(),
                config().tickDelayDeviation(),
                config().tickDelayTarget()
        );
        tick(tickLength);

        return tickLength;
    }

    public long sleepDelay() {
        long sleepLength = calc.randomDelay(
                config().sleepWeightedDistribution(),
                config().sleepMin(),
                config().sleepMax(),
                config().sleepDeviation(),
                config().sleepTarget()
        );
        sleepExact(sleepLength);

        return sleepLength;
    }

    public void randomDelay() {
        switch (calc.getRandomIntBetweenRange(0, 1)) {
            case 0:
                tick(1, 2);
                sleepDelay();
                break;
            case 1:
                sleepDelay();
                break;
        }
    }

    public iPlayer localPlayer() {
        return new iPlayer(this, client.getLocalPlayer(), client.getLocalPlayer().getPlayerComposition());
    }

    public AccountType accountType() {
        return getFromClientThread(() -> client.getAccountType());
    }

    public Position base() {
        return new Position(client.getBaseX(), client.getBaseY(), client.getPlane());
    }

    /**
     * Whether the player is inside of an instance.
     */
    public boolean inInstance() {
        return client().isInInstancedRegion();
    }

    /**
     * Given an instance template position, returns all occurences of
     * the template tile inside the instance.
     */
    public List<Position> instancePositions(Position templatePosition) {
        var results = new ArrayList<Position>();

        for (var z = 0; z < 4; z++) {
            for (var x = 0; x < 104; x++) {
                for (var y = 0; y < 104; y++) {
                    var tile = new iTile(this, client.getScene().getTiles()[z][x][y]);
                    if (tile.templatePosition().equals(templatePosition)) {
                        results.add(tile.position());
                    }
                }
            }
        }

        return results;
    }

    public iTile tile(Position position) {
        int plane = position.z;
        int x = position.x - client.getBaseX();
        int y = position.y - client.getBaseY();
        if (plane < 0 || plane >= 4) {
            return null;
        }
        if (x < 0 || x >= 104) {
            return null;
        }
        if (y < 0 || y >= 104) {
            return null;
        }
        Tile tile = client.getScene().getTiles()[plane][x][y];
        return new iTile(this, tile);
    }

    public Stream<iTile> tiles() {
        return Arrays.stream(client().getScene().getTiles())
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .map(to -> new iTile(this, to));
    }

    public GameObjectStream objects() {
        return getFromClientThread(() -> new GameObjectStream(iUtils.objects.stream()
                .map(o -> new iObject(
                        this,
                        o,
                        client().getObjectDefinition(o.getId())
                ))
                .collect(Collectors.toList())
                .stream())
        );
    }

    public GroundItemStream groundItems() {
        return getFromClientThread(() -> new GroundItemStream(iUtils.tileItems.stream()
                .map(o -> new iGroundItem(
                        this,
                        o,
                        client().getItemComposition(o.getId())
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

    public iWidget widget(int group, int file) {
        Widget widget = client.getWidget(group, file);
        if (widget == null) return null;

        return new iWidget(this, widget);
    }

    public iWidget widget(int group, int file, int child) {
        if (client.getWidget(group, file) == null) {
            return null;
        }
        return new iWidget(this, client.getWidget(group, file).getDynamicChildren()[child]);
    }

    public iWidget widget(WidgetInfo widgetInfo) {
        Widget widget = client.getWidget(widgetInfo);
        if (widget == null) return null;

        return new iWidget(this, widget);
    }

    public InventoryItemStream inventory() {
        return getFromClientThread(() -> new InventoryItemStream(widget(WidgetInfo.INVENTORY).getWidgetItems().stream()
                .map(wi -> new InventoryItem(this, wi, client().getItemDefinition(wi.getId())))
                .collect(Collectors.toList())
                .stream())
        );
    }

    public EquipmentSlot equipmentSlot(int index) {
        for (var slot : EquipmentSlot.values()) {
            if (slot.index == index) {
                return slot;
            }
        }
        return null;
    }

    public EquipmentItemStream equipment() {
        Map<Item, EquipmentSlot> equipped = new HashMap();
        if (client.getItemContainer(InventoryID.EQUIPMENT) != null) {
            Item[] items = client.getItemContainer(InventoryID.EQUIPMENT).getItems();
            for (int i = 0; i <= items.length - 1; i++) {
                if (items[i].getId() == -1 || items[i].getId() == 0) {
                    continue;
                }
                equipped.put(items[i], equipmentSlot(i));
            }
        }
        return getFromClientThread(() -> new EquipmentItemStream(equipped.entrySet().stream()
                .map(i -> new EquipmentItem(this, i.getKey(), client().getItemDefinition(i.getKey().getId()), i.getValue()))
                .collect(Collectors.toList())
                .stream())
                .filter(Objects::nonNull)
        );
    }

    public ItemContainer container(InventoryID inventoryID) {
        return client.getItemContainer(inventoryID);
    }

    public ItemContainer container(int containerId) {
        InventoryID inventoryID = InventoryID.getValue(containerId);
        return client.getItemContainer(inventoryID);
    }

    /**
     * Opens the container interface using the given index.
     *
     * @param index the index of the interface to open. Interface index's are in order of how they appear in game by default
     *              e.g. inventory is 3, logout is 10
     */
    public void openInterface(int index) {
        if (client == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        clientThread.invoke(() -> client.runScript(915, index)); //open inventory
    }

    public void chooseNumber(int number) {
        closeWidget = false;
        clientThread.invoke(() -> {
            client.runScript(108, "Enter amount:");
            client.setVar(VarClientStr.INPUT_TEXT, "" + number);
            client.runScript(112, 84, 0, "");
            client.runScript(112, -1, 10, "");
        });
        closeWidget = true;
    }

    public void typeNumber(int number) {
        keyboard.typeString(String.valueOf(number));
        sleep(calc.getRandomIntBetweenRange(80, 250));
        keyboard.pressKey(VK_ENTER);
        tick();
    }

    public void chooseString(String text) {
        keyboard.typeString(text);
        sleep(calc.getRandomIntBetweenRange(80, 250));
        keyboard.pressKey(VK_ENTER);
    }

    public void pressKey(int keyEvent) {
        keyboard.pressKey(keyEvent);
    }

    /**
     * Sends an item choice to the server.
     */
    public void chooseItem(int item) {
        clientThread.invoke(() -> client.runScript(754, item, 84));
    }

    /**
     * The widget which contains all screens (bank, grand exchange, trade, etc.)
     */
    public iWidget screenContainer() {
        if (client.isResized()) {
            if (varb(4607) == 0) {
                return widget(161, 16); //classic resizable
            }
            return widget(164, 16); //modern resizable
        }
        return widget(548, 40); //fixed
    }

    ///////////////////////////////////////////////////
    //                  Variables                    //
    ///////////////////////////////////////////////////

    public int varb(int id) {
        return getFromClientThread(() -> client.getVarbitValue(id));
    }

    public int varp(int id) {
        return getFromClientThread(() -> client.getVarpValue(id));
    }

    public int energy() {
        return client.getEnergy();
    }

    public int experience(Skill skill) {
        return client.getSkillExperience(skill);
    }

    public int modifiedLevel(Skill skill) {
        return client.getBoostedSkillLevel(skill);
    }

    public int baseLevel(Skill skill) {
        return client.getRealSkillLevel(skill);
    }

    public GrandExchangeOffer grandExchangeOffer(int slot) {
        return client.getGrandExchangeOffers()[slot];
    }

    public boolean membersWorld() {
        return client().getWorldType().contains(WorldType.MEMBERS);
    }

    ///////////////////////////////////////////////////
    //                    Other                      //
    ///////////////////////////////////////////////////

    public void sleepApproximately(int averageTime) { //TODO
        sleepExact(calc.randomDelay(true, (int) (averageTime * 0.7), (int) (averageTime * 1.3), 50, averageTime));
    }

    public void sleepExact(long time) {
        log.debug("Performing sleep for: {}ms", time);
        long endTime = System.currentTimeMillis() + time;

        time = endTime - System.currentTimeMillis();

        if (time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void waitUntil(BooleanSupplier condition) {
        if (!waitUntil(condition, 100)) {
            throw new IllegalStateException("timed out");
        }
    }

    public boolean waitUntil(BooleanSupplier condition, int ticks) {
        for (var i = 0; i < ticks; i++) {
            if (condition.getAsBoolean()) {
                return true;
            }
            tick();
        }
        return false;
    }

    public boolean waitChange(Supplier<Object> supplier, int ticks) {
        var initial = supplier.get();

        for (var i = 0; i < ticks; i++) {
            tick();

            if (!Objects.equals(supplier.get(), initial)) {
                return true;
            }
        }

        return false;
    }

    public void waitChange(Supplier<Object> supplier) {
        if (!waitChange(supplier, 100)) {
            throw new IllegalStateException("timed out");
        }
    }

    public <T> T waitFor(Supplier<T> supplier) {
        var t = waitFor(supplier, 100);

        if (t == null) {
            throw new IllegalStateException("timed out");
        }

        return t;
    }

    public <T> T waitFor(Supplier<T> supplier, int ticks) {
        for (int i = 0; i < ticks; i++) {
            var t = supplier.get();

            if (t != null) {
                return t;
            }

            tick();
        }

        return null;
    }

    public void sendGameMessage(String message) {
        utils.sendGameMessage(message);
    }

    public InteractionManager interactionManager() {
        return interactionManager;
    }

    private static final Polygon NOT_WILDERNESS_BLACK_KNIGHTS = new Polygon( // this is black knights castle
            new int[]{2994, 2995, 2996, 2996, 2994, 2994, 2997, 2998, 2998, 2999, 3000, 3001, 3002, 3003, 3004, 3005, 3005,
                    3005, 3019, 3020, 3022, 3023, 3024, 3025, 3026, 3026, 3027, 3027, 3028, 3028, 3029, 3029, 3030, 3030, 3031,
                    3031, 3032, 3033, 3034, 3035, 3036, 3037, 3037},
            new int[]{3525, 3526, 3527, 3529, 3529, 3534, 3534, 3535, 3536, 3537, 3538, 3539, 3540, 3541, 3542, 3543, 3544,
                    3545, 3545, 3546, 3546, 3545, 3544, 3543, 3543, 3542, 3541, 3540, 3539, 3537, 3536, 3535, 3534, 3533, 3532,
                    3531, 3530, 3529, 3528, 3527, 3526, 3526, 3525},
            43
    );
    private static final Cuboid MAIN_WILDERNESS_CUBOID = new Cuboid(2944, 3525, 0, 3391, 4351, 3);
    private static final Cuboid GOD_WARS_WILDERNESS_CUBOID = new Cuboid(3008, 10112, 0, 3071, 10175, 3);
    private static final Cuboid WILDERNESS_UNDERGROUND_CUBOID = new Cuboid(2944, 9920, 0, 3391, 10879, 3);

    /**
     * Gets the wilderness level based on a world point
     * Java reimplementation of clientscript 384 [proc,wilderness_level]
     *
     * @param point the point in the world to get the wilderness level for
     * @return the int representing the wilderness level
     */
    public static int getWildernessLevelFrom(WorldPoint point) {
        if (MAIN_WILDERNESS_CUBOID.contains(point)) {
            if (NOT_WILDERNESS_BLACK_KNIGHTS.contains(point.getX(), point.getY())) {
                return 0;
            }

            return ((point.getY() - 3520) / 8) + 1; // calc(((coordz(coord) - (55 * 64)) / 8) + 1)
        } else if (GOD_WARS_WILDERNESS_CUBOID.contains(point)) {
            return ((point.getY() - 9920) / 8) - 1; // calc(((coordz(coord) - (155 * 64)) / 8) - 1)
        } else if (WILDERNESS_UNDERGROUND_CUBOID.contains(point)) {
            return ((point.getY() - 9920) / 8) + 1; // calc(((coordz(coord) - (155 * 64)) / 8) + 1)
        }
        return 0;
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
