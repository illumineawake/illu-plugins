package net.runelite.client.plugins.iblackjack.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

@Slf4j
public class HopTask extends Task {
    @Inject
    private WorldService worldService;

    private net.runelite.api.World quickHopTargetWorld;
    private int displaySwitcherAttempts = 0;
    private int originalWorld;
    private final WorldArea SHOP_AREA = new WorldArea(new WorldPoint(3353, 2953, 0), new WorldPoint(3364, 2961, 0));

    @Override
    public boolean validate() {
        if (originalWorld != -1 && inventory.isFull() && inShopArea()) {
            return true;
        }
        if (isShopOpen()) {
            Widget jugWidget = client.getWidget(300, 16);
            if (jugWidget == null || jugWidget.getChild(3).getItemQuantity() <= 0) {
                return true;
            }
        }
        return shouldHop && inShopArea();
//		return shouldHop && !isShopOpen() && !inventory.isFull() && inventory.containsItem(ItemID.COINS_995) &&
//			client.getLocalPlayer().getWorldLocation().distanceTo(SHOP_AREA) == 0;
    }

    @Override
    public String getTaskDescription() {
        return status;
    }

    private World findWorld(List<World> worlds, EnumSet<WorldType> currentWorldTypes, int totalLevel, int currentLocation) {
        World world = worlds.get(new Random().nextInt(worlds.size()));

        EnumSet<WorldType> types = world.getTypes().clone();

        types.remove(WorldType.LAST_MAN_STANDING);

        if (types.contains(WorldType.SKILL_TOTAL)) {
            try {
                int totalRequirement = Integer.parseInt(world.getActivity().substring(0, world.getActivity().indexOf(" ")));

                if (totalLevel >= totalRequirement) {
                    types.remove(WorldType.SKILL_TOTAL);
                }
            } catch (NumberFormatException ex) {
                log.warn("Failed to parse total level requirement for target world", ex);
            }
        }

        if (currentWorldTypes.equals(types)) {
            int worldLocation = world.getLocation();

            if (worldLocation == currentLocation) {
                return world;
            }
        }

        return null;
    }

    private void hop() {
        WorldResult worldResult = worldService.getWorlds();
        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        World currentWorld = worldResult.findWorld(client.getWorld());
        log.info("Current world: {}", currentWorld.getLocation());
        if (currentWorld == null) {
            return;
        }

        EnumSet<WorldType> currentWorldTypes = currentWorld.getTypes().clone();

        currentWorldTypes.remove(WorldType.PVP);
        currentWorldTypes.remove(WorldType.HIGH_RISK);
        currentWorldTypes.remove(WorldType.BOUNTY);
        currentWorldTypes.remove(WorldType.SKILL_TOTAL);
        currentWorldTypes.remove(WorldType.LAST_MAN_STANDING);

        List<World> worlds = worldResult.getWorlds();

        int totalLevel = client.getTotalLevel();

        World world;
        do {
            world = findWorld(worlds, currentWorldTypes, totalLevel, currentWorld.getLocation());
        }
        while (world == null || world == currentWorld);

        hop(world.getId());
    }

    private void hop(int worldId) {
        WorldResult worldResult = worldService.getWorlds();
        // Don't try to hop if the world doesn't exist
        World world = worldResult.findWorld(worldId);
        if (world == null) {
            return;
        }

        final net.runelite.api.World rsWorld = client.createWorld();
        rsWorld.setActivity(world.getActivity());
        rsWorld.setAddress(world.getAddress());
        rsWorld.setId(world.getId());
        rsWorld.setPlayerCount(world.getPlayers());
        rsWorld.setLocation(world.getLocation());
        rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

        if (client.getGameState() == GameState.LOGIN_SCREEN) {
            client.changeWorld(rsWorld);
            return;
        }
        utils.sendGameMessage("Hopping to world: " + world.getId());
        quickHopTargetWorld = rsWorld;
        displaySwitcherAttempts = 0;
    }

    @Override
    public void onGameTick(GameTick event) {
        if (isShopOpen()) {
            status = "Close shop and hop";
            entry = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), 11, 19660801, false);
            utils.doActionMsTime(entry, new Point(0, 0), sleepDelay());
            shouldHop = true;
        } else if (client.getWidget(WidgetInfo.WORLD_SWITCHER_LIST) == null) {
            status = "Opening world hop";
            client.openWorldHopper();
        } else {
            if (originalWorld != -1 && inventory.isFull()) {
                status = "Returning to original world: " + originalWorld;
                hop(originalWorld);
                if (quickHopTargetWorld != null) {
                    client.hopToWorld(quickHopTargetWorld);
                    shouldHop = false;
                    quickHopTargetWorld = null;
                    originalWorld = -1;
                } else {
                    status = "Hopping to original world failed";
                }
            }
//			client.hopToWorld(quickHopTargetWorld);
            else if (quickHopTargetWorld != null) {
                originalWorld = (originalWorld == -1) ? client.getWorld() : originalWorld;
                status = "Hopping to world: " + quickHopTargetWorld.getId();
                client.hopToWorld(quickHopTargetWorld);
                shouldHop = false;
                quickHopTargetWorld = null;
            } else {
                originalWorld = (originalWorld == -1) ? client.getWorld() : originalWorld;
                hop();
            }
        }
        log.info(status);
    }
}