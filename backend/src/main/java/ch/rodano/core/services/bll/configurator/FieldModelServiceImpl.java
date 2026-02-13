package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.FieldModelDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.FieldModelDAOService;

@Service
@Transactional
public class FieldModelServiceImpl implements FieldModelService {

	private final FieldModelDAOService fieldModelDAOService;

	public FieldModelServiceImpl(final FieldModelDAOService fieldModelDAOService) {
		this.fieldModelDAOService = fieldModelDAOService;
	}

	@Override
	public List<FieldModelDTO> getFieldModels(final UUID projectId) {
		return fieldModelDAOService.getFieldModels(projectId);
	}

	@Override
	public FieldModelDTO getFieldModel(final UUID projectId, final UUID fieldModelId) {
		final var fieldModel = fieldModelDAOService.getFieldModel(projectId, fieldModelId);
		if(fieldModel == null) {
			throw new NotFoundException("Field model not found:  " + fieldModelId);
		}
		return fieldModel;
	}

	@Override
	public FieldModelDTO createFieldModel(final UUID projectId, final FieldModelDTO fieldModel) {
		if(fieldModel.getId() == null || fieldModel.getId().isBlank()) {
			throw new IllegalArgumentException("Field model code is required");
		}
		if(fieldModel.getShortname() == null || fieldModel.getShortname().isEmpty()) {
			throw new IllegalArgumentException("Field model shortname is required");
		}

		return fieldModelDAOService.createFieldModel(projectId, fieldModel);
	}

	@Override
	public FieldModelDTO updateFieldModel(final UUID projectId, final UUID fieldModelId, final FieldModelDTO fieldModel) {
		final var existing = fieldModelDAOService.getFieldModel(projectId, fieldModelId);
		if(existing == null) {
			throw new NotFoundException("Field model not found:  " + fieldModelId);
		}

		if(fieldModel.getId() == null || fieldModel.getId().isBlank()) {
			throw new IllegalArgumentException("Field model code is required");
		}

		if(fieldModel.getShortname() == null || fieldModel.getShortname().isEmpty()) {
			throw new IllegalArgumentException("Field model shortname is required");
		}

		return fieldModelDAOService.updateFieldModel(projectId, fieldModelId, fieldModel);
	}

	@Override
	public void deleteFieldModel(final UUID projectId, final UUID fieldModelId) {
		final var existing = fieldModelDAOService.getFieldModel(projectId, fieldModelId);
		if(existing == null) {
			throw new NotFoundException("Field model not found:  " + fieldModelId);
		}

		fieldModelDAOService.deleteFieldModel(projectId, fieldModelId);
	}
}
