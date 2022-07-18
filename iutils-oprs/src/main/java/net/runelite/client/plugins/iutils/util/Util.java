package net.runelite.client.plugins.iutils.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Util {
    public static void sleep(long time) {
        if (time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);// todo
            }
        }
    }

    public static byte[] gzip(byte[] bytes) throws IOException {
        var baos = new ByteArrayOutputStream();

        try (var gzip = new GZIPOutputStream(baos)) {
            gzip.write(bytes);
        }

        return baos.toByteArray();
    }

    public static byte[] ungzip(byte[] bytes) throws IOException {
        return new GZIPInputStream(new ByteArrayInputStream(bytes)).readAllBytes();
    }
}
