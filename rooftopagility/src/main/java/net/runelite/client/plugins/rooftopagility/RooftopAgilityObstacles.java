package net.runelite.client.plugins.rooftopagility;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.ObjectID;
import static net.runelite.api.ObjectID.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

public enum RooftopAgilityObstacles
{
	//TREE GNOME
	GNOME_LOG(new WorldPoint(2470, 3435, 0), new WorldPoint(2489, 3447, 0), LOG_BALANCE_23145, RooftopAgilityObstacleType.GROUND_OBJECT),
	GNOME_NET(new WorldPoint(2470, 3423, 0), new WorldPoint(2477, 3430, 0), OBSTACLE_NET_23134),
	GNOME_TREE(new WorldPoint(2470, 3421, 1), new WorldPoint(2476, 3425, 1), TREE_BRANCH_23559),
	GNOME_ROPE(new WorldPoint(2469, 3416, 2), new WorldPoint(2479, 3423, 2), BALANCING_ROPE_23557, RooftopAgilityObstacleType.GROUND_OBJECT),
	GNOME_TREE_TWO(new WorldPoint(2482, 3416, 2), new WorldPoint(2489, 3423, 2), TREE_BRANCH_23560),
	GNOME_NET_TWO(new WorldPoint(2482, 3418, 0), new WorldPoint(2489, 3427, 0), OBSTACLE_NET_23135),
	GNOME_PIPE(new WorldPoint(2482, 3427, 0), new WorldPoint(2489, 3433, 0), OBSTACLE_PIPE_23139),
	//DRAYNOR
	DRAY_WALL(new WorldPoint(3082, 3238, 0), new WorldPoint(3105, 3293, 0), ROUGH_WALL, RooftopAgilityObstacleType.DECORATION, BANK_BOOTH_10355),
	DRAY_TIGHTROPE(new WorldPoint(3096, 3275, 3), new WorldPoint(3103, 3282, 3), TIGHTROPE, RooftopAgilityObstacleType.GROUND_OBJECT),
	DRAY_TIGHTROPE_TWO(new WorldPoint(3086, 3271, 3), new WorldPoint(3093, 3279, 3), TIGHTROPE_11406, RooftopAgilityObstacleType.GROUND_OBJECT),
	DRAY_NARROW_WALL(new WorldPoint(3087, 3263, 3), new WorldPoint(3095, 3269, 3), NARROW_WALL),
	DRAY_WALL_TWO(new WorldPoint(3082, 3256, 3), new WorldPoint(3089, 3262, 3), WALL_11630), //COULD CONFLICT WITH NEXT LINE
	DRAY_GAP(new WorldPoint(3087, 3254, 3), new WorldPoint(3095, 3256, 3), GAP_11631),
	DRAY_CRATE(new WorldPoint(3095, 3255, 3), new WorldPoint(3102, 3262, 3), CRATE_11632),
	//ALKHARID
	ALK_ROUGHWALL(new WorldPoint(3268, 3159, 0), new WorldPoint(3322, 3200, 0), ROUGH_WALL_11633, RooftopAgilityObstacleType.DECORATION),
	ALK_TIGHTROPE(new WorldPoint(3270, 3179, 3), new WorldPoint(3278, 3193, 3), TIGHTROPE_14398, RooftopAgilityObstacleType.GROUND_OBJECT),
	ALK_CABLE(new WorldPoint(3263, 3160, 3), new WorldPoint(3274, 3174, 3), CABLE),
	ALK_ZIPLINE(new WorldPoint(3282, 3159, 3), new WorldPoint(3303, 3176, 3), ZIP_LINE_14403),
	ALK_TROPICAL_TREE(new WorldPoint(3312, 3159, 1), new WorldPoint(3319, 3166, 1), TROPICAL_TREE_14404),
	ALK_ROOF_TOP_BEAMS(new WorldPoint(3311, 3172, 2), new WorldPoint(3319, 3180, 2), ROOF_TOP_BEAMS, RooftopAgilityObstacleType.DECORATION),
	ALK_TIGHTROPE_TWO(new WorldPoint(3311, 3180, 3), new WorldPoint(3319, 3187, 3), TIGHTROPE_14409, RooftopAgilityObstacleType.GROUND_OBJECT),
	ALK_GAP(new WorldPoint(3296, 3184, 3), new WorldPoint(3306, 3194, 3), GAP_14399),
	//VARROCK
	COURSE_GROUND(new WorldPoint(3184, 3386, 0), new WorldPoint(3258, 3428, 0), ROUGH_WALL_14412, RooftopAgilityObstacleType.DECORATION, ObjectID.BANK_BOOTH_10583),
	ROOFTOP_ONE(new WorldPoint(3213, 3409, 3), new WorldPoint(3220, 3420, 3), CLOTHES_LINE),
	ROOFTOP_TWO(new WorldPoint(3200, 3412, 3), new WorldPoint(3209, 3420, 3), GAP_14414),
	CROSSWALK(new WorldPoint(3192, 3415, 1), new WorldPoint(3198, 3417, 1), WALL_14832),
	ROOFTOP_THREE(new WorldPoint(3191, 3401, 3), new WorldPoint(3198, 3407, 3), GAP_14833),
	ROOFTOP_FOUR(new WorldPoint(3181, 3393, 3), new WorldPoint(3209, 3401, 3), GAP_14834),
	ROOFTOP_FIVE(new WorldPoint(3217, 3392, 3), new WorldPoint(3233, 3404, 3), GAP_14835),
	ROOFTOP_SIX(new WorldPoint(3235, 3402, 3), new WorldPoint(3240, 3409, 3), LEDGE_14836),
	ROOFTOP_SEVEN(new WorldPoint(3235, 3410, 3), new WorldPoint(3240, 3416, 3), EDGE),
	//Canifis
	CAN_GROUND(new WorldPoint(3459, 3464, 0), new WorldPoint(3519, 3514, 0), TALL_TREE_14843, BANK_BOOTH_24347),
	CAN_ROOFTOP_ONE(new WorldPoint(3504, 3491, 2), new WorldPoint(3512, 3499, 2), GAP_14844),
	CAN_ROOFTOP_TWO(new WorldPoint(3495, 3503, 2), new WorldPoint(3505, 3508, 2), GAP_14845),
	CAN_ROOFTOP_THREE(new WorldPoint(3484, 3498, 2), new WorldPoint(3494, 3506, 2), GAP_14848),
	CAN_ROOFTOP_FOUR(new WorldPoint(3474, 3491, 3), new WorldPoint(3481, 3501, 3), GAP_14846),
	CAN_ROOFTOP_FIVE(new WorldPoint(3477, 3481, 2), new WorldPoint(3485, 3488, 2), POLEVAULT),
	CAN_ROOFTOP_SIX(new WorldPoint(3488, 3468, 3), new WorldPoint(3505, 3480, 3), GAP_14847),
	CAN_ROOFTOP_SEVEN(new WorldPoint(3508, 3474, 2), new WorldPoint(3517, 3484, 2), GAP_14897),
	//FALADOR
	FAL_GROUND(new WorldPoint(3008, 3328, 0), new WorldPoint(3071, 3391, 0), ROUGH_WALL_14898, RooftopAgilityObstacleType.DECORATION, ObjectID.BANK_BOOTH_24101),
	FAL_ROOFTOP_ONE(new WorldPoint(3034, 3342, 3), new WorldPoint(3040, 3347, 3), TIGHTROPE_14899, RooftopAgilityObstacleType.GROUND_OBJECT),
	FAL_ROOFTOP_TWO(new WorldPoint(3043, 3341, 3), new WorldPoint(3051, 3350, 3), HAND_HOLDS_14901),
	FAL_ROOFTOP_THREE(new WorldPoint(3047, 3356, 3), new WorldPoint(3051, 3359, 3), GAP_14903),
	FAL_ROOFTOP_FOUR(new WorldPoint(3044, 3360, 3), new WorldPoint(3049, 3367, 3), GAP_14904),
	FAL_ROOFTOP_FIVE(new WorldPoint(3033, 3360, 3), new WorldPoint(3042, 3364, 3), TIGHTROPE_14905),
	FAL_ROOFTOP_SIX(new WorldPoint(3025, 3352, 3), new WorldPoint(3029, 3355, 3), TIGHTROPE_14911, RooftopAgilityObstacleType.GROUND_OBJECT),
	FAL_ROOFTOP_SEVEN(new WorldPoint(3008, 3352, 3), new WorldPoint(3021, 3358, 3), GAP_14919),
	FAL_ROOFTOP_EIGHT(new WorldPoint(3015, 3343, 3), new WorldPoint(3022, 3350, 3), LEDGE_14920),
	FAL_ROOFTOP_NINE(new WorldPoint(3010, 3343, 3), new WorldPoint(3015, 3347, 3), LEDGE_14921),
	FAL_ROOFTOP_TEN(new WorldPoint(3008, 3335, 3), new WorldPoint(3014, 3343, 3), LEDGE_14922),
	FAL_ROOFTOP_ELEVEN(new WorldPoint(3013, 3331, 3), new WorldPoint(3018, 3335, 3), LEDGE_14924),
	FAL_ROOFTOP_TWELVE(new WorldPoint(3019, 3331, 3), new WorldPoint(3027, 3335, 3), EDGE_14925),
	//SEERS
	SEERS_GROUND(new WorldPoint(2689, 3457, 0), new WorldPoint(2750, 3517, 0), WALL_14927, RooftopAgilityObstacleType.DECORATION, BANK_BOOTH_25808),
	SEERS_ROOF_ONE(new WorldPoint(2720, 3489, 3), new WorldPoint(2731, 3498, 3), GAP_14928),
	SEERS_ROOF_TWO(new WorldPoint(2702, 3486, 2), new WorldPoint(2714, 3499, 2), TIGHTROPE_14932, RooftopAgilityObstacleType.GROUND_OBJECT),
	SEERS_ROOF_THREE(new WorldPoint(2707, 3475, 2), new WorldPoint(2717, 3483, 2), GAP_14929),
	SEERS_ROOF_FOUR(new WorldPoint(2697, 3468, 3), new WorldPoint(2718, 3478, 3), GAP_14930),
	SEERS_ROOF_FIVE(new WorldPoint(2689, 3458, 2), new WorldPoint(2704, 3467, 2), EDGE_14931),
	//Pollniveach
	POLL_GROUND(new WorldPoint(3328, 2944, 0), new WorldPoint(3392, 3008, 0), BASKET_14935),
	POLL_ROOF_ONE(new WorldPoint(3346, 2963, 1), new WorldPoint(3352, 2969, 1), MARKET_STALL_14936),
	POLL_ROOF_TWO(new WorldPoint(3352, 2973, 1), new WorldPoint(3356, 2977, 1), BANNER_14937),
	POLL_ROOF_THREE(new WorldPoint(3360, 2977, 1), new WorldPoint(3363, 2980, 1), GAP_14938),
	POLL_ROOF_FOUR(new WorldPoint(3366, 2976, 1), new WorldPoint(3372, 2975, 1), TREE_14939),
	POLL_ROOF_FIVE(new WorldPoint(3365, 2982, 1), new WorldPoint(3370, 2987, 1), ROUGH_WALL_14940, RooftopAgilityObstacleType.DECORATION),
	POLL_ROOF_SIX(new WorldPoint(3355, 2980, 2), new WorldPoint(3366, 2986, 2), MONKEYBARS),
	POLL_ROOF_SEVEN(new WorldPoint(3357, 2991, 2), new WorldPoint(3367, 2996, 2), TREE_14944),
	POLL_ROOF_EIGHT(new WorldPoint(3356, 3000, 2), new WorldPoint(3363, 3005, 2), DRYING_LINE),
	//Prifddinas
	PRIF_LADDER(new WorldPoint(3237, 6099, 0), new WorldPoint(3275, 6114, 0), LADDER_36221, ObjectID.BANK_BOOTH_10355),
	PRIF_TIGHTROPE(new WorldPoint(3254, 6102, 2), new WorldPoint(3259, 6112, 2), TIGHTROPE_36225),//TIGHTROPE_36255
	PRIF_CHIMNEY(new WorldPoint(3271, 6104, 2), new WorldPoint(3276, 6107, 2), CHIMNEY_36227),
	PRIF_ROOFEDGE(new WorldPoint(3268, 6111, 2), new WorldPoint(3270, 6116, 2), ROOF_EDGE),
	PRIF_DARK_HOLE(new WorldPoint(3267, 6115, 0), new WorldPoint(3271, 6119, 0), DARK_HOLE_36229),
	PRIF_LADDER_TWO(new WorldPoint (2239, 3386, 0), new WorldPoint(2271, 3410, 0), LADDER_36231),
	PRIF_LADDER_FAIL(new WorldPoint (3265, 6138, 0), new WorldPoint (3276, 6150, 0), LADDER_36232),
	PRIF_ROPE_BRIDGE(new WorldPoint(2264, 3388, 2), new WorldPoint(2270, 3394, 2), ROPE_BRIDGE_36233),
	PRIF_TIGHTROPE_TWO(new WorldPoint(2252, 3386, 2), new WorldPoint(2259, 3391, 2), TIGHTROPE_36234),
	PRIF_ROPE_BRIDGE_TWO(new WorldPoint(2242, 3393, 2), new WorldPoint(2248, 3399, 2), ROPE_BRIDGE_36235),
	PRIF_TIGHTROPE_THREE(new WorldPoint(2243, 3404, 2), new WorldPoint(2249, 3411, 2), TIGHTROPE_36236),
	PRIF_TIGHTROPE_FOUR(new WorldPoint(2248, 3414, 2), new WorldPoint(2254, 3420, 2), TIGHTROPE_36237),
	PRIF_DARKHOLE_TWO(new WorldPoint(2255, 3424, 0), new WorldPoint(2263, 3436, 0), DARK_HOLE_36238),
	//Rellekka
	RELL_GROUND(new WorldPoint(2612, 3654, 0), new WorldPoint(2672, 3687, 0), ROUGH_WALL_14946, RooftopAgilityObstacleType.DECORATION),
	RELL_ROOF_ONE(new WorldPoint(2621, 3671, 3), new WorldPoint(2627, 3677, 3), GAP_14947),
	RELL_ROOF_TWO(new WorldPoint(2614, 3657, 3), new WorldPoint(2623, 3669, 3), TIGHTROPE_14987),
	RELL_ROOF_THREE(new WorldPoint(2625, 3649, 3), new WorldPoint(2631, 3656, 3), GAP_14990),
	RELL_ROOF_FOUR(new WorldPoint(2638, 3648, 3), new WorldPoint(2645, 3654, 3), GAP_14991),
	RELL_ROOF_FIVE(new WorldPoint(2642, 3656, 3), new WorldPoint(2651, 3663, 3), TIGHTROPE_14992),
	RELL_ROOF_SIX(new WorldPoint(2654, 3663, 3), new WorldPoint(2667, 3686, 3), PILE_OF_FISH),
	//Ardougne
	ARDY_GROUND(new WorldPoint(2640, 3274, 0), new WorldPoint(2678, 3321, 0), WOODEN_BEAMS, RooftopAgilityObstacleType.DECORATION, ObjectID.BANK_BOOTH_10355),
	ARDY_GAP(new WorldPoint(2670, 3298, 3), new WorldPoint(2675, 3312, 3), GAP_15609),
	ARDY_BEAM(new WorldPoint(2660, 3317, 3), new WorldPoint(2666, 3323, 3), PLANK_26635, RooftopAgilityObstacleType.GROUND_OBJECT),
	ARDY_GAP_TWO(new WorldPoint(2652, 3317, 3), new WorldPoint(2658, 3322, 3), GAP_15610),
	ARDY_GAP_THREE(new WorldPoint(2647, 3310, 3), new WorldPoint(2654, 3315, 3), GAP_15611),
	ARDY_STEEP_ROOF(new WorldPoint(2650, 3299, 3), new WorldPoint(2656, 3310, 3), STEEP_ROOF),
	ARDY_GAP_FOUR(new WorldPoint(2653, 3290, 3), new WorldPoint(2658, 3298, 3), GAP_15612);

	@Getter(AccessLevel.PACKAGE)
	private final WorldArea location;

	@Getter(AccessLevel.PACKAGE)
	private final int obstacleId;

	@Getter(AccessLevel.PACKAGE)
	private RooftopAgilityObstacleType type = RooftopAgilityObstacleType.NORMAL;

	@Getter(AccessLevel.PACKAGE)
	private int bankID = 0;

	RooftopAgilityObstacles(final WorldPoint min, final WorldPoint max, final int obstacleId)
	{
		this.location = new WorldArea(min, max);
		this.obstacleId = obstacleId;
	}

	RooftopAgilityObstacles(final WorldPoint min, final WorldPoint max, final int obstacleId, final int bankID)
	{
		this.location = new WorldArea(min, max);
		this.obstacleId = obstacleId;
		this.bankID = bankID;
	}

	RooftopAgilityObstacles(final WorldPoint min, final WorldPoint max, final int obstacleId, final RooftopAgilityObstacleType type)
	{
		this.location = new WorldArea(min, max);
		this.obstacleId = obstacleId;
		this.type = type;
	}

	RooftopAgilityObstacles(final WorldPoint min, final WorldPoint max, final int obstacleId, final RooftopAgilityObstacleType type, final int bankID)
	{
		this.location = new WorldArea(min, max);
		this.obstacleId = obstacleId;
		this.type = type;
		this.bankID = bankID;
	}

	public RooftopAgilityObstacleType getObstacleType()
	{
		return type;
	}

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
