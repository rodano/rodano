package ch.rodano.core.services.bll.study;

import java.io.IOException;
import java.util.UUID;

import ch.rodano.configuration.model.study.Study;

public interface StudyService {

	/**
	 * @return the study
	 */
	Study getStudy();

	void loadStudyForProject(UUID projectId) throws IOException;

	UUID getCurrentProjectId();

	boolean isStudyLoaded();

	void reloadStudyFromDatabase() throws IOException;
}
