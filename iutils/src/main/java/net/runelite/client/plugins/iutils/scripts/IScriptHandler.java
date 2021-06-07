package net.runelite.client.plugins.iutils.scripts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.util.Util;

@Slf4j
public class IScriptHandler implements Runnable {
    private final iScript script;

    public IScriptHandler(iScript script) {
        this.script = script;
    }

    public void run() {
        script.onStart();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                script.loop();
            } catch (IllegalStateException | AssertionError | NullPointerException e) {
                log.info("Caught error, restarting in 3 seconds");
                e.printStackTrace();
                Util.sleep(3000);
            }
        }
    }
}
