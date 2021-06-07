package net.runelite.client.plugins.iutils.ui;

public enum Prayer {
    THICK_SKIN(4104, 5),
    BURST_OF_STRENGTH(4105, 6),
    CLARITY_OF_THOUGHT(4106, 7),
    SHARP_EYE(4122, 23),
    MYSTIC_WILL(4123, 24),
    ROCK_SKIN(4107, 8),
    SUPERHUMAN_STRENGTH(4108, 9),
    IMPROVED_REFLEXES(4109, 10),
    RAPID_RESTORE(4110, 11),
    RAPID_HEAL(4111, 12),
    PROTECT_ITEM(4112, 13),
    HAWK_EYE(4124, 25),
    MYSTIC_LORE(4125, 26),
    STEEL_SKIN(4113, 14),
    ULTIMATE_STRENGTH(4114, 15),
    INCREDIBLE_REFLEXES(4115, 16),
    PROTECT_FROM_MAGIC(4116, 17),
    PROTECT_FROM_MISSILES(4117, 18),
    PROTECT_FROM_MELEE(4118, 19),
    EAGLE_EYE(4126, 27),
    MYSTIC_MIGHT(4127, 28),
    RETRIBUTION(4119, 20),
    REDEMPTION(4120, 21),
    SMITE(4121, 22),
    CHIVALRY(4128, 29),
    PIETY(4129, 30),
    PRESERVE(5466, 33),
    RIGOUR(5464, 31),
    AUGURY(5465, 32);

    public final int varb;
    public final int widget;

    Prayer(int varb, int widget) {
        this.varb = varb;
        this.widget = widget;
    }
}
