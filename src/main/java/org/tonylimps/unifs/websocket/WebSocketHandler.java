package org.tonylimps.unifs.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tonylimps.unifs.mapper.UserRoomMapper;
import org.tonylimps.unifs.model.pojo.Command;
import org.tonylimps.unifs.service.TicketService;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final TicketService ticketService;
    private final JsonMapper jsonMapper;
    private final UserRoomMapper userRoomMapper;
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketHandler(JsonMapper jsonMapper, UserRoomMapper userRoomMapper, TicketService ticketService) {
        this.jsonMapper = jsonMapper;
        this.userRoomMapper = userRoomMapper;
        this.ticketService = ticketService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        Command command = jsonMapper.readValue(message.getPayload(), Command.class);
        if (!auth(session, command)) return;
        String type = command.getType();
        Long from = command.getFrom();
        Long to = command.getTo();
        switch (type) {
            case Command.Type.OFFER, Command.Type.BYE -> broadcast(message, from);
            case Command.Type.ANSWER, Command.Type.CANDIDATE -> forward(message, to);
        }
    }

    private void broadcast(TextMessage message, Long from) {
        Set<Long> targetUsers = userRoomMapper.selectSameRoomUserIds(from);
        targetUsers.forEach(targetUser -> forward(message, targetUser));
    }

    private void forward(TextMessage message, Long to) {
        try {
            sessions.get(to).sendMessage(message);
        } catch (IOException e) {
            log.warn("Forward message failed: {} Content: {}", e.getMessage(), message.getPayload());
        }
    }

    private boolean auth(WebSocketSession session, Command command) {
        Long id = (Long) session.getAttributes().get("userId");
        if (id != null && sessions.containsKey(id)) return true;

        String type = command.getType();
        if (!type.equals(Command.Type.AUTH)) return false;

        String ticket = command.getTicket();
        id = ticketService.consume(ticket);
        if (id == null) return false;
        if (!command.getFrom().equals(id)) return false;

        sessions.put(id, session);
        session.getAttributes().put("userId", id);
        return true;
    }

}
