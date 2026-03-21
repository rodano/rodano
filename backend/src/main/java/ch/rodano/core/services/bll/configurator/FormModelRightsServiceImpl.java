package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.EntityRightDTO;
import ch.rodano.core.services.dao.configurator.FormModelRightsDAOService;

@Service
@Transactional
public class FormModelRightsServiceImpl implements FormModelRightsService {

	private final FormModelRightsDAOService formModelRightsDAOService;

	public FormModelRightsServiceImpl(final FormModelRightsDAOService formModelRightsDAOService) {
		this.formModelRightsDAOService = formModelRightsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, Map<UUID, EntityRightDTO>> getFormModelRights(final UUID projectId) {
		return formModelRightsDAOService.getFormModelRights(projectId);
	}

	@Override
	public void saveFormModelRights(final UUID projectId, final Map<UUID, Map<UUID, EntityRightDTO>> rights) {
		formModelRightsDAOService.saveFormModelRights(projectId, rights);
	}
}
