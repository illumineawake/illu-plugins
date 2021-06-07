package net.runelite.client.plugins.iutils.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RandomizedStreamAdapter<T, S extends RandomizedStreamAdapter<T, S>> extends StreamAdapter<T, S> {
    protected RandomizedStreamAdapter(Stream<T> stream) {
        super(stream);
    }

    public S shuffled() {
        return wrap(collect(Collectors.collectingAndThen(Collectors.toList(), (List<T> collected) -> {
            Collections.shuffle(collected);
            return collected.stream();
        })));
    }

    public int size() {
        return (int) count();
    }

    public T first() {
        return findFirst().orElse(null);
    }

    public List<T> all() {
        return collect(Collectors.toList());
    }

    public boolean exists() {
        return findFirst().isPresent();
    }

    public T random() {
        return shuffled().findAny().orElse(null);
    }

    public Optional<T> random(ToIntFunction<T> weigher) {
        class Entry {
            final T element;
            final int weight;

            Entry(T element, int weight) {
                this.element = element;
                this.weight = weight;
            }
        }

        List<Entry> entries = map(e -> new Entry(e, weigher.applyAsInt(e))).collect(Collectors.toList());

        if (entries.isEmpty()) {
            return Optional.empty();
        }

        int i = new Random().nextInt(entries.stream().mapToInt(e -> e.weight).max().orElseThrow());

        int current = 0;

        for (Entry entry : entries) {
            current += entry.weight;

            if (current >= i) {
                return Optional.of(entry.element);
            }
        }

        throw new AssertionError();
    }
}
