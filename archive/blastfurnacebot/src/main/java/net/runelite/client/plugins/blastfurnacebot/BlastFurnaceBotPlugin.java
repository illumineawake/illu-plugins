/*
 * Copyright (c) 2018, Seth <Sethtroll3@gmail.com>
 * Copyright (c) 2019, Brandon White <bmwqg@live.com>
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
package net.runelite.client.plugins.blastfurnacebot;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static net.runelite.api.NullObjectID.NULL_29330;
import static net.runelite.api.NullObjectID.NULL_9092;
import static net.runelite.api.ObjectID.*;
import static net.runelite.client.plugins.blastfurnacebot.BlastFurnaceState.*;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "Blast Furnace - Illumine",
        description = "Illumine bot for Blast Furnace minigame",
        tags = {"minigame", "skilling", "smithing", "illumine", "bot"},
        type = PluginType.MINIGAME
)
@Slf4j
public class BlastFurnaceBotPlugin extends Plugin {
    private static final int BAR_DISPENSER = NULL_9092;
    private static final int BF_COFFER = NULL_29330;
    private static final long COST_PER_HOUR = 72000;
    private static final String FOREMAN_PERMISSION_TEXT = "Okay, you can use the furnace for ten minutes. Remember, you only need half as much coal as with a regular furnace.";
    List<Integer> inventorySetup = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private GameObject conveyorBelt;

    @Getter(AccessLevel.PACKAGE)
    private GameObject barDispenser;

    private ForemanTimer foremanTimer;

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BotOverlay overlay;

    @Inject
    private ProfitOverlay profitOverlay;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private BlastFurnaceBotConfig config;

    @Inject
    private iUtils utils;

    BlastFurnaceState state;
    MenuEntry targetMenu;
    LocalPoint beforeLoc = new LocalPoint(0, 0); //initiate to mitigate npe, this sucks
    Instant botTimer;
    Bars bar;

    int cofferRefill;
    int cofferMinValue;
    int tickDelay;
    int barPrice;
    int orePrice;
    int coalPrice;
    int staminaPotPrice;
    int previousAmount = 0;
    long barsPerHour = 0;
    long barsAmount = 0;
    long profit = 0;
    private int timeout = 0;
    private boolean coalBagFull;

    @Override
    protected void startUp() {
        overlayManager.add(overlay);
        overlayManager.add(profitOverlay);
        coalBagFull = false;
        timeout = 0;
        targetMenu = null;
        botTimer = Instant.now();
        bar = config.getBar();
        initInventory();
        cofferMinValue = config.cofferThreshold();
        cofferRefill = config.cofferAmount();
        tickDelay = config.delayAmount();
        getItemPrices();
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        overlayManager.remove(profitOverlay);
        conveyorBelt = null;
        barDispenser = null;
        foremanTimer = null;
        botTimer = null;
        barsAmount = 0;
        previousAmount = 0;
        barsPerHour = 0;
        profit = 0;
    }

    @Subscribe
    private void onConfigChange(ConfigChanged event) {
        if (event.getGroup().equals("blastfurnacebot")) {
            switch (event.getKey()) {
                case "cofferThreshold":
                    cofferMinValue = config.cofferThreshold();
                    log.info("Minimum coffer value updated to: " + cofferMinValue);
                    break;
                case "cofferAmount":
                    cofferRefill = config.cofferAmount();
                    log.info("Coffer refill value updated to: " + cofferRefill);
                    break;
                case "delayAmount":
                    tickDelay = config.delayAmount();
                    log.info("tick delay value updated to: " + tickDelay);
                    break;
                case "bar":
                    bar = config.getBar();
                    getItemPrices();
                    log.info("Bar configured to: " + bar.name());
                    barsAmount = 0;
                    previousAmount = 0;
                    barsPerHour = 0;
                    profit = 0;
                    initInventory();
                    botTimer = Instant.now();
                    break;
            }
        }
    }

    private void initInventory() {
        inventorySetup.clear();
        inventorySetup = (bar.getMinCoalAmount() == 0) ?
                List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4) :
                List.of(ItemID.COAL_BAG_12019, ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4);
        log.info("required inventory items: {}", inventorySetup.toString());
    }

    private void getItemPrices() {
        barPrice = utils.getOSBItem(bar.getItemID()).getSell_average();
        orePrice = utils.getOSBItem(bar.getOreID()).getBuy_average();
        coalPrice = utils.getOSBItem(Ores.COAL.getOreID()).getBuy_average();
        staminaPotPrice = utils.getOSBItem(ItemID.STAMINA_POTION4).getBuy_average();

        log.info("{} price: {}, Ore price: {}, Coal price: {}, stamina pot price: {}", bar.name(), barPrice, orePrice, coalPrice, staminaPotPrice);
    }

    public void barsMade() {
        int amount = client.getVar(bar.getVarbit());
        if (amount != previousAmount) {
            previousAmount = amount;
            barsAmount += amount;
        }
    }

    public long profitPerHour() {
        int foremanMultiplier = (client.getRealSkillLevel(Skill.SMITHING) < 60) ? 1 : 0;
        switch (bar.name()) {
            case "IRON_BAR":
            case "SILVER_BAR":
            case "GOLD_BAR":
                return (barsPerHour * barPrice) - ((barsPerHour * orePrice) + (9 * staminaPotPrice) + COST_PER_HOUR + (foremanMultiplier * 60000));
            case "STEEL_BAR":
                return (barsPerHour * barPrice) - ((barsPerHour * orePrice) + (barsPerHour * coalPrice) + (9 * staminaPotPrice) + COST_PER_HOUR + (foremanMultiplier * 60000));
            case "MITHRIL_BAR":
                return (barsPerHour * barPrice) - ((barsPerHour * orePrice) + ((barsPerHour * 2) * coalPrice) + (9 * staminaPotPrice) + COST_PER_HOUR + (foremanMultiplier * 60000));
            case "ADAMANTITE_BAR":
                return (barsPerHour * barPrice) - ((barsPerHour * orePrice) + ((barsPerHour * 3) * coalPrice) + (9 * staminaPotPrice) + COST_PER_HOUR);
            case "RUNITE_BAR":
                return (barsPerHour * barPrice) - ((barsPerHour * orePrice) + ((barsPerHour * 4) * coalPrice) + (9 * staminaPotPrice) + COST_PER_HOUR);
        }
        return 0;
    }

    public long getBarsPH() {
        Duration duration = Duration.between(botTimer, Instant.now());
        return barsAmount * (3600000 / duration.toMillis());
    }

    private void updateCalc() {
        barsMade();
        barsPerHour = getBarsPH();
        profit = profitPerHour();
    }

    private void openBank() {
        GameObject bankObject = object.findNearestGameObject(26707);
        if (bankObject != null) {
            targetMenu = new LegacyMenuEntry("", "", bankObject.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), bankObject.getSceneMinLocation().getX(), bankObject.getSceneMinLocation().getY(), true);
            mouse.clickRandomPointCenter(-100, 100);
            timeout = calc.getRandomIntBetweenRange(1, tickDelay);
        }
    }

    private void putConveyorBelt() {
        targetMenu = new LegacyMenuEntry("", "", conveyorBelt.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), conveyorBelt.getSceneMinLocation().getX(), conveyorBelt.getSceneMinLocation().getY(), false);
        utils.sleep(10, 100);
        mouse.clickRandomPointCenter(-100, 100);
        timeout = calc.getRandomIntBetweenRange(1, tickDelay);
    }

    private void collectFurnace() {
        log.info("At collectFurnace(), collecting bars");
        targetMenu = (client.getVar(Varbits.BAR_DISPENSER) == 1) ?
                new LegacyMenuEntry("", "", 0, MenuOpcode.WALK.getId(), barDispenser.getSceneMinLocation().getX(), barDispenser.getSceneMinLocation().getY(), false)
                : new LegacyMenuEntry("", "", barDispenser.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), barDispenser.getSceneMinLocation().getX(), barDispenser.getSceneMinLocation().getY(), false);

        mouse.clickRandomPointCenter(-100, 100);
        timeout = calc.getRandomIntBetweenRange(1, tickDelay);
    }

    private void fillCoalBag(WidgetItem coalBag) {
        targetMenu = new LegacyMenuEntry("", "", coalBag.getId(), MenuOpcode.ITEM_FIRST_OPTION.getId(), coalBag.getIndex(), 9764864, false);
        utils.sleep(25, 100);
        mouse.clickRandomPointCenter(-100, 100);
    }

    private void emptyCoalBag(WidgetItem coalBag) {
        targetMenu = new LegacyMenuEntry("", "", coalBag.getId(), MenuOpcode.ITEM_FOURTH_OPTION.getId(), coalBag.getIndex(), 9764864, false);
        utils.sleep(25, 100);
        mouse.clickRandomPointCenter(-100, 100);
    }

    private BlastFurnaceState collectBars() {
        if (utils.getInventorySpace() < 26) {
            log.info("collect bars but need inventory space first");
            openBank();
            return OPENING_BANK;
        }
        collectFurnace();
        return COLLECTING_BARS;
    }

    private boolean shouldCheckForemanFee() {
        return client.getRealSkillLevel(Skill.SMITHING) < 60
                && (foremanTimer == null || Duration.between(Instant.now(), foremanTimer.getEndTime()).toSeconds() <= 30);
    }

    private void setForemanTime(Widget npcDialog) {
        String npcText = Text.sanitizeMultilineText(npcDialog.getText());

        if (npcText.equals(FOREMAN_PERMISSION_TEXT)) {
            foremanTimer = new ForemanTimer(this, itemManager);
        }
    }

    private BlastFurnaceState getState() {
        if (conveyorBelt == null || barDispenser == null) {
            conveyorBelt = object.findNearestGameObject(CONVEYOR_BELT);
            barDispenser = object.findNearestGameObject(BAR_DISPENSER);
            if (conveyorBelt == null || barDispenser == null) {
                return OUT_OF_AREA;
            }
        }
        if (state == OUT_OF_ITEMS) {
            utils.sendGameMessage("Out of of materials, log off!!!");
            return OUT_OF_AREA;
        }
        if (timeout > 0) {
            playerUtils.handleRun(10, 30);
            return TIMEOUT;
        }
        if (playerUtils.isMoving(beforeLoc)) {
            timeout = calc.getRandomIntBetweenRange(1, tickDelay);
            return MOVING;
        }
        if (!bank.isBankOpen()) {
            if (shouldCheckForemanFee()) {
                if (!inventory.inventoryContains(ItemID.COINS_995, 2500)) {
                    openBank();
                    return OPENING_BANK;
                }
                Widget payDialog = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTION1);
                if (payDialog != null) {
                    targetMenu = new LegacyMenuEntry("", "", 0, MenuOpcode.WIDGET_TYPE_6.getId(), 1, 14352385, false);
                    mouse.clickRandomPointCenter(-100, 100);
                    return PAY_FOREMAN;
                }
                Widget npcDialog = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
                if (npcDialog != null) {
                    setForemanTime(npcDialog);
                    return PAY_FOREMAN;
                }
                NPC foreman = utils.findNearestNpc(2923);
                if (foreman != null) {
                    targetMenu = new LegacyMenuEntry("", "", foreman.getIndex(), MenuOpcode.NPC_THIRD_OPTION.getId(), 0, 0, false);
                    mouse.clickRandomPointCenter(-100, 100);
                    return PAY_FOREMAN;
                }
            }
            if (!client.getWidget(162, 40).isHidden()) //deposit amount for coffer widget
            {
                if (!inventory.inventoryContains(ItemID.COINS_995, cofferRefill)) {
                    openBank();
                    return OPENING_BANK;
                }
                int randDepositAmount = calc.getRandomIntBetweenRange(cofferRefill, 10000);
                int depositAmount = (inventory.inventoryContains(ItemID.COINS_995, randDepositAmount)) ? randDepositAmount : cofferRefill;

                utils.typeString(String.valueOf(depositAmount));
                utils.sleep(50, 100);
                utils.pressKey(KeyEvent.VK_ENTER);
                utils.sleep(200, 350);
                return FILL_COFFER;
            }
            playerUtils.handleRun(20, 20);
            if (inventory.inventoryContains(bar.getItemID())) //TODO: update iutils to take a String contains, so can search if inventory has any Bars
            { //INVENTORY CONTAINS BARS
                openBank();
                return OPENING_BANK;
            }
            if (client.getVar(Varbits.BAR_DISPENSER) > 0) //BARS IN FURNACE
            {
                if (utils.getInventorySpace() < 26) {
                    openBank();
                    return OPENING_BANK;
                }
                collectFurnace();
                return COLLECTING_BARS;
            }
            if (client.getVar(Varbits.BLAST_FURNACE_COFFER) < cofferMinValue) {
                if (inventory.inventoryContains(ItemID.COINS_995, cofferRefill)) {
                    //TODO handle filling up coffer
                    GameObject coffer = object.findNearestGameObject(BF_COFFER);
                    if (coffer != null) {
                        targetMenu = new LegacyMenuEntry("", "", coffer.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), coffer.getSceneMinLocation().getX(), coffer.getSceneMinLocation().getY(), false);
                        utils.sleep(50, 250);
                        mouse.clickRandomPointCenter(-100, 100);
                        timeout = calc.getRandomIntBetweenRange(1, tickDelay);
                    } else {
                        utils.sendGameMessage("Coffer is null, wrong world?");
                    }
                    return FILL_COFFER;
                } else {
                    openBank();
                    return OPENING_BANK;
                }
            }
            GameObject bank = object.findNearestGameObject(BANK_CHEST_26707);
            if (bank != null) {
                WidgetItem coalBag = inventory.getInventoryWidgetItem(ItemID.COAL_BAG_12019);
                if (client.getLocalPlayer().getWorldLocation().distanceTo(bank.getWorldLocation()) < 8) //At bank location
                {
                    if (utils.getItems(List.of(ItemID.COAL, bar.getOreID())).isEmpty()) //Inventory does not contain coal or ore
                    {
                        openBank();
                        return OPENING_BANK;
                    }
                    if (coalBag != null) {
                        if (!coalBagFull) {
                            if (inventory.inventoryContains(ItemID.COAL)) {
                                fillCoalBag(coalBag);
                            }
                            if (inventory.inventoryContains(bar.getOreID())) //shouldn't happen
                            {
                                putConveyorBelt();
                                return PUT_CONVEYOR_BELT;
                            }
                        }
                        if (coalBagFull) {
                            if (!utils.getItems(List.of(ItemID.COAL, bar.getOreID())).isEmpty()) {
                                putConveyorBelt();
                                return PUT_CONVEYOR_BELT;
                            }
                        }
                    } //TODO handle not having a coal bag
                } else //Not near bank chest, assume near conveyor belt
                {
                    if (utils.getItems(List.of(ItemID.COAL, bar.getOreID())).isEmpty()) {
                        if (!coalBagFull || coalBag == null) {
                            utils.sleep(60, 250);
                            if (client.getVar(Varbits.BAR_DISPENSER) > 0) {
                                collectFurnace();
                                return COLLECTING_BARS;
                            } else {
                                openBank();
                                return OPENING_BANK;
                            }
                        }
                        if (coalBagFull && coalBag != null) {
                            emptyCoalBag(coalBag);
                        }
                    }
                    if (!utils.getItems(List.of(ItemID.COAL, bar.getOreID())).isEmpty()) {
                        putConveyorBelt();
                        if (!coalBagFull || coalBag == null) {
                            timeout = calc.getRandomIntBetweenRange(1, tickDelay);
                            return PUT_CONVEYOR_BELT;
                        }
                    }
                }
            }
        } else if (bank.isBankOpen()) //redundant but doing for readability
        {
            WidgetItem inventoryBar = inventory.getInventoryWidgetItem(bar.getItemID());
            if (inventoryBar != null) {
                log.info("depositing bars");
                bank.depositAllExcept(inventorySetup);
                return DEPOSITING;
            }
            if (client.getVar(Varbits.BAR_DISPENSER) > 0) //Bars in dispenser
            {
                log.info("bars ready for collection, bank is open, depositing inventory and collecting");
                if (utils.getInventorySpace() < 26) //TODO: create inventoryContainsExcluding method in utils
                {
                    //bank.depositAllExcept(inventorySetup);
                    bank.depositAll();
                    return DEPOSITING;
                }
                return collectBars();
            }
            if ((client.getVar(Varbits.BLAST_FURNACE_COFFER) < cofferMinValue) || shouldCheckForemanFee()) {
                if (inventory.inventoryContains(ItemID.COINS_995, cofferRefill)) {
                    utils.closeBank();
                    return FILL_COFFER;
                }
                if (inventory.inventoryFull() && !inventory.inventoryContains(ItemID.COINS_995)) {
                    log.info("Depositing inventory to make room for coins");
                    bank.depositAllExcept(inventorySetup);
                    return DEPOSITING;
                }
                if (bank.bankContains(ItemID.COINS_995, cofferRefill)) {
                    Widget bankCoins = bank.getBankItemWidget(ItemID.COINS_995);
                    bank.withdrawAllItem(bankCoins);
                    return FILL_COFFER;
                } else {
                    utils.sendGameMessage("Out of coins, required: " + cofferRefill);
                    utils.closeBank();
                    utils.sendGameMessage("Log Off.");
                    return OUT_OF_ITEMS;
                }
            }
            Widget staminaPotionBank = bank.getBankItemWidgetAnyOf(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4);
            if (staminaPotionBank != null && utils.getItems(List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4)).isEmpty()) {
                bank.depositAllExcept(inventorySetup);
                log.info("withdrawing stam pot");
                bank.withdrawItem(staminaPotionBank);
                return WITHDRAWING;
            }
            if (!inventory.inventoryContains(ItemID.COAL_BAG_12019) && inventorySetup.contains(ItemID.COAL_BAG_12019)) {
                Widget coalBagBank = bank.getBankItemWidget(ItemID.COAL_BAG_12019);
                if (coalBagBank != null) {
                    bank.depositAllExcept(inventorySetup);
                    log.info("withdrawing coal bag");
                    bank.withdrawItem(coalBagBank);
                    return WITHDRAWING;
                } else {
                    utils.sendGameMessage("We don't have a coal bag!");
                    utils.closeBank();
                    utils.sendGameMessage("Log Off.");
                    return OUT_OF_ITEMS;
                }
            }
            if ((client.getVar(Ores.COAL.getVarbit()) <= bar.getMinCoalAmount() || !coalBagFull) && client.getVar(Ores.COAL.getVarbit()) < 220 && bar.getMinCoalAmount() != 0) {
                if (inventory.inventoryContains(ItemID.COAL)) {
                    if (!coalBagFull) {
                        utils.closeBank(); //filling handled in bank not open logic
                        return FILL_COAL_BAG;
                    } else {
                        putConveyorBelt();
                        return PUT_CONVEYOR_BELT;
                    }
                }
                if (inventory.inventoryFull()) //TODO: actually handle this properly
                {
                    bank.depositAllExcept(inventorySetup);
                    utils.sendGameMessage("inventory is full but need to withdraw coal");
                    return OUT_OF_ITEMS;
                }
                Widget coalBank = bank.getBankItemWidget(ItemID.COAL);
                if (coalBank != null) {
                    bank.depositAllExcept(inventorySetup);
                    log.info("withdrawing coal");
                    bank.withdrawAllItem(coalBank);
                    log.info("sleeping");
                    return WITHDRAWING; //This might be the wrong return
                } else {
                    utils.sendGameMessage("out of coal, log off.");
                    return OUT_OF_ITEMS;
                }
            }
            if (client.getVar(Ores.COAL.getVarbit()) > bar.getMinCoalAmount() || bar.getMinCoalAmount() == 0) //logic probably needs updating
            {
                if (inventory.inventoryFull() || inventory.inventoryContains(bar.getOreID())) {
                    if (inventory.inventoryContains(bar.getOreID())) {
                        log.info("putting Ore onto belt");
                        putConveyorBelt();
                        return PUT_CONVEYOR_BELT;
                    } else {
                        bank.depositAllExcept(inventorySetup);
                        utils.sendGameMessage("need to withdraw Ore but inventory is full, something went wrong.");
                        return OUT_OF_ITEMS;
                    }
                }
                Widget bankOre = bank.getBankItemWidget(bar.getOreID());
                if (bankOre != null) {
                    bank.depositAllExcept(inventorySetup);
                    log.info("withdrawing ore");
                    bank.withdrawAllItem(bankOre);
                    timeout = calc.getRandomIntBetweenRange(1, tickDelay);
                    return WITHDRAWING;
                } else {
                    utils.sendGameMessage("Out of ore, log off");
                    return OUT_OF_ITEMS;
                }
            }
        }
        return null;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (targetMenu == null) {
            log.info("Modified MenuEntry is null");
            return;
        }
        //TODO: build this into utils or use random handler getter?
        if (utils.getRandomEvent()) //for random events
        {
            log.info("Blast furnace bot not overriding click due to random event");
            return;
        }
        if (targetMenu.getIdentifier() == ItemID.COAL_BAG_12019) {
            if (targetMenu.getOpcode() == MenuOpcode.ITEM_FIRST_OPTION.getId()) {
                coalBagFull = true;
            }
            if (targetMenu.getOpcode() == MenuOpcode.ITEM_FOURTH_OPTION.getId()) {
                coalBagFull = false;
            }
        }
        log.info("inserting menu at MOC event: " + targetMenu.toString());
        event.setMenuEntry(targetMenu);
        timeout = calc.getRandomIntBetweenRange(1, tickDelay);
        targetMenu = null; //this allow the player to interact with the client without their clicks being overridden
    }

    @Provides
    BlastFurnaceBotConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BlastFurnaceBotConfig.class);
    }

    @Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject gameObject = event.getGameObject();

        switch (gameObject.getId()) {
            case CONVEYOR_BELT:
                conveyorBelt = gameObject;
                break;

            case BAR_DISPENSER:
                barDispenser = gameObject;
                break;
        }
    }

    @Subscribe
    private void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject gameObject = event.getGameObject();

        switch (gameObject.getId()) {
            case CONVEYOR_BELT:
                conveyorBelt = null;
                break;

            case BAR_DISPENSER:
                barDispenser = null;
                break;
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOADING) {
            conveyorBelt = null;
            barDispenser = null;
        }
    }

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() != WidgetID.MULTISKILL_MENU_GROUP_ID && event.getGroupId() != WidgetID.CHATBOX_GROUP_ID && event.getGroupId() != WidgetID.DIALOG_OPTION_GROUP_ID) {
            return;
        }
        //Collect Bars
        if (event.getGroupId() == WidgetID.MULTISKILL_MENU_GROUP_ID) {
            targetMenu = new LegacyMenuEntry("", "", 1, 57, -1, 17694734, false); //Take Bar from Bar Dispenser
            mouse.clickRandomPointCenter(-100, 100);
            return;
        }
        //Deposit coins widget option
        if (event.getGroupId() == WidgetID.DIALOG_OPTION_GROUP_ID) {
            targetMenu = new LegacyMenuEntry("", "", 0, 30, 1, 14352385, false); //Take Bar from Bar Dispenser
            mouse.clickRandomPointCenter(-100, 100);
            return;
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (client != null && client.getLocalPlayer() != null && client.getGameState() == GameState.LOGGED_IN) {
            updateCalc();
            if (!iterating) {
                state = getState();
                beforeLoc = client.getLocalPlayer().getLocalLocation();
                if (state != null) {
                    log.info(state.name());
                    switch (state) {
                        case TIMEOUT:
                            timeout--;
                            return;
                    }
                } else {
                    log.info("state is null");
                }
            } else {
                log.info("utils is iterating");
            }
        }
    }
}
