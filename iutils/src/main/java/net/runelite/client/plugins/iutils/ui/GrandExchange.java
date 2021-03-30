package net.runelite.client.plugins.iutils.ui;

import net.runelite.client.plugins.iutils.bot.Bot;

// TODO: selling, several offers at once, custom prices, collect to inventory
public class GrandExchange {
    private final Bot bot;

    public GrandExchange(Bot bot) {
        this.bot = bot;
    }

    public boolean buy(int item, int quantity, double priceMultiplier, int timeout) {
        if (!isOpen()) {
            throw new IllegalStateException("grand exchange window is closed");
        }

        if (bot.inventory().withId(995).first() == null) {
            throw new IllegalStateException("you'll need some coins to buy stuff");
        }

        int slot = freeSlot();
        startBuyOffer(slot);
        bot.tick(12);
        bot.chooseItem(item);
        bot.waitUntil(() -> currentItem() == item);

        if (quantity != currentQuantity()) { // todo: use +/- buttons
            bot.widget(465, 24, 7).interact(0);
            bot.tick(4);
            bot.chooseNumber(quantity);
            bot.tick();
        }

        int price = (int) Math.ceil(priceMultiplier * currentPrice());
        price = Math.min(price, bot.inventory().withId(995).first().quantity() / quantity);

        if (price != currentPrice()) { // todo: use +/- buttons
            bot.widget(465, 24, 12).interact(0);
            bot.tick(4);
            bot.chooseNumber(price);
            bot.tick();
        }

        bot.widget(465, 27).interact(0);

        bot.waitUntil(() -> bot.grandExchangeOffer(slot) != null);

        long start = System.currentTimeMillis();

        while (bot.grandExchangeOffer(slot).completedQuantity() != quantity && System.currentTimeMillis() - start < timeout) {
            bot.tick();
        }

        if (bot.grandExchangeOffer(slot).completedQuantity() != quantity) {
            System.out.println("[Grand Exchange] Timed out waiting for offer to complete: " + bot.grandExchangeOffer(slot).completedQuantity() + " / " + quantity);
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

    public void startBuyOffer(int slot) {
        if (slot < 0 || slot > 7) {
            throw new IllegalArgumentException("slot = " + slot);
        }

        if (currentOpenSlot() != 0) {
            bot.widget(465, 4).interact(0);
            bot.tick();
        }

        if (bot.grandExchangeOffer(slot) != null) {
            throw new IllegalArgumentException("slot not free");
        }

        bot.widget(465, 7 + slot, 3).interact(0);
        bot.waitUntil(() -> currentOpenSlot() != 0);
    }

    private int freeSlot() {
        for (int slot = 0; slot < 8; slot++) {
            if (bot.grandExchangeOffer(slot) == null) {
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
