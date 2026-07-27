package ch.rodano.core.services.bll.study;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Service;

import tools.jackson.databind.json.JsonMapper;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.Action;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.configuration.core.Environment;
import ch.rodano.core.model.configuration.LZW;
import ch.rodano.core.utils.file.ResourceUtils;

@Service
public class StudyServiceImpl implements StudyService, InfoContributor {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final JsonMapper mapper;

	private final String configurationResource;
	private final Integer configVersion;
	private final Configurator configurator;

	private Study study;
	private String studyChecksum;

	public StudyServiceImpl(
		@Value("${rodano.config:${rodano.config.jar}}") final String configurationResource,
		@Value("${rodano.config.version:0}") final Integer configVersion,
		final JsonMapper mapper,
		final Configurator configurator

	) throws IOException {
		this.mapper = mapper;
		this.configurationResource = configurationResource;
		this.configVersion = configVersion;
		this.configurator = configurator;

		load();
	}

	/**
	 * Load the configuration file
	 *
	 * @throws IOException Thrown if an error occurred while reading the configuration resource
	 */
	private void load() throws IOException {
		try(final var is = ResourceUtils.readResource(configurationResource)) {
			try {
				final var md = MessageDigest.getInstance("SHA-1");
				final var watchedIs = new DigestInputStream(is, md);

				study = mapper.readValue(watchedIs, Study.class);
				study.init();
				studyChecksum = Hex.encodeHexString(md.digest());

				checkConfiguration();

				if(Environment.DEV.equals(configurator.getEnvironment())) {
					giveAllRightsToAdmin();
				}
			}
			catch(final NoSuchAlgorithmException e) {
				//no way to come here, SHA-1 exists
				logger.error(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public void read(final OutputStream os) throws FileNotFoundException, IOException {
		try(var is = ResourceUtils.readResource(configurationResource)) {
			is.transferTo(os);
		}
	}

	@Override
	public void save(final InputStream is, final boolean compressed) throws IOException {
		// Manage compression
		if(compressed) {
			// Retrieve array of codes
			final var type = mapper.getTypeFactory().constructCollectionLikeType(List.class, Integer.class);
			final List<Integer> integers = mapper.readValue(is, type);

			// Uncompress
			final var result = LZW.decompress(integers);

			// Check data
			//TODO improve this and avoid reading the configuration twice (here and in the call the reload method)
			mapper.readValue(result, Study.class);

			// Write to file
			ResourceUtils.writeResource(configurationResource, result.getBytes());
		}
		else {
			//load the input stream in memory so it can be used multiple times
			final byte[] bytes = is.readAllBytes();
			// Check data
			//TODO improve this and avoid reading the configuration twice (here and in the call the reload method)
			mapper.readValue(bytes, Study.class);

			// Write to file
			ResourceUtils.writeResource(configurationResource, bytes);
		}

		// Reload configuration
		reload();
	}

	@Override
	public void reload() throws IOException {
		load();
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

			//give rights on workflows
			final SortedMap<String, Set<String>> workflowRights = new TreeMap<>();
			study.getWorkflows().forEach(workflow -> {
				final var actionIds = workflow.getActions().stream().map(Action::getId).collect(Collectors.toSet());
				workflowRights.put(workflow.getId(), actionIds);
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
	public Study getStudy() {
		return study;
	}

	@Override
	public void contribute(final Builder builder) {
		builder.withDetail("config", Map.of("sha1", studyChecksum, "date", study.getConfigDate()));
	}
}
