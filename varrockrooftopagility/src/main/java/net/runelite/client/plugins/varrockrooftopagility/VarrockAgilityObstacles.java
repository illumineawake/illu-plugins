package net.runelite.client.plugins.varrockrooftopagility;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import static net.runelite.api.ObjectID.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import lombok.AccessLevel;
import lombok.Getter;

public enum VarrockAgilityObstacles
{
	//ROUGH_WALL_14412, CLOTHES_LINE, GAP_14414, WALL_14832, GAP_14833, GAP_14834, GAP_14835, LEDGE_14836, EDGE
	COURSE_GROUND(new WorldArea(new WorldPoint(3184, 3386, 0), new WorldPoint(3243, 3428, 0)),ROUGH_WALL_14412),
	ROOFTOP_ONE(new WorldArea(new WorldPoint(3213, 3409, 3), new WorldPoint(3220, 3420, 3)),CLOTHES_LINE),
	ROOFTOP_TWO(new WorldArea(new WorldPoint(3200, 3412, 3), new WorldPoint(3209, 3420, 3)),GAP_14414),
	CROSSWALK(new WorldArea(new WorldPoint(3192, 3415, 1), new WorldPoint(3198, 3417, 1)),WALL_14832),
	ROOFTOP_THREE(new WorldArea(new WorldPoint(3191, 3401, 3), new WorldPoint(3198, 3407, 3)),GAP_14833),
	ROOFTOP_FOUR(new WorldArea(new WorldPoint(3181, 3393, 3), new WorldPoint(3208, 3401, 3)),GAP_14834),
	ROOFTOP_FIVE(new WorldArea(new WorldPoint(3217, 3392, 3), new WorldPoint(3233, 3404, 3)), GAP_14835),
	ROOFTOP_SIX(new WorldArea(new WorldPoint(3235, 3402, 3), new WorldPoint(3240, 3409, 3)), LEDGE_14836),
	ROOFTOP_SEVEN(new WorldArea(new WorldPoint(3235, 3410, 3), new WorldPoint(3240, 3416, 3)), EDGE);

	//@Getter(AccessLevel.PACKAGE)
	private final WorldArea location;

	//@Getter(AccessLevel.PACKAGE)
	private final int obstacleId;

	VarrockAgilityObstacles(final WorldArea location, final int obstacleId)
	{
		this.location = location;
		this.obstacleId = obstacleId;
	}

	public WorldArea getLocation() { return location; }

	public int getObstacleId() { return obstacleId; }


	public static VarrockAgilityObstacles getArea(WorldPoint worldPoint)
	{
		for (VarrockAgilityObstacles obstacle : values())
		{
			if (obstacle.getLocation().distanceTo(worldPoint) == 0)
			{
				return obstacle;
			}
		}
		return null;
	}

}