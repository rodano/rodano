package ch.rodano.core.services.bll.project;

import java.util.UUID;

import ch.rodano.core.model.user.User;

public interface ProjectAccessService {

	boolean canRead(User user, UUID projectId);

	boolean canWrite(User user, UUID projectId);
}
