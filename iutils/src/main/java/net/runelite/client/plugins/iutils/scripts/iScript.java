package net.runelite.client.plugins.iutils.scripts;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        stop();
    }

    protected abstract void loop();

    protected abstract void onStart();

    protected abstract void onStop();
}
