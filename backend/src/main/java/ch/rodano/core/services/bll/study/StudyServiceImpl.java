package ch.rodano.core.services.bll.study;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.profile.ProfileRight;
import ch.rodano.configuration.model.rights.Right;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.configuration.core.Environment;
import ch.rodano.core.loader.DatabaseStudyLoader;
import ch.rodano.core.services.project.ProjectIdResolver;
import ch.rodano.core.services.rule.ConstraintLoaderService;

@Service
public class StudyServiceImpl implements StudyService, InfoContributor {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ObjectMapper objectMapper;
	private final Integer configVersion;
	private final Configurator configurator;
	private final ProjectIdResolver projectIdResolver;
	private final DatabaseStudyLoader databaseStudyLoader;
	private final ConstraintLoaderService constraintLoaderService;

	private Study study;
	private String studyChecksum;

	public StudyServiceImpl(
		@Value("${rodano.config.version:0}") final Integer configVersion,
		final ObjectMapper objectMapper,
		final Configurator configurator,
		final ProjectIdResolver projectIdResolver,
		final DatabaseStudyLoader databaseStudyLoader,
		final ConstraintLoaderService constraintLoaderService
	) {
		this.objectMapper = objectMapper;
		this.configVersion = configVersion;
		this.configurator = configurator;
		this.projectIdResolver = projectIdResolver;
		this.databaseStudyLoader = databaseStudyLoader;
		this.constraintLoaderService = constraintLoaderService;
	}

	@Override
	public void loadStudyForProject(final UUID projectId) {
		logger.info("Loading study for project {}", projectId);

		projectIdResolver.setProjectId(projectId);
		study = databaseStudyLoader.loadStudy(projectId);
		study.setConfigVersion(configVersion);
		study.init();

		constraintLoaderService.loadAndAssignConstraints(study);

		checkConfiguration();

		if(Environment.DEV.equals(configurator.getEnvironment())) {
			giveAllRightsToAdmin();
		}

		calculateChecksum();

		logger.info("Study loaded successfully {}", study.getId());
	}

	private void calculateChecksum() {
		try {
			final var md = MessageDigest.getInstance("SHA-1");
			final String studyJson = objectMapper.writeValueAsString(study);
			studyChecksum = Hex.encodeHexString(md.digest(studyJson.getBytes()));
		}
		catch(final Exception e) {
			logger.warn("Could not calculate study checksum", e);
			studyChecksum = "checksum-unavailable";
		}
	}

	@Override
	public boolean isStudyLoaded() {
		return study != null;
	}

	@Override
	public UUID getCurrentProjectId() {
		return study != null ? study.getProjectId() : null;
	}

	@Override
	public Study getStudy() {
		if(study == null) {
			throw new IllegalStateException("No study loaded. Please select a project first.");
		}
		return study;
	}

	/**
	 * Check that the config file is up to date
	 */
	private void checkConfiguration() {
		if(study.getConfigVersion() > configVersion) {
			throw new UnsupportedOperationException(String.format("Version of configuration is higher than version of Rodano (%s > %s)", study.getConfigVersion(), configVersion));
		}
		if(study.getConfigVersion() < configVersion) {
			throw new UnsupportedOperationException(String.format("Version of configuration is lower than version of Rodano (%s < %s)", study.getConfigVersion(), configVersion));
		}
	}

	/**
	 * Give all rights to the admin profile
	 */
	private void giveAllRightsToAdmin() {
		try {
			final Profile admin = study.getProfile("ADMIN");

			// Give rights on workflows
			// Retrieve profile ids
			final Set<String> profileIds = study.getProfiles().stream().map(Profile::getId).collect(Collectors.toSet());

			final SortedMap<String, Right> workflowRights = new TreeMap<>();
			study.getWorkflows().forEach(workflow -> {
				final Right right = new Right();
				right.setRight(true);
				final SortedMap<String, ProfileRight> childRights = new TreeMap<>();
				workflow.getActions().forEach(action -> {
					final ProfileRight profileRight = new ProfileRight();
					profileRight.setSystem(true);
					profileRight.setProfileIds(profileIds);
					childRights.put(action.getId(), profileRight);
				});
				right.setChildRights(childRights);
				workflowRights.put(workflow.getId(), right);
			});

			admin.setGrantedWorkflowIds(workflowRights);

			//give rights on right assignables
			//build all rights set
			final Set<Rights> allRights = new TreeSet<>(Arrays.asList(Rights.values()));
			final var displayableCollector = Collectors.toMap(Displayable::getId, _ -> allRights);

			admin.setGrantedScopeModelIdRights(study.getScopeModels().stream().collect(displayableCollector));
			admin.setGrantedEventModelIdRights(study.getEventModels().stream().collect(displayableCollector));
			admin.setGrantedDatasetModelIdRights(study.getDatasetModels().stream().collect(displayableCollector));
			admin.setGrantedFormModelIdRights(study.getFormModels().stream().collect(displayableCollector));
		}
		catch(@SuppressWarnings("unused") final NoNodeException e) {
			// Nothing to do if there is no admin profile
			logger.warn("No admin profile for the given instance");
		}
	}

	@Override
	public void reloadStudyFromDatabase() throws IOException {
		if(study == null) {
			throw new IllegalStateException("No study loaded. Cannot reload.");
		}

		logger.info("Reloading study from database for project {}", study.getId());

		final UUID projectId = study.getProjectId();

		study = databaseStudyLoader.loadStudy(projectId);
		study.setConfigVersion(configVersion);
		study.init();

		constraintLoaderService.loadAndAssignConstraints(study);

		checkConfiguration();

		if(Environment.DEV.equals(configurator.getEnvironment())) {
			giveAllRightsToAdmin();
		}

		calculateChecksum();

		logger.info("Study reloaded successfully from database");
	}

	@Override
	public void contribute(final Builder builder) {
		if(study != null) {
			builder.withDetail("config", Map.of(
				"sha1", studyChecksum != null ? studyChecksum : "not-calculated",
				"date", study.getConfigDate(),
				"projectId", study.getProjectId().toString(),
				"code", study.getId(),
				"version", configVersion
			));
		}
		else {
			builder.withDetail("config", Map.of(
				"loaded", false,
				"message", "No study loaded. Select a project to load configuration."
			));
		}
	}

	@Override
	public void clearStudy() {
		this.study = null;
		this.studyChecksum = null;
	}
}
