package ch.rodano.api.config;

import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.utils.ACL;

public interface StudyDTOService {

	PublicStudyDTO createPublicStudyDTO(Study study);

	StudyDTO createStudyDTO(Study study, ACL acl);

}
