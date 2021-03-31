package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.scene.Area;
import net.runelite.client.plugins.iutils.scene.RectangularArea;

public class BankLocations {
    public static final Area LUMBRIDGE_BANK = new RectangularArea(3207, 3222, 3210, 3215, 2);
    public static final Area VARROCK_WEST_BANK = new RectangularArea(3180, 3447, 3190, 3433, 0);
    public static final Area VARROCK_EAST_BANK = new RectangularArea(3250, 3424, 3257, 3416, 0);
    public static final Area GRAND_EXCHANGE_BANK = new RectangularArea(3160, 3495, 3170, 3484, 0);
    public static final Area EDGEVILLE_BANK = new RectangularArea(3091, 3499, 3098, 3488, 0);
    public static final Area FALADOR_EAST_BANK = Area.union(new RectangularArea(3009, 3358, 3018, 3353, 0), new RectangularArea(3019, 3356, 3021, 3353, 0));
    public static final Area FALADOR_WEST_BANK = Area.union(new RectangularArea(2943, 3373, 2947, 3368, 0), new RectangularArea(2949, 3369, 2945, 3366, 0));
    public static final Area DRAYNOR_BANK = new RectangularArea(3088, 3246, 3097, 3240, 0);
    public static final Area DUEL_ARENA_BANK = new RectangularArea(3380, 3273, 3384, 3267, 0);
    public static final Area SHANTAY_PASS_BANK = new RectangularArea(3304, 3125, 3312, 3117, 0);
    public static final Area AL_KHARID_BANK = new RectangularArea(3265, 3173, 3272, 3161, 0);
    public static final Area CATHERBY_BANK = new RectangularArea(2806, 3445, 2812, 3438, 0);
    public static final Area SEERS_VILLAGE_BANK = new RectangularArea(2721, 3493, 2730, 3490, 0);
    public static final Area ARDOUGNE_NORTH_BANK = new RectangularArea(2612, 3335, 2621, 3330, 0);
    public static final Area ARDOUGNE_SOUTH_BANK = new RectangularArea(2649, 3287, 2658, 3280, 0);
    public static final Area PORT_KHAZARD_BANK = new RectangularArea(2659, 3164, 2665, 3158);
    public static final Area YANILLE_BANK = new RectangularArea(2609, 3097, 2616, 3088, 0);
    public static final Area CORSAIR_COVE_BANK = new RectangularArea(2568, 2867, 2572, 2863, 0);
    public static final Area CASTLE_WARS_BANK = new RectangularArea(2442, 3084, 2445, 3082, 0);
    public static final Area LLETYA_BANK = new RectangularArea(2350, 3166, 2354, 3161, 0);
    public static final Area GRAND_TREE_WEST_BANK = new RectangularArea(2438, 3489, 2442, 3487, 1);
    public static final Area GRAND_TREE_SOUTH_BANK = new RectangularArea(2448, 3482, 2450, 3478, 1);
    public static final Area TREE_GNOME_STRONGHOLD_BANK = new RectangularArea(2443, 3427, 2448, 3422, 1);
    public static final Area SHILO_VILLAGE_BANK = new RectangularArea(2851, 2957, 2853, 2951, 0);
    public static final Area NEITIZNOT_BANK = new RectangularArea(2334, 3808, 2339, 3805, 0);
    public static final Area JATIZSO_BANK = new RectangularArea(2415, 3803, 2418, 3799, 0);
    public static final Area BARBARIAN_OUTPOST_BANK = new RectangularArea(2533, 3576, 2537, 3572, 0);
    public static final Area ETCETARIA_BANK = new RectangularArea(2618, 3896, 2621, 3893, 0);
    public static final Area DARKMEYER_BANK = new RectangularArea(3601, 3370, 3609, 3365, 0);
    public static final Area CHARCOAL_BURNERS_BANK = new RectangularArea(1711, 3469, 1723, 3460, 0);
    public static final Area HOSIDIUS_BANK = new RectangularArea(1749, 3594, 1745, 3603, 0);
    public static final Area PORT_PISCARILIUS_BANK = new RectangularArea(1794, 3793, 1811, 3784);

    public static final Area ANY = Area.union(
            LUMBRIDGE_BANK,
            VARROCK_WEST_BANK,
            VARROCK_EAST_BANK,
            GRAND_EXCHANGE_BANK,
            EDGEVILLE_BANK,
            FALADOR_EAST_BANK,
            FALADOR_WEST_BANK,
            DRAYNOR_BANK,
            DUEL_ARENA_BANK,
            SHANTAY_PASS_BANK,
            AL_KHARID_BANK,
            CATHERBY_BANK,
            SEERS_VILLAGE_BANK,
            ARDOUGNE_NORTH_BANK,
            ARDOUGNE_SOUTH_BANK,
            PORT_KHAZARD_BANK,
            YANILLE_BANK,
            CORSAIR_COVE_BANK,
            CASTLE_WARS_BANK,
            LLETYA_BANK,
            GRAND_TREE_WEST_BANK,
            GRAND_TREE_SOUTH_BANK,
            TREE_GNOME_STRONGHOLD_BANK,
            SHILO_VILLAGE_BANK,
            NEITIZNOT_BANK,
            JATIZSO_BANK,
            BARBARIAN_OUTPOST_BANK,
            ETCETARIA_BANK,
            DARKMEYER_BANK,
            CHARCOAL_BURNERS_BANK,
            HOSIDIUS_BANK
    );
}
