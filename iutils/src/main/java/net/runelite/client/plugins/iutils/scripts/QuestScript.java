package net.runelite.client.plugins.iutils.scripts;

import net.runelite.api.Skill;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.iutils.api.EquipmentSlot;
import net.runelite.client.plugins.iutils.api.Magic;
import net.runelite.client.plugins.iutils.api.Prayers;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.bot.InventoryItem;
import net.runelite.client.plugins.iutils.bot.iNPC;
import net.runelite.client.plugins.iutils.scene.Area;
import net.runelite.client.plugins.iutils.scene.RectangularArea;
import net.runelite.client.plugins.iutils.ui.Bank;
import net.runelite.client.plugins.iutils.ui.Chatbox;
import net.runelite.client.plugins.iutils.ui.GrandExchange;
import net.runelite.client.plugins.iutils.walking.BankLocations;
import net.runelite.client.plugins.iutils.walking.Walking;

import javax.inject.Inject;
import java.util.Arrays;

public abstract class QuestScript extends Plugin implements Runnable {
    private static final RectangularArea GRAND_EXCHANGE = new RectangularArea(3159, 3493, 3169, 3485);
    @Inject
    protected Bot bot;
    @Inject
    protected Walking walking;
    @Inject
    protected Chatbox chatbox;
    @Inject
    protected Prayers prayers;

    protected void equip(int id) {
        if (Arrays.stream(bot.container(94).getItems()).anyMatch(i -> i.getId() == id)) {
            return;
        }

        obtain(new ItemQuantity(id, 1));

        bot.inventory().withId(id).first().interact(1);
        bot.waitUntil(() -> Arrays.stream(bot.container(94).getItems()).anyMatch(x -> x.getId() == id));
    }

    protected void obtain(ItemQuantity... items) {
        if (inventoryHasItems(items)) {
            System.out.println("Required items already in inventory, carry on...");
            return;
        }

        bank().depositInventory();

        Arrays.stream(items)
                .map(i -> new ItemQuantity(i.id, i.quantity - bank().quantity(i.id)))
                .filter(i -> i.quantity > 0)
                .forEach(i -> grandExchange().buy(i.id, i.quantity));
        bank().depositInventory();
        Arrays.stream(items).forEach(i -> bank().withdraw(i.id, i.quantity, false));
    }

    protected boolean inventoryHasItems(ItemQuantity... items) {
        for (var item : items) {
            if (bot.inventory().withId(item.id) == null) {
                System.out.println(item.id + " is null");
                System.out.println("Quantity: " + bot.inventory().withId(item.id).quantity());
            }
            if (bot.inventory().withId(item.id).quantity() < item.quantity) {
                return false;
            }
        }
        return true;
    }

    protected void handleLevelUp() {
        if (bot.widget(162, 562).nestedInterface() == 233) {
            System.out.println("Closing chat dialog");
            bot.widget(233, 3).select();
            bot.tick();
            chat();
        }
    }

    protected Bank bank() {
        var bank = new Bank(bot);

        if (!bank.isOpen()) {
            if (!BankLocations.ANY.contains(bot.localPlayer().position())) {
                walking.walkTo(BankLocations.ANY);
            }
            if (bot.npcs().withName("Banker").exists()) {
                bot.npcs().withName("Banker").nearest().interact("Bank");
            } else if (bot.objects().withName("Bank booth").withAction("Bank").exists()) {
                bot.objects().withName("Bank booth").withAction("Bank").nearest().interact("Bank");
            } else {
                bot.objects().withName("Bank chest").nearest().interact("Use");
            }
            bot.waitUntil(bank::isOpen);
        }

        return bank;
    }

    protected GrandExchange grandExchange() {
        if (!bot.inventory().withId(995).exists()) {
            bank().withdraw(995, Integer.MAX_VALUE, false);
        }

        var grandExchange = new GrandExchange(bot);

        if (!grandExchange.isOpen()) {
            if (!GRAND_EXCHANGE.contains(bot.localPlayer().position())) {
                System.out.println(GRAND_EXCHANGE.toString() + " doesn't contain player: " + bot.localPlayer().position());
                walking.walkTo(GRAND_EXCHANGE);
            }

            bot.npcs().withName("Grand Exchange Clerk").nearest().interact("Exchange");
            bot.waitUntil(grandExchange::isOpen);
        }

        return grandExchange;
    }

    protected void teleportToLumbridge() {
        bot.widget(218, 5).interact("Cast");
        bot.waitUntil(() -> bot.localPlayer().position().regionID() == 12850);
    }

    protected void killNpc(String name) {
        var npc = bot.npcs().withName(name).withoutTarget().nearest();
        killNpc(npc);
    }

    protected void killNpc(int id) {
        var npc = bot.npcs().withId(id).withoutTarget().nearest();

        if (bot.npcs().withId(id).withTarget(bot.localPlayer()).nearest() != null) {
            npc = bot.npcs().withId(id).withTarget(bot.localPlayer()).nearest();
        }

        killNpc(npc);
    }

    private void killNpc(iNPC npc) {
        while (bot.npcs().withIndex(npc.index()).exists()) {
            if (bot.localPlayer().target() != npc) {
                npc.interact("Attack");
                bot.waitUntil(() -> bot.localPlayer().target() == npc, 10);
            }

            heal();
            restorePrayer();
            restoreStats();
            bot.tick();
        }
    }

    private boolean needsStatRestore() {
        var matters = new Skill[]{Skill.ATTACK, Skill.DEFENCE, Skill.STRENGTH};
        for (var skill : matters) {
            if (bot.modifiedLevel(skill) < bot.baseLevel(skill)) {
                return true;
            }
        }
        return false;
    }

    private void restoreStats() {
        if (bot.inventory().withNamePart("restore").exists() && needsStatRestore()) {
            bot.inventory().withNamePart("restore").first().interact("Drink");
        }
    }

    private void restorePrayer() {
        if (bot.modifiedLevel(Skill.PRAYER) < bot.baseLevel(Skill.PRAYER) / 2) {
            //todo add super restores?
            if (bot.inventory().withNamePart("Prayer potion(").exists()) {
                bot.inventory().withNamePart("Prayer potion(").first().interact("Drink");
            }
        }
    }

    protected void heal() {
        if (bot.modifiedLevel(Skill.HITPOINTS) < bot.baseLevel(Skill.HITPOINTS) / 2) {
            var food = bot.inventory().withAction("Eat").first();
            if (food != null) {
                food.interact("Eat");
                bot.tick();
            }
        }
    }

    protected void chatNpc(Area location, String npcName, String... chatOptions) {
        walking.walkTo(location);
        bot.npcs().withName(npcName).nearest().interact("Talk-to");
        chatbox.chat(chatOptions);
        bot.tick();
    }

    protected void chatNpc(Area location, int npcId, String... chatOptions) {
        walking.walkTo(location);
        bot.npcs().withId(npcId).nearest().interact("Talk-to");
        chatbox.chat(chatOptions);
        bot.tick();
    }

    protected void unequip(int item, EquipmentSlot slot) {
        if (Arrays.stream(bot.container(94).getItems()).anyMatch(i -> i.getId() == item)) {
            bot.widget(slot.widgetID, slot.widgetChild).interact(0);
            bot.tick();
        }
    }

    protected void castSpellNpc(String name, Magic spell) {
        var npc = bot.npcs().withName(name).nearest();
        castSpellNpc(npc, spell);
    }

    protected void castSpellNpc(iNPC npc, Magic spell) {
        if (npc != null) {
            bot.widget(218, spell.widgetChild).useOn(npc);
            bot.tick();
        }
    }

    protected void castSpellItem(InventoryItem it, Magic spell) {
        if (it != null) {
            bot.widget(218, spell.widgetChild).useOn(it);
            bot.tick();
        }
    }

    protected boolean inCombat() {
        return bot.npcs().withTarget(bot.localPlayer()).exists() || bot.localPlayer().target() != null;
    }

    protected boolean itemOnGround(String item) {
        return bot.groundItems().withName(item).exists();
    }

    protected void chat(String... options) {
        chatbox.chat(options);
    }

    protected void interactObject(Area area, String object, String action) {
        walking.walkTo(area);
        bot.objects().withName(object).withAction(action).nearest().interact(action);
    }

    protected void interactObject(Area area, int id, String action) {
        walking.walkTo(area);
        bot.objects().withId(id).withAction(action).nearest().interact(action);
    }

    protected void useItemItem(String item1, String item2) {
        bot.inventory().withName(item1).first().useOn(bot.inventory().withName(item2).first());
    }

    protected void useItemItem(int item1, String item2) {
        bot.inventory().withId(item1).first().useOn(bot.inventory().withName(item2).first());
    }


    protected void take(RectangularArea area, String item) {
        walking.walkTo(area);
        bot.waitUntil(() -> bot.groundItems().withName(item).exists());
        bot.groundItems().withName(item).nearest().interact("Take");
        waitItem(item);
    }

    protected boolean hasItem(String item) {
        return bot.inventory().withName(item).exists();
    }

    protected void waitItem(String item) {
        var oldSize = bot.inventory().withName(item).size();
        bot.waitUntil(() -> bot.inventory().withName(item).size() > oldSize);
    }

    protected void useItemObject(Area area, String item, String object) {
        walking.walkTo(area);
        bot.inventory().withName(item).first().useOn(bot.objects().withName(object).nearest());
    }

    protected void useItemObject(Area area, int item, String object) {
        walking.walkTo(area);
        bot.inventory().withId(item).first().useOn(bot.objects().withName(object).nearest());
    }

    protected void interactNpc(RectangularArea area, String npc, String action) {
        walking.walkTo(area);
        bot.npcs().withName(npc).nearest().interact(action);
    }

    protected void interactNpc(RectangularArea area, int npc, String action) {
        walking.walkTo(area);
        bot.npcs().withId(npc).nearest().interact(action);
    }

    public void waitNpc(String name) {
        bot.waitUntil(() -> bot.npcs().withName("Restless ghost").exists());
    }

    public boolean hasItem(String name, int quantity) {
        return bot.inventory().withName(name).count() >= quantity;
    }

    protected void interactItem(String item, String action) {
        bot.inventory().withName(item).first().interact(action);
    }

    public static class ItemQuantity {
        public final int id;
        public int quantity;


        public ItemQuantity(int id, int quantity) {
            this.id = id;
            this.quantity = quantity;
        }
    }
}
