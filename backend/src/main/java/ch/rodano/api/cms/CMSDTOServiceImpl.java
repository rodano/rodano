package ch.rodano.api.cms;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.cms.CMSLayout;
import ch.rodano.configuration.model.cms.CMSSection;
import ch.rodano.configuration.model.cms.CMSWidget;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.utils.RightsService;

@Service
public class CMSDTOServiceImpl implements CMSDTOService {

	private final RightsService rightsService;

	public CMSDTOServiceImpl(final RightsService rightsService) {
		this.rightsService = rightsService;
	}

	@Override
	public CMSLayoutDTO createLayoutDTO(final CMSLayout cmsLayout, final List<Role> roles) {
		final var dto = new CMSLayoutDTO();

		//generate sections
		final List<CMSSectionDTO> sections = new ArrayList<>();
		final var features = roles.stream().flatMap(role -> role.getProfile().getGrantedFeatureIds().stream()).toList();

		for(final var s : cmsLayout.getSections()) {
			final var requiredRight = s.getRequiredRight();
			if((requiredRight == null || !requiredRight.isValid()) && (s.getRequiredFeature() == null || features.contains(s.getRequiredFeature()))) {
				sections.add(createSectionDTO(s, roles));
			}
			else if(requiredRight != null && requiredRight.isValid()) {
				if(!roles.isEmpty() && rightsService.hasRight(roles, requiredRight.getRightEntity(), requiredRight.getId(), requiredRight.getRight())) {
					sections.add(createSectionDTO(s, roles));
				}
			}
		}
		dto.sections = sections;

		return dto;
	}

	public CMSSectionDTO createSectionDTO(final CMSSection cmsSection, final List<Role> roles) {
		final var dto = new CMSSectionDTO();
		dto.id = cmsSection.getId();
		dto.labels = cmsSection.getLabels();

		//generate widgets
		final List<CMSWidget> widgets = new ArrayList<>();

		//build user feature list
		final var features = roles.stream().flatMap(role -> role.getProfile().getGrantedFeatureIds().stream()).toList();

		for(final var w : cmsSection.getWidgets()) {
			if((w.getRequiredRight() == null || !w.getRequiredRight().isValid()) && (w.getRequiredFeature() == null || features.contains(w.getRequiredFeature()))) {
				widgets.add(w);
			}
			else if(w.getRequiredRight() != null && w.getRequiredRight().isValid()) {
				if(!roles.isEmpty() && rightsService.hasRight(roles, w.getRequiredRight().getRightEntity(), w.getRequiredRight().getId(), w.getRequiredRight().getRight())) {
					widgets.add(w);
				}
			}
		}

		dto.widgets = widgets.stream()
			.map(CMSWidgetDTO::new)
			.toList();

		return dto;
	}

}
