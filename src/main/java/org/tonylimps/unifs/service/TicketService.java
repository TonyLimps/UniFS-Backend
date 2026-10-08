package org.tonylimps.unifs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.tonylimps.unifs.model.dto.response.TicketResponse;
import org.tonylimps.unifs.model.pojo.TicketSession;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicketService {
    private final int TICKET_EXPIRE_SECONDS;
    private final Map<String, TicketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public TicketService(@Value("${ticket-expire-seconds}") int ticketExpireSeconds) {
        this.TICKET_EXPIRE_SECONDS = ticketExpireSeconds;
    }

    public TicketResponse getTicket(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String ticket = UUID.randomUUID().toString();

        var session = new TicketSession();
        session.setUserId(userId);
        session.setExpireTime(LocalDateTime.now().plusSeconds(TICKET_EXPIRE_SECONDS));
        sessions.put(ticket, session);

        var response = new TicketResponse();
        response.setTicket(ticket);
        return response;
    }

    public Long consume(String ticket) {
        var session = sessions.get(ticket);
        if (session == null) return null;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = session.getExpireTime();
        if (expireTime.isBefore(now)) return null;
        sessions.remove(ticket);

        return session.getUserId();
    }

    @Scheduled(cron = "${clean-expired-tickets-cron}")
    public void cleanExpiredTickets() {
        LocalDateTime now = LocalDateTime.now();
        sessions.forEach((ticket, session) -> {
            if (session.getExpireTime().isBefore(now)) {
                sessions.remove(ticket);
            }
        });
    }
}
