package net.runelite.client.plugins.rooftopagility;

import static net.runelite.api.ObjectID.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

public enum RooftopAgilityObstacles
{
	//VARROCK
	COURSE_GROUND(new WorldArea(new WorldPoint(3184, 3386, 0), new WorldPoint(3243, 3428, 0)),ROUGH_WALL_14412),
	ROOFTOP_ONE(new WorldArea(new WorldPoint(3213, 3409, 3), new WorldPoint(3220, 3420, 3)),CLOTHES_LINE),
	ROOFTOP_TWO(new WorldArea(new WorldPoint(3200, 3412, 3), new WorldPoint(3209, 3420, 3)),GAP_14414),
	CROSSWALK(new WorldArea(new WorldPoint(3192, 3415, 1), new WorldPoint(3198, 3417, 1)),WALL_14832),
	ROOFTOP_THREE(new WorldArea(new WorldPoint(3191, 3401, 3), new WorldPoint(3198, 3407, 3)),GAP_14833),
	ROOFTOP_FOUR(new WorldArea(new WorldPoint(3181, 3393, 3), new WorldPoint(3208, 3401, 3)),GAP_14834),
	ROOFTOP_FIVE(new WorldArea(new WorldPoint(3217, 3392, 3), new WorldPoint(3233, 3404, 3)), GAP_14835),
	ROOFTOP_SIX(new WorldArea(new WorldPoint(3235, 3402, 3), new WorldPoint(3240, 3409, 3)), LEDGE_14836),
	ROOFTOP_SEVEN(new WorldArea(new WorldPoint(3235, 3410, 3), new WorldPoint(3240, 3416, 3)), EDGE),
	//FALADOR
	FAL_GROUND(new WorldArea(new WorldPoint(3008, 3328, 0), new WorldPoint(3071, 3391, 0)), ROUGH_WALL_14898),
	FAL_ROOFTOP_ONE(new WorldArea(new WorldPoint(3034, 3342, 3), new WorldPoint(3040, 3347, 3)),TIGHTROPE_14899),//THIS IS A GROUND OBJECT
	FAL_ROOFTOP_TWO(new WorldArea(new WorldPoint(3043, 3341, 3), new WorldPoint(3051, 3349, 3)),HAND_HOLDS_14901),
	FAL_ROOFTOP_THREE(new WorldArea(new WorldPoint(3047, 3356, 3), new WorldPoint(3051, 3359, 3)),GAP_14903),
	FAL_ROOFTOP_FOUR(new WorldArea(new WorldPoint(3044, 3360, 3), new WorldPoint(3049, 3367, 3)),GAP_14904),
	FAL_ROOFTOP_FIVE(new WorldArea(new WorldPoint(3033, 3360, 3), new WorldPoint(3042, 3364, 3)),TIGHTROPE_14905),
	FAL_ROOFTOP_SIX(new WorldArea(new WorldPoint(3025, 3352, 3), new WorldPoint(3029, 3355, 3)),TIGHTROPE_14911),//THIS IS A GROUND OBJECT
	FAL_ROOFTOP_SEVEN(new WorldArea(new WorldPoint(3008, 3352, 3), new WorldPoint(3021, 3358, 3)),GAP_14919),
	FAL_ROOFTOP_EIGHT(new WorldArea(new WorldPoint(3015, 3343, 3), new WorldPoint(3022, 3350, 3)),LEDGE_14920),
	FAL_ROOFTOP_NINE(new WorldArea(new WorldPoint(3010, 3343, 3), new WorldPoint(3015, 3347, 3)),LEDGE_14921),
	FAL_ROOFTOP_TEN(new WorldArea(new WorldPoint(3008, 3334, 3), new WorldPoint(3014, 3343, 3)),LEDGE_14922),
	FAL_ROOFTOP_ELEVEN(new WorldArea(new WorldPoint(3013, 3331, 3), new WorldPoint(3018, 3334, 3)),LEDGE_14924),
	FAL_ROOFTOP_TWELVE(new WorldArea(new WorldPoint(3019, 3331, 3), new WorldPoint(3027, 3335, 3)),EDGE_14925);


	/*ROUGH_WALL_14898, TIGHTROPE_14899, HAND_HOLDS_14901, GAP_14903, GAP_14904, TIGHTROPE_14905,
	TIGHTROPE_14911, GAP_14919, LEDGE_14920, LEDGE_14921, LEDGE_14922, LEDGE_14923, LEDGE_14924, EDGE_14925,*/

	//@Getter(AccessLevel.PACKAGE)
	private final WorldArea location;

	//@Getter(AccessLevel.PACKAGE)
	private final int obstacleId;

	RooftopAgilityObstacles(final WorldArea location, final int obstacleId)
	{
		this.location = location;
		this.obstacleId = obstacleId;
	}

	public WorldArea getLocation() { return location; }

	public int getObstacleId() { return obstacleId; }

	public static RooftopAgilityObstacles getObstacle(WorldPoint worldPoint)
	{
		for (RooftopAgilityObstacles obstacle : values())
		{
			if (obstacle.getLocation().distanceTo(worldPoint) == 0)
			{
				return obstacle;
			}
		}
		return null;
	}

}