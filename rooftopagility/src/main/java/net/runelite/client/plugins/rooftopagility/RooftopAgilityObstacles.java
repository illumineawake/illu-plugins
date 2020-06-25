package net.runelite.client.plugins.rooftopagility;

import static net.runelite.api.ObjectID.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

public enum RooftopAgilityObstacles
{
	//TREE GNOME
	GNOME_LOG(new WorldArea(new WorldPoint(2470, 3435, 0), new WorldPoint(2489, 3447, 0)), LOG_BALANCE_23145), //GROUND_OBJECT
	GNOME_NET(new WorldArea(new WorldPoint(2470, 3423, 0), new WorldPoint(2477, 3430, 0)), OBSTACLE_NET_23134),
	GNOME_TREE(new WorldArea(new WorldPoint(2470, 3421, 1), new WorldPoint(2476, 3425, 1)), TREE_BRANCH_23559),
	GNOME_ROPE(new WorldArea(new WorldPoint(2469, 3416, 2), new WorldPoint(2479, 3423, 2)), BALANCING_ROPE_23557), //GROUND_OBJECT
	GNOME_TREE_TWO(new WorldArea(new WorldPoint(2482, 3416, 2), new WorldPoint(2489, 3423, 2)), TREE_BRANCH_23560),
	GNOME_NET_TWO(new WorldArea(new WorldPoint(2482, 3418, 0), new WorldPoint(2489, 3427, 0)), OBSTACLE_NET_23135),
	GNOME_PIPE(new WorldArea(new WorldPoint(2482, 3427, 0), new WorldPoint(2489, 3433, 0)),OBSTACLE_PIPE_23139),
	//DRAYNOR
	DRAY_WALL(new WorldArea(new WorldPoint(3082, 3254, 0), new WorldPoint(3105, 3293, 0)), ROUGH_WALL), //DECORATION
	DRAY_TIGHTROPE(new WorldArea(new WorldPoint(3096, 3275, 3), new WorldPoint(3103, 3282, 3)), TIGHTROPE), //GROUND OBJECT
	DRAY_TIGHTROPE_TWO(new WorldArea(new WorldPoint(3086, 3271, 3), new WorldPoint(3093, 3279, 3)), TIGHTROPE_11406), //GROUND OBJECT
	DRAY_NARROW_WALL(new WorldArea(new WorldPoint(3087, 3263, 3), new WorldPoint(3095, 3269, 3)), NARROW_WALL),
	DRAY_WALL_TWO(new WorldArea(new WorldPoint(3082, 3256, 3), new WorldPoint(3089, 3262, 3)), WALL_11630), //COULD CONFLICT WITH NEXT LINE
	DRAY_GAP(new WorldArea(new WorldPoint(3087, 3254, 3), new WorldPoint(3095, 3256, 3)), GAP_11631),
	DRAY_CRATE(new WorldArea(new WorldPoint(3095, 3255, 3), new WorldPoint(3102, 3262, 3)), CRATE_11632),
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
	FAL_ROOFTOP_TEN(new WorldArea(new WorldPoint(3008, 3335, 3), new WorldPoint(3014, 3343, 3)),LEDGE_14922),
	FAL_ROOFTOP_ELEVEN(new WorldArea(new WorldPoint(3013, 3331, 3), new WorldPoint(3018, 3335, 3)),LEDGE_14924),
	FAL_ROOFTOP_TWELVE(new WorldArea(new WorldPoint(3019, 3331, 3), new WorldPoint(3027, 3335, 3)),EDGE_14925),
	//SEERS
	SEERS_GROUND(new WorldArea(new WorldPoint(2689, 3457, 0), new WorldPoint(2750, 3517, 0)), WALL_14927), //DECORATION
	SEERS_ROOF_ONE(new WorldArea(new WorldPoint(2720, 3489, 3), new WorldPoint(2731, 3498, 3)), GAP_14928),
	SEERS_ROOF_TWO(new WorldArea(new WorldPoint(2702, 3486, 2), new WorldPoint(2714, 3499, 2)), TIGHTROPE_14932), //GROUND OBJECT
	SEERS_ROOF_THREE(new WorldArea(new WorldPoint(2707, 3475, 2), new WorldPoint(2717, 3483, 2)),GAP_14929),
	SEERS_ROOF_FOUR(new WorldArea(new WorldPoint(2697, 3468, 3), new WorldPoint(2718, 3478, 3)), GAP_14930),
	SEERS_ROOF_FIVE(new WorldArea(new WorldPoint(2689, 3458, 2), new WorldPoint(2704, 3467, 2)), EDGE_14931),
	//Rellekka
	RELL_GROUND(new WorldArea(new WorldPoint(2612, 3654, 0), new WorldPoint(2672, 3687, 0)), ROUGH_WALL_14946), //Decoration
	RELL_ROOF_ONE(new WorldArea(new WorldPoint(2621, 3671, 3), new WorldPoint(2627, 3677, 3)), GAP_14947),
	RELL_ROOF_TWO(new WorldArea(new WorldPoint(2614, 3657, 3), new WorldPoint(2623, 3669, 3)), TIGHTROPE_14987),
	RELL_ROOF_THREE(new WorldArea(new WorldPoint(2625, 3649, 3), new WorldPoint(2631, 3656, 3)), GAP_14990),
	RELL_ROOF_FOUR(new WorldArea(new WorldPoint(2638, 3648, 3), new WorldPoint(2645, 3654, 3)), GAP_14991),
	RELL_ROOF_FIVE(new WorldArea(new WorldPoint(2642, 3656, 3), new WorldPoint(2651, 3663, 3)), TIGHTROPE_14992),
	RELL_ROOF_SIX(new WorldArea(new WorldPoint(2654, 3663, 3), new WorldPoint(2667, 3686, 3)), PILE_OF_FISH);

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