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
package net.runelite.client.plugins.lewdblast;

import com.google.inject.Provides;

import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.DelayQueue;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.database.data.Keys;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.WorldLocation;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.*;

import static net.runelite.api.ObjectID.BAR_DISPENSER;
import static net.runelite.api.ObjectID.CONVEYOR_BELT;
import static net.runelite.client.plugins.iutils.iUtils.iterating;
import org.pf4j.Extension;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "Lewd Blastfurnace",
	enabledByDefault = false,
	description = "Illumine - auto eat food and drink some potions below configured values",
	tags = {"illumine", "auto", "bot", "imenud", "food", "potions", "stamina", "prayer"},
	type = PluginType.UTILITY
)
@Slf4j
public class lewdblastPlugin extends Plugin {

	private ForemanTimer foremanTimer;
	private static final String FOREMAN_PERMISSION_TEXT = "Okay, you can use the furnace for ten minutes. Remember, you only need half as much coal as with a regular furnace.";


	@Inject
	private Client client;

	@Inject
	private lewdblastConfiguration config;

	@Inject
	private iUtils utils;

	@Inject
	private MouseUtils mouse;

	@Inject
	private PlayerUtils playerUtils;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private CalculationUtils calc;

	@Inject
	private MenuUtils menu;

	@Inject
	private BankUtils bank;

	@Inject
	private ObjectUtils object;

	@Inject
	private KeyboardUtils keyboard;

	@Inject
	private WalkUtils walk;

	@Inject
	private NPCUtils npc;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	MenuEntry targetMenu;
	Player player;

	private int quantity;
	private int timeout;

	private boolean coalOnBelt = false;
	private boolean foremanPaid = false;
	private boolean ressuply = false;
	private boolean pricesSet = true;

	List<Integer> REQUIRED_ITEMS = new ArrayList<>();
	Set<Integer> STAMINA_POTIONS = Set.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4);

	public final WorldPoint dispenserTile = new WorldPoint(1939, 4963, 0);

	WorldArea doorArea3 = new WorldArea(new WorldPoint(2928,10195,0),new WorldPoint(2932,10197,0));
	WorldArea doorArea1 = new WorldArea(new WorldPoint(2928,10186,0),new WorldPoint(2932,10189,0));

	WorldArea geArea = new WorldArea(new WorldPoint(3144,3468,0),new WorldPoint(3186,3508,0));



	@Provides
	lewdblastConfiguration provideConfig(ConfigManager configManager) {
		return configManager.getConfig(lewdblastConfiguration.class);
	}

	@Override
	protected void startUp() {
		utils.sendGameMessage("START");

		REQUIRED_ITEMS = List.of(ItemID.COAL_BAG_12019,ItemID.IRON_ORE) ;
	}

	@Override
	protected void shutDown() {

	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event) {

	}

	@Subscribe
	private void onGameTick(GameTick event) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		if (config.startBot() && client != null && client.getLocalPlayer() != null && client.getGameState() == GameState.LOGGED_IN) {

			player = client.getLocalPlayer();

			Widget barCollectWid = client.getWidget(270,14);
			Widget continueWid = client.getWidget(233,3);
			Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);

			Widget grandExchange = client.getWidget(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER);
			Widget offer4 = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER4);
			Widget offer1 = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER4);
			Widget collectWid = client.getWidget(465,6);

			GameObject bankObject = object.findNearestGameObject(26707);



			if(timeout > 0){
				timeout--;
			}
			else if(iterating){
				log.debug("iterating");
			}
			else if(player.getIdlePoseAnimation() != player.getPoseAnimation() && client.getVar(Varbits.BLAST_FURNACE_IRON_BAR)==0){
				utils.sendGameMessage("Walking");
				if(!playerUtils.isRunEnabled() && client.getEnergy()>20 && !bank.isOpen()){
					playerUtils.enableRun(runOrb.getBounds());
				}
			}
			else if(ressuply){
				if(inventory.containsAllOf(List.of(441,454,12626)) && (grandExchange == null || offer4.getChild(16).getText().contains("Empty"))){
					WallObject doorObject1 = object.findNearestWallObject(6977);
					WallObject doorObject2 = object.findNearestWallObject(6102);
					WallObject doorObject3 = object.findNearestWallObject(6975);
					GameObject stairObject = object.findNearestGameObject(9084);

					if(grandExchange != null ) {
						utils.sendGameMessage("Closing GE");
						targetMenu = new MenuEntry("Close", "", 1, MenuOpcode.CC_OP.getId(), 11, 30474242, false);
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100, 0);
					}
					else if(inventory.containsItem(ItemID.RING_OF_WEALTH_5)){
						utils.sendGameMessage("Wearing ring of wealth");
						targetMenu = new MenuEntry("Wear", "Wear", 11980, MenuOpcode.ITEM_SECOND_OPTION.getId(), inventory.getWidgetItem(ItemID.RING_OF_WEALTH_5).getIndex(), 9764864, false);
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100,0);
					}
					else if(client.getVar(Varbits.QUEST_TAB) != 2){
						utils.sendGameMessage("Opening minigame tab");
						targetMenu = new MenuEntry("Minigame List", "", 1, 57, -1, 41222157, false);
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100,0);
					}
					else if(player.getWorldArea().intersectsWith(geArea)){
						utils.sendGameMessage("Teleporting blast furnace");
						targetMenu = new MenuEntry("Teleport to <col=ff8040>Blast Furnace</col>", "", 1, 57, 2, 4980762, false);
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100,0);
						timeout = 30;
					}
					else if(player.getWorldArea().intersectsWith(doorArea3)){
						utils.sendGameMessage("Clicking stairs");
						targetMenu = new MenuEntry("Climb-down", "<col=ffff>Stairs</col>", 9084, 3, stairObject.getSceneMinLocation().getX(), stairObject.getSceneMinLocation().getY(), true);
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100,0);
						timeout = 2;
						ressuply = false;
						pricesSet = false;
					}
					else if(player.getWorldArea().intersectsWith(doorArea1)){
						if( doorObject2 != null && doorObject2.getConfig() ==320) {
							utils.sendGameMessage("Clicking door 2 " );
							targetMenu = new MenuEntry("Open", doorObject2.toString(), doorObject2.getId(), 3, doorObject2.getLocalLocation().getSceneX(), doorObject2.getLocalLocation().getSceneY(), false);
						}
						else if(doorObject3 != null) {
							utils.sendGameMessage("Clicking door 3");
							targetMenu = new MenuEntry("Open", doorObject3.toString(), 6975, 3, doorObject3.getLocalLocation().getSceneX(), doorObject3.getLocalLocation().getSceneY(), false);
						}
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100, 0);
						timeout = 5;
					}
					else if(doorObject1 != null) {
						utils.sendGameMessage("Clicking door 1");
						targetMenu = new MenuEntry("Open", doorObject1.toString(), doorObject1.getId(), 3, doorObject1.getLocalLocation().getSceneX(), doorObject1.getLocalLocation().getSceneY(), false);
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100,0);
						timeout = 5;
					}
				}
				else if(bank.isOpen() && player.getWorldArea().intersectsWith(geArea)){
					if(client.getVar(Varbits.BANK_NOTE_FLAG) == 0){
						utils.sendGameMessage("Setting to note");
						targetMenu = new MenuEntry("Note", "", 1, MenuOpcode.CC_OP.getId(), -1, 786455, false);
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100,0);
					}
					else if(inventory.getEmptySlots() < 26){
						utils.sendGameMessage("Deposting items");
						bank.depositAll();
					}
					else if(!inventory.containsItem("Steel bar") && bank.contains(ItemID.STEEL_BAR,0)){
						utils.sendGameMessage("Withdrawing steel bar");
						bank.withdrawAllItem(ItemID.STEEL_BAR);
					}
					else if(!inventory.containsItem("Coins") && bank.contains(ItemID.COINS_995,0)){
						utils.sendGameMessage("Withdrawing coins");
						bank.withdrawAllItem(ItemID.COINS_995);
					}
					else if(inventory.containsItem(995) || inventory.containsItem(2354)){
						utils.sendGameMessage("Opening GE");
						openGE();
					}
				}
				else if(grandExchange!=null && !grandExchange.isHidden()){
					Widget ringWidget = client.getWidget(WidgetInfo.EQUIPMENT_RING);
					if(collectWid.getChild(1) != null && !collectWid.getChild(1).isHidden()){
						targetMenu = new MenuEntry("Collect to inventory", "", 1, MenuOpcode.CC_OP.getId(), 0, 30474246, false);
						menu.setEntry(targetMenu);
						mouse.delayClickRandomPointCenter(-100, 100,0);
					}
					else if(inventory.containsItem(2354)){
						sellItem("Steel bar",2);
					}
					else if(pricesSet && inventory.containsItem(ItemID.COINS_995) && offer1.getChild(16).getText().contains("Empty")){
						quantity = (int) (long) (((double)inventory.getItemCount(ItemID.COINS_995,true)/1600000) * 4300);
						utils.sendGameMessage(String.valueOf(quantity));
						pricesSet = false;
					}
					else if(!inventory.containsItem(ItemID.RING_OF_WEALTH_5)&& (ringWidget.getName() == "" || ringWidget.getName().equals("<col=ff9040>Ring of wealth</col>")) && offer4.getChild(16).getText().contains("Empty")){
						utils.sendGameMessage("Buying ring of wealth");
						buyItem("ring of wealth",3,2, 1);
					}
					else if(!inventory.containsItem(454) && offer4.getChild(16).getText().contains("Empty")){
						utils.sendGameMessage("Buying Coal");
						buyItem("coal",3,240, quantity);
					}
					else if(!inventory.containsItem(441) && offer4.getChild(16).getText().contains("Empty")){
						utils.sendGameMessage("Buying iron ore");
						buyItem("iron ore",0,60, quantity);
					}
					else if(!inventory.containsItem(12626) && offer4.getChild(16).getText().contains("Empty")){
						utils.sendGameMessage("Buying stamina");
						buyItem("stamina",15,6000, (int) (quantity*0.011627));
					}
				}
				else if(player.getWorldArea().intersectsWith(geArea) && !bank.isOpen() && (grandExchange ==null || grandExchange.isHidden())) {
					utils.sendGameMessage("Opening bank");
					openBank();
				}
				else if(!player.getWorldArea().intersectsWith(geArea)) {
					teleportRingOfWealth(3);
					timeout =5;
				}

			}
			else if(client.getRealSkillLevel(Skill.SMITHING) < 60 && (!foremanPaid || shouldCheckForemanFee() )){ //shouldCheckForemanFee()
				Widget payDialog = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTION1);
				Widget npcDialog = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
				NPC foreman = npc.findNearestNpc(2923);
				if (!inventory.containsStackAmount(ItemID.COINS_995, 2500) && !bank.isOpen()) {
					openBank();
				}
				else if(bank.isOpen() && inventory.getEmptySlots()<1){
					bank.depositAllExcept(Collections.singleton(ItemID.COAL_BAG_12019));
				}
				else if(bank.isOpen() && !inventory.containsStackAmount(ItemID.COINS_995, 2500)){
					bank.withdrawAllItem(ItemID.COINS_995);
				}
				else if (payDialog != null) {
					targetMenu = new MenuEntry("", "", 0, MenuOpcode.WIDGET_TYPE_6.getId(), 1, 14352385, false);
					menu.setEntry(targetMenu);
					mouse.delayClickRandomPointCenter(-100, 100,0);
				}
				else if (npcDialog != null) {
					setForemanTime(npcDialog);
					foremanPaid = true;
				}
				else if (foreman != null){
					targetMenu = new MenuEntry("", "", foreman.getIndex(), MenuOpcode.NPC_THIRD_OPTION.getId(), 0, 0, false);
					menu.setEntry(targetMenu);
					mouse.delayClickRandomPointCenter(-100, 100,0);

				}
			}
 			else if(continueWid !=null && continueWid.getText().contains("Click here to continue")){
				utils.sendGameMessage("Clicking continue");
				keyboard.pressKey(KeyEvent.VK_SPACE);
			}
			else if(barCollectWid!=null){
				utils.sendGameMessage("Interacting with collect widget");
				if(client.getVar(Varbits.BLAST_FURNACE_IRON_BAR)>0) {
					resumePauseWidget(17694734, client.getVar(Varbits.BLAST_FURNACE_IRON_BAR));
				}
				else if(client.getVar(Varbits.BLAST_FURNACE_STEEL_BAR)>0) {
					resumePauseWidget(17694734, client.getVar(Varbits.BLAST_FURNACE_STEEL_BAR));
				}
			}
			else if(client.getVar(Varbits.BAR_DISPENSER) > 1){
				utils.sendGameMessage("Collecting Bars");
				if(inventory.getEmptySlots() < 27 && !bank.isOpen()){
					utils.sendGameMessage("Opening bank to deposit to collect bars");
					openBank();
				}
				else if(bank.isOpen() && inventory.getEmptySlots() < 27){
					utils.sendGameMessage("Depositing ores");
					bank.depositAllExcept(Collections.singleton(ItemID.COAL_BAG_12019));
				}
				else {
					collectFurnace();

				}
				coalOnBelt = false;
			}
			else if(!bank.isOpen() && (!inventory.containsItem(ItemID.COAL_BAG_12019) || !inventory.containsItem(ItemID.IRON_ORE) || client.getVar(VarPlayer.POUCH_STATUS) == 16) &&  player.getWorldLocation().distanceTo(bankObject.getWorldLocation()) < 10){
				utils.sendGameMessage("Opening bank");
				openBank();
			}
			else if(bank.isOpen() && checkItems()){
				utils.sendGameMessage("Out of items");
				ressuply = true;
			}
			else if(shouldSipStamina() && bank.isOpen()){
				if(inventory.containsItem(STAMINA_POTIONS)){
					utils.sendGameMessage("Drinking stamina");
					drinkStam();
				}
				else if(inventory.getEmptySlots()<2){
					utils.sendGameMessage("Depositing items for stam");
					bank.depositAllExcept(Collections.singleton(ItemID.COAL_BAG_12019));
				}
				else if(bank.containsAnyOf(STAMINA_POTIONS) && !inventory.containsItem(STAMINA_POTIONS)){
					utils.sendGameMessage("Withdrawing stamina");
					Widget bankItem = bank.getBankItemWidgetAnyOf(STAMINA_POTIONS);
					bank.withdrawAllItem(bankItem);
				}
			}
			else if(bank.isOpen() && inventory.containsExcept(REQUIRED_ITEMS)){
				utils.sendGameMessage("Deposting all");
				bank.depositAllExcept(REQUIRED_ITEMS);
			}
			else if(bank.isOpen() && !inventory.containsItem(ItemID.COAL_BAG_12019)){
				utils.sendGameMessage("Withdrawing coal bag");
				bank.withdrawAllItem(ItemID.COAL_BAG_12019);
				timeout = 2;
			}
			else if( bank.isOpen() && inventory.containsItem(ItemID.COAL_BAG_12019) && client.getVar(VarPlayer.POUCH_STATUS) == 16){
				utils.sendGameMessage("Coal bag is empty filling up");
				fillBag();
			}
			else if(bank.isOpen() && !inventory.containsExcept(REQUIRED_ITEMS) && client.getVar(VarPlayer.POUCH_STATUS) == 0 && !inventory.containsItem(ItemID.IRON_ORE)){
				utils.sendGameMessage("Withdrawing Iron");
				bank.withdrawAllItem(ItemID.IRON_ORE);
				timeout = 2;
			}
			else if(inventory.containsItem(ItemID.COAL)){
				utils.sendGameMessage("Putting coal on belt");
				putConveyorBelt();
				coalOnBelt = true;
			}
			else if(!coalOnBelt && client.getVar(VarPlayer.POUCH_STATUS) == 0 && inventory.getEmptySlots() >= 27 && player.getWorldLocation().getX() == 1942 && player.getWorldLocation().getY()==4967){
				utils.sendGameMessage("Empty coal");
				emptyBag();
			}
			else if(inventory.containsItem(ItemID.COAL_BAG_12019) && inventory.containsItem(ItemID.IRON_ORE)){
				utils.sendGameMessage("Putting iron on belt");
				putConveyorBelt();
				timeout =1;
			}
			else if(coalOnBelt){
				utils.sendGameMessage("Walkig to tile");
				walk.webWalk(dispenserTile,0,false,0);
			}
			else{
				utils.sendGameMessage("No state");
			}


		}

	}

	private void sellItem(String itemName,Integer price){
		Widget grandExchangeOffer = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_CONTAINER);
		Widget titleWid = client.getWidget(WidgetInfo.CHATBOX_TITLE);

		if(grandExchangeOffer.isHidden()){
			targetMenu = new MenuEntry("Offer", "<col=ff9040>"+itemName+"</col>", 1, MenuOpcode.CC_OP.getId(), inventory.getWidgetItem(2354).getIndex(), 30605312, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-100, 100,0);
		}
		else if(grandExchangeOffer.getChild(39).getText().equals(price +" coins")){
			targetMenu = new MenuEntry("Confirm", "", 1, MenuOpcode.CC_OP.getId(), -1, 30474267, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-100, 100,0);
			timeout = 2;
		}
		else if(titleWid.getText().contains("Set a price") && !titleWid.isHidden()){
			utils.sendGameMessage("Typing Quantity");
			client.setVar(VarClientInt.INPUT_TYPE, 7);
			client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(price));
			client.runScript(681, null);
			client.runScript(ScriptID.MESSAGE_LAYER_CLOSE, 0, 0);
		}
		else if(!grandExchangeOffer.isHidden() && grandExchangeOffer.getChild(18).getText().contains("Sell")){
			utils.sendGameMessage("Setting price");
			targetMenu = new MenuEntry("Enter price", "", 1, MenuOpcode.CC_OP.getId(), 12, 30474264, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-100, 100,0);
		}
	}

	private void buyItem(String itemSearch, Integer param0, Integer price, Integer quantity){
		Widget grandExchangeOffer = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_CONTAINER);
		Widget searchResults = client.getWidget(WidgetInfo.CHATBOX_GE_SEARCH_RESULTS);
		Widget searchWid = client.getWidget(WidgetInfo.CHATBOX_FULL_INPUT);
		Widget titleWid = client.getWidget(WidgetInfo.CHATBOX_TITLE);


		if(grandExchangeOffer.isHidden()) {
			utils.sendGameMessage("Make buy offer");
			targetMenu = new MenuEntry("Create <col=ff9040>Buy</col> offer", "", 1, MenuOpcode.CC_OP.getId(), 3, 30474250, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-100, 100, 0);
		}
		else if(searchResults.getChildren() != null && (searchResults.getChildren().length != 5 && searchResults.getChildren().length >1)){
			utils.sendGameMessage("Clicking item");
			targetMenu = new MenuEntry("", "", 1, MenuOpcode.CC_OP.getId(), param0, 10616885, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-100, 100,0);
		}
		else if(!grandExchangeOffer.isHidden() && searchWid.getText().contains("What would you like")){
			utils.sendGameMessage("Search item");
			client.setVar(VarClientInt.INPUT_TYPE, 14);
			client.setVar(VarClientStr.INPUT_TEXT, itemSearch);
			client.runScript(681, null);
		}
		else if(grandExchangeOffer.getChild(39).getText().replace(",", "").equals(price +" coins") && grandExchangeOffer.getChild(32).getText().replace(",", "").equals(String.valueOf(quantity))){
			utils.sendGameMessage("Confirming offer");
			targetMenu = new MenuEntry("Confirm", "", 1, MenuOpcode.CC_OP.getId(), -1, 30474267, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-100, 100,0);
			timeout = 2;
		}
		else if(titleWid.getText().contains("How many do you wish") && !titleWid.isHidden()){
			utils.sendGameMessage("Typing Quantity" + grandExchangeOffer.getChild(32).getText().replace(",", "") +"@");
			client.setVar(VarClientInt.INPUT_TYPE, 7);
			client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(quantity));
			client.runScript(681, null);
		}
		else if(grandExchangeOffer.getChild(39).getText().replace(",", "").equals(price +" coins")){
			utils.sendGameMessage("Cliciking quantity");
			targetMenu = new MenuEntry("Enter quantity", "", 1, MenuOpcode.CC_OP.getId(), 7, 30474264, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-100, 100,0);
		}

		else if(titleWid.getText().contains("Set a price") && !titleWid.isHidden()){
			utils.sendGameMessage("Setting price");
			client.setVar(VarClientInt.INPUT_TYPE, 7);
			client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(price));
			client.runScript(681, null);
		}
		else if(client.getVar(VarPlayer.CURRENT_GE_ITEM)>0 && titleWid.isHidden()){
			utils.sendGameMessage("Click setting price");
			targetMenu = new MenuEntry("Enter price", "", 1, MenuOpcode.CC_OP.getId(), 12, 30474264, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-100, 100,0);
		}

	}



	private boolean checkItems() {
		if ((!inventory.containsItem(ItemID.COAL) && !inventory.containsItem(ItemID.COAL+1) && !bank.contains(ItemID.COAL,0)) ||
				(!inventory.containsItem(ItemID.IRON_ORE) && !inventory.containsItem(ItemID.IRON_ORE+1) && !bank.contains(ItemID.IRON_ORE,0)) ||
				(!inventory.containsItem(STAMINA_POTIONS) && !inventory.containsItem(12626) && !bank.containsAnyOf(STAMINA_POTIONS))) {

			return true;

		}
		return false;
	}


	private void openGE() {
		NPC bankNpc = npc.findNearestNpc("Grand Exchange Clerk");
		if (bankNpc != null) {

			targetMenu = new MenuEntry("Exchange", "<col=ffff00>Grand Exchange Clerk", 18880, MenuOpcode.NPC_THIRD_OPTION.getId(), 0, 0, false);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-200, 200, 0);
		}
	}


	private void openBank() {
		GameObject bankObject = object.findNearestGameObject(26707);
		NPC bankNpc = npc.findNearestNpc("Banker");

		if (bankObject != null) {

			targetMenu = new MenuEntry("", "", bankObject.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), bankObject.getSceneMinLocation().getX(), bankObject.getSceneMinLocation().getY(), true);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-200, 200, 0);
		}
		else if(bankNpc != null) {
			targetMenu = new MenuEntry("Bank", "<col=ffff00>Banker", 18882, MenuOpcode.NPC_THIRD_OPTION.getId(), 0, 0, true);
			menu.setEntry(targetMenu);
			mouse.delayClickRandomPointCenter(-200, 200, 0);
		}
	}

	private void fillBag(){
		targetMenu = new MenuEntry("Fill", "Fill", 9, 1007,
				0, 983043, true);
		menu.setEntry(targetMenu);
		mouse.delayClickRandomPointCenter(-200, 200, 0);
		//TODO add timeout here

	}

	private void emptyBag(){
		targetMenu = new MenuEntry("Empty", "<col=ff9040>Coal bag", 12019, 36,
				0, 9764864, false);
		menu.setEntry(targetMenu);
		mouse.delayClickRandomPointCenter(-200, 200, 0);

	}

	private void putConveyorBelt()
	{
		GameObject conveyorBelt = object.findNearestGameObject(CONVEYOR_BELT);
		targetMenu = new MenuEntry("", "", conveyorBelt.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), conveyorBelt.getSceneMinLocation().getX(), conveyorBelt.getSceneMinLocation().getY(), false);
		menu.setEntry(targetMenu);
		mouse.delayClickRandomPointCenter(-100, 100,0);
	}

	private void collectFurnace()
	{
		GameObject barDispenser = object.findNearestGameObject(9092);
		targetMenu = new MenuEntry("Take", barDispenser.toString(), 9092, MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), barDispenser.getSceneMinLocation().getX(), barDispenser.getSceneMinLocation().getY(), false);
		menu.setEntry(targetMenu);
		mouse.delayClickRandomPointCenter(-100, 100,0);
	}

	private void drinkStam()
	{
		WidgetItem useableItem = inventory.getWidgetItem(STAMINA_POTIONS);
		if (useableItem != null)
		{
			targetMenu = new MenuEntry("Drink", useableItem.toString(), 9, MenuOpcode.CC_OP_LOW_PRIORITY.getId(),
					useableItem.getIndex(), 983043, true);
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(useableItem.getCanvasBounds(), 0);
		}
	}

	private boolean shouldSipStamina()
	{
		return (client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0) ||
				(client.getEnergy() < 23 ||
						(inventory.containsItem(STAMINA_POTIONS) && client.getEnergy() < 23));
	}

	static void resumePauseWidget(int widgetId, int arg) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final int garbageValue = 1877711272;
		final String className = "fe";
		final String methodName = "hd";

		Class clazz = Class.forName(className);
		Method method = clazz.getDeclaredMethod(methodName, int.class, int.class, int.class);
		method.setAccessible(true);
		method.invoke(null, widgetId, arg, garbageValue);
	}

	private boolean shouldCheckForemanFee()
	{
		return (foremanTimer == null || Duration.between(Instant.now(), foremanTimer.getEndTime()).toSeconds() <= 30);
	}

	private void setForemanTime(Widget npcDialog)
	{
		String npcText = Text.sanitizeMultilineText(npcDialog.getText());

		if (npcText.equals(FOREMAN_PERMISSION_TEXT))
		{
			foremanTimer = new ForemanTimer(this, itemManager);
		}
	}

	private void teleportRingOfWealth(int menuIdentifier)
	{
		targetMenu = new MenuEntry("", "", menuIdentifier, MenuOpcode.CC_OP.getId(), -1,
				25362455, false);
		Widget ringWidget = client.getWidget(WidgetInfo.EQUIPMENT_RING);
		if (ringWidget != null)
		{
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(ringWidget.getBounds(), 0);
		}
		else
		{
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(new Point(0, 0), 0);
		}
	}


		@Subscribe
	private void onChatMessage(ChatMessage event) {

	}


	@Subscribe
	protected void onGameStateChanged(GameStateChanged event) {

	}

	@Subscribe
	public void onStatChanged(StatChanged event) {

	}

}
