package ch.rodano.api.project;

import org.springframework.stereotype.Component;

import ch.rodano.core.model.project.Project;

@Component
public class ProjectMapper {

	public ProjectDTO toDTO(final Project project) {
		return new ProjectDTO(
			project.getProjectId(),
			project.getCode(),
			project.getShortname(),
			project.getLongname(),
			project.getDescription(),
			project.getUrl(),
			project.getColor(),
			project.getIntroductionText(),
			project.getVersionDate(),
			project.getConfigDate(),
			project.getStatus(),
			project.getCreatedDate()
		);
	}
}
