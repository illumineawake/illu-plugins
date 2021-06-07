package net.runelite.client.plugins.iquestassistant;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.scene.Area;
import net.runelite.client.plugins.iutils.scripts.UtilsScript;

import javax.inject.Inject;

@Slf4j
public abstract class Task extends UtilsScript {
    public Task() {
    }

    @Inject
    protected iQuestAssistantConfig config;

    public abstract boolean validate();

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }

    public abstract void run();

}
