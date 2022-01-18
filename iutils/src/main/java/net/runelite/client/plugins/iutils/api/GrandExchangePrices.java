package net.runelite.client.plugins.iutils.api;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Obtains latest item prices from the <a href="https://oldschool.runescape.wiki/w/RuneScape:Real-time_Prices">OSRS Wiki API</a>.
 * @Author Runemoro
 */

@Slf4j
public class GrandExchangePrices {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ReentrantLock UPDATE_LOCK = new ReentrantLock();
    private static long lastUpdateTime = 0;
    private static AllPricesData data;

    public static ItemPrice get(int id) {
        UPDATE_LOCK.lock();

        try {
            if (System.currentTimeMillis() - lastUpdateTime > 5 * 60 * 1000) {
                update();
            }
        } finally {
            log.info("Unlocking price get thread");
            UPDATE_LOCK.unlock();
            log.info("Successfully unlocked");
        }
        log.info("Returning ItemPrice data for id: {} - {}", id, data.data.get(id).toString());
        return data.data.get(id);
    }

    private static void update() {
        try {
            log.info("Sending price update request");
            var json = HTTP_CLIENT.send(
                    HttpRequest.newBuilder(new URI("https://prices.runescape.wiki/api/v1/osrs/latest")).build(),
                    HttpResponse.BodyHandlers.ofString()
            ).body();
            log.info("Returned price update request");
            data = new Gson().fromJson(json, AllPricesData.class);
            lastUpdateTime = System.currentTimeMillis();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static class AllPricesData {
        private final Map<Integer, ItemPrice> data;

        private AllPricesData(Map<Integer, ItemPrice> data) {
            this.data = data;
        }
    }

    public static class ItemPrice {
        public final int high;
        public final int low;
        public final long highTime;
        public final int lowTime;

        private ItemPrice(int high, int low, long highTime, int lowTime) {
            this.high = high;
            this.low = low;
            this.highTime = highTime;
            this.lowTime = lowTime;
        }

        public String toString() {
            return low + "-" + high;
        }
    }
}
