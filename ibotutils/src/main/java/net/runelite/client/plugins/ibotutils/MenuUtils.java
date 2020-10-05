package net.runelite.client.plugins.ibotutils;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class MenuUtils
{
	@Inject
	private Client client;

	@Inject
	private WalkUtils walk;

	private MenuEntry entry;

	private boolean consumeClick;
	private boolean modifiedMenu;
	private int modifiedItemID;
	private int modifiedItemIndex;
	private int modifiedOpCode;

	public void setSelectedSpell(WidgetInfo info)
	{
		final Widget widget = client.getWidget(info);

		client.setSelectedSpellWidget(widget.getId());
		client.setSelectedSpellChildIndex(-1);
	}

	public void setEntry(MenuEntry menuEntry)
	{
		entry = menuEntry;
	}

	public void setEntry(MenuEntry menuEntry, boolean consume)
	{
		entry = menuEntry;
		consumeClick = consume;
	}

	public void setModifiedEntry(MenuEntry menuEntry, int itemID, int itemIndex, int opCode)
	{
		entry = menuEntry;
		modifiedMenu = true;
		modifiedItemID = itemID;
		modifiedItemIndex = itemIndex;
		modifiedOpCode = opCode;
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getOpcode() == MenuOpcode.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
				event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
		{
			return;
		}
		if (entry != null)
		{
			client.setLeftClickMenuEntry(entry);
			if (modifiedMenu)
			{
				event.setModified();
			}
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getOpcode() == MenuOpcode.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
				event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
		{
			//Either logging out or world-hopping which is handled by 3rd party plugins so let them have priority
			log.info("Received world-hop/login related click. Giving them priority");
			entry = null;
			return;
		}
		if (entry != null)
		{
			event.consume();
			if (consumeClick)
			{
				log.info("Consuming a click and not sending anything else");
				consumeClick = false;
				return;
			}
			if (event.getOption().equals("Walk here") && walk.walkAction)
			{
				log.debug("Walk action");
				walk.walkTile(walk.coordX, walk.coordY);
				walk.walkAction = false;
				return;
			}
			if (modifiedMenu)
			{
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(modifiedItemIndex);
				client.setSelectedItemID(modifiedItemID);
				log.info("doing a Modified MOC, mod ID: {}, mod index: {}, param1: {}", modifiedItemID, modifiedItemIndex, entry.getParam1());
				client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(), modifiedOpCode,
						entry.getParam0(), entry.getParam1());
				modifiedMenu = false;
			}
			else
			{
				client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(), entry.getOpcode(),
						entry.getParam0(), entry.getParam1());
			}
			entry = null;
		}
	}

}
