package net.runelite.client.plugins.iutils.game;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InteractionManager {

    private final Game game;

    public InteractionManager(Game game) {
        this.game = game;
    }

    public void submit(Runnable runnable) {
//        log.info("interacting");
        game.sleepDelay();
        runnable.run();
    }

    public void interact(int identifier, int opcode, int param0, int param1) {
        log.info("interacting");
        game.sleepDelay();
        game.clientThread.invoke(() -> game.client().invokeMenuAction("", "", identifier, opcode, param0, param1));
    }

}
