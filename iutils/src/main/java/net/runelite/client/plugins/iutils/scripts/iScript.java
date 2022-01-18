package net.runelite.client.plugins.iutils.scripts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;

@Slf4j
public abstract class iScript extends UtilsScript {
    private IScriptHandler scriptHandler;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> future;
    private volatile boolean started;

    /**
     * Start script handler, will stop any script handlers already running first
     */
    public void start() {
        if (scriptHandler != null) {
            log.info("Script already running, stopping it first!");
            stop();
        }
        scriptHandler = new IScriptHandler(this);

        if (future == null || future.isCancelled() || future.isDone()) {
            future = executorService.submit(scriptHandler);
            started = true;
        } else {
            stop();
        }
    }

    /**
     * Starts or stops script handler depending on handler state
     */
    public void execute() {
        if (scriptHandler != null) {
            log.info("Script already running, stopping it first!");
            stop();
            return;
        }
        scriptHandler = new IScriptHandler(this);

        if (future == null || future.isCancelled() || future.isDone()) {
            future = executorService.submit(scriptHandler);
            started = true;
        } else {
            stop();
        }
    }

    public boolean started() {
        return started;
    }

    public void stop() {
        if (future != null) {
            onStop();
            future.cancel(true);
        } else {
            log.info("Couldn't find future to stop");
        }
        scriptHandler = null;
        started = false;
    }

    @Override
    protected void shutDown() {
        log.info("Shutting down");
        stop();
    }

    private void checkIdleLogout() {
        // Check clientside AFK first, because this is required for the server to disconnect you for being afk
        int idleClientTicks = game.client.getKeyboardIdleTicks();

        if (game.client.getMouseIdleTicks() < idleClientTicks) {
            idleClientTicks = game.client.getMouseIdleTicks();
        }

        if (idleClientTicks > 12500) {
            Random r = new Random();
            log.info("iScript Resetting idle");

            if (r.nextBoolean()) {
                game.pressKey(VK_LEFT);
            } else {
                game.pressKey(VK_RIGHT);
            }

            game.client.setKeyboardIdleTicks(0);
            game.client.setMouseIdleTicks(0);
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!started()) {
            return;
        }
        checkIdleLogout();
    }

    protected abstract void loop();

    protected abstract void onStart();

    protected abstract void onStop();
}
