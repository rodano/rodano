package ch.rodano.api.search;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.scope.ScopeSearch;

public interface ExtendedScopeResultService {

	PagedResult<ExtendedScopeSearchResultDTO> search(final ScopeSearch search) ;
}
