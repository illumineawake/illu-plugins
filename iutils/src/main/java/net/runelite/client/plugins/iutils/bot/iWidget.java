package net.runelite.client.plugins.iutils.bot;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.iutils.api.Interactable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class iWidget implements Interactable, Useable {

    private final Bot bot;
    private final Widget widget;

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

    public int x() {
        return widget.getOriginalX();
    }

    public int y() {
        return widget.getOriginalY();
    }

    public boolean hidden() {
        return widget.isHidden();
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
                    widget.getIndex(),
                    widget.getId()
            );
        });
    }
}
