package com.example.kotlinexample

import com.google.inject.Provides
import net.runelite.api.events.GameTick
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.PluginType
import org.pf4j.Extension
import javax.inject.Inject

@Extension
@PluginDescriptor(
        name = "Kotlin example",
        description = "Kotlin example",
        type = PluginType.MISCELLANEOUS
)
class KotlinExamplePlugin : Plugin() {

    // Logger object
    companion object : Log()

    // Injects our config
    @Inject
    private lateinit var config: KotlinExampleConfig

    // Provides our config
    @Provides
    fun provideConfig(configManager: ConfigManager): KotlinExampleConfig {
        return configManager.getConfig(KotlinExampleConfig::class.java)
    }

    override fun startUp() {
        // runs on plugin startup
        log.info("Plugin started")

        // example how to use config items
        if (config.example())
        {
            // do stuff
            log.info("The value of 'config.example()' is ${config.example()}")
        }
    }

    override fun shutDown() {
        // runs on plugin shutdown
        log.info("Plugin stopped")
    }

    @Subscribe
    fun onGameTick(gameTick: GameTick) {
        // runs every gametick
        log.info("Gametick")
    }
}