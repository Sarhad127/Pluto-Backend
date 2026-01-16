package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.Invitation;
import org.tutorial.springemailtutorial.model.InvitationStatus;

import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByInviteeEmailAndBoardIdAndStatus(
            String inviteeEmail,
            Long boardId,
            InvitationStatus status
    );
    void deleteByBoard(Board board);
}
