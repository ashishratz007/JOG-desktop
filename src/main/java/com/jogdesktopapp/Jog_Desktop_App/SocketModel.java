package com.jogdesktopapp.Jog_Desktop_App;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class SocketModel extends WebSocketClient {
    private static SocketModel instance;
    private static final String SERVER_URL = "ws://socket.jog-joinourgame.com:8080";
    private static final int RECONNECT_DELAY = 5000; // 5 seconds

    private SocketModel(URI serverUri) {
        super(serverUri);
    }

    public static SocketModel getInstance() {
        if (instance == null) {
            try {
                instance = new SocketModel(new URI(SERVER_URL));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("✅ Connected to WebSocket Server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("📩 Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("❌ Disconnected: " + reason);
        reconnect();
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("⚠️ Error: " + ex.getMessage());
        reconnect();
    }

    public void reconnect() {
        System.out.println("🔄 Attempting to reconnect in " + (RECONNECT_DELAY / 1000) + " seconds...");
        try {
            Thread.sleep(RECONNECT_DELAY);
            instance = new SocketModel(new URI(SERVER_URL));
            instance.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketModel socket = SocketModel.getInstance();
        socket.connect();
    }
}
