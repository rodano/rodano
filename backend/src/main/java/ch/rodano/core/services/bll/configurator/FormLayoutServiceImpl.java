package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.LayoutDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.FormLayoutDAOService;

@Service
@Transactional
public class FormLayoutServiceImpl implements FormLayoutService {

	private final FormLayoutDAOService formLayoutDAOService;

	public FormLayoutServiceImpl(final FormLayoutDAOService formLayoutDAOService) {
		this.formLayoutDAOService = formLayoutDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<LayoutDTO> getLayouts(final UUID projectId, final UUID formModelId) {
		return formLayoutDAOService.getLayouts(projectId, formModelId);
	}

	@Override
	@Transactional(readOnly = true)
	public LayoutDTO getLayout(final UUID projectId, final UUID formModelId, final UUID formLayoutId) {
		final var formLayout = formLayoutDAOService.getLayout(projectId, formModelId, formLayoutId);
		if(formLayout == null) {
			throw new NotFoundException("Form layout not found: " + formLayoutId);
		}
		return formLayout;
	}

	@Override
	public LayoutDTO createLayout(final UUID projectId, final UUID formModelId, final LayoutDTO layout) {
		return formLayoutDAOService.createLayout(projectId, formModelId, layout);
	}

	@Override
	public LayoutDTO updateLayout(final UUID projectId, final UUID formModelId, final UUID formLayoutId, final LayoutDTO layout) {
		final var existing = formLayoutDAOService.getLayout(projectId, formModelId, formLayoutId);
		if(existing == null) {
			throw new NotFoundException("Form layout not found: " + formLayoutId);
		}
		return formLayoutDAOService.updateLayout(projectId, formModelId, formLayoutId, layout);
	}

	@Override
	public void deleteLayout(final UUID projectId, final UUID formModelId, final UUID formLayoutId) {
		final var existing = formLayoutDAOService.getLayout(projectId, formModelId, formLayoutId);
		if(existing == null) {
			throw new NotFoundException("Form layout not found: " + formLayoutId);
		}
		formLayoutDAOService.deleteLayout(projectId, formModelId, formLayoutId);
	}
}
