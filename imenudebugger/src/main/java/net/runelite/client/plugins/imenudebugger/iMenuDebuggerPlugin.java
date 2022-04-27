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
package net.runelite.client.plugins.imenudebugger;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.util.Text;
import org.pf4j.Extension;

import javax.inject.Inject;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iMenu Debugger Plugin",
        enabledByDefault = false,
        description = "Illumine - Menu Debugger plugin. Has no function other than debugging",
        tags = {"illumine", "menu", "debug", "bot"}
)
@Slf4j
public class iMenuDebuggerPlugin extends Plugin {

    @Inject
    private iMenuDebuggerConfig config;

    @Inject
    private iUtils utils;

    @Inject
    private ConfigManager configManager;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;


    @Provides
    iMenuDebuggerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iMenuDebuggerConfig.class);
    }

    @Override
    protected void startUp() {
    }

    @Override
    protected void shutDown() {
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (!config.menuClicked()) {
            return;
        }
        log.info("MenuOption value: {}, MenuTarget value: {}, Id value: {}, MenuAction value: {}, param0: {}, param1: {}", event.getMenuOption(), event.getMenuTarget(), event.getId(), event.getMenuAction(), event.getParam0(), event.getParam1());

        if (config.printChat()) {
            utils.sendGameMessage("MenuOption value: " + event.getMenuOption());
            utils.sendGameMessage("MenuTarget value: " + event.getMenuTarget());
            utils.sendGameMessage("Id value: " + event.getId());
            utils.sendGameMessage("MenuAction value: " + event.getMenuAction());
            utils.sendGameMessage("ActionParam value: " + event.getActionParam());
            utils.sendGameMessage("WidgetId value: " + event.getWidgetId());
//            utils.sendGameMessage("selectedItemIndex value: " + event.getSelectedItemIndex());
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if (!config.chatMessage()) {
            return;
        }
        log.info("Message: {}", event.getMessage());
        log.info("Type: {}", event.getType());
        log.info("Name: {}", Text.toJagexName(event.getName()));
        log.info("Sender: {}", event.getSender());
    }

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded event) {
        if (!config.widget()) {
            return;
        }
        log.info("Widget spawned: {}", event.toString());
    }

    @Subscribe
    private void onWidgetClosed(WidgetClosed event) {
        if (!config.widget()) {
            return;
        }
        log.info("Widget closed: {}", event.toString());
    }

    @Subscribe
    private void onWidgetHiddenChanged(WidgetHiddenChanged event) {
        if (!config.widget()) {
            return;
        }
        log.info("Widget hidden: {}", event.toString());
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iMenuDebugger")) {
            return;
        }

        if (configButtonClicked.getKey().equals("printVar")) {
            clientThread.invoke(() -> {
                if (config.varbit() != 0) {
                    utils.sendGameMessage("Varbit " + config.varbit() + " value: " + client.getVarbitValue(config.varbit()));
                }
                if (config.varPlayer() != 0) {
                    utils.sendGameMessage("VarPlayer " + config.varPlayer() + " value: " + client.getVarpValue(config.varPlayer()));
                }
            });
        }
    }

    @Subscribe
    private void onItemContainerChanged(ItemContainerChanged event) {
        if (!config.widget()) {
            return;
        }
        log.info("Container changed: {}", event.toString());
    }
}