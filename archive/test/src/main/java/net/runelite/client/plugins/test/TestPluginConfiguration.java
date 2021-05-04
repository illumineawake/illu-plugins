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
package net.runelite.client.plugins.test;

import net.runelite.client.config.*;

import java.util.function.Consumer;

@ConfigGroup("Test")
public interface TestPluginConfiguration extends Config
{
	@ConfigTitleSection(
		keyName = "delayTitle",
		name = "Delay Title",
		description = "Delay settings are below this title",
		position = 2
	)
	@Range
		(
			min = 0,
			max = 550
		)
	@ConfigItem(
		keyName = "minDelay",
		name = "Absolute Delay Min",
		description = "",
		position = 3,
		titleSection = "delayTitle"
	)
	default int min()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "maxDelay",
		name = "Absolute Delay Max",
		description = "",
		position = 4,
		titleSection = "delayTitle"
	)
	default int max()
	{
		return 5;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "target",
		name = "Delay Target",
		description = "",
		position = 5,
		titleSection = "delayTitle"
	)
	default int target()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "deviation",
		name = "Delay Deviation",
		description = "",
		position = 6,
		titleSection = "delayTitle"
	)
	default int deviation()
	{
		return 0;//10
	}

	@ConfigItem(
		keyName = "weightedDistribution",
		name = "Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 7,
		titleSection = "delayTitle"
	)
	default boolean weightedDistribution()
	{
		return false;
	}

	@ConfigSection(
		keyName = "buttonSection",
		name = "Button config section",
		description = "This is the button config section",
		position = 8
	)
	default boolean buttonSection()
	{
		return false;
	}

	@Range(
		min = 0,
		max = 100
	)
	@ConfigItem(
		keyName = "volume",
		name = "Volume modification",
		description = "Configures tick/tock volume; only effects custom sounds.",
		position = 9,
		section = "buttonSection"
	)
	@Units(Units.PERCENT)
	default int volume()
	{
		return 35;
	}

	@ConfigItem(
		keyName = "testButton",
		name = "Test Button",
		description = "Test button that changes variable value",
		position = 10,
		section = "buttonSection"
	)
	default Consumer<TestPlugin> testButton()
	{
		return (testPlugin) ->
		{
			if (testPlugin.pluginManager.isPluginEnabled(testPlugin))
			{
				testPlugin.startBot = !testPlugin.startBot;
			}
			else
			{
				testPlugin.startBot = false;
			}
			System.out.println("test button was pressed in config, status is: " + testPlugin.startBot);
		};
	}

	@ConfigItem(
		keyName = "testCourses",
		name = "Supported Courses (Don't edit, FYI only)",
		description = "Support agility courses, don't enter anything into this field",
		position = 11

	)
	default String testCourses()
	{
		return "Gnome, Draynor, Varrock, Canifis, Falador, Seers, Rellekka, Ardougne";
	}
}
