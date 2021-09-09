package net.runelite.client.plugins.iutils.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Skill;

@Getter
@AllArgsConstructor
public enum CombatStyle {
    ACCURATE(CombatType.MELEE, Skill.ATTACK),
    AGGRESSIVE(CombatType.MELEE, Skill.STRENGTH),
    SPECIAL(CombatType.MELEE, Skill.STRENGTH),
    DEFENSIVE(CombatType.MELEE, Skill.DEFENCE),
    RAPID(CombatType.RANGED, Skill.RANGED),
    MAGIC(CombatType.MAGIC, Skill.MAGIC);

    private final CombatType combatType;
    private final Skill skill;
}
