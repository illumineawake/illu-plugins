package net.runelite.client.plugins.iutils.game;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.client.plugins.iutils.scene.Position;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class iNPC extends iActor {
    private final NPC npc;
    private final NPCComposition definition;

    public iNPC(Game game, NPC npc, NPCComposition definition) {
        super(game, npc);
        this.npc = npc;
        this.definition = definition;
    }

    @Override
    public Client client() {
        return game.client();
    }

    public NPCComposition definition() {
        return definition;
    }

    /**
     * The index of the NPC, between {@code 0} and {@code 32768}. The
     * index is a number which uniquely identifies a single NPC in the
     * world.
     * <p>
     * It is not guaranteed to be the same across worlds or server
     * restarts.
     *
     * @see iNPC#id()
     */
    public int index() {
        return npc.getIndex();
    }

    /**
     * The ID of the NPC. This refers to the "transformed" ID, which may depend
     * on the value of a varp or varb.
     */
    public int id() {
        return npc.getId();
    }

    @Override
    public String name() {
        return npc.getName();
    }

    @Override
    public int combatLevel() {
        return npc.getCombatLevel();
    }

    @Override
    public Game game() {
        return game;
    }

    @Override
    public Position position() {
        return new Position(npc.getWorldLocation());
    }

    @Override
    public int animation() {
        return npc.getAnimation();
    }

    @Override
    public int spotAnimation() {
        return npc.getSpotAnimFrame();
    }

    @Override
    public int orientation() {
        return npc.getOrientation();
    }

    @Override
    public List<String> actions() {
        return Arrays.stream(definition().getActions())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void interact(String action) {
        String[] actions = definition().getActions();

        for (int i = 0; i < actions.length; i++) {
            if (action.equalsIgnoreCase(actions[i])) {
                interact(i);
                return;
            }
        }

        throw new IllegalArgumentException("action \"" + action + "\" not found on NPC " + id());
    }

    private int getActionId(int action) {
        switch (action) {
            case 0:
                return MenuAction.NPC_FIRST_OPTION.getId();
            case 1:
                return MenuAction.NPC_SECOND_OPTION.getId();
            case 2:
                return MenuAction.NPC_THIRD_OPTION.getId();
            case 3:
                return MenuAction.NPC_FOURTH_OPTION.getId();
            case 4:
                return MenuAction.NPC_FIFTH_OPTION.getId();
            default:
                throw new IllegalArgumentException("action = " + action);
        }
    }

    public void interact(int action) {
        System.out.println("NPC interact");
        game().interactionManager().interact(index(), getActionId(action), 0, 0);
        System.out.println("NPC finished interact");
    }

    public String toString() {
        return index() + ": " + name() + " (" + id() + ") at " + position();
    }
}
