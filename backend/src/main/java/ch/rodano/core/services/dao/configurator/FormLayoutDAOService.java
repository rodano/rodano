package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.LayoutDTO;

public interface FormLayoutDAOService {

	List<LayoutDTO> getLayouts(UUID projectId, UUID formModelId);

	LayoutDTO getLayout(UUID projectId, UUID formModelId, UUID formLayoutId);

	LayoutDTO createLayout(UUID projectId, UUID formModelId, LayoutDTO layout);

	LayoutDTO updateLayout(UUID projectId, UUID formModelId, UUID formLayoutId, LayoutDTO layout);

	void deleteLayout(UUID projectId, UUID formModelId, UUID formLayoutId);
}
