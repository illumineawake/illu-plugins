package net.runelite.client.plugins.iblackjack.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.iblackjack.Task;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;

@Slf4j
public class ShopTask extends Task {
    @Override
    public boolean validate() {
        return (isShopOpen() || inShopArea()) && !inventory.isFull() && inventory.containsItem(ItemID.COINS_995);
    }

    @Override
    public String getTaskDescription() {
        return status;
    }

    @Override
    public void onGameTick(GameTick event) {
        if (isShopOpen()) {
            Widget jugWidget = client.getWidget(300, 16);
            if (jugWidget != null && jugWidget.getChild(3).getItemQuantity() > 0) {
                status = "Buying Jug of Wine";
                entry = new LegacyMenuEntry("", "", 5, MenuAction.CC_OP.getId(), 3, 19660816, false);
                utils.doActionMsTime(entry, jugWidget.getBounds(), sleepDelay());
            }
        } else {
            NPC barman = npc.findNearestNpc(NpcID.ALI_THE_BARMAN);
            if (barman != null) {
                status = "Opening shop";
                entry = new LegacyMenuEntry("", "", barman.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0, false);
                utils.doActionMsTime(entry, new Point(0, 0), sleepDelay());
            }
        }
        log.info(status);
    }
}