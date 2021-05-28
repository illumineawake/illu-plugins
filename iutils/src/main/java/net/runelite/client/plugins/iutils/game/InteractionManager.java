package net.runelite.client.plugins.iutils.game;

import net.runelite.api.widgets.WidgetInfo;

public class InteractionManager {

    private final Game game;

    public InteractionManager(Game game) {
        this.game = game;
    }

    public void submit(Runnable runnable) {
        game.sleepDelay();
        runnable.run();
    }

    public void interact(int identifier, int opcode, int param0, int param1) {
        game.sleepDelay();
        game.clientThread.invoke(() -> game.client().invokeMenuAction("","", identifier, opcode, param0, param1));
    }

}
