package org.tonylimps.unifs.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tonylimps.unifs.mapper.UserRoomMapper;
import org.tonylimps.unifs.model.pojo.Command;
import org.tonylimps.unifs.service.TicketService;
import tools.jackson.databind.DatabindException;
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
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long id = (Long) session.getAttributes().get("userId");
        if(id != null) sessions.remove(id);
        log.debug("WS session closed: userId={} sid={} status={}", id, session.getId(), status);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.debug("WS session established: sid={}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        Command command;
        try {
            command = jsonMapper.readValue(message.getPayload(), Command.class);
        } catch (DatabindException e) {
            closeSession(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        if (!auth(session, command)) {
            closeSession(session, CloseStatus.POLICY_VIOLATION);
            return;
        }
        String type = command.getType();
        Long to = command.getTo();
        switch (type) {
            case Command.Type.OFFER -> broadcast(command, message);
            case Command.Type.ANSWER, Command.Type.CANDIDATE -> forward(command, message, to);
        }
    }

    private void broadcast(Command command, TextMessage message) {
        Set<Long> targetUsers = userRoomMapper.selectSameRoomUserIds(command.getFrom());
        targetUsers.forEach(targetUser -> forward(command, message, targetUser));
    }

    private void forward(Command command, TextMessage message, Long to) {
        WebSocketSession target = sessions.get(to);
        if (target == null) {
            log.debug("WS forward skipped: from={} to={} type={}", command.getFrom(), to, command.getType());
            return;
        }
        try {
            target.sendMessage(message);
            log.debug("WS forward succeed: from={} to={} type={}", command.getFrom(), to, command.getType());
        } catch (IOException e) {
            log.error("WS forward failed: from={} to={} type={}", command.getFrom(), to, command.getType(), e);
        }
    }

    private boolean auth(WebSocketSession session, Command command) {
        Long id = (Long) session.getAttributes().get("userId");
        if (id != null && sessions.containsKey(id)) return true;

        String type = command.getType();
        if (!type.equals(Command.Type.AUTH)) return false;

        String ticket = command.getTicket();
        id = ticketService.consume(ticket);
        if (id == null) {
            log.warn("WS auth failed: invalid/expired ticket, sid={} from={}", session.getId(), command.getFrom());
            return false;
        }
        if (!command.getFrom().equals(id)) {
            log.warn("WS auth failed: ticket owner={} but from={} sid={}", id, command.getFrom(), session.getId());
            return false;
        }

        sessions.put(id, session);
        session.getAttributes().put("userId", id);
        log.debug("WS auth succeed: uid={} sid={}", id, session.getId());
        return true;
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        try {
            session.close(status);
        } catch (IOException e) {
            log.error("WS session close error: userId={} sid={}", userId, session.getId(), e);
        }
    }
}
