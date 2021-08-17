/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.iquestassistant;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iquestassistant.tasks.ChatOptionsAnyTask;
import net.runelite.client.plugins.iquestassistant.tasks.ChatOptionsQuestHelperTask;
import net.runelite.client.plugins.iquestassistant.tasks.ChatOptionsTask;
import net.runelite.client.plugins.iquestassistant.tasks.ContinueChatTask;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scripts.iScript;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iQuest Assistant",
        enabledByDefault = false,
        description = "Illumine - Quest Assistant plugin",
        tags = {"illumine", "quest", "assistant"}
)
@Slf4j
public class iQuestAssistant extends iScript {

    @Inject
    private Client client;

    @Inject
    private iQuestAssistantConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConfigManager configManager;

    private TaskSet tasks = new TaskSet();
    public static String status = "starting...";

    @Provides
    iQuestAssistantConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iQuestAssistantConfig.class);
    }

    @Override
    protected void startUp() {
        if (client.getLocalPlayer() != null) {
            log.info("Starting quest assistant from startUp");
            execute();
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.LOGGED_IN) {
            if (!this.started()) {
                log.info("Starting quest assistant from Game State");
                execute();
            }
        }
    }

    @Override
    protected void onStart() {
        log.info("starting Quest Assistant plugin");
        loadTasks();
    }

    @Override
    protected void onStop() {
        log.info("Stopping Quest Assistant plugin");
        tasks.clear();
    }

    @Override
    public void loop() {
        if (client != null && client.getLocalPlayer() != null && client.getGameState() == GameState.LOGGED_IN) {
            game.tick();
            var task = tasks.getValidTask();
            if (task != null) {
                status = task.getTaskDescription();
                task.run();
            } else {
                status = "Task not found";
            }
        }
    }

    private void loadTasks() {
        tasks.clear();
        tasks.addAll(
                injector.getInstance(ContinueChatTask.class),
                injector.getInstance(ChatOptionsQuestHelperTask.class),
                injector.getInstance(ChatOptionsTask.class),
                injector.getInstance(ChatOptionsAnyTask.class)
        );
    }
}