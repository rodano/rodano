package ch.rodano.api.config;

import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface ConfigDTOService {

	ScopeModelDTO createScopeModelDTO(ScopeModel scopeModel, ACL acl);

	DatasetModelDTO createDatasetModelDTO(DatasetModel datasetModel, ACL acl);

	LayoutDTO createLayoutDTO(Scope scope, Optional<Event> event, Form form, Layout layout, ACL acl, String[] languages);

	List<LayoutDTO> createLayoutDTOs(Scope scope, Optional<Event> event, Form form, ACL acl, String[] languages);
}
