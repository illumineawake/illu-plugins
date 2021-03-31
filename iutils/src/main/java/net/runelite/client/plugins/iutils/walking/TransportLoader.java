package net.runelite.client.plugins.iutils.walking;

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
        List<Transport> transports = new ArrayList<>();

        try {
            for (String line : new String(TransportLoader.class.getResourceAsStream("/transports.txt").readAllBytes()).split("\n")) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(" ");

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
            transports.add(npcChatTransport(new Position(3054, 3245, 0), new Position(1824, 3691, 0), 8484, "That's great, can you take me there please?"));
        } else {
            transports.add(npcTransport(new Position(3054, 3245, 0), new Position(1824, 3695, 1), 8630, "Port Piscarilius"));
        }

        // Port Piscarilius
        transports.add(npcTransport(new Position(1824, 3691, 0), new Position(3055, 3242, 1), 2147, "Port Sarim"));

        return transports;
    }

    private static Transport npcTransport(Position source, Position target, int id, String action) {
        return new Transport(source, target, 1, 10, bot -> bot.npcs().withId(id).nearest(source).interact(action));
    }

    private static Transport npcChatTransport(Position source, Position target, int id, String... options) {
        return new Transport(source, target, 1, 10, bot -> {
            bot.npcs().withId(id).nearest(target).interact("Talk-to");
            new Chatbox(bot).chat(options);
        });
    }

    private static Transport objectTransport(Position source, Position target, int id, String action) {
        return new Transport(source, target, 1, Integer.MAX_VALUE, bot -> bot.objects().withId(id).nearest(source).interact(action));
    }

    private static Transport itemObjectTransport(Position source, Position target, int item, int object) {
        return new Transport(source, target, 1, Integer.MAX_VALUE, bot -> bot.inventory().withId(item).first().useOn(bot.objects().withId(object).nearest(source)));
    }
}
