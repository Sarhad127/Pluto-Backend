package org.tutorial.springemailtutorial.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.Invitation;
import org.tutorial.springemailtutorial.model.InvitationStatus;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.InvitationRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final JavaMailSender javaMailSender;
    private final BoardRepository boardRepository;

    public void inviteUserToBoard(String boardId, String inviteeEmail, User inviter) {
        Board board = boardRepository.findById(Long.valueOf(boardId))
                .orElseThrow(() -> new RuntimeException("Board not found"));
        Invitation invitation = new Invitation();
        invitation.setBoard(board);
        invitation.setInviter(inviter);
        invitation.setInviteeEmail(inviteeEmail);
        invitation.setStatus(InvitationStatus.PENDING);
        invitationRepository.save(invitation);
        sendInvitationEmail(inviteeEmail, board, inviter);
    }

    private void sendInvitationEmail(String inviteeEmail, Board board, User inviter) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(inviteeEmail);
        message.setSubject("Invitation to Join Board: " + board.getTitle());
        message.setText("Hello,\n\n" + inviter.getUsername() +
                " has invited you to join the board '" + board.getTitle() + "'.\n\n" +
                "To accept the invitation, please visit the following link: \n" +
                "todo-frontend-production-8fe7.up.railway.app/accept-invitation?boardId=" + board.getId());

        javaMailSender.send(message);
    }

    @Transactional
    public void acceptInvitation(Long boardId, User invitee) {
        List<Invitation> pendingInvitations = invitationRepository.findByInviteeEmailAndBoardIdAndStatus(
                invitee.getEmail(),
                boardId,
                InvitationStatus.PENDING
        );
        if (pendingInvitations.isEmpty()) {
            throw new RuntimeException("No pending invitation found");
        }
        Invitation invitation = pendingInvitations.stream()
                .max(Comparator.comparing(Invitation::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("Error processing invitation"));
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        Board board = invitation.getBoard();
        if (!board.getUsers().contains(invitee)) {
            board.getUsers().add(invitee);
            boardRepository.save(board);
        }
        pendingInvitations.stream()
                .filter(inv -> !inv.getId().equals(invitation.getId()))
                .forEach(inv -> {
                    inv.setStatus(InvitationStatus.REJECTED);
                    invitationRepository.save(inv);
                });
    }
}
