package net.runelite.client.plugins.iutils;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.rs.api.RSClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Singleton
public class WalkUtils
{
	@Inject
	private Client client;

	@Inject
	private MouseUtils mouse;

	@Inject
	private CalculationUtils calc;

	@Inject
	private ObjectUtils object;

	@Inject
	private MenuUtils menu;

	@Inject
	private iUtils utils;

	private int currentIndex;
	private int obstacleAttempts;
	private boolean handlingObstacle;
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private final String DAX_API_URL = "https://api.dax.cloud/walker/generatePath";

	private List<WorldPoint> currentPath = new ArrayList<>();
	WorldPoint nextPoint;
	WorldArea previousArea;
	private final List<String> actions = List.of("Pay-toll(10gp)", "Activate", "Ascend", "Attack", "Balance", "Balance-across",	"Bank",
		"Board", "Build", "Build mode",	"Capture", "Channel", "Chop", "Chop down",
		"Chop-down",
		"Claim-staves",
		"Clamber",
		"Clear",
		"Climb",
		"Climb down",
		"Climb over",
		"Climb through",
		"Climb up",
		"Climb-down",
		"Climb-into",
		"Climb-off",
		"Climb-on",
		"Climb-over",
		"Climb-through",
		"Climb-under",
		"Climb-up",
		"Commune",
		"Configure",
		"Continue-trek",
		"Cook",
		"Crawl",
		"Crawl through",
		"Crawl-down",
		"Crawl-through",
		"Cross",
		"Cross-to",
		"Cut",
		"Deposit",
		"Descend",
		"Dig",
		"Disarm",
		"Dispel",
		"Distract",
		"Dive",
		"Dive in",
		"Drink",
		"Drink-from",
		"Edgeville",
		"Empty",
		"Enter",
		"Enter-corrupted",
		"Escape",
		"Evade-event",
		"Exit",
		"Exit-through",
		"Feel",
		"Fire!",
		"Fish",
		"Forfeit",
		"Free",
		"Friend's house",
		"Fuel",
		"Get in",
		"Go-down",
		"Go-through",
		"Go-up",
		"Grapple",
		"Harvest",
		"Hide-behind",
		"Hide-in",
		"Home",
		"Hurdle",
		"Iceberg",
		"Inspect",
		"Interact",
		"Investigate",
		"Join",
		"Jump",
		"Jump off",
		"Jump on",
		"Jump-across",
		"Jump-down",
		"Jump-in",
		"Jump-off",
		"Jump-on",
		"Jump-Over",
		"Jump-to",
		"Jump-up",
		"Leap",
		"Leave",
		"Leave platform",
		"Leave-area",
		"Light",
		"Listen",
		"Load",
		"Look-in",
		"Look-inside",
		"Look-through",
		"Look-up",
		"menuOption",
		"Mine",
		"Move",
		"Navigate",
		"Observe",
		"Open",
		"Operate",
		"Paddle Canoe",
		"Pass",
		"Pass-through",
		"Pay",
		"Pay-fare",
		"Pay-toll(2-Ecto)",
		"Peek",
		"Pick",
		"Pick-Fruit",
		"Pick-lock",
		"Pick-up",
		"Picklock",
		"Pray",
		"Pray-at",
		"Press",
		"Private",
		"Pull",
		"Push",
		"Push-through",
		"Quick-board",
		"Quick-enter",
		"Quick-escape",
		"Quick-exit",
		"Quick-open",
		"Quick-pass",
		"Quick-start",
		"Rake",
		"Reach",
		"Read",
		"Reminisce",
		"Remove",
		"Repair",
		"Ride",
		"Ring",
		"Roll",
		"Scale",
		"Search",
		"Search for traps",
		"Set up",
		"Set-trap",
		"Shoot",
		"Slash",
		"Slayer",
		"Smash-to-bits",
		"Squeeze-past",
		"Squeeze-through",
		"Standard",
		"Step-into",
		"Step-on",
		"Stock-Up",
		"Swing",
		"Swing Across",
		"Swing-across",
		"Swing-on",
		"Take",
		"Take-concoction",
		"Take-powder",
		"Talk-to",
		"Teeth-grip",
		"Teleport",
		"Teleport to Destination",
		"Touch",
		"Travel",
		"Travel to platform",
		"Turn",
		"Unblock",
		"Unlock",
		"Use",
		"Use-Lift",
		"Vault",
		"View",
		"Walk on",
		"Walk through",
		"Walk-across",
		"Walk-down",
		"Walk-on",
		"Walk-over",
		"Walk-through",
		"Walk-up",
		"Weiss",
		"Worship",
		"Zanaris",
		"Smelt",
		"Steal-from",
		"Take treasure",
		"Visit-Last");

	public boolean retrievingPath;
	private int nextFlagDist = -1;
	public int coordX;
	public int coordY;
	public boolean walkAction;

	/**
	 * Walks to a scene tile, must be accompanied with a click using it without
	 * will cause a ban.
	 **/
	public void walkTile(int x, int y)
	{
		RSClient rsClient = (RSClient) client;
		rsClient.setSelectedSceneTileX(x);
		rsClient.setSelectedSceneTileY(y);
		rsClient.setViewportWalking(true);
		rsClient.setCheckClick(false);
	}

	public void sceneWalk(LocalPoint localPoint, int rand, long delay)
	{
		coordX = localPoint.getSceneX() + calc.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
		coordY = localPoint.getSceneY() + calc.getRandomIntBetweenRange(-Math.abs(rand), Math.abs(rand));
		log.info("Coord values: {}, {}", coordX, coordY);
		walkAction = true;
		menu.setEntry(new MenuEntry("Walk here", "", 0, MenuOpcode.WALK.getId(),
			0, 0, false));
		mouse.delayMouseClick(new Point(0, 0), delay);
	}

	public void sceneWalk(WorldPoint worldPoint, int rand, long delay)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint != null)
		{
			sceneWalk(localPoint, rand, delay);
		}
		else
		{
			log.info("WorldPoint to LocalPoint coversion is null");
		}
	}

	/**
	 * Web-Walking functions
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

	public WorldPoint getNextPointFromEnd(List<WorldPoint> worldPoints, int randomRadius)
	{
		int listSize = worldPoints.size();
		for (int i = listSize - 1; i > 0; i--)
		{
			if (worldPoints.get(i).isInScene(client))
			{
				//log.info("WorldPoint: {} is inScene.", worldPoints.get(i));
				WorldPoint scenePoint = worldPoints.get((i >= listSize - 1) ? i : (i - calc.getRandomIntBetweenRange(2, 4))); //returns a few tiles into the scene unless it's the destination tile
				return getRandPoint(scenePoint, randomRadius);
			}
		}
		return null;
	}

	public WorldPoint getNextPointFromStart(List<WorldPoint> worldPoints, int randomRadius)
	{
		int maxDistance = calc.getRandomIntBetweenRange(45, 60);
		handlingObstacle = false;
		log.info("Previous area: {}", previousArea.toWorldPoint().toString());
		int listSize = worldPoints.size();
		if (currentIndex >= worldPoints.size())
		{
			log.info("Resetting current walk index");
			currentIndex = 0;
		}
		for (currentIndex = currentIndex; currentIndex <= listSize; currentIndex++)
		{
			WorldPoint currentPoint = worldPoints.get(currentIndex);
			log.info("InScene: {}, Loc: {}", currentPoint.isInScene(client),currentPoint);
			if (currentPoint.isInScene(client) && client.getLocalPlayer().getWorldLocation().distanceTo(currentPoint) < maxDistance)
			{
				if (!previousArea.canTravelInDirection(client, currentPoint.getX() - previousArea.toWorldPoint().getX(),
					currentPoint.getY() - previousArea.toWorldPoint().getY()) &&
					object.findNearestWallObjectMenuWithin(currentPoint, 1, "Close") == null)
				{
					log.info("Missing LOS. Previous: {}, current: {}, region: {}", previousArea.toWorldPoint().toString(), currentPoint.toString(), currentPoint.getRegionID());
					/*if (client.getLocalPlayer().getWorldLocation().distanceTo(currentPoint) > 10 &&
					previousArea != client.getLocalPlayer().getWorldArea())
					{
						log.info("No LOS but distance is >10 so walking closer"); //LOS sometimes triggered by scene not being loaded
						currentIndex -= 9;
						previousArea = worldPoints.get(currentIndex).toWorldArea();
						handlingObstacle = false;
						return getRandPoint(worldPoints.get(currentIndex), 1);
					}*/
					handlingObstacle = true;
					TileObject obstacleObject = object.findNearestObjectWithin(currentPoint, 0);
					if (obstacleObject == null)
					{
						log.info("Trying to find at previous point: {}", previousArea.toWorldPoint());
						obstacleObject = object.findNearestObjectWithin(previousArea.toWorldPoint(), 0);
					}
					if (obstacleObject != null)
					{
						if (handleObstacle(obstacleObject))
						{
							previousArea = obstacleObject.getWorldLocation().toWorldArea();
							return obstacleObject.getWorldLocation();
						}
						else
						{
							log.info("Obstacle found while walking but we couldn't handle it! Trying walk");
							if (obstacleAttempts < 5)
							{
								handlingObstacle = false;
								previousArea = worldPoints.get(currentIndex).toWorldArea();
								return getRandPoint(worldPoints.get(currentIndex - 1), 1);
							}
							else
								return null;
//							return (obstacleAttempts < 5) ? client.getLocalPlayer().getWorldLocation() : null;
						}
					}
					else
					{
						log.info("LOS to next tile not achieved but can't find obstacle, trying walk");
						if (obstacleAttempts < 5)
						{
							handlingObstacle = false;
							previousArea = worldPoints.get(currentIndex).toWorldArea();
							return getRandPoint(worldPoints.get(currentIndex - 1), 1);
						}
						else
							return null;
//						return (obstacleAttempts < 5) ? client.getLocalPlayer().getWorldLocation() : null;
					}
				}
				if (currentIndex < listSize - 1) //destination tile
				{
					log.info("Incrementing");
					obstacleAttempts = 0;
					handlingObstacle = false;
					previousArea = worldPoints.get(currentIndex).toWorldArea();
				}
				else
				{
					log.info("Found destination tile");
					obstacleAttempts = 0;
					handlingObstacle = false;
					previousArea = worldPoints.get(currentIndex).toWorldArea();
					return getRandPoint(worldPoints.get(currentIndex), randomRadius);
				}
			}
			else
			{
				log.info("Found non-scene tile, returning previous: {}", previousArea.toWorldPoint());
				WorldPoint scenePoint = worldPoints.get((currentIndex >= listSize - 1) ? currentIndex : (currentIndex - calc.getRandomIntBetweenRange(2, 4))); //returns a few tiles into the scene unless it's the destination tile
				return getRandPoint(scenePoint, randomRadius);
			}
			log.info("currentIndex {}, current list size: {}", currentIndex, listSize);
		}
		return null;
	}

	private boolean handleObstacle(TileObject obstacleObject)
	{
		final int BASE_OPCODE = MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId();
		List<String> obstacleActions = new ArrayList<>(Arrays.asList(client.getObjectDefinition(obstacleObject.getId()).getActions()));
		log.info("Obstacle: {} - {}, {} - Actions: {}", obstacleObject.getId(), client.getObjectDefinition(obstacleObject.getId()).getName(), obstacleObject.getWorldLocation(), obstacleActions.toString());
		String matchingAction = actions.stream()
			.filter(obstacleActions::contains)
			.findFirst()
			.orElse(null);

		obstacleAttempts++;

		if (matchingAction != null)
		{
			int opcode = BASE_OPCODE + obstacleActions.indexOf(matchingAction);
			log.info("Traversal obstacle found. ID: {}, Loc: {} Action: {}, Opcode: {}",obstacleObject.getId(), obstacleObject.getWorldLocation(), matchingAction, opcode);
			utils.doTileObjectActionGameTick(obstacleObject, opcode, 0);
			return true;
		}
		return false;
	}

	public String getDaxResult(WorldPoint start, WorldPoint destination)
	{
		Player player = client.getLocalPlayer();
		Path path = new Path(start, destination, player);
		Gson gson = new Gson();
		String jsonString = gson.toJson(path);
		String result = "";
		try
		{
			retrievingPath = true;
			result = post(DAX_API_URL, jsonString);
		}
		catch (IOException e)
		{
			retrievingPath = false;
			e.printStackTrace();
		}
		retrievingPath = false;
		return result;
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
		WorldPoint randPoint = losPoints.get(calc.getRandomIntBetweenRange(0, losPoints.size() - 1));
		log.info("Source Point: {}, Random point: {}", sourcePoint, randPoint);
		return randPoint;
	}

	public boolean webWalk(WorldPoint destination, int randRadius, boolean isMoving, long sleepDelay)
	{
		if (retrievingPath)
		{
			log.info("Waiting for path retrieval");
			return true;
		}
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			if (player.getWorldLocation().distanceTo(destination) <= randRadius)
			{
				currentPath.clear();
				nextPoint = null;
				return true;
			}
			if (currentPath.isEmpty() || currentPath.get(currentPath.size() - 1).distanceTo(destination) > 5) //no current path or destination doesn't match destination param
			{
				currentIndex = 0;
				log.info("Empty path: {}, size = destination: {}", currentPath.isEmpty(), currentPath.size());
				String daxResult = getDaxResult(player.getWorldLocation(), destination);
				log.info("daxResult: {}", daxResult);
				if (daxResult.contains("Too Many Requests"))
				{
					log.info("Too many dax requests, trying again");
					return true;
				}
				if (daxResult.contains("NO_WEB_PATH"))
				{
					log.info("Dax path not found");
					return false;
				}
				if (daxResult.isEmpty())
				{
					log.info("Dax path is empty, failed to retrieve path");
					return false;
				}
				currentPath = jsonToObject(daxResult); //get a new path
				log.info("Path found: {}", currentPath);
				previousArea = player.getWorldArea();
			}
			if (nextFlagDist == -1)
			{
				nextFlagDist = calc.getRandomIntBetweenRange(0, 10);
			}
			if (!isMoving || (nextPoint != null && nextPoint.distanceTo(player.getWorldLocation()) < nextFlagDist && !handlingObstacle))
			{
				nextPoint = getNextPointFromStart(currentPath, randRadius);
				if (handlingObstacle && nextPoint != null)
				{
					log.info("Handling walk obstacle");
					return true;
				}
				if (nextPoint != null)
				{
					log.info("Walking to next tile: {}", nextPoint);
					sceneWalk(nextPoint, 0, sleepDelay);
					nextFlagDist = nextPoint.equals(destination) ? 0 : calc.getRandomIntBetweenRange(0, 10);
					return true;
				}
				else
				{
					log.info("nextPoint is null");
					return false;
				}
			}
			return true;
		}
		return retrievingPath;
	}
}
