package net.runelite.client.plugins.iutils.game;

public class ItemQuantity {
    public final int id;
    public int quantity;

    public ItemQuantity(int id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public String toString() {
        return "Item: " + id + ", Quantity: " + quantity;
    }
}
