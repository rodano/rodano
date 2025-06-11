package ch.rodano.core.services.bll.export.scope;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.dao.role.RoleDAOService;

@Service
public class ScopeExportServiceImpl implements ScopeExportService {
	private final UserService userService;
	private final RoleDAOService roleDAOService;
	private final StudyService studyService;
	private final ScopeService scopeService;
	private final ScopeRelationService scopeRelationService;

	public ScopeExportServiceImpl(
		final UserService userService,
		final RoleDAOService roleDAOService,
		final StudyService studyService,
		final ScopeService scopeService,
		final ScopeRelationService scopeRelationService
	) {
		this.userService = userService;
		this.roleDAOService = roleDAOService;
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.scopeRelationService = scopeRelationService;
	}

	@Override
	public void exportScopes(final OutputStream out, final ScopeSearch search, final String[] languages) throws IOException {
		//TODO create a custom jOOQ queries to gather all the details that needs to be exported
		final var result = scopeService.search(search);
		final var study = studyService.getStudy();

		final var model = study.getScopeModel(search.getScopeModelId().get());

		try(var writer = new CSVWriter(new OutputStreamWriter(out))) {

			//header
			final var header = new ArrayList<>();

			if(!model.isRoot()) {
				header.add(model.getDefaultParent().getLocalizedShortname(languages));
			}

			header.add(String.format("%s code", model.getLocalizedShortname(languages)));
			header.add(String.format("%s name", model.getLocalizedShortname(languages)));

			final Map<Long, String> users = new TreeMap<>();
			if(StringUtils.isNotBlank(model.getDefaultProfileId())) {
				// Retrieve users
				for(final var role : roleDAOService.getRolesByProfile(model.getDefaultProfileId())) {
					//do not consider role link to a robot
					if(role.getUserFk() != null) {
						users.put(role.getScopeFk(), userService.getUserByPk(role.getUserFk()).getName());
					}
				}

				header.add(model.getDefaultProfile().get().getLocalizedShortname(languages));
			}

			final var leafScopeModel = studyService.getStudy().getLeafScopeModel();
			if(!model.isLeaf()) {
				header.add(leafScopeModel.getLocalizedShortname(languages));
			}

			header.add("Removed");

			writer.writeNext(header.toArray(new String[0]));

			//lines
			for(final var scope : result.getObjects()) {
				final var line = new ArrayList<String>(header.size());
				if(!model.isRoot()) {
					line.add(scopeRelationService.getDefaultParent(scope).getLocalizedShortname(languages));
				}

				line.add(scope.getCode());
				line.add(scope.getLocalizedShortname(languages));

				if(model.getDefaultProfileId() != null) {
					line.add(users.getOrDefault(scope.getPk(), ""));
				}

				if(!model.isLeaf()) {
					line.add(Integer.toString(scopeService.getLeafCount(scope)));
				}

				line.add(Boolean.toString(scope.getDeleted()));

				writer.writeNext(line.toArray(new String[0]));
			}
		}
	}
}
