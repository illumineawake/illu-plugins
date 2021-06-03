package net.runelite.client.plugins.iutils.game;

import net.runelite.client.plugins.iutils.walking.TeleportSpell;
import net.runelite.client.plugins.iutils.walking.TeleportTab;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TeleportMethod {

    private Game game;
    private TeleportLocation teleportLocation;
    private int quantity;

    public TeleportMethod(Game game, TeleportLocation teleportLocation, int quantity) {
        this.teleportLocation = teleportLocation;
        this.quantity = quantity;
        this.game = game;
    }

    public List<ItemQuantity> getItems() {
        List<ItemQuantity> items = new ArrayList<>();
        TeleportTab tabLocation = this.teleportLocation.getTeleportTab();
        TeleportSpell locationSpell = this.teleportLocation.getTeleportSpell();

        if (tabLocation.canUse(game) && game.membersWorld()) {
            return List.of(new ItemQuantity(tabLocation.getTabletId(), this.quantity));
        }

        if (locationSpell != null && locationSpell.hasRequirements(game)) {
            for (int i = 0; i < this.quantity; i++) {
                items.addAll(locationSpell.recipe(game));
            }
            return items;
        }

        return items;
    }
}
