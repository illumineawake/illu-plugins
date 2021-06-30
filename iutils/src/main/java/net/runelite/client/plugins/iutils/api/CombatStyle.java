package net.runelite.client.plugins.iutils.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CombatStyle {
    ACCURATE(CombatType.MELEE),
    AGGRESSIVE(CombatType.MELEE),
    SPECIAL(CombatType.MELEE),
    DEFENSIVE(CombatType.MELEE),
    RAPID(CombatType.RANGED),
    MAGIC(CombatType.MAGIC);

    private final CombatType combatType;
}
