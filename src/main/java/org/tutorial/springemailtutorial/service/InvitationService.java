package org.tutorial.springemailtutorial.service;

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
import org.tutorial.springemailtutorial.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
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
                "http://yourapp.com/accept-invitation?boardId=" + board.getId());

        javaMailSender.send(message);
    }

    public void acceptInvitation(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (invitation.getStatus() == InvitationStatus.PENDING) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);

            Board board = invitation.getBoard();
            User invitee = userRepository.findByEmail(invitation.getInviteeEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            board.getUsers().add(invitee);
            boardRepository.save(board);
        }
    }
}
