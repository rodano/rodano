package ch.rodano.api.administration;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.core.database.initializer.ProjectInitializer;
import ch.rodano.core.loader.DatabaseStudyLoader;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.core.services.project.ProjectIdResolver;

@RestController
@RequestMapping("/administration/projects")
public class ProjectInitializationController {

	private final ProjectInitializer projectInitializer;
	private final DatabaseStudyLoader databaseStudyLoader;
	private final ProjectIdResolver projectIdResolver;

	public ProjectInitializationController(final ProjectInitializer projectInitializer,
										   final DatabaseStudyLoader databaseStudyLoader,
										   final ProjectIdResolver projectIdResolver) {
		this.projectInitializer = projectInitializer;
		this.databaseStudyLoader = databaseStudyLoader;
		this.projectIdResolver = projectIdResolver;
	}

	@PostMapping("/initialize")
	public ResponseEntity<String> initializeProject(@RequestBody final ProjectInitRequest request) throws InvalidValueException, BadlyFormattedValue, IOException {
		final UUID projectId = projectIdResolver.resolveCode(request.projectCode);

		if (projectId == null) {
			return ResponseEntity.badRequest().body(
				"Project with code '" + request.projectCode + "' not found in database. " +
					"Please run the batch import first to load the project configuration."
			);
		}

		final var study = databaseStudyLoader.loadStudy(projectId);
		final var options = ProjectInitializer.ProjectInitOptions.defaults().withAdminUser(request.adminName, request.adminEmail, request.adminPassword);

		if(request.withDemoUsers) {
			options.withDemoUsers();
		}

		if(request.withDemoData) {
			options.withDemoData();
		}

		projectInitializer.initializeProject(study, options);

		return ResponseEntity.ok(String.format("Project %s (ID: %s) initialized successfully", study.getId(), projectId));
	}

	public static class ProjectInitRequest {
		public String projectCode;
		public String adminName = "Admin User";
		public String adminEmail;
		public String adminPassword;
		public boolean withDemoUsers = false;
		public boolean withDemoData = false;
	}
}
