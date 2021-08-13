package net.runelite.client.plugins.iutils.game;

import net.runelite.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class iPlayer extends iActor {
    private final Player player;
    private final PlayerComposition definition;

    public iPlayer(Game game, Player player, PlayerComposition definition) {
        super(game, player);
        this.player = player;
        this.definition = definition;
    }

    @Override
    public Client client() {
        return game.client();
    }

    public PlayerComposition definition() {
        return definition;
    }

    /**
     * The index of the player, between {@code 0} and {@code 2048}. This
     * is also known as the player ID, but called {@code index} here
     * to avoid confusion between NPC indices and NPC ids.
     */
    public int index() {
        return player.getPlayerId();
    }

    @Override
    public String name() {
        return player.getName();
    }

    @Override
    public int combatLevel() {
        return player.getCombatLevel();
    }

    public SkullIcon skull() {
        return player.getSkullIcon();
    }

    public HeadIcon overheadPrayer() {
        return player.getOverheadIcon();
    }

//    public int npcTransform() {
//        return npcTransform;
//    } //TODO

    /**
     * Whether the player is a female.
     *
     * @see iPlayer#equipment()
     * @see iPlayer#bodyColors()
     */
    public boolean female() {
        return definition().isFemale();
    }

    /**
     * The current equipment of the player. These are either
     * under {@code 512}, in which case they refer to a player
     * model, or over {@code 512}, in which case subtracting
     * {@code 512} gives an item ID.
     *
     * @see iPlayer#female()
     * @see iPlayer#bodyColors()
     */
    public int[] equipment() {
        return definition().getEquipmentIds();
    }

    /**
     * The colors of the skin and clothes of the player.
     *
     * @see iPlayer#female()
     * @see iPlayer#equipment()
     */
    public int[] bodyColors() { //TODO
        return player.getModel().getFaceColors1();
    }

    public int walkAnimation() {
        return player.getWalkAnimation();
    }

    public int healthRatio() {
        return player.getHealthRatio();
    }

    public int healthScale() {
        return player.getHealthScale();
    }

    public int idlePoseAnimation() {
        return player.getIdlePoseAnimation();
    }

    public int poseAnimation() {
        return player.getPoseAnimation();
    }

    public boolean isMoving() {
        return game.localPlayer().idlePoseAnimation() != game.localPlayer().poseAnimation();
    }

    public boolean isIdle() {
        return !isMoving() && player.getAnimation() == -1;
    }

    public boolean isFriend() {
        return player.isFriend();
    }

    public boolean isFriendChatMember() {
        return player.isFriendsChatMember();
    }

    @Override
    public List<String> actions() {
        return Arrays.stream(client().getPlayerOptions())
                .collect(Collectors.toList());
    }

    @Override
    public void interact(String action) {
        String[] actions = client().getPlayerOptions();

        for (int i = 0; i < actions.length; i++) {
            if (action.equalsIgnoreCase(actions[i])) {
                interact(i);
                return;
            }
        }

        throw new IllegalArgumentException("action \"" + action + "\" not found on NPC " + index());
    }

    private int getActionId(int action) {
        switch (action) {
            case 0:
                return MenuAction.PLAYER_FIRST_OPTION.getId();
            case 1:
                return MenuAction.PLAYER_SECOND_OPTION.getId();
            case 2:
                return MenuAction.PLAYER_THIRD_OPTION.getId();
            case 3:
                return MenuAction.PLAYER_FOURTH_OPTION.getId();
            case 4:
                return MenuAction.PLAYER_FIFTH_OPTION.getId();
            case 5:
                return MenuAction.PLAYER_SIXTH_OPTION.getId();
            case 6:
                return MenuAction.PLAYER_SEVENTH_OPTION.getId();
            case 7:
                return MenuAction.PLAYER_EIGTH_OPTION.getId();
            default:
                throw new IllegalArgumentException("action = " + action);
        }
    }

    public void interact(int action) {
        game.interactionManager().interact(index(), getActionId(action), 0, 0);
    }

    public String toString() {
        return name() + " (level " + combatLevel() + ")";
    }
}
