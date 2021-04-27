package net.runelite.client.plugins.iutils.walking;

import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.bot.Bot;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.ui.Chatbox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class TransportLoader {
    public static final int COINS = 995;

    public static List<Transport> buildTransports(Bot bot) {
        var transports = new ArrayList<Transport>();

        try {
            for (var line : new String(TransportLoader.class.getResourceAsStream("/transports.txt").readAllBytes()).split("\n")) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                var parts = line.split(" ");

                transports.add(objectTransport(
                        new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])),
                        new Position(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5])),
                        Integer.parseInt(parts[parts.length - 1]),
                        parts[6].replace('_', ' '))
                );
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Edgeville
        if (bot.modifiedLevel(Skill.AGILITY) >= 21) {
            transports.add(objectTransport(new Position(3142, 3513, 0), new Position(3137, 3516, 0), 16530, "Climb-into"));
            transports.add(objectTransport(new Position(3137, 3516, 0), new Position(3142, 3513, 0), 16529, "Climb-into"));
        }

        // Glarial's Tomb
        transports.add(itemObjectTransport(new Position(2557, 3444, 0), new Position(2555, 9844, 0), 294, 1992));
        transports.add(itemObjectTransport(new Position(2557, 3445, 0), new Position(2555, 9844, 0), 294, 1992));
        transports.add(itemObjectTransport(new Position(2558, 3443, 0), new Position(2555, 9844, 0), 294, 1992));
        transports.add(itemObjectTransport(new Position(2559, 3443, 0), new Position(2555, 9844, 0), 294, 1992));
        transports.add(itemObjectTransport(new Position(2560, 3444, 0), new Position(2555, 9844, 0), 294, 1992));
        transports.add(itemObjectTransport(new Position(2560, 3445, 0), new Position(2555, 9844, 0), 294, 1992));
        transports.add(itemObjectTransport(new Position(2558, 3446, 0), new Position(2555, 9844, 0), 294, 1992));
        transports.add(itemObjectTransport(new Position(2559, 3446, 0), new Position(2555, 9844, 0), 294, 1992));

        // Waterfall Island
        transports.add(itemObjectTransport(new Position(2512, 3476, 0), new Position(2513, 3468, 0), 954, 1996));
        transports.add(itemObjectTransport(new Position(2512, 3466, 0), new Position(2511, 3463, 0), 954, 2020));

        // Crabclaw isle
        if (bot.inventory().withId(COINS).quantity() >= 10000) {
            transports.add(npcTransport(new Position(1782, 3458, 0), new Position(1778, 3417, 0), 7483, "Travel"));
        }

        transports.add(npcTransport(new Position(1779, 3418, 0), new Position(1784, 3458, 0), 7484, "Travel"));

        // Port Sarim
        if (bot.varb(4897) == 0) {
            if (bot.varb(8063) >= 7) { // todo: lower?
                transports.add(npcChatTransport(new Position(3054, 3245, 0), new Position(1824, 3691, 0), 8484, "Can you take me to Great Kourend?"));
            } else {
                transports.add(npcChatTransport(new Position(3054, 3245, 0), new Position(1824, 3691, 0), 8484, "That's great, can you take me there please?"));
            }
        } else {
            transports.add(npcTransport(new Position(3054, 3245, 0), new Position(1824, 3695, 1), 8630, "Port Piscarilius"));
        }

        //entrana
        transports.add(npcActionTransport(new Position(3041, 3237, 0), new Position(2834, 3331, 1), 1166, "Take-boat"));
        transports.add(npcActionTransport(new Position(2834, 3335, 0), new Position(3048, 3231, 1), 1170, "Take-boat"));
        transports.add(npcChatTransport(new Position(2821, 3374, 0), new Position(2822, 9774, 0), 1164, "Well that is a risk I will have to take."));

        // Paterdomus
        transports.add(trapdoorTransport(new Position(3405, 3506, 0), new Position(3405, 9906, 0), 1579, 1581));
        transports.add(trapdoorTransport(new Position(3423, 3485, 0), new Position(3440, 9887, 0), 3432, 3433));

        //in aid of myreque
        transports.add(trapdoorTransport(new Position(3491, 3232, 0), new Position(14723, 2000, 0), 12743, 12743));

        // Port Piscarilius
        transports.add(npcTransport(new Position(1824, 3691, 0), new Position(3055, 3242, 1), 2147, "Port Sarim"));

        // Meyerditch
        transports.add(new Transport(
                new Position(3638, 3251, 0),
                new Position(3626, 9618, 0),
                0, 0,
                g -> {
                    if (g.varb(2590) < 1) {
                        g.objects().withId(17562).nearest().interact("Press");
                        g.waitUntil(() -> g.varb(2590) == 1);
                    }

                    if (g.varb(2590) < 2) {
                        g.objects().withId(18120).nearest().interact("Open");
                        g.waitUntil(() -> g.varb(2590) == 2);
                    }

                    g.objects().withId(18120).nearest().interact("Climb-down");

                    if (g.varb(6396) >= 90) {
                        new Chatbox(g).chat("Yes.");
                    }
                }
        ));

        transports.add(trapdoorTransport(new Position(3606, 3215, 0), new Position(3603, 9611, 0), 32577, 32578));

        /*
         * TODO: if the target is in the wilderness, add these
         * 2561 3311 0 3154 3924 0 Pull Lever 1814
         * 3153 3923 0 2562 3311 0 Pull Lever 1815
         * 3067 10253 0 2271 4680 0 Pull Lever 1816
         * 2271 4680 0 3067 10254 0 Pull Lever 1817
         * 2539 4712 0 3090 3956 0 Pull Lever 5960
         * 3090 3956 0 2539 4712 0 Pull Lever 5959
         * 3090 3475 0 3154 3924 0 Pull Lever 26761
         *
         * wilderness ditch
         */

        return transports;
    }


    private static Transport trapdoorTransport(Position source, Position target, int closedId, int openId) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, bot -> {
            if (bot.objects().withId(closedId).inside(source.areaWithin(1)).exists()) {
                bot.objects().withId(closedId).inside(source.areaWithin(1)).nearest().interact("Open");
                bot.waitUntil(() -> !bot.objects().withId(closedId).inside(source.areaWithin(1)).exists());
            }
            bot.objects().withId(openId).nearest().interact("Climb-down");
        });
    }

    private static Transport npcTransport(Position source, Position target, int id, String action) {
        return new Transport(source, target, 10, 0, bot -> bot.npcs().withId(id).nearest(source).interact(action));
    }

    private static Transport npcChatTransport(Position source, Position target, int id, String... options) {
        return new Transport(source, target, 10, 0, bot -> {
            bot.npcs().withId(id).nearest(target).interact("Talk-to");
            new Chatbox(bot).chat(options);
        });
    }

    private static Transport npcActionTransport(Position source, Position target, int id, String action) {
        return new Transport(source, target, 10, 0, bot -> bot.npcs().withId(id).nearest(target).interact(action));
    }

    private static Transport objectTransport(Position source, Position target, int id, String action) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, bot -> bot.objects2().withId(id).nearest(source).interact(action));
    }

    private static Transport objectChatTransport(Position source, Position target, int id, String action, String... options) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, bot -> {
            bot.objects2().withId(id).nearest(source).interact(action);
            new Chatbox(bot).chat(options);
        });
    }

    private static Transport itemObjectTransport(Position source, Position target, int item, int object) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, bot -> bot.inventory().withId(item).first().useOn(bot.objects().withId(object).nearest(source)));
    }
}
