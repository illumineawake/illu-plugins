package net.runelite.client.plugins.iutils.ui;

import net.runelite.client.plugins.iutils.api.Magic;
import net.runelite.client.plugins.iutils.bot.Bot;

import javax.inject.Inject;

/**
 * @author kylestev
 */
public class StandardSpellbook {
    private final Bot bot;

    @Inject
    public StandardSpellbook(Bot bot) {
        this.bot = bot;
    }

    public void castSpell(Magic spell) {
        bot.widget(218, spell.widgetChild).interact("Cast");
    }

    public void lumbridgeHomeTeleport() {
        // TODO: check response to update timer in Profile
        castSpell(Magic.LUMBRIDGE_HOME_TELEPORT);
        bot.waitUntil(() -> bot.localPlayer().position().regionID() == 12850, 20000);
        bot.tick(5);
    }
}
