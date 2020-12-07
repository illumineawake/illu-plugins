/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.essencehighlighter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("EssenceHighlighter")
public interface EssenceConfig extends Config
{

	@ConfigItem(
		keyName = "showDenseRunestoneIndicator",
		name = "Show icon on Dense Runestone",
		description = "Configures whether to display an indicator when dense runestone is ready to be mined",
		position = 0
	)
	default boolean showDenseRunestoneIndicator()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showDenseRunestoneClickbox",
		name = "Highlight Dense Runestone",
		description = "Configures whether to display a click box when dense runestone is ready to be mined",
		position = 1
	)
	default boolean showDenseRunestoneClickbox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showDenseBloodALTARicon",
		name = "Show RC Icon on blood altar",
		description = "Configures whether to display a rc icon on blood altar",
		position = 2
	)
	default boolean showDenseBloodALTARicon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightDenseBloodALTAR",
		name = "Color Code blood altar",
		description = "Color coding the blood altar",
		position = 3
	)
	default boolean highlightDenseBloodALTAR()
	{
		return true;
	}
}
