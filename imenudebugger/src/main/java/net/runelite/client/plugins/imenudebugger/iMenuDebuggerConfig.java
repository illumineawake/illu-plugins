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

import net.runelite.client.config.*;

@ConfigGroup("iMenuDebugger")
public interface iMenuDebuggerConfig extends Config {

    @ConfigItem(
            keyName = "menuClicked",
            name = "Log Menu Clicked events",
            description = "Enable to log menu option clicked events",
            position = 10
    )
    default boolean menuClicked() {
        return true;
    }

    @ConfigItem(
            keyName = "widget",
            name = "Log Widget spawned/despawned events",
            description = "Enable to log widget spawned/despawned events",
            position = 20
    )
    default boolean widget() {
        return false;
    }

    @ConfigItem(
            keyName = "chatMessage",
            name = "Log Chat events",
            description = "Enable to log chat events",
            position = 20
    )
    default boolean chatMessage() {
        return false;
    }

    @ConfigItem(
            keyName = "printChat",
            name = "Print to game chat",
            description = "Enable to print menu entry to game chat",
            position = 50
    )
    default boolean printChat() {
        return true;
    }

    @ConfigItem(
            keyName = "varbit",
            name = "Varbit ID",
            description = "Provide Varbit ID then press the Print Varb/Varp button print the value of it to chat",
            position = 60
    )
    default int varbit() {
        return 0;
    }

    @ConfigItem(
            keyName = "varPlayer",
            name = "VarPlayer ID",
            description = "Provide VarPlayer ID then press the Print Varb/Varp button print the value of it to chat",
            position = 65
    )
    default int varPlayer() {
        return 0;
    }

    @ConfigItem(
            keyName = "printVar",
            name = "Print Varb/Varp",
            description = "Press to print the provided varb/varp values to chat",
            position = 70,
            title = "agilityTitle"
    )
    default Button printVar() {
        return new Button();
    }
}
