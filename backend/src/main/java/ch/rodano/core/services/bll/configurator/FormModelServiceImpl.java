package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.FormModelDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.FormModelDAOService;

@Service
@Transactional
public class FormModelServiceImpl implements FormModelService {

	private final FormModelDAOService formModelDAOService;

	public FormModelServiceImpl(final FormModelDAOService formModelDAOService) {
		this.formModelDAOService = formModelDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<FormModelDTO> getFormModels(final UUID projectId) {
		return formModelDAOService.getFormModels(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public FormModelDTO getFormModel(final UUID projectId, final UUID formModelId) {
		final var formModel = formModelDAOService.getFormModel(projectId, formModelId);
		if(formModel == null) {
			throw new NotFoundException("Form model not found: " + formModelId);
		}
		return formModel;
	}

	@Override
	public FormModelDTO createFormModel(final UUID projectId, final FormModelDTO formModel) {
		return formModelDAOService.createFormModel(projectId, formModel);
	}

	@Override
	public FormModelDTO updateFormModel(final UUID projectId, final UUID formModelId, final FormModelDTO formModel) {
		final var existing = formModelDAOService.getFormModel(projectId, formModelId);
		if(existing == null) {
			throw new NotFoundException("Form model not found: " + formModelId);
		}
		return formModelDAOService.updateFormModel(projectId, formModelId, formModel);
	}

	@Override
	public void deleteFormModel(final UUID projectId, final UUID formModelId) {
		final var existing = formModelDAOService.getFormModel(projectId, formModelId);
		if(existing == null) {
			throw new NotFoundException("Form model not found: " + formModelId);
		}
		formModelDAOService.deleteFormModel(projectId, formModelId);
	}
}
