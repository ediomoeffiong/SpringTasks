package com.fifthlab.springtasks.repository;

import com.fifthlab.springtasks.model.InvitationStatus;
import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.model.TaskInvitation;
import com.fifthlab.springtasks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskInvitationRepository extends JpaRepository<TaskInvitation, Long> {
    List<TaskInvitation> findByReceiverAndStatus(User receiver, InvitationStatus status);
    List<TaskInvitation> findByTaskAndStatus(Task task, InvitationStatus status);
    Optional<TaskInvitation> findByTaskAndReceiverAndStatus(Task task, User receiver, InvitationStatus status);
}
