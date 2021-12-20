package net.runelite.client.plugins.iutils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static net.runelite.client.plugins.iutils.iUtils.sleep;

@Slf4j
@Singleton
public class PlayerUtils {
    @Inject
    private Client client;

    @Inject
    private MouseUtils mouse;

    @Inject
    private MenuUtils menu;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private BankUtils bank;

    @Inject
    private CalculationUtils calc;

    @Inject
    private ExecutorService executorService;

    private int nextRunEnergy;

    public boolean isMoving() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return false;
        }
        return player.getIdlePoseAnimation() != player.getPoseAnimation();
    }

    public boolean isMoving(LocalPoint lastTickLocalPoint) {
        return !client.getLocalPlayer().getLocalLocation().equals(lastTickLocalPoint);
    }

    public boolean isInteracting() {
        sleep(25);
        return isMoving() || client.getLocalPlayer().getAnimation() != -1;
    }

    public boolean isAnimating() {
        return client.getLocalPlayer().getAnimation() != -1;
    }

    public boolean isRunEnabled() {
        return client.getVarpValue(173) == 1;
    }

    //enables run if below given minimum energy with random positive variation
    public void handleRun(int minEnergy, int randMax) {
        assert client.isClientThread();
        if (nextRunEnergy < minEnergy || nextRunEnergy > minEnergy + randMax) {
            nextRunEnergy = calc.getRandomIntBetweenRange(minEnergy, minEnergy + calc.getRandomIntBetweenRange(0, randMax));
        }
        if (client.getEnergy() > nextRunEnergy ||
                client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0) {
            if (drinkStamPot(15 + calc.getRandomIntBetweenRange(0, 30))) {
                return;
            }
            if (!isRunEnabled()) {
                nextRunEnergy = 0;
                Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);
                if (runOrb != null) {
                    enableRun(runOrb.getBounds());
                }
            }
        }
    }

    public void handleRun(int minEnergy, int randMax, int potEnergy) {
        assert client.isClientThread();
        if (nextRunEnergy < minEnergy || nextRunEnergy > minEnergy + randMax) {
            nextRunEnergy = calc.getRandomIntBetweenRange(minEnergy, minEnergy + calc.getRandomIntBetweenRange(0, randMax));
        }
        if (client.getEnergy() > (minEnergy + calc.getRandomIntBetweenRange(0, randMax)) ||
                client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0) {
            if (drinkStamPot(potEnergy)) {
                return;
            }
            if (!isRunEnabled()) {
                nextRunEnergy = 0;
                Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);
                if (runOrb != null) {
                    enableRun(runOrb.getBounds());
                }
            }
        }
    }

    public void enableRun(Rectangle runOrbBounds) {
        log.info("enabling run");
        executorService.submit(() ->
        {
            menu.setEntry(new LegacyMenuEntry("Toggle Run", "", 1, 57, -1,
                    10485783, false));
            mouse.delayMouseClick(runOrbBounds, calc.getRandomIntBetweenRange(10, 250));
        });
    }

    //Checks if Stamina enhancement is active and if stamina potion is in inventory
    public WidgetItem shouldStamPot(int energy) {
        final List<Integer> STAMINA_POTIONS = List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3,
                ItemID.STAMINA_POTION4, ItemID.ENERGY_POTION1, ItemID.ENERGY_POTION2, ItemID.ENERGY_POTION3, ItemID.ENERGY_POTION4,
                ItemID.SUPER_ENERGY1, ItemID.SUPER_ENERGY2, ItemID.SUPER_ENERGY3, ItemID.SUPER_ENERGY4, ItemID.EGNIOL_POTION_1,
                ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4);

        if (!inventory.getItems(STAMINA_POTIONS).isEmpty()
                && client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0 && client.getEnergy() < energy && !bank.isOpen()) {
            return inventory.getWidgetItem(STAMINA_POTIONS);
        } else {
            return null;
        }
    }

    public boolean drinkStamPot(int energy) {
        WidgetItem staminaPotion = shouldStamPot(energy);
        if (staminaPotion != null) {
            log.info("using stamina potion");
            menu.setEntry(new LegacyMenuEntry("", "", staminaPotion.getId(), MenuAction.ITEM_FIRST_OPTION.getId(),
                    staminaPotion.getIndex(), 9764864, false));
            mouse.delayMouseClick(staminaPotion.getCanvasBounds(), calc.getRandomIntBetweenRange(5, 200));
            return true;
        }
        return false;
    }

    public List<Item> getEquippedItems() {
        assert client.isClientThread();

        List<Item> equipped = new ArrayList<>();
        Item[] items = client.getItemContainer(InventoryID.EQUIPMENT).getItems();
        for (Item item : items) {
            if (item.getId() == -1 || item.getId() == 0) {
                continue;
            }
            equipped.add(item);
        }
        return equipped;
    }

    /*
     *
     * Returns if a specific item is equipped
     *
     * */
    public boolean isItemEquipped(Collection<Integer> itemIds) {
        assert client.isClientThread();

        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipmentContainer != null) {
            Item[] items = equipmentContainer.getItems();
            for (Item item : items) {
                if (itemIds.contains(item.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
