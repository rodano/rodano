package ch.rodano.core.dao;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.cms.CMSLayout;

@Repository
public class CMSLayoutDAO {

	private final CMSSectionDAO cmsSectionDAO;

	public CMSLayoutDAO(final CMSSectionDAO cmsSectionDAO) {
		this.cmsSectionDAO = cmsSectionDAO;
	}

	public CMSLayout findByMenu(final UUID menuId) {
		final var sections = cmsSectionDAO.findByMenu(menuId);

		if(sections.isEmpty()) {
			return null;
		}

		final CMSLayout layout = new CMSLayout();
		layout.setSections(sections);

		return layout;
	}
}
