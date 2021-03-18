package net.runelite.client.plugins.iutils.bot;

import net.runelite.api.widgets.Widget;
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

        throw new IllegalArgumentException("no action " + action + " on widget " + group + "." + file + (child == -1 ? "" : "[" + child + "]"));
    }
}
