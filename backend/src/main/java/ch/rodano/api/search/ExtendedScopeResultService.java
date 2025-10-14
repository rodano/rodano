package ch.rodano.api.search;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeSearch;
import java.util.List;

import ch.rodano.core.utils.ACL;

public interface ExtendedScopeResultService {

	PagedResult<ExtendedScopeSearchResultDTO> search(final ScopeSearch search) ;
	ExtendedScopeSearchResultDTO createDTO(final Scope scope, final List<ACL> acls);
}
