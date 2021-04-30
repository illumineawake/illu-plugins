package net.runelite.client.plugins.iutils.api;

import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.ui.Prayer;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class Prayers {
    private final Bot bot;
    private final Map<Prayer, Long> prayerTimes = new HashMap<>();
    private final Map<Prayer, Boolean> prayerStatuses = new HashMap<>();

    @Inject
    public Prayers(Bot bot) {
        this.bot = bot;
    }

    public boolean active(Prayer prayer) {
        if (System.currentTimeMillis() - prayerTimes.getOrDefault(prayer, 0L) < 1800) {
            return prayerStatuses.getOrDefault(prayer, false);
        }

        return bot.varb(prayer.varb) == 1;
    }

    public void setEnabled(Prayer prayer, boolean enabled) {
        if (bot.modifiedLevel(Skill.PRAYER) != 0) {

            if (enabled == active(prayer)) {
                return;
            }

            prayerTimes.put(prayer, System.currentTimeMillis());
            prayerStatuses.put(prayer, enabled);

            bot.widget(541, prayer.widget).interact(0);
        }
    }
}
