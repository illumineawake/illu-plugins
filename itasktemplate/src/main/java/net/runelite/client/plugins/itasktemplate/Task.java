package net.runelite.client.plugins.itasktemplate;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.scripts.UtilsScript;

@Slf4j
public abstract class Task extends UtilsScript {
    public Task() {
    }

    public abstract boolean validate();

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }

    public abstract void run();

}
