package org.tutorial.springemailtutorial.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.InvitationRequest;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.service.InvitationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/invite")
    public ResponseEntity<String> inviteUser(@RequestBody InvitationRequest request, Authentication authentication) {
        User inviter = (User) authentication.getPrincipal();
        invitationService.inviteUserToBoard(request.getBoardId(), request.getInviteeEmail(), inviter);
        return ResponseEntity.ok("Invitation sent successfully!");
    }

    @GetMapping("/accept")
    public ResponseEntity<String> acceptInvitation(@RequestParam Long invitationId) {
        invitationService.acceptInvitation(invitationId);
        return ResponseEntity.ok("Invitation accepted successfully!");
    }
}
