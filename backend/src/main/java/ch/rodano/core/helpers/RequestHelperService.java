package ch.rodano.core.helpers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.predicate.ValueSource;
import ch.rodano.configuration.model.request.Request;
import ch.rodano.configuration.model.request.ResultType;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.graph.RodanoSqlResult;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.Utils;

//TODO remove this as soon as SubstudyService.findPotentialScopePks has been rewritten
@Deprecated
@Service
public class RequestHelperService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DSLContext create;
	private final StudyService studyService;

	public RequestHelperService(
		final DSLContext create,
		final StudyService studyService
	) {
		this.create = create;
		this.studyService = studyService;
	}

	//if a list of value source is used, we assume that all value sources target the same event and the same document
	//to be improved by matching required datasetModels for parameters and required datasetModels for results
	public String getSqlRequest(
		final Request request,
		final ScopeModel scopeModel,
		final Collection<Scope> scopes,
		final List<ValueSource> results,
		final List<ValueSource> additionalCriteria
	) {
		final var study = studyService.getStudy();
		final List<ValueSource> parameters = new ArrayList<>(request.getParameters());

		//Augment the parameters with the additionalCriteria, if present
		if(!additionalCriteria.isEmpty()) {
			parameters.addAll(additionalCriteria);
		}

		final var sql = new StringBuilder("select ");

		//search field use by group by
		//try to find specified order by
		final var groupBy = results.stream().findFirst().map(ValueSource::getFieldModelId);
		if(groupBy.isPresent()) {
			sql.append(String.format("doc.%s, ", groupBy.get()));
		}

		//build part of the query that select the list of fields
		final var fieldModelIds = results.stream().map(ValueSource::getSqlFieldModel).collect(Collectors.joining(","));
		sql.append(fieldModelIds);

		//for following code, taking first result in the results list
		final var result = results.get(0);

		//data table
		sql.append(" from ");
		sql.append(result.getDatasetModel().getExportTableName());
		sql.append(" doc ");

		sql.append("inner join scope s on doc.scope_fk = s.pk and s.deleted = 0 ");

		//requirements tables
		for(var i = 0; i < parameters.size(); i++) {
			if(!parameters.get(i).getDatasetModelId().equals(result.getDatasetModelId())) {
				final var documentTable = study.getDatasetModel(parameters.get(i).getDatasetModelId()).getExportTableName();
				sql.append(String.format("inner join %1$s D%2$d on doc.scope_fk = D%2$d.scope_fk ", documentTable, i));
			}
		}

		//scope instances requirements
		if(scopes != null) {
			final var scopePks = scopes.stream().map(Scope::getPk).toList();
			sql.append("inner join scope_ancestor sa on doc.scope_fk = sa.scope_fk ");
			sql.append(String.format("where sa.ancestor_fk in (%s) ", Utils.implodeForSQL(scopePks)));
			sql.append("and sa.start_date < now() and (sa.end_date is null or sa.end_date > now()) and ");
		}
		else {
			sql.append("where");
		}

		//scope model requirements
		sql.append(String.format("doc.scope_model_id = '%s'", scopeModel.getId()));

		//data table
		sql.append(result.getSqlEvent());

		// build where clause
		for(var i = 0; i < parameters.size(); i++) {
			final var parameter = parameters.get(i);
			//retrieve real field model
			final var fieldModel = study.getDatasetModel(parameter.getDatasetModelId()).getFieldModel(parameter.getFieldModelId());
			//build column name
			sql.append(" and ");
			final var doc = parameter.getDatasetModelId().equals(result.getDatasetModelId()) ? "doc" : String.format("d%d", i);
			final var column = String.format("%s.%s", doc, parameter.getFieldModelId());
			//build operator and value
			if(parameter.getCriteria().getOperator().hasValue()) {
				sql.append(parameter.getCriteria().getOperator().toSql(fieldModel, column, parameter.getCriteria().getValue()));
			}
			else {
				sql.append(parameter.getCriteria().getOperator().toSql(fieldModel, column));
			}
		}

		//add requirements on attribute
		if(result.getIgnoreNull()) {
			sql.append(" and ");
			sql.append(result.getFieldModelId());
			sql.append(" is not null");
		}

		if(StringUtils.isNotEmpty(result.getRawSql())) {
			sql.append(" ");
			sql.append(result.getRawSql());
		}

		//group by
		if(groupBy.isPresent()) {
			sql.append(" group by doc.");
			sql.append(groupBy.get());
		}

		//order by
		sql.append(" order by 1");

		logger.debug(sql.toString());
		return sql.toString();
	}

	public RodanoSqlResult getResults(final Request request, final ScopeModel scopeModel, final List<Scope> scopes, final boolean forStatictics, final List<ValueSource> additionalCriteria) {

		final Map<String, Double> resultCount = new LinkedHashMap<>();

		for(final var valueSource : request.getResults()) {
			valueSource.setForStatistics(forStatictics);

			//build sql request
			final var sql = getSqlRequest(request, scopeModel, scopes, Collections.singletonList(valueSource), additionalCriteria);

			final var jooqQuery = create.resultQuery(sql);
			try(
				final var results = jooqQuery.fetchResultSet()
			) {

				while(results.next()) {
					//retrieve category key and consider null values as empty strings
					final var key = StringUtils.defaultString(results.getString(1));
					resultCount.put(key, results.getDouble("total"));
				}
			}
			catch(final SQLException e) {
				logger.error("Unable to calculate value", e);
			}
		}

		//build final results
		final var result = new RodanoSqlResult();
		result.setResultCount(resultCount);
		result.setInPercent(request.getResultType() == ResultType.PERCENT);
		return result;
	}
}
