package net.runelite.client.plugins.iquestassistant;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.plugins.iutils.game.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Dialogue {
    CLIENT_OF_KOUREND("Veos", "Sounds interesting! How can I help?", "Can you take me to Great Kourend?", "Have you got any quests for me?", "Let's talk about your client...", "I've lost something you've given me."),
    CLIENT_OF_KOUREND_1("Leenz", "Can I ask you about Port Piscarilius?", "Why should I gain favour with Port Piscarilius?"),
    CLIENT_OF_KOUREND_2("Horace", "Can I ask you about Hosidius?", "Why should I gain favour with Hosidius?"),
    CLIENT_OF_KOUREND_3("Jennifer", "Can I ask you about Shayzien?", "Why should I gain favour with Shayzien?"),
    CLIENT_OF_KOUREND_4("Munty", "Can I ask you about Lovakengj?", "Why should I gain favour with Lovakengj?"),
    CLIENT_OF_KOUREND_5("Regath", "Can I ask you about Arceuus?", "Why should I gain favour with Arceuus?"),

    COOKS_ASSISTANT(QuestVarPlayer.QUEST_COOKS_ASSISTANT, 0, "Cook", "I'll get right on it."),

    DEMON_SLAYER(QuestVarbits.QUEST_DEMON_SLAYER, 0, "Gypsy Aris", "Ok, here you go.", "Okay, where is he? I'll kill him for you!", "So how did Wally kill Delrith?"),
    DEMON_SLAYER_1(QuestVarbits.QUEST_DEMON_SLAYER, 1, "Sir Prysin", "Gypsy Aris said I should come and talk to you.", "I need to find Silverlight.", "He's back and unfortunately I've got to deal with him.", "So give me the keys!", "Can you give me your key?"),
    DEMON_SLAYER_2(QuestVarbits.QUEST_DEMON_SLAYER, 2, "Captain Rovin", "Yes I know, but this is important.", "There's a demon who wants to invade this city.", "Yes, very.", "It's not them who are going to fight the demon, it's me.", "Sir Prysin said you would give me the key.", "Why did he give you one of the keys then?"),
    DEMON_SLAYER_3(QuestVarbits.QUEST_DEMON_SLAYER, 2, "Traiborn", "I need to get a key given to you by Sir Prysin.", "Well, have you got any keys knocking around?", "I'll get the bones for you."),

    DORICS_QUEST(QuestVarPlayer.QUEST_DORICS_QUEST, 0, "Doric", "I wanted to use your anvils.", "Yes, I will get you the materials."),

    DRUIDIC_RITUAL(QuestVarPlayer.QUEST_DRUIDIC_RITUAL, 0, "Kaqemeex", "I'm in search of a quest.", "Okay, I will try and help."),
    DRUIDIC_RITUAL_1(QuestVarPlayer.QUEST_DRUIDIC_RITUAL, 1, "Sanfew", "I've been sent to help purify the Varrock stone circle."),
    DRUIDIC_RITUAL_2(QuestVarPlayer.QUEST_DRUIDIC_RITUAL, 2, "Sanfew", "Ok, I'll do that then."),

    DWARF_CANNON(QuestVarPlayer.QUEST_DWARF_CANNON, 0, "Captain Lawgof", "Sure, I'd be honoured to join."),
    DWARF_CANNON_1(QuestVarPlayer.QUEST_DWARF_CANNON, 6, "Captain Lawgof", "Okay, I'll see what I can do."),
    DWARF_CANNON_2(QuestVarPlayer.QUEST_DWARF_CANNON, 8, "Captain Lawgof", "Okay then, just for you!"),

    FAIRYTALE_PART_I(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 0, "Martin the Master Gardener", "Ask about the quest.", "Anything I can help with?", "Now that I think about it, " +
            "you're right!"),
    FAIRYTALE_PART_I_1(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 10, "Frizzy Skernip", "Are you a member of the Group of Advanced Gardeners?"),
    FAIRYTALE_PART_I_2(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 10, "Heskel", "Are you a member of the Group of Advanced Gardeners?"),
    FAIRYTALE_PART_I_3(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 10, "Dreven", "Are you a member of the Group of Advanced Gardeners?"),
    FAIRYTALE_PART_I_4(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 10, "Fayeth", "Are you a member of the Group of Advanced Gardeners?"),
    FAIRYTALE_PART_I_5(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 10, "Treznor", "Are you a member of the Group of Advanced Gardeners?"),
    FAIRYTALE_PART_I_6(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 10, "Martin the Master Gardener", "Ask about the quest."),
    FAIRYTALE_PART_I_7(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 20, "Fairy Godfather", "Where's the Fairy Queen?"),
    FAIRYTALE_PART_I_8(QuestVarbits.QUEST_FAIRYTALE_I_GROWING_PAINS, 50, "Malignius Mortifer", "I need help with fighting a Tanglefoot.",
            "I was asking you about fighting a Tanglefoot..."),

    FAIRYTALE_PART_II(QuestVarbits.QUEST_FAIRYTALE_II_CURE_A_QUEEN, 0, "Martin the Master Gardener", "Ask about the quest."),
    FAIRYTALE_PART_II_1(QuestVarbits.QUEST_FAIRYTALE_II_CURE_A_QUEEN, 10, "Martin the Master Gardener", "Ask about the quest.", "I suppose I'd better go and see what the problem is then."),
    FAIRYTALE_PART_II_2(QuestVarbits.QUEST_FAIRYTALE_II_CURE_A_QUEEN, 20, "Fairy Godfather", "Where is the Fairy Queen?", "Where could she have been taken to?", "Yes, okay."),

    NATURE_SPIRIT(QuestVarPlayer.QUEST_NATURE_SPIRIT, 0, "Drezel", "Well, what is it, I may be able to help?"),
    NATURE_SPIRIT_1(QuestVarPlayer.QUEST_NATURE_SPIRIT, 10, "Filliman Tarlock", "How long have you been a ghost?"),
    NATURE_SPIRIT_2(QuestVarPlayer.QUEST_NATURE_SPIRIT, 15, "Filliman Tarlock", "How long have you been a ghost?"),
    NATURE_SPIRIT_3(QuestVarPlayer.QUEST_NATURE_SPIRIT, 30, "Filliman Tarlock", "How can I help?"),
    NATURE_SPIRIT_4(QuestVarPlayer.QUEST_NATURE_SPIRIT, 40, "Filliman Tarlock", "I think I've solved the puzzle!"),

    PRIEST_IN_PERIL(QuestVarPlayer.QUEST_PRIEST_IN_PERIL, 0, "King Roald", "I'm looking for a quest!"),
    PRIEST_IN_PERIL_1(QuestVarPlayer.QUEST_PRIEST_IN_PERIL, 1, "Monk of Zamorak", "I'll get going.", "Roald sent me to check on Drezel.", "Sure. I'm a helpful person!"),
    PRIEST_IN_PERIL_2(QuestVarPlayer.QUEST_PRIEST_IN_PERIL, 4, "Drezel", "So, what now?", "Yes, of course."),

    RESTLESS_GHOST(QuestVarPlayer.QUEST_THE_RESTLESS_GHOST, 0, "Father Aereck", "I'm looking for a quest!", "Yes"),
    RESTLESS_GHOST_1(QuestVarPlayer.QUEST_THE_RESTLESS_GHOST, 1, "Father Urhney", "Father Aereck sent me to talk to you.", "He's got a ghost haunting his graveyard."),
    RESTLESS_GHOST_2(QuestVarPlayer.QUEST_THE_RESTLESS_GHOST, 2, "Restless ghost", "Yep, now tell me what the problem is."),

    ROMEO_AND_JULIET(QuestVarPlayer.QUEST_ROMEO_AND_JULIET, 0, "Romeo", "Yes, I have seen her actually!", "Yes, ok, I'll let her know."),
    ROMEO_AND_JULIET_1(QuestVarPlayer.QUEST_ROMEO_AND_JULIET, 10, "Juliet", "Ok, thanks."),
    ROMEO_AND_JULIET_2(QuestVarPlayer.QUEST_ROMEO_AND_JULIET, 30, "Father Lawrence", "Ok, thanks."),
    ROMEO_AND_JULIET_3(QuestVarPlayer.QUEST_ROMEO_AND_JULIET, 40, "Apothecary", "Talk about something else.", "Talk about Romeo & Juliet."),

    RUNE_MYSTERIES_3(QuestVarPlayer.QUEST_RUNE_MYSTERIES, 3, "Aubury", "I have been sent here with a package for you."),
    RUNE_MYSTERIES_4(QuestVarPlayer.QUEST_RUNE_MYSTERIES, 4, "Aubury"),

    THE_GRAND_TREE(QuestVarPlayer.QUEST_THE_GRAND_TREE, 0, "King Narnode Shareen", "You seem worried, what's up?", "I'd be happy to help!"),
    THE_GRAND_TREE_1(QuestVarPlayer.QUEST_THE_GRAND_TREE, 20, "King Narnode Shareen", "I think so!", "None of the above.", "A man came to me with the King's seal.", "I gave the man Daconia rocks.", "And Daconia rocks will kill the tree!"),
    THE_GRAND_TREE_2(QuestVarPlayer.QUEST_THE_GRAND_TREE, 80, "Captain Errdo", "Take me to Karamja please!"),
    THE_GRAND_TREE_3(QuestVarPlayer.QUEST_THE_GRAND_TREE, 80, "Foreman", "Sadly his wife is no longer with us!", "He loves worm holes.", "Anita."),
    THE_GRAND_TREE_4(QuestVarPlayer.QUEST_THE_GRAND_TREE, 80, "Anita", "I suppose so."),

    TREE_GNOME_VILLAGE(QuestVarPlayer.QUEST_TREE_GNOME_VILLAGE, 0, "King Bolren", "Can I help at all?", "I would be glad to help."),
    TREE_GNOME_VILLAGE_1(QuestVarPlayer.QUEST_TREE_GNOME_VILLAGE, 1, "Commander Montai", "Ok, I'll gather some wood."),
    TREE_GNOME_VILLAGE_2(QuestVarPlayer.QUEST_TREE_GNOME_VILLAGE, 3, "Commander Montai", "I'll try my best."),
    TREE_GNOME_VILLAGE_3(QuestVarPlayer.QUEST_TREE_GNOME_VILLAGE, 4, "Commander Montai", "0001", "0002", "0003", "0004"), //TODO
    TREE_GNOME_VILLAGE_4(QuestVarPlayer.QUEST_TREE_GNOME_VILLAGE, 6, "King Bolren", "I will find the warlord and bring back the orbs."), //TODO

    WATERFALL_QUEST(QuestVarPlayer.QUEST_WATERFALL_QUEST, 0, "Almera", "How can I help?"),

    WITCHS_HOUSE(QuestVarPlayer.QUEST_WITCHS_HOUSE, 0, "Boy", "What's the matter?", "Ok, I'll see what I can do."),

    WITCHS_POTION(QuestVarPlayer.QUEST_WITCHS_POTION, 0, "Hetty", "I am in search of a quest.", "Yes, help me become one with my darker side."),

    X_MARKS_THE_SPOT(QuestVarbits.QUEST_X_MARKS_THE_SPOT, 0, "Veos", "I'm looking for a quest.", "Sounds good, what do I do?", "Can I help?");

    @Getter(AccessLevel.PACKAGE)
    private QuestVarPlayer questVarPlayer;

    @Getter(AccessLevel.PACKAGE)
    private QuestVarbits questVarBits;

    @Getter(AccessLevel.PACKAGE)
    private int value;

    @Getter(AccessLevel.PACKAGE)
    private List<Integer> values = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    private final String npcName;

    @Getter(AccessLevel.PACKAGE)
    private final String[] dialogueOptions;

    Dialogue(final QuestVarPlayer questVarPlayer, final int value, final String npcName, String... dialogueOptions) {
        this.questVarPlayer = questVarPlayer;
        this.value = value;
        this.npcName = npcName;
        this.dialogueOptions = dialogueOptions;
    }

    Dialogue(final QuestVarbits questVarBits, final int value, final String npcName, String... dialogueOptions) {
        this.questVarBits = questVarBits;
        this.value = value;
        this.npcName = npcName;
        this.dialogueOptions = dialogueOptions;
    }

    Dialogue(final String npcName, String... dialogueOptions) {
        this.npcName = npcName;
        this.dialogueOptions = dialogueOptions;
    }

    public static String getDialogue(Game game, List<String> options) {
        for (Dialogue dialogue : values()) {
            List<String> dialogueOptions = new ArrayList<String>(Arrays.asList(dialogue.dialogueOptions));
            for (String option : options) {
                if (option.equals("Yes.")) return option;

                if (!dialogueOptions.contains(option)) continue;

                if (varMatch(game, dialogue) && game.npcs().withName(dialogue.npcName).exists()) {
                    return option;
                }
            }
        }
        return "";
    }

    private static boolean varMatch(Game game, Dialogue dialogue) {
        if (dialogue.questVarPlayer == null && dialogue.questVarBits == null) {
            return true;
        }

        if (dialogue.questVarBits != null && (game.varb(dialogue.questVarBits.getId()) == dialogue.value || dialogue.values.contains(dialogue.questVarBits.getId()))) {
            return true;
        }

        return dialogue.questVarPlayer != null && (game.varp(dialogue.questVarPlayer.getId()) == dialogue.value || dialogue.values.contains(dialogue.questVarPlayer.getId()));
    }
}
