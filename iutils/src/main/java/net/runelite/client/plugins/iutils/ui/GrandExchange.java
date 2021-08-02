package net.runelite.client.plugins.iutils.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.plugins.iutils.api.GrandExchangePrices;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.ItemQuantity;

import java.util.List;

// TODO: selling, several offers at once, custom prices, collect to inventory
@Slf4j
public class GrandExchange {
    private final Game game;

    public GrandExchange(Game game) {
        this.game = game;
    }

    public void sell(int item, int price) {
        if (!isOpen()) {
            game.npcs().withName("Grand Exchange Clerk").nearest().interact("Exchange");
            game.waitUntil(this::isOpen);
        }

        var baseId = game.getFromClientThread(() -> game.client().getItemComposition(item).getNote() == 799 ?
                game.client().getItemComposition(item).getLinkedNoteId() : item);
        game.widget(467, 0, game.inventory().withId(item).first().slot()).interact(0);
        game.waitUntil(() -> currentSellItem() == baseId);

        if (price != currentPrice()) {
            game.widget(465, 24, 12).interact(0);
            game.tick(2);
            game.chooseNumber(price);
            game.tick(2);
        }

        game.widget(465, 27).interact(0);
        game.tick(5);
        collectToInv();
    }

    /**
     * Buys items from GE, if prices are over 8000GP it will progressively buy, otherwise attempts to buy instantly
     * */
    public void buy(int item, int quantity) {
        if (GrandExchangePrices.get(item).high > 1000) {
            if (!buyProgressively(item, quantity,1.2, 30, 20)) {
                throw new AssertionError("Failed to buy GE item progressively");
            }
        } else {
            buyInstantly(item, quantity);
        }
    }

    public void buy(List<ItemQuantity> items) {
        for (ItemQuantity item : items) {
            buy(item.id, item.quantity);
        }
    }

    /**
     * Buys list of items at GE with a high max price to buy quickly
     * */
    public void buyInstantly(List<ItemQuantity> items) {
        for (ItemQuantity item : items) {
            buyInstantly(item.id, item.quantity);
        }
    }

    /**
     * Buys items at GE with a high max price to buy quickly
     * */
    public void buyInstantly(int item, int quantity) {
        if (!isOpen()) {
            log.info("Opening Grand Exchange");
            game.npcs().withName("Grand Exchange Clerk").nearest().interact("Exchange");
            game.waitUntil(this::isOpen);
            game.tick(2, 4);
        }

        if (game.inventory().withId(995).first() == null) {
            throw new IllegalStateException("you'll need some coins to buy stuff");
        }
        log.info("Buying: {} quantity: {}", item, quantity);
        var slot = freeSlot();

        startBuyOffer(slot);

        game.chooseItem(item);
        game.waitUntil(() -> currentBuyItem() == item);

        if (quantity != currentQuantity()) { // todo: use +/- buttons
            game.widget(465, 24, 7).interact(0);
            game.tick(2, 4);

            game.chooseNumber(quantity);
            game.tick(2, 4);
        }

        var price = Math.min(
                (int) Math.ceil(10 * currentPrice()),
                game.inventory().withId(995).first().quantity() / quantity
        );

        if (price != currentPrice()) {
            game.widget(465, 24, 12).interact(0);
            game.tick(2, 4);

            game.chooseNumber(price);
            game.tick(2, 4);
        }

        game.tick();

        game.widget(465, 27).interact(0);

        game.waitUntil(() -> game.grandExchangeOffer(slot) != null);
        game.tick(2, 4);

        var ticks = 0;

        while (game.grandExchangeOffer(slot).getQuantitySold() != quantity && ticks++ < 10) {
            game.tick(1, 4);
        }

        if (game.grandExchangeOffer(slot).getQuantitySold() == quantity) {
            collectToBank();
            return;
        }

        if (game.grandExchangeOffer(slot).getQuantitySold() != quantity) {
            game.widget(465, 7, 2).interact(1);
            game.tick(4);
            collectToInv();
            throw new IllegalStateException("timed out waiting for offer to complete: " + game.grandExchangeOffer(slot).getQuantitySold() + " / " + quantity);
        }
    }

    /**
     * Attempts to buy items using given multiplier and progressively increases until bought
     */
    public boolean buyProgressively(int item, int quantity, double priceMultiplier, int maxAttempts, int tickTimeout) {
        var lastPrice = -1;

        for (int attempts = 1; attempts < maxAttempts; attempts++) {
            if (!isOpen()) {
                log.info("Opening Grand Exchange");
                game.npcs().withName("Grand Exchange Clerk").nearest().interact("Exchange");
                game.waitUntil(this::isOpen);
                game.tick(2, 4);
            }

            if (game.inventory().withId(995).first() == null) {
                throw new IllegalStateException("you'll need some coins to buy stuff");
            }
            log.info("Progressive buying: {} quantity: {} attempt: {}", item, quantity, attempts);
            var slot = freeSlot();

            startBuyOffer(slot);
            game.chooseItem(item);
            game.waitUntil(() -> currentBuyItem() == item);

            if (quantity != currentQuantity()) {
                game.widget(465, 24, 7).interact(0);
                game.tick(2, 4);

                game.chooseNumber(quantity);
                game.tick(2, 4);
            }

            int price = (int) Math.ceil((priceMultiplier * attempts) * currentPrice());
            price = Math.min(price, game.inventory().withId(995).first().quantity() / quantity);

            if (lastPrice == price) {
                log.info("Not enough money for progressive buy offer");
                //TODO withdraw money from bank
                return false;
            }
            lastPrice = price;

            if (price != currentPrice()) {
                game.widget(465, 24, 12).interact(0);
                game.tick(2, 4);

                game.chooseNumber(price);
                game.tick(2, 4);
            }

            game.tick();
            game.widget(465, 27).interact(0);
            game.waitUntil(() -> game.grandExchangeOffer(slot) != null);
            game.tick(2, 4);

            var startTicks = game.ticks();
            while (game.grandExchangeOffer(slot).getQuantitySold() != quantity && (game.ticks() - startTicks) < tickTimeout) {
                game.tick(1, 4);
            }

            if (game.grandExchangeOffer(slot).getQuantitySold() == quantity) {
                collectToBank();
                return true;
            }

            if (game.grandExchangeOffer(slot).getQuantitySold() != quantity) {
                game.widget(465, 7, 2).interact(1);
                game.tick(4);
                collectToInv();
                log.info("Attempt {} timed out for progressive buy offer: {} / {}", attempts, game.grandExchangeOffer(slot).getQuantitySold(), quantity);
                //TODO abort offer?
            }
        }
        return false;
    }

    public void collectToBank() {
        game.widget(465, 6, 0).interact(1);
        game.tick(4, 6);
    }

    public void collectToInv() {
        game.widget(465, 6, 0).interact(0);
        game.sleepApproximately(2000);
    }

    public void startBuyOffer(int slot) {
        if (slot < 0 || slot > 7) {
            throw new IllegalArgumentException("slot = " + slot);
        }

        if (currentOpenSlot() != 0) {
            game.widget(465, 4).interact(0);
            game.tick(2, 4);
        }

        if (game.grandExchangeOffer(slot).getState() != GrandExchangeOfferState.EMPTY) {
            throw new IllegalArgumentException("slot not free");
        }

        game.widget(465, 7 + slot, 3).interact(0);
        game.waitUntil(() -> currentOpenSlot() != 0);
        game.tick(2, 4);
    }

    private int freeSlot() {
        for (int slot = 0; slot < 8; slot++) {
            GrandExchangeOffer offer = game.grandExchangeOffer(slot);
            if (offer == null || offer.getState() == GrandExchangeOfferState.EMPTY) {
                return slot;
            }
        }

        throw new IllegalStateException("there are no free slots");
    }

    private OfferType offerCreationType() {
        return game.varb(4397) == 0 ? OfferType.BUY_OFFER_CREATION : OfferType.SELL_OFFER_CREATION;
    }

    public boolean isOpen() {
        return game.screenContainer().nestedInterface() == 465;
    }

    public int currentOpenSlot() {
        return game.varb(4439);
    }

    public int currentBuyItem() {
        return game.varp(1151);
    }

    public int currentSellItem() {
        return game.varp(1151);
    }

    public int currentQuantity() {
        return game.varb(4396);
    }

    public int currentPrice() {
        return game.varb(4398);
    }

    public enum OfferType {
        BUY_OFFER_CREATION,
        SELL_OFFER_CREATION,
    }
}
