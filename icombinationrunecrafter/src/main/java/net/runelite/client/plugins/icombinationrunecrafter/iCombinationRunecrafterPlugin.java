/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.icombinationrunecrafter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.api.GrandExchangePrices;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.runelite.api.MenuAction.ITEM_USE_ON_GAME_OBJECT;
import static net.runelite.client.plugins.icombinationrunecrafter.iCombinationRunecrafterState.*;
import static net.runelite.client.plugins.iutils.iUtils.iterating;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iCombination Runecrafter Plugin",
        enabledByDefault = false,
        description = "Illumine - Combination Runecrafting plugin",
        tags = {"illumine", "runecrafting", "bot", "smoke", "steam", "lava", "combination"}
)
@Slf4j
public class iCombinationRunecrafterPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private iCombinationRunecrafterConfig config;

    @Inject
    private iUtils utils;

    @Inject
    private MouseUtils mouse;

    @Inject
    private PlayerUtils playerUtils;

    @Inject
    private BankUtils bank;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private InterfaceUtils interfaceUtils;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    @Inject
    private ObjectUtils object;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private iCombinationRunecrafterOverlay overlay;

    @Inject
    private ReflectBreakHandler chinBreakHandler;

    LegacyMenuEntry targetMenu;
    Instant botTimer;
    Player player;
    iCombinationRunecrafterState state;
    iCombinationRunecrafterState necklaceState;
    iCombinationRunecrafterState staminaState;

    LocalPoint beforeLoc = new LocalPoint(0, 0);
    GameObject bankChest;
    GameObject mysteriousRuins;
    GameObject fireAltar;
    Widget bankItem;
    WidgetItem useableItem;

    Set<Integer> DUEL_RINGS = Set.of(ItemID.RING_OF_DUELING2, ItemID.RING_OF_DUELING3, ItemID.RING_OF_DUELING4, ItemID.RING_OF_DUELING5, ItemID.RING_OF_DUELING6, ItemID.RING_OF_DUELING7, ItemID.RING_OF_DUELING8);
    Set<Integer> BINDING_NECKLACE = Set.of(ItemID.BINDING_NECKLACE);
    Set<Integer> STAMINA_POTIONS = Set.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4);
    Set<Integer> TIARAS = Set.of(ItemID.FIRE_TIARA);
    List<Integer> REQUIRED_ITEMS = new ArrayList<>();

    boolean startBot;
    boolean setTalisman;
    boolean outOfNecklaces;
    boolean outOfStaminaPots;
    long sleepLength;
    int tickLength;
    int timeout;
    int coinsPH;
    int beforeEssence;
    int totalEssence;
    int beforeMaterialRunes;
    int totalMaterialRunes;
    int beforeTalisman;
    int totalTalisman;
    int totalCraftedRunes;
    int beforeCraftedRunes;
    int currentCraftedRunes;
    int totalDuelRings;
    int totalNecklaces;
    int totalStaminaPots;
    int runesPH;
    int profitPH;
    int totalProfit;
    int runesCost;
    int essenceCost;
    int talismanCost;
    int duelRingCost;
    int necklaceCost;
    int staminaPotCost;
    int materialRuneCost;
    int essenceTypeID;
    int talismanID;
    int materialRuneID;
    int createdRuneTypeID;

    @Provides
    iCombinationRunecrafterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iCombinationRunecrafterConfig.class);
    }

    @Override
    protected void startUp() {
        chinBreakHandler.registerPlugin(this);
    }

    @Override
    protected void shutDown() {
        resetVals();
        chinBreakHandler.unregisterPlugin(this);
    }

    private void resetVals() {
        log.info("stopping Combination Runecrafting plugin");
        chinBreakHandler.stopPlugin(this);
        startBot = false;
        botTimer = null;
        overlayManager.remove(overlay);
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iCombinationRunecrafter")) {
            return;
        }
        log.info("button {} pressed!", configButtonClicked.getKey());
        if (configButtonClicked.getKey().equals("startButton")) {
            if (!startBot) {
                startBot = true;
                chinBreakHandler.startPlugin(this);
                botTimer = Instant.now();
                initCounters();
                state = null;
                necklaceState = null;
                targetMenu = null;
                setTalisman = false;
                createdRuneTypeID = config.getRunecraftingType().getCreatedRuneID();
                talismanID = config.getRunecraftingType().getTalismanID();
                materialRuneID = config.getRunecraftingType().getMaterialRuneID();
                essenceTypeID = config.getEssence().getId();
                REQUIRED_ITEMS = List.of(talismanID, materialRuneID, essenceTypeID);
                updatePrices();
                botTimer = Instant.now();
                overlayManager.add(overlay);
            } else {
                resetVals();
            }
        }
    }

    @Subscribe
    private void onConfigChange(ConfigChanged event) {
        if (!event.getGroup().equals("iCombinationRunecrafter")) {
            return;
        }
        switch (event.getKey()) {
            case "getEssence":
                essenceTypeID = config.getEssence().getId();
                essenceCost = (essenceTypeID != ItemID.DAEYALT_ESSENCE) ?
                        GrandExchangePrices.get(essenceTypeID).high : 0;
                break;
            case "getRunecraftingType":
                createdRuneTypeID = config.getRunecraftingType().getCreatedRuneID();
                talismanID = config.getRunecraftingType().getTalismanID();
                materialRuneID = config.getRunecraftingType().getMaterialRuneID();
                break;
        }
        setTalisman = false;
        REQUIRED_ITEMS = List.of(talismanID, materialRuneID, essenceTypeID);
        updatePrices();
    }

    private void initCounters() {
        timeout = 0;
        coinsPH = 0;
        beforeEssence = 0;
        totalEssence = 0;
        beforeMaterialRunes = 0;
        totalMaterialRunes = 0;
        beforeTalisman = 0;
        totalTalisman = 0;
        beforeCraftedRunes = 0;
        totalCraftedRunes = 0;
        totalDuelRings = 0;
        totalNecklaces = 0;
        totalStaminaPots = 0;
        runesPH = 0;
        profitPH = 0;
        totalProfit = 0;
        currentCraftedRunes = 0;
    }

    private void updatePrices() {
        runesCost = GrandExchangePrices.get(createdRuneTypeID).high;
        essenceCost = (essenceTypeID != ItemID.DAEYALT_ESSENCE) ?
                GrandExchangePrices.get(essenceTypeID).high : 0;
        talismanCost = GrandExchangePrices.get(talismanID).high;
        duelRingCost = GrandExchangePrices.get(ItemID.RING_OF_DUELING8).high;
        materialRuneCost = GrandExchangePrices.get(materialRuneID).high;
        necklaceCost = GrandExchangePrices.get(ItemID.BINDING_NECKLACE).high;
        staminaPotCost = GrandExchangePrices.get(ItemID.STAMINA_POTION4).high;
        log.info("Item prices set to at - Crafted Runes: {}gp, Essence: {}gp, Talisman: {}gp, " +
                        "Ring of Dueling {}gp, Material Runes: {}gp, Binding Necklace: {}gp, Stamina Potion (4): {}gp",
                runesCost, essenceCost, talismanCost, duelRingCost, materialRuneCost, necklaceCost, staminaPotCost);
    }

    private int itemTotals(int itemID, int beforeAmount, boolean stackableItem) {
        int currentAmount = inventory.getItemCount(itemID, stackableItem);
        return (beforeAmount > currentAmount) ? beforeAmount - currentAmount : 0;
    }

    private void updateTotals() {
        totalEssence += itemTotals(essenceTypeID, beforeEssence, false);
        beforeEssence = inventory.getItemCount(essenceTypeID, false);

        totalMaterialRunes += itemTotals(materialRuneID, beforeMaterialRunes, true);
        beforeMaterialRunes = inventory.getItemCount(materialRuneID, true);

        totalTalisman += itemTotals(talismanID, beforeTalisman, true);
        beforeTalisman = inventory.getItemCount(talismanID, true);

        currentCraftedRunes = inventory.getItemCount(createdRuneTypeID, true);
        if (beforeCraftedRunes < currentCraftedRunes) {
            totalCraftedRunes += currentCraftedRunes;
        }
        beforeCraftedRunes = currentCraftedRunes;

        if (!playerUtils.isItemEquipped(DUEL_RINGS) || playerUtils.isItemEquipped(Set.of(ItemID.RING_OF_DUELING1))) {
            totalDuelRings++;
        }

        if (config.bindingNecklace() && !outOfNecklaces && !playerUtils.isItemEquipped(BINDING_NECKLACE)) {
            totalNecklaces++;
        }
    }

    public void updateStats() {
        updateTotals();
        runesPH = (int) getPerHour(totalCraftedRunes);
        totalProfit = (int) ((totalCraftedRunes * runesCost) - ((totalEssence * essenceCost) + (totalMaterialRunes * materialRuneCost) +
                (totalTalisman * talismanCost) + (totalDuelRings * duelRingCost) + (totalNecklaces * necklaceCost) +
                ((totalStaminaPots * 0.25) * staminaPotCost)));
        profitPH = (int) getPerHour(totalProfit);
    }

    public long getPerHour(int quantity) {
        Duration timeSinceStart = Duration.between(botTimer, Instant.now());
        if (!timeSinceStart.isZero()) {
            return (int) ((double) quantity * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
        }
        return 0;
    }

    private long sleepDelay() {
        sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return sleepLength;
    }

    private int tickDelay() {
        tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        log.info("tick delay for {} ticks", tickLength);
        return tickLength;
    }

    private void teleportRingOfDueling(int menuIdentifier) {
        targetMenu = new LegacyMenuEntry("", "", menuIdentifier, MenuAction.CC_OP.getId(), -1,
                25362456, false);
        Widget ringWidget = client.getWidget(WidgetInfo.EQUIPMENT_RING);
        if (ringWidget != null) {
            menu.setEntry(targetMenu);
            mouse.delayMouseClick(ringWidget.getBounds(), sleepDelay());
        } else {
            menu.setEntry(targetMenu);
            mouse.delayMouseClick(new Point(0, 0), sleepDelay());
        }
    }

    private iCombinationRunecrafterState getItemState(Set<Integer> itemIDs) {
        if (inventory.containsItem(itemIDs)) {
            useableItem = inventory.getWidgetItem(itemIDs);
            return ACTION_ITEM;
        }
        if (bank.containsAnyOf(itemIDs)) {
            bankItem = bank.getBankItemWidgetAnyOf(itemIDs);
            return WITHDRAW_ITEM;
        }
        return OUT_OF_ITEM;
    }

    private boolean shouldSipStamina() {
        return (config.staminaPotion() && client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0) &&
                (client.getEnergy() <= (75 - calc.getRandomIntBetweenRange(0, 40)) ||
                        (inventory.containsItem(STAMINA_POTIONS) && client.getEnergy() < 75));
    }

    private iCombinationRunecrafterState getRequiredItemState() {
        if ((!inventory.containsItem(talismanID) && !bank.contains(talismanID, 1)) ||
                (!inventory.containsItem(materialRuneID) && !bank.contains(materialRuneID, 26)) ||
                (!inventory.containsItem(essenceTypeID) && !bank.contains(essenceTypeID, 10))) {
            bankItem = null;
            return OUT_OF_ITEM;
        }
        for (int itemID : REQUIRED_ITEMS) {
            if (!inventory.containsItem(itemID)) {
                bankItem = bank.getBankItemWidget(itemID);
                return (itemID == talismanID) ? WITHDRAW_ITEM : WITHDRAW_ALL_ITEM;
            }
        }
        return OUT_OF_ITEM;
    }

    private iCombinationRunecrafterState getState() {
        if (timeout > 0) {
            playerUtils.handleRun(20, 30);
            return TIMEOUT;
        }
        if (iterating) {
            return ITERATING;
        }
        if (playerUtils.isMoving(beforeLoc) || player.getAnimation() == 714) //teleport animation
        {
            playerUtils.handleRun(20, 30);
            return MOVING;
        }
        if (!playerUtils.isItemEquipped(TIARAS)) {
            utils.sendGameMessage("Fire Tiara not equipped. Stopping.");
            return OUT_OF_ITEM;
        }
        if (chinBreakHandler.shouldBreak(this)) {
            return HANDLE_BREAK;
        }

        mysteriousRuins = object.findNearestGameObject(34817); //Mysterious Ruins
        fireAltar = object.findNearestGameObject(ObjectID.ALTAR_34764);
        bankChest = object.findNearestGameObject(ObjectID.BANK_CHEST_4483);

        if (mysteriousRuins != null) {
            if (inventory.containsAllOf(REQUIRED_ITEMS)) {
                return ENTER_MYSTERIOUS_RUINS;
            } else {
                return (playerUtils.isItemEquipped(DUEL_RINGS) || playerUtils.isItemEquipped(Set.of(ItemID.RING_OF_DUELING1))) ?
                        TELEPORT_CASTLE_WARS : OUT_OF_ITEM;
            }
        }
        if (fireAltar != null) {
            if (inventory.containsAllOf(REQUIRED_ITEMS)) {
                return USE_FIRE_ALTAR;
            } else {
                return (playerUtils.isItemEquipped(DUEL_RINGS) || playerUtils.isItemEquipped(Set.of(ItemID.RING_OF_DUELING1))) ?
                        TELEPORT_CASTLE_WARS : OUT_OF_ITEM;
            }
        }
        if (bankChest != null) {
            if (!bank.isOpen()) {
                updateStats();
                return OPEN_BANK;
            }
            if (bank.isOpen()) {
                if (inventory.containsAllOf(REQUIRED_ITEMS) && playerUtils.isItemEquipped(DUEL_RINGS)) {
                    updateStats();
                    return TELEPORT_DUEL_ARENA;
                }
                if (inventory.isFull()) {
                    return DEPOSIT_ALL;
                }
                if (!playerUtils.isItemEquipped(DUEL_RINGS)) {
                    return getItemState(DUEL_RINGS);
                }
                if (config.bindingNecklace() && !playerUtils.isItemEquipped(BINDING_NECKLACE)) {
                    necklaceState = getItemState(BINDING_NECKLACE);
                    if (!(necklaceState == OUT_OF_ITEM && !config.stopNecklace())) {
                        return necklaceState;
                    } else {
                        outOfNecklaces = true;
                    }
                }
                if (shouldSipStamina()) {
                    staminaState = getItemState(STAMINA_POTIONS);
                    if (!(staminaState == OUT_OF_ITEM && !config.stopStamina())) {
                        return staminaState;
                    } else {
                        outOfStaminaPots = true;
                    }
                }
                if (inventory.containsExcept(REQUIRED_ITEMS)) {
                    return DEPOSIT_ALL_EXCEPT;
                }
                return getRequiredItemState();
            }
        }
        return OUT_OF_AREA;
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startBot || chinBreakHandler.isBreakActive(this)) {
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            if (!client.isResized()) {
                utils.sendGameMessage("illu - client must be set to resizable");
                startBot = false;
                return;
            }
            state = getState();
            log.debug(state.name());
            switch (state) {
                case TIMEOUT:
                    timeout--;
                    break;
                case ITERATING:
                    break;
                case MOVING:
                    timeout = tickDelay();
                    break;
                case ENTER_MYSTERIOUS_RUINS:
                    utils.doGameObjectActionMsTime(mysteriousRuins, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
                    timeout = tickDelay();
                    break;
                case TELEPORT_CASTLE_WARS:
                    teleportRingOfDueling(3);
                    timeout = tickDelay();
                    break;
                case USE_FIRE_ALTAR:
                    WidgetItem airTalisman = inventory.getWidgetItem(talismanID);
                    if (airTalisman != null) {
                        targetMenu = new LegacyMenuEntry("", "", fireAltar.getId(), ITEM_USE_ON_GAME_OBJECT.getId(),
                                fireAltar.getSceneMinLocation().getX(), fireAltar.getSceneMinLocation().getY(), false);
                        utils.doModifiedActionMsTime(targetMenu, airTalisman.getId(), airTalisman.getIndex(), ITEM_USE_ON_GAME_OBJECT.getId(), fireAltar.getConvexHull().getBounds(), sleepDelay());
                        timeout = tickDelay();
                    }
                    break;
                case OPEN_BANK:
                    utils.doGameObjectActionMsTime(bankChest, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
                    timeout = tickDelay();
                    break;
                case TELEPORT_DUEL_ARENA:
                    teleportRingOfDueling(2);
                    timeout = tickDelay();
                    break;
                case DEPOSIT_ALL:
                    bank.depositAll();
                    break;
                case DEPOSIT_ALL_EXCEPT:
                    bank.depositAllExcept(REQUIRED_ITEMS);
                    break;
                case ACTION_ITEM:
                    if (useableItem != null) {
                        if (STAMINA_POTIONS.contains(useableItem.getId())) {
                            totalStaminaPots++;
                        }
                        LegacyMenuEntry targetMenu = new LegacyMenuEntry("", "", 9, MenuAction.CC_OP_LOW_PRIORITY.getId(),
                                useableItem.getIndex(), 983043, true);
                        utils.doActionMsTime(targetMenu, new Point(0, 0), sleepDelay());
                    }
                    break;
                case WITHDRAW_ITEM:
                    bank.withdrawItem(bankItem);
                    break;
                case WITHDRAW_ALL_ITEM:
                    bank.withdrawAllItem(bankItem);
                    break;
                case HANDLE_BREAK:
                    chinBreakHandler.startBreak(this);
                    setTalisman = false;
                    timeout = 10;
                    break;
                case OUT_OF_ITEM:
                    utils.sendGameMessage("Out of required items. Stopping.");
                    if (config.logout()) {
                        interfaceUtils.logout();
                    }
                    startBot = false;
                    resetVals();
                    break;
            }
            beforeLoc = player.getLocalLocation();
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (!startBot) {
            return;
        }
        if (event.getGameState() == GameState.LOGGED_IN) {
            setTalisman = false;
            state = TIMEOUT;
            timeout = 2;
        }
    }
}
