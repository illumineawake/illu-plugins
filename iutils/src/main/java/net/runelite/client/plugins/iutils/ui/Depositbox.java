package net.runelite.client.plugins.iutils.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.iWidget;

import javax.inject.Inject;

@Slf4j
public class Depositbox {
    public final Game game;

    @Inject
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

    public boolean deposit(int itemID, String depositAction) {
        if (!isOpen()) {
            return false;
        }

        for (iWidget item : game.widget(192, 2).items()) {
            if (item.itemId() == itemID) {
                log.info("Depositing item: {} {}", itemID, item.index());
                game.widget(192, 2, item.index()).interact(depositAction);
                return true;
            }
        }

        return false;
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