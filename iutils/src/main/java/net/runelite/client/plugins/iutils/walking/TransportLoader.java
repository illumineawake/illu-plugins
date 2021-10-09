package net.runelite.client.plugins.iutils.walking;

import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.ui.Chatbox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class TransportLoader {
    public static final int COINS = 995;

    public static class SpiritTree {
        private Position position;
        private String location;

        public SpiritTree(Position position, String location) {
            this.position = position;
            this.location = location;
        }
    }

    public static final List<SpiritTree> SPIRIT_TREES = List.of(
            new SpiritTree(new Position(2542, 3170, 0), "Tree gnome Village"),
            new SpiritTree(new Position(2461, 3444, 0), "Gnome Stronghold"),
            new SpiritTree(new Position(2555, 3259, 0), "Battlefield of Khazard"),
            new SpiritTree(new Position(3185, 3508, 0), "Grand Exchange"),
            new SpiritTree(new Position(2488, 2850, 0), "Feldip Hills")
    );

    public static List<Transport> buildTransports(Game game) {
        var transports = new ArrayList<Transport>();

        try {
            for (var line : new String(TransportLoader.class.getResourceAsStream("/transports.txt").readAllBytes()).split("\n")) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                transports.add(parseTransportLine(line));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Edgeville
        if (game.modifiedLevel(Skill.AGILITY) >= 21 && game.membersWorld()) {
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
        if (game.inventory().withId(COINS).quantity() >= 10000) {
            transports.add(npcTransport(new Position(1782, 3458, 0), new Position(1778, 3417, 0), 7483, "Travel"));
        }

        transports.add(npcTransport(new Position(1779, 3418, 0), new Position(1784, 3458, 0), 7484, "Travel"));

        // Port Sarim
        if (game.varb(4897) == 0) {
            if (game.varb(8063) >= 7) { // todo: lower?
                transports.add(npcChatTransport(new Position(3054, 3245, 0), new Position(1824, 3691, 0), 8484, "Can you take me to Great Kourend?"));
            } else {
                transports.add(npcChatTransport(new Position(3054, 3245, 0), new Position(1824, 3691, 0), 8484, "That's great, can you take me there please?"));
            }
        } else {
            transports.add(npcTransport(new Position(3054, 3245, 0), new Position(1824, 3695, 1), "Veos", "Port Piscarilius"));
        }

        //White Wolf Mountain Tunnel
        if (game.varp(11) >= 5) {
            transports.add(objectTransport(new Position(2877, 3482, 0), new Position(2876, 9878, 0), 57, "Climb-down"));
            transports.add(objectTransport(new Position(2876, 9878, 0), new Position(2877, 3482, 0), 56, "Climb-up"));
        }

        //entrana
        transports.add(npcActionTransport(new Position(3041, 3237, 0), new Position(2834, 3331, 1), 1166, "Take-boat"));
        transports.add(npcActionTransport(new Position(2834, 3335, 0), new Position(3048, 3231, 1), 1170, "Take-boat"));
        transports.add(npcChatTransport(new Position(2821, 3374, 0), new Position(2822, 9774, 0), 1164, "Well that is a risk I will have to take."));

        // Paterdomus
        transports.add(trapdoorTransport(new Position(3405, 3506, 0), new Position(3405, 9906, 0), 1579, 1581));
        transports.add(trapdoorTransport(new Position(3423, 3485, 0), new Position(3440, 9887, 0), 3432, 3433));
        transports.add(trapdoorTransport(new Position(3422, 3484, 0), new Position(3440, 9887, 0), 3432, 3433));

        // Port Piscarilius
        transports.add(npcTransport(new Position(1824, 3691, 0), new Position(3055, 3242, 1), "Veos", "Port Sarim"));

        // Meyerditch
        transports.add(new Transport(
                new Position(3638, 3251, 0),
                game.varb(6396) >= 105 ? new Position(3629, 9644, 0) : new Position(3626, 9618, 0),
                0, 0,
                g -> {
                    if (g.varb(2590) < 1) {
                        g.objects().withId(18146).nearest().interact("Press");
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

        if (game.varb(2573) >= 320) {
            transports.add(parseTransportLine("3649 3220 0 3631 3219 0 Enter Door 32660"));
            transports.add(parseTransportLine("3631 3219 0 3649 3219 0 Enter Door 32659"));
            transports.add(parseTransportLine("3649 3219 0 3631 3219 0 Enter Door 32660"));
            transports.add(parseTransportLine("3631 3220 0 3649 3219 0 Enter Door 32659"));
            transports.add(parseTransportLine("3649 3218 0 3631 3219 0 Enter Door 32660"));
            transports.add(parseTransportLine("3631 3218 0 3649 3219 0 Enter Door 32659"));
        }

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

        // Tree Gnome Village
        transports.add(objectChatTransport(new Position(2461, 3382, 0), new Position(2461, 3385, 0), 190, "Sorry, I'm a bit busy."));
        if (game.varp(111) > 0) {
            transports.add(npcTransport(new Position(2504, 3192, 0), new Position(2515, 3159, 0), 4968, "Follow"));
            transports.add(npcTransport(new Position(2515, 3159, 0), new Position(2504, 3192, 0), 4968, "Follow"));
        }

        //Spirit Tree's
        if (game.varp(111) == 9 && game.membersWorld()) {
            for (var source : SPIRIT_TREES) {
                if (source.location.equals("Gnome Stronghold") && game.varp(150) < 160) {
                    continue;
                }
                for (var target : SPIRIT_TREES) {
                    transports.add(spritTreeTransport(source.position, target.position, target.location));
                }
            }
        }

        // Mort Myre Swamp
        if (game.varb(3870) != 7) {
            transports.add(objectWarningTransport(new Position(3444, 3458, 0), new Position(3444, 3457, 0), 3506, "Open", 580, 17));
            transports.add(objectWarningTransport(new Position(3443, 3458, 0), new Position(3444, 3457, 0), 3507, "Open", 580, 17));
        } else {
            transports.add(objectTransport(new Position(3444, 3458, 0), new Position(3444, 3457, 0), 3506, "Open"));
            transports.add(objectTransport(new Position(3443, 3458, 0), new Position(3443, 3457, 0), 3507, "Open"));
        }

        // Canifis
        if (game.varp(387) >= 110) {
            transports.add(objectTransport(new Position(3495, 3465, 0), new Position(3477, 9845, 0), 5055, "Open"));
        }

        // Burgh de Rott
        transports.add(trapdoorTransport(new Position(3491, 3232, 0), new Position(3491, 9632, 1), 12744, 12745)); // Before cleaning rubble
        transports.add(trapdoorTransport(new Position(3491, 3232, 0), new Position(3490, 9631, 0), 12744, 12745)); // After cleaning rubble

        // Meyerditch
        transports.add(meyerditchFloorTransport(new Position(3590, 3173, 1)));
        transports.add(meyerditchFloorTransport(new Position(3589, 3174, 1)));
        transports.add(meyerditchFloorTransport(new Position(3589, 3173, 1)));
        transports.add(meyerditchFloorTransport(new Position(3588, 3173, 1)));

        // TODO: these doors are only available after darkness of hallowvale (or some step in it?)
        //   as well as a few others that are currently still in transport.txt
//        3625 3223 0 3625 3224 0 Open Door 18057
//        3625 3224 0 3625 3223 0 Open Door 18057

        transports.add(new Transport(
                new Position(3589, 3214, 0),
                new Position(3589, 3215, 0),
                Integer.MAX_VALUE,
                0,
                g -> {
                    g.objects().withName("Rocky surface").nearest().interact("Search");
                    new Chatbox(g).chat();
                    g.objects().withId(18054).nearest().interact("Open"); //TODO may require a check against with baseId
                }
        ));

        if (game.varb(7255) >= 62) {
            transports.add(parseTransportLine("3492 9862 0 3513 9811 0 Enter Cave entrance 12771"));
        }

        var icyeneGraveyardSources = List.of(new Position(3684, 3176, 0), new Position(3684, 3175, 0), new Position(3684, 3174, 0));
        var icyeneGraveyardTarget = game.varb(7255) <= 44 ? new Position(3684, 3181, 0) : new Position(3684, 3176, 0);

        var meiyerditchSources = List.of(new Position(3605, 3161, 1), new Position(3605, 3163, 1));
        var meyerditchTarget = new Position(3605, 3161, 1);

        var burghDeRottSource = new Position(3525, 3170, 0);
        var burghDeRottTarget = new Position(3525, 3170, 0);

        if (game.varb(7255) < 42) {
            transports.add(objectTransport(burghDeRottSource, meyerditchTarget, 12945, "Board"));

            for (var meiyerditchSource : meiyerditchSources) {
                transports.add(objectTransport(meiyerditchSource, burghDeRottTarget, 17961, "Board"));
            }
        } else {
            transports.add(objectChatTransport(burghDeRottSource, meyerditchTarget, 12945, "Board", "Meiyerditch."));
            transports.add(objectChatTransport(burghDeRottSource, icyeneGraveyardTarget, 12945, "Board", "Icyene Graveyard."));

            for (var meiyerditchSource : meiyerditchSources) {
                transports.add(objectChatTransport(meiyerditchSource, burghDeRottTarget, 17961, "Board", "Burgh de Rott."));
                transports.add(objectChatTransport(meiyerditchSource, icyeneGraveyardTarget, 17961, "Board", "Icyene Graveyard."));
            }

            for (var source : icyeneGraveyardSources) {
                transports.add(objectChatTransport(source, burghDeRottTarget, 39520, "Board", "Burgh de Rott."));
                transports.add(objectChatTransport(source, meyerditchTarget, 39520, "Board", "Meiyerditch."));
            }
        }


        // darkmeyer wall (needs long rope)
//        3672 3376 0 3670 3375 0 Climb Wall 39541
//        3672 3374 0 3670 3375 0 Climb Wall 39541

        return transports;
    }

    private static Transport meyerditchFloorTransport(Position source) {
        return new Transport(
                source,
                new Position(3588, 3173, 0),
                Integer.MAX_VALUE,
                0,
                g -> {
                    if (g.objects().withId(18122).nearest().id() == 18123) {
                        g.objects().withId(18122).nearest().interact("Search");
                        new Chatbox(g).chat("Yes.");
                        g.waitUntil(() -> g.objects().withId(18122).nearest().id() != 18123);
                    }

                    g.objects().withId(18122).nearest().interact("Climb-down");
                }
        );
    }

    private static Transport spritTreeTransport(Position source, Position target, String location) {
        return new Transport(
                source,
                target,
                Integer.MAX_VALUE,
                0,
                game -> {
                    game.objects().withName("Spirit tree").nearest(source).interact("Travel");
                    game.waitUntil(() -> game.screenContainer().nestedInterface() == 187);

                    if (game.screenContainer().nestedInterface() == 187) {
                        new Chatbox(game).selectMenu(location);
                        game.waitUntil(() -> target.contains(game.localPlayer().position()));
                    }
                });
    }

    private static Transport objectWarningTransport(Position source, Position target, int id, String action, int interfaceId, int widgetId) {
        return new Transport(
                source,
                target,
                Integer.MAX_VALUE,
                0,
                game -> {
                    game.objects().withId(id).nearest(source).interact(action);
                    game.waitUntil(() -> target.contains(game.localPlayer().position()) || game.screenContainer().nestedInterface() == interfaceId);

                    if (game.screenContainer().nestedInterface() == interfaceId) {
                        game.widget(interfaceId, widgetId).interact("Yes");
                        game.waitUntil(() -> game.screenContainer().nestedInterface() == -1);
                    }
                });
    }

    private static Transport parseTransportLine(String line) {
        var parts = line.split(" ");

        return objectTransport(
                new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])),
                new Position(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5])),
                Integer.parseInt(parts[parts.length - 1]),
                parts[6].replace('_', ' ')
        );
    }

    private static Transport trapdoorTransport(Position source, Position target, int closedId, int openId) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, game -> {
            if (game.objects().withId(closedId).inside(source.areaWithin(1)).exists()) {
                game.objects().withId(closedId).inside(source.areaWithin(1)).nearest().interact("Open");
                game.waitUntil(() -> !game.objects().withId(closedId).inside(source.areaWithin(1)).exists());
            }
            game.objects().withId(openId).nearest().interact("Climb-down");
            game.tick(1);
        });
    }

    private static Transport npcTransport(Position source, Position target, String npcName, String action) {
        return new Transport(source, target, 10, 0, game -> game.npcs().withName(npcName).withAction(action).nearest(source).interact(action));
    }

    private static Transport npcTransport(Position source, Position target, int id, String action) {
        return new Transport(source, target, 10, 0, game -> game.npcs().withId(id).nearest(source).interact(action));
    }

    private static Transport npcChatTransport(Position source, Position target, int id, String... options) {
        return new Transport(source, target, 10, 0, game -> {
            game.npcs().withId(id).nearest(target).interact("Talk-to");
            new Chatbox(game).chat(options);
        });
    }

    private static Transport npcActionTransport(Position source, Position target, int id, String action) {
        return new Transport(source, target, 10, 0, game -> game.npcs().withId(id).nearest(target).interact(action));
    }

    private static Transport objectTransport(Position source, Position target, int id, String action) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, game -> {
            game.objects().withId(id).nearest(source).interact(action);
        });
    }

    private static Transport objectChatTransport(Position source, Position target, int id, String action, String... options) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, game -> {
            game.objects().withId(id).nearest(source).interact(action);
            var chatbox = new Chatbox(game);

            if (game.waitUntil(() -> chatbox.chatState() != Chatbox.ChatState.CLOSED, 8)) { // in case still doing the previous transport
                chatbox.chat(options);
            }

            game.tick(8);
        });
    }

    private static Transport itemObjectTransport(Position source, Position target, int item, int object) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, game -> game.inventory().withId(item).first().useOn(game.objects().withId(object).nearest(source)));
    }

    private static Transport objectChatTransport(Position source, Position target, int id, String action, BooleanSupplier stopCondition, int maxTimeout, String... chatOptions) {
        return new Transport(source, target, Integer.MAX_VALUE, 0, game -> {
            game.objects().withId(id).nearest(source).interact(action);
            var chatbox = new Chatbox(game);

            if (game.waitUntil(() -> chatbox.chatState() != Chatbox.ChatState.CLOSED, 8)) { // in case still doing the previous transport
                chatbox.chat(chatOptions);
            }

            game.waitUntil(stopCondition, maxTimeout);
        });
    }
}
