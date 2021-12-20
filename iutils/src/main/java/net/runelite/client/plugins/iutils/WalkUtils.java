package net.runelite.client.plugins.iutils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.rs.api.RSClient;
import okhttp3.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Singleton
public class WalkUtils {
    @Inject
    private Client client;

    @Inject
    private MouseUtils mouse;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    @Inject
    private iUtils utils;

    @Inject
    private ExecutorService executorService;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final String DAX_API_URL = "https://api.dax.cloud/walker/generatePath";
    private List<WorldPoint> currentPath = new ArrayList<>();
    WorldPoint nextPoint;

    public boolean retrievingPath;
    private int nextFlagDist = -1;
    public static int coordX;
    public static int coordY;
    public boolean walkAction;

    /**
     * Walks to a scene tile, must be accompanied with a click using it without
     * will cause a ban.
     **/
    public void walkTile(int x, int y) {
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    public void sceneWalk(LocalPoint localPoint, int rand, long delay) {
        coordX = localPoint.getSceneX() + calc.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
        coordY = localPoint.getSceneY() + calc.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
        log.debug("Coord values: {}, {}", coordX, coordY);
        walkAction = true;
        utils.doActionMsTime(new LegacyMenuEntry("Walk here", "", 0, MenuAction.WALK.getId(),
                0, 0, false), new Point(0, 0), delay);
    }

    public void sceneWalk(WorldPoint worldPoint, int rand, long delay) {
        LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
        if (localPoint != null) {
            sceneWalk(localPoint, rand, delay);
        } else {
            log.info("WorldPoint to LocalPoint coversion is null");
        }
    }

    public void sceneWalk(Position position, int rand, long delay) {
        coordX = position.x +
                calc.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
        coordY = position.y +
                calc.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
        log.debug("Coord values: {}, {}", coordX, coordY);
        walkAction = true;
        utils.doActionMsTime(new LegacyMenuEntry("Walk here", "", 0, MenuAction.WALK.getId(),
                0, 0, false), new Point(0, 0), delay);
    }

    /**
     * Web-Walking functions
     **/
    public static String post(String url, String json) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json); // new
        log.info("Sending POST request: {}", body);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("key", "sub_DPjXXzL5DeSiPf")
                .addHeader("secret", "PUBLIC-KEY")
                .post(body)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body().string();
    }

    private List<WorldPoint> jsonToObject(String jsonString) {
        Gson g = new Gson();
        Outer outer = g.fromJson(jsonString, Outer.class);
        return outer.path;
    }

    public WorldPoint getNextPoint(List<WorldPoint> worldPoints, int randomRadius) {
        int listSize = worldPoints.size();
        for (int i = listSize - 1; i > 0; i--) {
            if (worldPoints.get(i).isInScene(client)) {
                WorldPoint scenePoint = worldPoints.get((i >= listSize - 1) ? i : (i - calc.getRandomIntBetweenRange(2, 4))); //returns a few tiles into the scene unless it's the destination tile
                return getRandPoint(scenePoint, randomRadius);
            }
        }
        return null;
    }

    public String getDaxPath(WorldPoint start, WorldPoint destination) {
        Player player = client.getLocalPlayer();
        Path path = new Path(start, destination, player);
        Gson gson = new Gson();
        String jsonString = gson.toJson(path);
        String result = "";
        try {
            retrievingPath = true;
            result = post(DAX_API_URL, jsonString);
        } catch (IOException e) {
            retrievingPath = false;
            e.printStackTrace();
        }
        retrievingPath = false;
        return result;
    }

    //Calculates tiles that surround the source tile and returns a random viable tile
    public WorldPoint getRandPoint(WorldPoint sourcePoint, int randRadius) {
        if (randRadius <= 0) {
            return sourcePoint;
        }
        WorldArea sourceArea = new WorldArea(sourcePoint, 1, 1);
        WorldArea possibleArea = new WorldArea(
                new WorldPoint(sourcePoint.getX() - randRadius, sourcePoint.getY() - randRadius, sourcePoint.getPlane()),
                new WorldPoint(sourcePoint.getX() + randRadius, sourcePoint.getY() + randRadius, sourcePoint.getPlane())
        );
        List<WorldPoint> possiblePoints = possibleArea.toWorldPointList();
        List<WorldPoint> losPoints = new ArrayList<>();
        losPoints.add(sourcePoint);
        for (WorldPoint point : possiblePoints) {
            if (sourceArea.hasLineOfSightTo(client, point)) {
                losPoints.add(point);
            }
        }
        WorldPoint randPoint = losPoints.get(calc.getRandomIntBetweenRange(0, losPoints.size() - 1));
        log.info("Source Point: {}, Random point: {}", sourcePoint, randPoint);
        return randPoint;
    }

    public boolean webWalk(WorldPoint destination, int randRadius, boolean isMoving, long sleepDelay) {
        if (retrievingPath) {
            log.info("Waiting for path retrieval");
            return true;
        }
        Player player = client.getLocalPlayer();
        if (player != null) {
            if (player.getWorldLocation().distanceTo(destination) <= randRadius) {
                currentPath.clear();
                nextPoint = null;
                return true;
            }
            if (currentPath.isEmpty() || !currentPath.get(currentPath.size() - 1).equals(destination)) //no current path or destination doesn't match destination param
            {
                String daxResult = getDaxPath(player.getWorldLocation(), destination);
                log.info("daxResult: {}", daxResult);
                if (daxResult.contains("Too Many Requests")) {
                    log.info("Too many dax requests, trying agian");
                    return true;
                }
                if (daxResult.contains("NO_WEB_PATH")) {
                    log.info("Dax path not found");
                    return false;
                }
                if (daxResult.isEmpty()) {
                    log.info("Dax path is empty, failed to retrieve path");
                    return false;
                }
                currentPath = jsonToObject(daxResult); //get a new path
                log.info("Path found: {}", currentPath);
            }
            if (nextFlagDist == -1) {
                nextFlagDist = calc.getRandomIntBetweenRange(0, 10);
            }
            if (!isMoving || (nextPoint != null && nextPoint.distanceTo(player.getWorldLocation()) < nextFlagDist)) {
                nextPoint = getNextPoint(currentPath, randRadius);
                if (nextPoint != null) {
                    log.info("Walking to next tile: {}", nextPoint);
                    sceneWalk(nextPoint, 0, sleepDelay);
                    nextFlagDist = nextPoint.equals(destination) ? 0 : calc.getRandomIntBetweenRange(0, 10);
                    return true;
                } else {
                    log.debug("nextPoint is null");
                    return false;
                }
            }
            return true;
        }
        log.info("End of method");
        return retrievingPath;
    }
}
