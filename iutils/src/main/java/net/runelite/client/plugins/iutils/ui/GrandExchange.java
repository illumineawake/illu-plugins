package net.runelite.client.plugins.iutils.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.iutils.CalculationUtils;
import net.runelite.client.plugins.iutils.api.GrandExchangePrices;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.ItemQuantity;
import net.runelite.client.plugins.iutils.walking.BankLocations;

import javax.inject.Inject;
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
            game.widget(465, 25, 12).interact(0);
            game.tick(2);
            game.chooseNumber(price);
            game.tick(2);
        }

        game.widget(465, 29).interact(0);
        game.tick(5);
        collectToInv();
    }

    /**
     * Buys items from GE, if prices are over 8000GP it will progressively buy, otherwise attempts to buy instantly
     */
    public void buy(int item, int quantity) {
        log.info("BEFORE PRICE RETRIEVAL - Buying GE item: {}, quantity: {}", item, quantity);
        if (GrandExchangePrices.get(item).high * quantity > 1000) {
            log.info("AFTER PRICE RETRIEVAL - Buying GE item: {}, quantity: {}", item, quantity);
            if (!buyProgressively(item, quantity, 1.2, 30, CalculationUtils.random(15, 25))) {
                throw new UnsupportedOperationException("Failed to buy GE item: " + item + "  progressively");
            }
        } else if (!buyInstantly(item, quantity)) {
            throw new UnsupportedOperationException("Failed to buy GE item: " + item + " instantly");
        }
    }

    public void buy(List<ItemQuantity> items) {
        for (ItemQuantity item : items) {
            buy(item.id, item.quantity);
        }
    }

    /**
     * Buys list of items at GE with a high max price to buy quickly
     */
    public void buyInstantly(List<ItemQuantity> items) {
        for (ItemQuantity item : items) {
            buyInstantly(item.id, item.quantity);
        }
    }

    /**
     * Buys items at GE with a high max price to buy quickly
     */
    public boolean buyInstantly(int item, int quantity) {
        var lastprice = -1;

        for (int attempts = 0; attempts < 30; attempts++) {
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

            if (quantity != currentQuantity()) {
                game.widget(465, 25, 7).interact(0);
                game.tick(2, 4);

                game.chooseNumber(quantity);
                game.tick(2, 4);
            }

            var price = Math.min(
                    (int) Math.ceil((10 + (attempts * 5)) * currentPrice()),
                    game.inventory().withId(995).first().quantity() / quantity
            );

            if (price == lastprice) {
                log.info("Price is same as last attempt to buy item: {}. Checking bank for more GP", item);
                if (bank().quantity(ItemID.COINS_995) > 0) {
                    bank().withdraw(ItemID.COINS_995, Integer.MAX_VALUE, false);
                    continue;
                } else {
                    log.info("Not enough coins to buy item: {} from GE", item);
                    return false;
                }
            }

            lastprice = price;

            if (price != currentPrice()) {
                game.widget(465, 25, 12).interact(0);
                game.tick(2, 4);

                game.chooseNumber(price);
                game.tick(2, 4);
            }

            game.tick();

            game.widget(465, 29).interact(0);

            game.waitUntil(() -> game.grandExchangeOffer(slot) != null);
            game.tick(2, 4);

            var ticks = 0;

            while (game.grandExchangeOffer(slot).getQuantitySold() != quantity && ticks++ < 10) {
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
                log.info("timed out waiting for offer to complete: " + game.grandExchangeOffer(slot).getQuantitySold() + " / " + quantity);
            }
        }
        return false;
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
                game.widget(465, 25, 7).interact(0);
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
                game.widget(465, 25, 12).interact(0);
                game.tick(2, 4);

                game.chooseNumber(price);
                game.tick(2, 4);
            }

            game.tick();
            game.widget(465, 29).interact(0);
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

    private Bank bank() {
        var bank = new Bank(game);

        if (!bank.isOpen()) {
            BankLocations.walkToBank(game);
            if (game.npcs().withName("Banker").withAction("Bank").exists()) {
                game.npcs().withName("Banker").withAction("Bank").nearest().interact("Bank");
            } else if (game.objects().withName("Bank booth").withAction("Bank").exists()) {
                game.objects().withName("Bank booth").withAction("Bank").nearest().interact("Bank");
            } else {
                game.objects().withName("Bank chest").nearest().interact("Use");
            }
            game.waitUntil(bank::isOpen, 10);
        }
        return bank;
    }
}
