package net.runelite.client.plugins.iutils.ui;

import net.runelite.client.plugins.iutils.game.Game;

public class Depositbox {
    public final Game game;

    public Depositbox(Game game) {
        this.game = game;
    }

    public void depositInventory() {
        checkDepositboxOpen();
        if (game.inventory().quantity() != 0) {
            game.widget(192, 4).interact(0);
            game.waitUntil(() -> game.inventory().quantity() == 0, 5);
        }
    }

    public void depositEquipment() {
        checkDepositboxOpen();
        if (game.equipment().count() != 0) {
            game.widget(192, 6).interact(0);
            game.tick();
        }
    }

    public void depositLootingbag() {
        checkDepositboxOpen();
        game.widget(192, 8).interact(0);
        game.tick();
    }

    private void checkDepositboxOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("depositbox isn't open");
        }
    }

    public boolean isOpen() {
        return game.widget(192, 1) != null;
    }

    public void close() {
        if (isOpen()) {
            game.widget(192, 1, 11).interact(0);
            game.waitUntil(() -> !isOpen(), 5);
        }
    }
}