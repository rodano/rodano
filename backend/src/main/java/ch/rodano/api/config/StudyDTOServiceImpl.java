package ch.rodano.api.config;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ch.rodano.api.workflow.WorkflowDTOService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.helpers.time.TimeHelper;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.RightsService;

@Service
public class StudyDTOServiceImpl implements StudyDTOService {

	private final RoleService roleService;
	private final RightsService rightsService;
	private final MenuDTOService menuDTOService;
	private final WorkflowDTOService workflowDTOService;
	private final ConfigDTOService configDTOService;
	private final Configurator configurator;

	public StudyDTOServiceImpl(
		final RightsService rightsService,
		final MenuDTOService menuDTOService,
		final WorkflowDTOService workflowDTOService,
		final ConfigDTOService configDTOService,
		final RoleService roleService,
		final Configurator configurator
	) {
		this.roleService = roleService;
		this.rightsService = rightsService;
		this.menuDTOService = menuDTOService;
		this.workflowDTOService = workflowDTOService;
		this.configDTOService = configDTOService;
		this.configurator = configurator;
	}

	private void updateDTO(final PublicStudyDTO dto, final Study study, final ACL acl) {
		dto.id = study.getId();
		dto.shortname = study.getShortname();
		dto.color = study.getColor();
		dto.logo = study.getLogo();

		dto.url = study.getUrl();

		final var homePage = study.getPrivateHomePage();
		dto.homePage = menuDTOService.createDTO(homePage, acl);

		dto.eproEnabled = study.isEproEnabled();

		dto.introductionText = study.getIntroductionText();
		dto.copyright = "Powered by Rodano";

		dto.defaultLanguage = new LanguageDTO(study.getDefaultLanguage());
		for(final var language : study.getActivatedLanguages()) {
			dto.activatedLanguages.add(new LanguageDTO(language));
		}

		dto.environment = configurator.getEnvironment();
		dto.leafScopeModel = configDTOService.createScopeModelDTO(study.getLeafScopeModel(), acl);

		dto.eproProfile = study.isEproEnabled() && StringUtils.isNotBlank(study.getEproProfileId()) ? new ProfileDTO(study.getEproProfile()) : null;
	}

	@Override
	public PublicStudyDTO createPublicStudyDTO(final Study study) {
		final var dto = new PublicStudyDTO();
		updateDTO(dto, study, ACL.ANONYMOUS);
		return dto;
	}

	@Override
	public StudyDTO createStudyDTO(final Study study, final ACL acl) {
		final var dto = new StudyDTO();
		updateDTO(dto, study, acl);

		dto.email = study.getEmail();
		dto.clientEmail = study.getClientEmail();

		dto.welcomeText = study.getWelcomeText();

		dto.configUser = study.getConfigUser();
		dto.configDate = TimeHelper.asZonedDateTime(study.getConfigDate());

		dto.rootScopeModelId = study.getRootScopeModel().getId();
		dto.leafScopeModelIds = study.getLeafScopeModels().stream().map(ScopeModel::getId).toList();
		dto.leafScopeId = study.getLeafScopeModel().getId();

		dto.scopeModels = study.getScopeModels().stream()
			.filter(s -> acl.hasRight(s, Rights.READ))
			.map(s -> configDTOService.createScopeModelDTO(s, acl))
			.toList();
		dto.datasetModels = study.getDatasetModels().stream()
			.filter(d -> acl.hasRight(d, Rights.READ))
			.map(d -> configDTOService.createDatasetModelDTO(d, acl))
			.toList();
		dto.formModels = study.getFormModels().stream()
			.filter(f -> acl.hasRight(f, Rights.READ))
			.map(FormModelDTO::new)
			.toList();
		dto.workflows = study.getWorkflows().stream()
			.filter(w -> acl.hasRight(w))
			.map(w -> workflowDTOService.createWorkflowDTO(w, acl)).toList();

		//return profiles on which the user has the right plus the profiles of their disabled roles
		//this is require to display the "contact" and "user" pages properly (a user who has disabled roles must be able to see its disabled roles)
		final var allRoles = roleService.getRoles(acl.actor());
		final var profiles = study.getProfiles().stream().filter(p -> rightsService.hasRight(allRoles, p, Rights.READ)).collect(Collectors.toSet());
		profiles.addAll(allRoles.stream().map(Role::getProfile).toList());
		dto.profiles = profiles.stream()
			.map(ProfileDTO::new)
			.toList();

		dto.menus = study.getMenus().stream().filter(m -> acl.hasRight(m)).map(m -> menuDTOService.createDTO(m, acl)).toList();

		return dto;
	}

}
