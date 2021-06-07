package net.runelite.client.plugins.iutils.game;

import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.client.plugins.iutils.api.Interactable;
import net.runelite.client.plugins.iutils.scene.Locatable;
import net.runelite.client.plugins.iutils.scene.Position;

public abstract class iActor implements Locatable, Interactable {
    protected final Actor actor;
    protected final Game game;

    public iActor(Game game, Actor actor) {
        this.game = game;
        this.actor = actor;
    }

    public Game game() {
        return game;
    }

    @Override
    public Position position() {
        return new Position(actor.getWorldLocation());
    }

    /**
     * The {@link iActor} this actor is currently targetting, or {@code null} if none. An
     * NPC may target another {@link iActor} if they are attacking them, talking to them,
     * following them, etc.
     */
    public iActor target() {
        Actor interacting = actor.getInteracting();
        if (interacting == null) {
            return null;
        }
        if (interacting instanceof NPC)
            return game.getFromClientThread(() -> new iNPC(game(), (NPC) interacting, client().getNpcDefinition(((NPC) interacting).getId())));
        else if (interacting instanceof Player) {
            Player player = (Player) interacting;
            return game.getFromClientThread(() -> new iPlayer(game(), player, player.getPlayerComposition()));
        } else
            throw new AssertionError("not possible, Actor is either an Npc or Player");
    }

    public int orientation() {
        return actor.getOrientation();
    }

    /**
     * Gets the current animation the actor is performing.
     *
     * @return the animation ID
     * @see AnimationID
     */
    public int animation() {
        return actor.getAnimation();
    }

    /**
     * The current spot animation of the actor. Spot animations are animations that
     * temporarily replace the entire model of the actor (for example, the teleport
     * tablet animation for players)
     */
    public int spotAnimation() {
        return actor.getSpotAnimFrame();
    }

    /**
     * Gets the health of the actor in {@link #getHealthScale()} units.
     * <p>
     * The server does not transmit actors' real health, only this value
     * between zero and {@link #getHealthScale()}. Some actors may be
     * missing this info, in which case -1 is returned.
     */
    public int getHealthRatio() {
        return actor.getHealthRatio();
    }

    /**
     * Gets the maximum value {@link #getHealthRatio()} can return
     * <p>
     * For actors with the default size health bar this is 30, but
     * for bosses with a larger health bar this can be a larger number.
     * Some actors may be missing this info, in which case -1 is returned.
     */
    public int getHealthScale() {
        return actor.getHealthScale();
    }

    public boolean isMoving() {
        return actor.isMoving();
    }

    /**
     * Returns true if this Actor has died
     *
     * @return
     */
    public boolean isDead() {
        return actor.isDead();
    }

    /**
     * The name of the actor, or {@code null} if it has none.
     */
    public abstract String name();

    /**
     * The combat level of the Actor, or {@code 0} if it has none.
     */
    public abstract int combatLevel();
}
