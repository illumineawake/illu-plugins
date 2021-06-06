package net.runelite.client.plugins.iutils.scripts;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public abstract class iScript extends UtilsScript {
    private IScriptRunner scriptRunner;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> future;
    private volatile boolean started;

    public void start() {
        if (scriptRunner != null) {
            log.info("Script already running, stopping it first!");
            stop();
            return;
        }
        scriptRunner = new IScriptRunner(this);

        if (future == null || future.isCancelled() || future.isDone()) {
            future = executorService.submit(scriptRunner);
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
        scriptRunner = null;
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
