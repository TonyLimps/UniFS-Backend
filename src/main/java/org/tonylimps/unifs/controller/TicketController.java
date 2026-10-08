package org.tonylimps.unifs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tonylimps.unifs.model.dto.response.TicketResponse;
import org.tonylimps.unifs.service.TicketService;

@RestController
@RequestMapping("/api/ws-ticket")
public class TicketController {
    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<TicketResponse> getTicket(Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicket(authentication));
    }
}
