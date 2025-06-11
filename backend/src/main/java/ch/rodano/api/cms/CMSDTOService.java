package ch.rodano.api.cms;

import java.util.List;

import ch.rodano.configuration.model.cms.CMSLayout;
import ch.rodano.core.model.role.Role;

public interface CMSDTOService {

	CMSLayoutDTO createLayoutDTO(CMSLayout cmsLayout, List<Role> roles);
}
