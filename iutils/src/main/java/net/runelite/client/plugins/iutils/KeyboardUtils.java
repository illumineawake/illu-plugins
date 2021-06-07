package net.runelite.client.plugins.iutils;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

import javax.inject.Singleton;
import java.awt.event.KeyEvent;

@Singleton
public class KeyboardUtils {

    @Inject
    private Client client;

    /**
     * This method must be called on a new
     * thread, if you try to call it on
     * {@link ClientThread}
     * it will result in a crash/desynced thread.
     */
    public void typeString(String string) {
        assert !client.isClientThread();

        for (char c : string.toCharArray()) {
            pressKey(c);
        }
    }

    public void pressKey(char key) {
        keyEvent(401, key);
        keyEvent(402, key);
        keyEvent(400, key);
    }

    public void pressKey(int key) {
        keyEvent(401, key);
        keyEvent(402, key);
        //keyEvent(400, key);
    }

    private void keyEvent(int id, char key) {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, KeyEvent.VK_UNDEFINED, key
        );
        client.getCanvas().dispatchEvent(e);
    }

    private void keyEvent(int id, int key) {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, key, KeyEvent.CHAR_UNDEFINED
        );
        client.getCanvas().dispatchEvent(e);
    }
}
