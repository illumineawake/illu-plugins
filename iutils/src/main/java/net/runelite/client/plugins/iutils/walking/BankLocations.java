package net.runelite.client.plugins.iutils.walking;

import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.scene.Area;
import net.runelite.client.plugins.iutils.scene.RectangularArea;

import java.util.ArrayList;

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
    public static final Area HALLOWED_SEPULCHRE_BANK = new RectangularArea(2383, 5997, 2420, 5963);
    public static final Area CANIFIS_BANK = new RectangularArea(3509, 3483, 3516, 3478);
    public static final Area BURGH_DE_ROTT_BANK = new RectangularArea(3492, 3213, 3496, 3210);
    public static final Area VER_SINHAZA_BANK = new RectangularArea(3649, 3208, 3652, 3209);

    public static void walkToBank(Game game) {
        var validBanks = new ArrayList<Area>();
        validBanks.add(LUMBRIDGE_BANK);
        validBanks.add(VARROCK_WEST_BANK);
        validBanks.add(VARROCK_EAST_BANK);
        validBanks.add(GRAND_EXCHANGE_BANK);
        validBanks.add(EDGEVILLE_BANK);
        validBanks.add(FALADOR_EAST_BANK);
        validBanks.add(FALADOR_WEST_BANK);
        validBanks.add(DRAYNOR_BANK);
        validBanks.add(DUEL_ARENA_BANK);
        validBanks.add(SHANTAY_PASS_BANK);
        validBanks.add(AL_KHARID_BANK);
        validBanks.add(CATHERBY_BANK);
        validBanks.add(SEERS_VILLAGE_BANK);
        validBanks.add(ARDOUGNE_NORTH_BANK);
        validBanks.add(ARDOUGNE_SOUTH_BANK);
        validBanks.add(PORT_KHAZARD_BANK);
        validBanks.add(YANILLE_BANK);
        validBanks.add(CORSAIR_COVE_BANK);
        validBanks.add(CASTLE_WARS_BANK);
        validBanks.add(LLETYA_BANK);
        validBanks.add(GRAND_TREE_WEST_BANK);
        validBanks.add(GRAND_TREE_SOUTH_BANK);
        validBanks.add(TREE_GNOME_STRONGHOLD_BANK);
        validBanks.add(SHILO_VILLAGE_BANK);
        validBanks.add(NEITIZNOT_BANK);
        validBanks.add(JATIZSO_BANK);
        validBanks.add(BARBARIAN_OUTPOST_BANK);
        validBanks.add(ETCETARIA_BANK);
        validBanks.add(DARKMEYER_BANK);
        validBanks.add(CHARCOAL_BURNERS_BANK);
        validBanks.add(HOSIDIUS_BANK);
        validBanks.add(HALLOWED_SEPULCHRE_BANK);
        validBanks.add(VER_SINHAZA_BANK);

        if (game.varp(302) >= 61) {
            validBanks.add(CANIFIS_BANK);
        }

        if (game.varb(1990) >= 200) {
            validBanks.add(BURGH_DE_ROTT_BANK);
        }

        new Walking(game).walkTo(Area.union(validBanks.toArray(new Area[0])));
    }
}
