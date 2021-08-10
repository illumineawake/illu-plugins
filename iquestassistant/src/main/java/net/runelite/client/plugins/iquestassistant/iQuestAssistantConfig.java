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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("iQuestAssistant")
public interface iQuestAssistantConfig extends Config {

    @ConfigItem(
            keyName = "continueChat",
            name = "Continue all chats",
            description = "Progress through all click here to continue... chats",
            position = 0
    )
    default boolean continueChat() {
        return true;
    }

    @ConfigItem(
            keyName = "questHelper",
            name = "Select Quest Helper options",
            description = "Enable to auto select highlighted quest helper options",
            position = 5
    )
    default boolean questHelper() {
        return true;
    }

    @ConfigItem(
            keyName = "supportedQuests",
            name = "Progress supported quest dialogue",
            description = "Progress through supported quest dialogue",
            position = 10
    )
    default boolean supportedQuests() {
        return false;
    }

    @ConfigItem(
            keyName = "allQuests",
            name = "Progress all quests (experimental)",
            description = "Progress through all quest dialogue. This will have bugs.",
            position = 15
    )
    default boolean allQuests() {
        return false;
    }

}
