package net.runelite.client.plugins.iutils.ui;

import net.runelite.client.plugins.iutils.bot.Bot;

public class MakeHelper {
    private final Bot bot;

    public MakeHelper(Bot bot) {
        this.bot = bot;
    }

    public boolean isOpen() {
        return bot.widget(162, 562).nestedInterface() == 270;
    }

    public void make(int index, int quantity) {
        if (!isOpen()) {
            throw new IllegalStateException("not open");
        }

        if (index > 9) {
            throw new IllegalArgumentException("index " + index);
        }

        if (quantity < 1 || quantity > 28) {
            throw new IllegalArgumentException("quantity " + quantity);
        }

        bot.widget(270, 14 + index, quantity).select();
    }
}
