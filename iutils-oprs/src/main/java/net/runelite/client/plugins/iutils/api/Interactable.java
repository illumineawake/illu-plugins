package net.runelite.client.plugins.iutils.api;

import java.util.List;

public interface Interactable {
    /**
     * A list of all available actions on the {@link Interactable}.
     */
    List<String> actions();

    /**
     * Performs a specified action on the {@link Interactable}.
     *
     * @throws IllegalArgumentException if there is no such action for this {@link Interactable}.
     * @see Interactable#actions()
     */
    void interact(String action);
}
