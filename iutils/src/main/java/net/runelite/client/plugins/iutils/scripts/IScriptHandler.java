package net.runelite.client.plugins.iutils.scripts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.util.Util;

@Slf4j
public class IScriptHandler implements Runnable {
    private final iScript script;
    private static final int FAILURE_RESET = 75000;
    private static final int MAX_FAILURES = 10;

    public IScriptHandler(iScript script) {
        this.script = script;
    }

    public void run() {
        var failures = 0;
        var lastFailure = System.currentTimeMillis();

        script.onStart();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                script.loop();
            } catch (IllegalStateException | AssertionError | NullPointerException e) {

                if (System.currentTimeMillis() - lastFailure > FAILURE_RESET) {
                    failures = 0;
                }

                lastFailure = System.currentTimeMillis();

                if (failures <= MAX_FAILURES) {
                    failures++;
                    log.info("Caught failure #{}, restarting in 3 seconds", failures);
                    e.printStackTrace();
                    log.info("{} - {} - caused by: {}", e.getLocalizedMessage(), e.getMessage(), e.getCause());
                    Util.sleep(3000);
                } else {
                    log.info("Caught > 10 failures, stopping plugin");
                    script.stop();
                    return;
                }
            }
            catch (UnsupportedOperationException e) {
                log.info("Caught unsupported terminal failure, stopping instantly");
                script.game.sendGameMessage("Caught unsupported terminal failure, stopping instantly");
                e.printStackTrace();
                log.info("{} - caused by: {}", e.getMessage(), e.getCause());
                script.game.sendGameMessage(e.getMessage() + " - caused by: " + e.getCause());
                script.stop();
                return;
            }
        }
    }
}
