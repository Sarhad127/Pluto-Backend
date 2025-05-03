package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
}
