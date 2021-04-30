package net.runelite.client.plugins.iutils.bot;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.iutils.api.Interactable;

import java.util.*;
import java.util.stream.Collectors;

public class iWidget implements Interactable, Useable {

    private final Bot bot;
    private final Widget widget;
    Map<Integer, iWidget> children = new HashMap<>();

    public iWidget(Bot bot, Widget widget) {
        this.bot = bot;
        this.widget = widget;
    }

    public Bot bot() {
        return bot;
    }

    public Client client() {
        return bot.client();
    }

    public int id() {
        return widget.getId();
    }

    public int itemId() {
        return widget.getItemId();
    }

    public int index() { return bot.getFromClientThread(() -> widget.getIndex()); }

    public int x() {
        return widget.getOriginalX();
    }

    public int y() {
        return widget.getOriginalY();
    }

    public String text() { return widget.getText(); }

    public int quantity() { return widget.getItemQuantity(); }

    public boolean hidden() {
        if (widget == null) {
            System.out.println("Widget is null");
        }
        return bot.getFromClientThread(widget::isHidden);
    }

    public List<WidgetItem> getWidgetItems() {
        ArrayList<WidgetItem> items = new ArrayList<>();

        for (WidgetItem slot : widget.getWidgetItems()) {
            if (slot != null) {
                items.add(slot);
            }
        }
        return items;
    }

    public List<iWidget> items() {
        ArrayList<iWidget> items = new ArrayList<>();

        for (Widget slot : widget.getDynamicChildren()) {
            if (slot != null) {
                items.add(new iWidget(bot(), slot));
            }
        }
        return items;
    }
//        return widget.getDynamicChildren(); }

    public int nestedInterface() {
        Widget[] nested = bot.getFromClientThread(widget::getNestedChildren);

        if (nested.length == 0) {
            return -1;
        }

        return nested[0].getId() >> 16;
    }

    @Override
    public List<String> actions() {
        return Arrays.stream(widget.getActions())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void interact(String action) {
        String[] actions = widget.getActions();

        for (int i = 0; i < actions.length; i++) {
            if (action.equalsIgnoreCase(actions[i])) {
                interact(i);
                return;
            }
        }

        throw new IllegalArgumentException("no action " + action + " on widget " + widget.getParentId() + "." + widget.getId());
        //        throw new IllegalArgumentException("no action " + action + " on widget " + widget.getParentId() + "." + file + (child == -1 ? "" : "[" + child + "]"));
    }

    public void interact(int action) {
        bot().clientThread.invoke(() -> {
            //TODO action might not require + 1 and param0 need to confirm returns -1 or child
            client().invokeMenuAction("", "",
                    action + 1,
                    MenuAction.CC_OP.getId(),
                    index(),
                    id()
            );
        });
    }

    public void select() {
        bot().clientThread.invoke(() -> {
            client().invokeMenuAction("", "",
                    0,
                    MenuAction.WIDGET_TYPE_6.getId(),
                    index(),
                    id()
            );
        });
    }

    public Widget child(int child) { //TODO untested
        var c = widget.getDynamicChildren()[child];

        if (c == null) {
            this.children.put(child, this);
        }

        return c;
    }

    public List<iWidget> children() { //TODO untested
        return new ArrayList<>(children.values());
    }

    @Override
    public void useOn(InventoryItem item) {
        bot.clientThread.invoke(() -> {
            bot.client.setSelectedSpellWidget(id());
            bot.client.setSelectedSpellChildIndex(-1);
            bot.client.invokeMenuAction("", "", item.id(),
                    MenuAction.ITEM_USE_ON_WIDGET.getId(), item.slot(), WidgetInfo.INVENTORY.getId());
        });
    }

    @Override
    public void useOn(iNPC npc) {
        bot.clientThread.invoke(() -> {
            bot.client.setSelectedSpellWidget(id());
            bot.client.setSelectedSpellChildIndex(-1);
            bot.client.invokeMenuAction("", "", npc.index(),
                    MenuAction.SPELL_CAST_ON_NPC.getId(), 0, 0);
        });
    }

    @Override
    public void useOn(iObject object) {
        bot.clientThread.invoke(() -> {
            bot.client.setSelectedSpellWidget(id());
            bot.client.setSelectedSpellChildIndex(-1);
            bot.client.invokeMenuAction("", "", object.id(),
                    MenuAction.SPELL_CAST_ON_GAME_OBJECT.getId(), object.menuPoint().getX(), object.menuPoint().getY());
        });
    }
}
