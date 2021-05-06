package net.runelite.client.plugins.iutils.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.plugins.iutils.bot.Bot;

// TODO: selling, several offers at once, custom prices, collect to inventory
@Slf4j
public class GrandExchange {
    private final Bot bot;

    public GrandExchange(Bot bot) {
        this.bot = bot;
    }

    public void buy(int item, int quantity) {
        if (!isOpen()) {
            System.out.println("Opening Grand Exchange");
            bot.npcs().withName("Grand Exchange Clerk").nearest().interact("Exchange");
            bot.waitUntil(this::isOpen);
        }

        if (bot.inventory().withId(995).first() == null) {
            throw new IllegalStateException("you'll need some coins to buy stuff");
        }
        log.info("Buying: {} quantity: {}" , item, quantity);
        var slot = freeSlot();

        startBuyOffer(slot);

        bot.chooseItem(item);
        bot.waitUntil(() -> currentItem() == item);

        if (quantity != currentQuantity()) { // todo: use +/- buttons
            bot.widget(465, 24, 7).interact(0);
            bot.tick();

            bot.chooseNumber(quantity);
            bot.tick();
        }

        var price = Math.min(
                (int) Math.ceil(10 * currentPrice()),
                bot.inventory().withId(995).first().quantity() / quantity
        );

        if (price != currentPrice()) {
            bot.widget(465, 24, 12).interact(0);
            bot.tick();

            bot.chooseNumber(price);
            bot.tick();
        }

        bot.tick();

        bot.widget(465, 27).interact(0);

        bot.waitUntil(() -> bot.grandExchangeOffer(slot) != null);

        var ticks = 0;

        while (bot.grandExchangeOffer(slot).getQuantitySold() != quantity && ticks++ < 10) {
            bot.tick();
        }

        if (bot.grandExchangeOffer(slot).getQuantitySold() == quantity) {
            collectToBank();
            return;
        }

        if (bot.grandExchangeOffer(slot).getQuantitySold() != quantity) {
            bot.widget(465, 7, 2).interact(1);
            bot.tick(4);
            collectToInv();
            throw new IllegalStateException("timed out waiting for offer to complete: " + bot.grandExchangeOffer(slot).getQuantitySold() + " / " + quantity);
        }
    }

    public boolean buyProgressively(int item, int quantity, double priceMultiplier, int timeout) {
        if (!isOpen()) {
            throw new IllegalStateException("grand exchange window is closed");
        }

        if (bot.inventory().withId(995).first() == null) {
            throw new IllegalStateException("you'll need some coins to buy stuff");
        }

        int slot = freeSlot();
        startBuyOffer(slot);
        bot.tick(4);
        bot.chooseItem(item);
        bot.waitUntil(() -> currentItem() == item);

        if (quantity != currentQuantity()) { // todo: use +/- buttons
            bot.widget(465, 24, 7).interact(0);
            bot.tick(4);
            bot.chooseNumber(quantity);
            bot.tick(3);
        }

        int price = (int) Math.ceil(priceMultiplier * currentPrice());
        price = Math.min(price, bot.inventory().withId(995).first().quantity() / quantity);

        if (price != currentPrice()) { // todo: use +/- buttons
            bot.widget(465, 24, 12).interact(0);
            bot.tick(4);
            bot.chooseNumber(price);
            bot.tick(3);
        }

        bot.widget(465, 27).interact(0);

        bot.waitUntil(() -> bot.grandExchangeOffer(slot) != null);

        long start = System.currentTimeMillis();

        while (bot.grandExchangeOffer(slot).getQuantitySold() != quantity && System.currentTimeMillis() - start < timeout) {
            bot.tick();
        }

        if (bot.grandExchangeOffer(slot).getQuantitySold() != quantity) {
            System.out.println("[Grand Exchange] Timed out waiting for offer to complete: " + bot.grandExchangeOffer(slot).getQuantitySold() + " / " + quantity);
            bot.widget(465, 14).interact(0);
            bot.tick(4);
            collectToBank();
            return false;
        }

        collectToBank();
        return true;
    }

    public void sell(int item, int price) {
        if (!isOpen()) {
            throw new IllegalStateException("grand exchange window is closed");
        }

        bot.widget(467, 0, bot.inventory().withId(item).first().slot()).interact(0);
        bot.sleepApproximately(3000);

        if (price != currentPrice()) {
            bot.widget(465, 24, 12).interact(0);
            bot.sleepApproximately(2000);
            bot.chooseNumber(price);
            bot.sleepApproximately(800);
        }

        bot.widget(465, 27).interact(0);
        bot.sleepApproximately(4000);
        collectToInventory();
    }

    public void collectToBank() {
        bot.widget(465, 6, 0).interact(1);
        bot.tick(4);
    }

    public void collectToInventory() {
        bot.widget(465, 6, 0).interact(0);
        bot.sleepApproximately(2000);
    }

    public void collectToInv() {
        bot.widget(465, 6, 0).interact(0);
        bot.sleepApproximately(2000);
    }

    public void startBuyOffer(int slot) {
        if (slot < 0 || slot > 7) {
            throw new IllegalArgumentException("slot = " + slot);
        }

        if (currentOpenSlot() != 0) {
            bot.widget(465, 4).interact(0);
            bot.tick();
        }

        if (bot.grandExchangeOffer(slot).getState() != GrandExchangeOfferState.EMPTY) {
            throw new IllegalArgumentException("slot not free");
        }

        bot.widget(465, 7 + slot, 3).interact(0);
        bot.waitUntil(() -> currentOpenSlot() != 0);
    }

    private int freeSlot() {
        for (int slot = 0; slot < 8; slot++) {
            GrandExchangeOffer offer = bot.grandExchangeOffer(slot);
            if (offer == null || offer.getState() == GrandExchangeOfferState.EMPTY) {
                System.out.println(bot.grandExchangeOffer(slot));
                return slot;
            }
        }

        throw new IllegalStateException("there are no free slots");
    }

    private OfferType offerCreationType() {
        return bot.varb(4397) == 0 ? OfferType.BUY_OFFER_CREATION : OfferType.SELL_OFFER_CREATION;
    }

    public boolean isOpen() {
        return bot.screenContainer().nestedInterface() == 465;
    }

    public int currentOpenSlot() {
        return bot.varb(4439);
    }

    public int currentItem() {
        return bot.varp(1151);
    }

    public int currentQuantity() {
        return bot.varb(4396);
    }

    public int currentPrice() {
        return bot.varb(4398);
    }

    public enum OfferType {
        BUY_OFFER_CREATION,
        SELL_OFFER_CREATION,
    }
}
