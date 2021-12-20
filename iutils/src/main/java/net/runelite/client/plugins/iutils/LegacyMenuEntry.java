package net.runelite.client.plugins.iutils;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.MenuAction;

@Getter
@Setter
public class LegacyMenuEntry {
    String option;
    String target;
    int identifier;
    MenuAction type;
    int opcode;
    int param0;
    int param1;
    boolean forceLeftClick;

    public LegacyMenuEntry(String option, String target, int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        this.option = option;
        this.target = target;
        this.identifier = identifier;
        this.type = type;
        this.param0 = param0;
        this.param1 = param1;
        this.forceLeftClick = forceLeftClick;
    }

    public LegacyMenuEntry(String option, String target, int identifier, int opcode, int param0, int param1, boolean forceLeftClick) {
        this.option = option;
        this.target = target;
        this.identifier = identifier;
        this.opcode = opcode;
        this.param0 = param0;
        this.param1 = param1;
        this.forceLeftClick = forceLeftClick;
    }

    public int getOpcode() {
        if (type != null) {
            return type.getId();
        } else {
            return opcode;
        }
    }

}