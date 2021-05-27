package net.runelite.client.plugins.iutils.game;

import net.runelite.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    public PlayerComposition definition() { return definition; }

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
     * @see iPlayer#bodyColors()*/
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

    public boolean isIdle() { return game.localPlayer().idlePoseAnimation() == game.localPlayer().poseAnimation() && player.getAnimation() == -1;}

    public boolean isFriend() {
        return player.isFriend();
    }

    public boolean isFriendChatMember() {
        return player.isFriendsChatMember();
    }

//    public boolean hidden() {
//        return hidden;
//    }

//    @Override
//    public List<HitSplat> hitSplats() {
//        return hitsplats;
//    }

//    @Override
//    public HealthBar healthBar() {
//        return healthbar;
//    }

    @Override
    public List<String> actions() {
        return Arrays.stream(player.getActions())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void interact(String action) {
        String[] actions = player.getActions();

        for (int i = 0; i < actions.length; i++) {
            if (action.equalsIgnoreCase(actions[i])) {
                interact(i);
                return;
            }
        }

        throw new IllegalArgumentException("action \"" + action + "\" not found on NPC " + index());
    }

    public void interact(int action) {
        game().clientThread.invoke(() -> {
            int menuAction;

            switch (action) {
                case 0:
                    menuAction = MenuAction.PLAYER_FIRST_OPTION.getId();
                    break;
                case 1:
                    menuAction = MenuAction.PLAYER_SECOND_OPTION.getId();
                    break;
                case 2:
                    menuAction = MenuAction.PLAYER_THIRD_OPTION.getId();
                    break;
                case 3:
                    menuAction = MenuAction.PLAYER_FOURTH_OPTION.getId();
                    break;
                case 4:
                    menuAction = MenuAction.PLAYER_FIFTH_OPTION.getId();
                    break;
                case 5:
                    menuAction = MenuAction.PLAYER_SIXTH_OPTION.getId();
                    break;
                case 6:
                    menuAction = MenuAction.PLAYER_SEVENTH_OPTION.getId();
                    break;
                case 7:
                    menuAction = MenuAction.PLAYER_EIGTH_OPTION.getId();
                    break;
                default:
                    throw new IllegalArgumentException("action = " + action);
            }
            client().invokeMenuAction("", "", index(), menuAction, 0, 0);
        });
    }

    public String toString() {
        return name() + " (level " + combatLevel() + ")";
    }
}
