package net.runelite.client.plugins.botutils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.rs.api.RSClient;
import okhttp3.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class WalkUtils
{
	@Inject
	private Client client;

	@Inject
	private BotUtils utils;

	@Inject
	private MouseUtils mouse;

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private final String DAX_API_URL = "https://api.dax.cloud/walker/generatePath";
	private List<WorldPoint> currentPath = new ArrayList<>();
	WorldPoint nextPoint;

	public boolean webWalking;
	private int nextFlagDist = -1;
	private int coordX;
	private int coordY;
	private boolean walkAction;

	/**
	 * Walks to a scene tile, must be accompanied with a click using it without
	 * will cause a ban.
	 **/
	private void walkTile(int x, int y)
	{
		RSClient rsClient = (RSClient) client;
		rsClient.setSelectedSceneTileX(x);
		rsClient.setSelectedSceneTileY(y);
		rsClient.setViewportWalking(true);
		rsClient.setCheckClick(false);
	}

	public void walk(LocalPoint localPoint, int rand, long delay)
	{
		coordX = localPoint.getSceneX() + utils.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
		coordY = localPoint.getSceneY() + utils.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
		walkAction = true;
		utils.targetMenu = new MenuEntry("Walk here", "", 0, MenuOpcode.WALK.getId(),
				0, 0, false);
		mouse.delayMouseClick(new Point(0, 0), delay);
	}

	public void walk(WorldPoint worldPoint, int rand, long delay)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint != null)
		{
			coordX = localPoint.getSceneX() + utils.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
			coordY = localPoint.getSceneY() + utils.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
			walkAction = true;
			utils.targetMenu = new MenuEntry("Walk here", "", 0, MenuOpcode.WALK.getId(),
					0, 0, false);
			mouse.delayMouseClick(new Point(0, 0), delay);
		}
		else
		{
			log.info("WorldPoint to LocalPoint coversion is null");
		}
	}

	/**
	 *
	 * Web-Walking functions
	 *
	 **/
	public static String post(String url, String json) throws IOException
	{

		OkHttpClient okHttpClient = new OkHttpClient();
		RequestBody body = RequestBody.create(json, JSON); // new
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

	private List<WorldPoint> jsonToObject(String jsonString)
	{
		Gson g = new Gson();
		Outer outer = g.fromJson(jsonString, Outer.class);
		//log.info("test list output: {}, \n length: {}", outer.path.toString(), outer.path.size());
		return outer.path;
	}

	public WorldPoint getNextPoint(List<WorldPoint> worldPoints, int randomRadius)
	{
		int listSize = worldPoints.size();
		for (int i = listSize - 1; i > 0; i--)
		{
			if (worldPoints.get(i).isInScene(client))
			{
				//log.info("WorldPoint: {} is inScene.", worldPoints.get(i));
				WorldPoint scenePoint = worldPoints.get((i >= listSize - 1) ? i : (i - utils.getRandomIntBetweenRange(2, 4))); //returns a few tiles into the scene unless it's the destination tile
				return getRandPoint(scenePoint, randomRadius);
			}
		}
		return null;
	}

	public List<WorldPoint> getDaxPath(WorldPoint start, WorldPoint destination)
	{
		Player player = client.getLocalPlayer();
		Path path = new Path(start, destination, player);
		Gson gson = new Gson();
		String jsonString = gson.toJson(path);
		String result = "";
		try
		{
			result = post(DAX_API_URL, jsonString);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return jsonToObject(result);
	}

	//Calculates tiles that surround the source tile and returns a random viable tile
	public WorldPoint getRandPoint(WorldPoint sourcePoint, int randRadius)
	{
		if (randRadius <= 0)
		{
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
		for (WorldPoint point : possiblePoints)
		{
			if (sourceArea.hasLineOfSightTo(client, point))
			{
				losPoints.add(point);
			}
		}
		WorldPoint randPoint = losPoints.get(utils.getRandomIntBetweenRange(0, losPoints.size() - 1));
		log.info("Source Point: {}, Random point: {}", sourcePoint, randPoint);
		return randPoint;
	}

	public boolean webWalk(WorldPoint destination, int randRadius, boolean isMoving, long sleepDelay)
	{
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			if (player.getWorldLocation().distanceTo(destination) <= randRadius)
			{
				//log.info("Arrived at destination");
				currentPath.clear();
				webWalking = false;
				nextPoint = null;
				return true;
			}
			webWalking = true;
			if (currentPath.isEmpty() || !currentPath.get(currentPath.size() - 1).equals(destination)) //no current path or destination doesn't match destination param
			{
				currentPath = getDaxPath(player.getWorldLocation(), destination); //get a new path
			}
			if (currentPath.isEmpty())
			{
				log.info("Current path is empty, failed to retrieve path");
				return false;
			}
			if (nextFlagDist == -1)
			{
				nextFlagDist = utils.getRandomIntBetweenRange(0, 10);
				//log.info("Next flag distance: {}", nextFlagDist);
			}
			if (!isMoving || (nextPoint != null && nextPoint.distanceTo(player.getWorldLocation()) < nextFlagDist))
			{
				nextPoint = getNextPoint(currentPath, randRadius);
				if (nextPoint != null)
				{
					log.info("Walking to next tile: {}", nextPoint);
					walk(nextPoint, 0, sleepDelay);
					nextFlagDist = nextPoint.equals(destination) ? 0 : utils.getRandomIntBetweenRange(0, 10);
					//log.info("Next flag distance: {}", nextFlagDist);
				}
				else
				{
					log.info("nextPoint is null");
					return false;
				}
			}
		}
		return false;
	}
}
