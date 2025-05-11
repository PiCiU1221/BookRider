package edu.zut.bookrider.security.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = (String) session.getAttributes().get("username");

        if (isEmail(username)) {
            String[] parts = username.split(":");
            username = parts[0];
        }

        String query = session.getUri().getQuery();
        String channel = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("channel")) {
                    channel = pair[1];
                }
            }
        }

        if (username != null && channel != null) {
            sessions.put(username + ":" + channel, session);
        }
    }

    private boolean isEmail(String identifier) {
        return identifier.contains("@") && identifier.contains(".");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().remove(session);
    }

    public void sendRefreshSignal(String username, String channel) {
        WebSocketSession session = sessions.get(username + ":" + channel);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("refresh"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
