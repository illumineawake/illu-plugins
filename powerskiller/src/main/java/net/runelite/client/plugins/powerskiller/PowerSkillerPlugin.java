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
package net.runelite.client.plugins.powerskiller;

import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.awt.Rectangle;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.NullObjectID;
import net.runelite.api.ObjectID;
import net.runelite.api.Player;
import net.runelite.api.GameState;
import net.runelite.api.MenuOpcode;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.NpcDefinitionChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import static net.runelite.client.plugins.powerskiller.PowerSkillerState.*;


@Extension
@PluginDependency(BotUtils.class)
@PluginDescriptor(
	name = "Power Skiller",
	enabledByDefault = false,
	description = "Illumine auto power-skill plugin",
	tags = {"fishing, mining, wood-cutting, illumine, bot, power, skill"},
	type = PluginType.SKILLING
)
@Slf4j
public class PowerSkillerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PowerSkillerConfiguration config;

	@Inject
	private BotUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	PluginManager pluginManager;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private PowerSkillerOverlay overlay;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	PowerSkillerState state;
	GameObject targetObject;
	NPC targetNPC;
	WallObject targetWall;
	MenuEntry targetMenu;
	WorldPoint skillLocation;
	Instant botTimer;
	LocalPoint beforeLoc;
	Player player;
	Rectangle altRect = new Rectangle(-100,-100, 10, 10);
	WorldArea DENSE_ESSENCE_AREA = new WorldArea(new WorldPoint(1754, 3845, 0), new WorldPoint(1770, 3862, 0));
	private final WorldPoint WEST_ROCK = new WorldPoint(3164, 2914, 0);
	private final WorldPoint SW_ROCK = new WorldPoint(3166, 2913, 0);
	private final WorldPoint SE_ROCK = new WorldPoint(3167, 2913, 0);
	private final WorldArea DESERT_QUARRY = new WorldArea(new WorldPoint(3148,2896,0),new WorldPoint(3186,2926,0));
	int waterskinsLeft;

	int timeout = 0;
	int opcode;
	long sleepLength;
	boolean startPowerSkiller;
	boolean npcMoved;
	private final Set<Integer> itemIds = new HashSet<>();
	private final Set<Integer> objectIds = new HashSet<>();
	private final Set<Integer> requiredIds = new HashSet<>();


	@Provides
	PowerSkillerConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PowerSkillerConfiguration.class);
		//TODO make GUI that can be updated in realtime, may require new JPanel
	}

	@Override
	protected void startUp()
	{
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown()
	{
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}

	private void resetVals()
	{
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
		state = null;
		timeout = 0;
		botTimer = null;
		skillLocation = null;
		startPowerSkiller = false;
		npcMoved = false;
		objectIds.clear();
		requiredIds.clear();
		itemIds.clear();
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("PowerSkiller"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startPowerSkiller)
			{
				startPowerSkiller = true;
				chinBreakHandler.startPlugin(this);
				state = null;
				targetMenu = null;
				botTimer = Instant.now();
				setLocation();
				getConfigValues();
				overlayManager.add(overlay);
			}
			else
			{
				resetVals();
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("PowerSkiller"))
		{
			return;
		}
		switch (event.getKey())
		{
			case "objectIds":
				objectIds.clear();
				objectIds.addAll(utils.stringToIntList(config.objectIds()));
				break;
			case "requiredItems":
				log.info("config changed");
				requiredIds.clear();
				if (!config.requiredItems().equals("0") && !config.requiredItems().equals(""))
				{
					log.info("adding required Ids: {}", config.requiredItems());
					requiredIds.addAll(utils.stringToIntList(config.requiredItems()));
				}
				break;
			case "dropInventory":
			case "items":
				itemIds.clear();
				itemIds.addAll(utils.stringToIntList(config.items()));
				break;
		}
	}

	private void getConfigValues()
	{
		objectIds.clear();
		requiredIds.clear();
		itemIds.clear();
		objectIds.addAll(utils.stringToIntList(config.objectIds()));
		if (!config.requiredItems().equals("0") && !config.requiredItems().equals(""))
		{
			requiredIds.addAll(utils.stringToIntList(config.requiredItems()));
		}
		itemIds.addAll(utils.stringToIntList(config.items()));
	}

	public void setLocation()
	{
		if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN))
		{
			skillLocation = client.getLocalPlayer().getWorldLocation();
			beforeLoc = client.getLocalPlayer().getLocalLocation();
		}
		else
		{
			log.debug("Tried to start bot before being logged in");
			skillLocation = null;
			resetVals();
		}
	}

	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}


	private GameObject getDenseEssence()
	{
		assert client.isClientThread();

		if (client.getVarbitValue(4927) == 0)
		{
			return utils.findNearestGameObject(NullObjectID.NULL_8981);
		}
		if (client.getVarbitValue(4928) == 0)
		{
			return utils.findNearestGameObject(NullObjectID.NULL_10796);
		}
		return null;
	}

	private void interactNPC()
	{
		targetNPC = utils.findNearestNpcWithin(skillLocation, config.locationRadius(), objectIds);
		opcode = (config.customOpcode() && config.objectOpcode() ? config.objectOpcodeValue() : MenuOpcode.NPC_FIRST_OPTION.getId());
		if (targetNPC != null)
		{
			targetMenu = new MenuEntry("", "", targetNPC.getIndex(), opcode, 0, 0, false);
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(targetNPC.getConvexHull().getBounds(), sleepDelay());
		}
		else
		{
			log.info("NPC is null");
		}
	}

	private void interactObject()
	{
		targetObject = (config.type() == PowerSkillerType.DENSE_ESSENCE) ? getDenseEssence() :
			utils.findNearestGameObjectWithin(skillLocation, config.locationRadius(), objectIds);
		opcode = (config.customOpcode() && config.objectOpcode() ? config.objectOpcodeValue() : MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId());
		if (targetObject != null)
		{
			targetMenu = new MenuEntry("", "", targetObject.getId(), opcode,
				targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
		}
		else
		{
			log.info("Game Object is null, ids are: {}", objectIds.toString());
		}
	}

	private void interactWall()
	{
		targetWall = utils.findWallObjectWithin(skillLocation, config.locationRadius(), objectIds);
		opcode = (config.customOpcode() && config.objectOpcode() ? config.objectOpcodeValue() : MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId());
		if (targetWall != null)
		{
			targetMenu = new MenuEntry("", "", targetWall.getId(), opcode,
					targetWall.getLocalLocation().getSceneX(), targetWall.getLocalLocation().getSceneY(), false);
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(targetWall.getConvexHull().getBounds(), sleepDelay());
		}
		else
		{
			log.info("Wall Object is null, ids are: {}", objectIds.toString());
		}
	}

	private PowerSkillerState getBankState()
	{
		if (!utils.isBankOpen() && !utils.isDepositBoxOpen())
		{
			return FIND_BANK;
		}
		if (config.dropInventory() && !utils.inventoryEmpty())
		{
			return DEPOSIT_ALL;
		}
		if (config.dropExcept())
		{
			if (!requiredIds.containsAll(itemIds) && !itemIds.contains(0))
			{
				requiredIds.addAll(itemIds);
			}
			return DEPOSIT_EXCEPT;
		}
		if (utils.inventoryContains(itemIds))
		{
			return DEPOSIT_ITEMS;
		}
		return BANK_NOT_FOUND;
	}

	private void openBank()
	{
		GameObject bank = utils.findNearestBank();
		if (bank != null)
		{
			targetMenu = new MenuEntry("", "", bank.getId(),
				utils.getBankMenuOpcode(bank.getId()), bank.getSceneMinLocation().getX(),
				bank.getSceneMinLocation().getY(), false);
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(bank.getConvexHull().getBounds(), sleepDelay());
		}
		else
		{
			utils.sendGameMessage("Bank not found, stopping");
			startPowerSkiller = false;
		}
	}

	private void handleDropAll()
    {
        if (config.customOpcode() && config.inventoryMenu())
        {
            Collection<Integer> inventoryItems = utils.getAllInventoryItemIDs();
            utils.inventoryItemsInteract(inventoryItems, config.inventoryOpcodeValue(), false,true, config.sleepMin(), config.sleepMax());
        }
        else
        {
            utils.dropInventory(true, config.sleepMin(), config.sleepMax());
        }
    }

    private void handleDropExcept()
    {
        if (config.customOpcode() && config.inventoryMenu() && config.combineItems())
        {
            utils.inventoryItemsCombine(itemIds, config.toolId(),config.inventoryOpcodeValue(), true,true, config.sleepMin(), config.sleepMax());
        }
        else if (config.customOpcode() && config.inventoryMenu())
        {
            utils.inventoryItemsInteract(itemIds, config.inventoryOpcodeValue(), true,true, config.sleepMin(), config.sleepMax());
        }
        else
        {
            utils.dropAllExcept(itemIds, true, config.sleepMin(), config.sleepMax());
        }
    }

    private void handleDropItems()
    {
        if (config.customOpcode() && config.inventoryMenu() && config.combineItems())
        {
            utils.inventoryItemsCombine(itemIds, config.toolId(),config.inventoryOpcodeValue(), false,true, config.sleepMin(), config.sleepMax());
        }
        else if (config.customOpcode() && config.inventoryMenu())
        {
            utils.inventoryItemsInteract(itemIds, config.inventoryOpcodeValue(), false,false, config.sleepMin(), config.sleepMax());
        }
        else
        {
            utils.dropItems(itemIds, true, config.sleepMin(), config.sleepMax());
        }
    }

	public PowerSkillerState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}
		if (utils.iterating)
		{
			return ITERATING;
		}
		if (!config.dropInventory() && !requiredIds.isEmpty() && !utils.inventoryContainsAllOf(requiredIds) &&
			config.type() != PowerSkillerType.DENSE_ESSENCE)
		{
			return MISSING_ITEMS;
		}
		if (utils.isMoving(beforeLoc))
		{
			timeout = 2 + tickDelay();
			return MOVING;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			return HANDLE_BREAK;
		}
		if(DESERT_QUARRY.intersectsWith(player.getWorldArea())){
			updateWaterskinsLeft();
			if(waterskinsLeft==0){
				return CASTING_HUMIDIFY;
			}
		}
		if(config.type() == PowerSkillerType.SANDSTONE){
			 if(utils.inventoryFull()){
				return ADDING_SANDSTONE_TO_GRINDER;
			} else if (player.getWorldLocation().equals(new WorldPoint(3152,2910,0))) {
				return WALKING_BACK_TO_SANDSTONE;
			}
		}
		if (utils.inventoryFull())
		{
			if (config.type() == PowerSkillerType.DENSE_ESSENCE)
			{
				return WAIT_DENSE_ESSENCE;
			}
			if (config.bankItems())
			{
				return getBankState();
			}
			if (config.dropInventory())
			{
				return DROP_ALL;
			}
			if (config.dropExcept() && !config.dropInventory())
			{
				if (!itemIds.containsAll(requiredIds))
				{
					itemIds.addAll(requiredIds);
				}
				return DROP_EXCEPT;
			}
			return (!utils.inventoryContains(itemIds)) ? INVALID_DROP_IDS : DROP_ITEMS;
		}
		if (config.safeSpot() &&
			skillLocation.distanceTo(player.getWorldLocation()) > (config.safeSpotRadius()))
		{
			return RETURN_SAFE_SPOT;
		}
		if (client.getLocalPlayer().getAnimation() == -1 || npcMoved)
		{
			if (config.type() == PowerSkillerType.DENSE_ESSENCE)
			{
				return (DENSE_ESSENCE_AREA.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0) ?
					FIND_GAME_OBJECT : WAIT_DENSE_ESSENCE;
			}
			return (config.type() == PowerSkillerType.NPC) ?
				FIND_NPC : FIND_GAME_OBJECT;
		}
		return ANIMATING;
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (!startPowerSkiller || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && skillLocation != null)
		{
			if (!client.isResized())
			{
				utils.sendGameMessage("illu - client must be set to resizable");
				startPowerSkiller = false;
				return;
			}
			state = getState();
			beforeLoc = player.getLocalLocation();
			switch (state)
			{
				case TIMEOUT:
					utils.handleRun(30, 20);
					timeout--;
					break;
				case CASTING_HUMIDIFY:
					castHumidify();
					timeout=tickDelay();
					break;
				case ADDING_SANDSTONE_TO_GRINDER:
					objectIds.clear();
					objectIds.add(ObjectID.GRINDER);
					interactSandstoneObject();
					objectIds.clear();
					objectIds.add(ObjectID.ROCKS_11386); //sandstone id
					timeout=tickDelay();
					break;
				case WALKING_BACK_TO_SANDSTONE:
					utils.walk(new WorldPoint(3166,2914,0),1,sleepDelay());
					timeout=tickDelay();
					break;
				case DROP_ALL:
					handleDropAll();
					timeout = tickDelay();
					break;
				case DROP_EXCEPT:
					handleDropExcept();
					timeout = tickDelay();
					break;
				case DROP_ITEMS:
                    handleDropItems();
					timeout = tickDelay();
					break;
				case FIND_GAME_OBJECT:
					if(config.type() == PowerSkillerType.SANDSTONE){
						interactSandstoneObject();
						timeout = tickDelay();
						return;
					}
					interactObject();
					timeout = tickDelay();
					break;
				case FIND_WALL:
					interactWall();
					timeout = tickDelay();
					break;
				case FIND_NPC:
					interactNPC();
					npcMoved = false;
					timeout = tickDelay();
					break;
				case FIND_BANK:
					openBank();
					timeout = tickDelay();
					break;
				case DEPOSIT_ALL:
					utils.depositAll();
					timeout = tickDelay();
					break;
				case DEPOSIT_EXCEPT:
					utils.depositAllExcept(requiredIds);
					timeout = tickDelay();
					break;
				case DEPOSIT_ITEMS:
					utils.depositAllOfItems(itemIds);
					timeout = tickDelay();
					break;
				case RETURN_SAFE_SPOT:
					utils.walk(skillLocation, config.safeSpotRadius(), sleepDelay());
					timeout = 2 + tickDelay();
					break;
				case MISSING_ITEMS:
					startPowerSkiller = false;
					utils.sendGameMessage("Missing required items IDs: " + requiredIds.toString() + " from inventory. Stopping.");
					if (config.logout())
					{
						utils.logout();
					}
					resetVals();
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					timeout = 10;
					break;
				case ANIMATING:
				case MOVING:
					utils.handleRun(30, 20);
					timeout = tickDelay();
					break;
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (config.customOpcode() && config.printOpcode())
		{
			utils.sendGameMessage("Identifier value: " + event.getIdentifier());
			utils.sendGameMessage("Opcode value: " + event.getOpcode());
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (targetObject == null || event.getGameObject() != targetObject || !startPowerSkiller)
		{
			return;
		}
		else
		{
			if (client.getLocalDestinationLocation() != null)
			{
				interactObject(); //This is a failsafe, Player can get stuck with a destination on object despawn and be "forever moving".
			}
		}
	}

	@Subscribe
	public void onNPCDefinitionChanged(NpcDefinitionChanged event)
	{
		if (targetNPC == null || event.getNpc() != targetNPC || !startPowerSkiller)
		{
			return;
		}
		if (timeout == 0)
		{
			interactNPC();
		}
		else
		{
			npcMoved = true;
		}
	}

	@Subscribe
	private void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != 93 || !startPowerSkiller || !config.dropOne())
		{
			return;
		}
		if(config.type() == PowerSkillerType.SANDSTONE){
			return;
		}
		if (config.dropInventory())
		{
			handleDropAll();
			timeout = tickDelay();
			return;
		}
		if (config.dropExcept() && !config.dropInventory())
		{
			handleDropExcept();
			timeout = tickDelay();
			return;
		}
		handleDropItems();
		timeout = tickDelay();
		return;
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && startPowerSkiller)
		{
			state = TIMEOUT;
			timeout = 2;
		}
	}

	private void interactSandstoneObject()
	{
		//a custom function that looks for a grinder outside of the players usual location radius
		//it also only interacts with the three most efficient sandstone rocks
		if(!objectIds.contains(ObjectID.GRINDER)){ //if not looking for the grinder
			//look for sandstone in the radius set by the player
			for(GameObject gameObject : utils.getGameObjects(ObjectID.ROCKS_11386)){
				if(gameObject.getWorldLocation().equals(WEST_ROCK)){
					targetObject=gameObject; //west rock
					break;
				} else if(gameObject.getWorldLocation().equals(SW_ROCK)){
					targetObject=gameObject; //south west rock
					break;
				} else if(gameObject.getWorldLocation().equals(SE_ROCK)){
					targetObject=gameObject; //south east rock
					break;
				}
			}
		} else { //looking for the grinder
			//extend search outside the players set radius
			targetObject = utils.getGameObjects(ObjectID.GRINDER).get(0);
		}
		opcode = (config.customOpcode() && config.objectOpcode() ? config.objectOpcodeValue() : MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId());
		if (targetObject != null)
		{
			targetMenu = new MenuEntry("", "", targetObject.getId(), opcode,
					targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
		}
		else
		{
			log.info("Game Object is null, ids are: {}", objectIds.toString());
		}
	}

	private void updateWaterskinsLeft(){
		waterskinsLeft=0;
		waterskinsLeft+=utils.getInventoryItemCount(1823,false)*4; //4 dose waterskin
		waterskinsLeft+=utils.getInventoryItemCount(1825,false)*3; //3 dose waterskin
		waterskinsLeft+=utils.getInventoryItemCount(1827,false)*2; //2 dose waterskin
		waterskinsLeft+=utils.getInventoryItemCount(1829,false); //1 dose waterskin

		if(waterskinsLeft==0){
			if(!utils.inventoryContains(1831)){
				waterskinsLeft=-1; //no waterskins detected
			}
		}
	}

	private void castHumidify(){
		if(!utils.inventoryContains(9075) && !utils.runePouchContains(9075)){
			utils.sendGameMessage("illu - out of astrals runes");
			startPowerSkiller = false;
		}
		targetMenu = new MenuEntry("Cast","<col=00ff00>Humidify</col>",1,57,-1,14286954,false);
		Widget spellWidget = utils.getSpellWidget("Humidify");
		if(spellWidget==null){
			utils.sendGameMessage("illu - unable to find humidify widget");
			startPowerSkiller = false;
		}
		utils.oneClickCastSpell(utils.getSpellWidgetInfo("Humidify"),targetMenu,sleepDelay());
	}
}
