package ch.rodano.core.services.bll.statistics;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import ch.rodano.api.dto.widget.SummaryColumnDTO;
import ch.rodano.api.dto.widget.SummaryDTO;
import ch.rodano.api.dto.widget.SummaryRowDTO;
import ch.rodano.api.scope.ScopeTinyDTO;
import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.UtilsService;

import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;

@Service
public class LockSummaryService {

	private static final Map<String, Long> DEFAULT_VALUES = Map.of(
		EntityLockStatus.SCOPES_LOCKED.name(), 0l,
		EntityLockStatus.SCOPES_UNLOCKED.name(), 0l,
		EntityLockStatus.EVENTS_LOCKED.name(), 0l,
		EntityLockStatus.EVENTS_UNLOCKED.name(), 0l
	);

	private final StudyService studyService;
	private final ScopeRelationService scopeRelationService;
	private final DSLContext create;

	public LockSummaryService(
		final ScopeRelationService scopeRelationService,
		final DSLContext create,
		final StudyService studyService
	) {
		this.scopeRelationService = scopeRelationService;
		this.create = create;
		this.studyService = studyService;
	}

	/**
	 * Get the lock summary
	 * @param scope Root scope for the data collection.
	 * @param leafScopeModel Leaf scope model object
	 * @return a summary
	 */
	public SummaryDTO getSummary(final Scope scope, final ScopeModel leafScopeModel) {
		//in any case, child scopes must be retrieved to be able to generate the final DTO
		final var childScopes = scopeRelationService.getEnabledChildren(scope, ZonedDateTime.now()).stream()
			.filter(s -> !s.getVirtual())
			.collect(Collectors.toSet());
		final var childScopePks = childScopes.stream().map(Scope::getPk).toList();

		final var scopesByPk = childScopes.stream().collect(Collectors.toMap(Scope::getPk, Function.identity()));
		scopesByPk.put(scope.getPk(), scope);

		final var summaryByScopePk = new LinkedHashMap<Long, Map<String, Long>>();

		//add root scope
		final var rootScopeValues = new HashMap<>(DEFAULT_VALUES);
		summaryByScopePk.put(scope.getPk(), rootScopeValues);

		//fetch scope and event statuses
		final var now = ZonedDateTime.now();
		final var scopeSubquery = create.select(
			SCOPE_ANCESTOR.ANCESTOR_FK.as("ancestor_fk"),
			SCOPE.PK.as("scope_pk"),
			SCOPE.LOCKED.as("scope_locked"),
			DSL.sum(DSL.when(EVENT.LOCKED.isTrue(), 1).otherwise(0)).coerce(Long.class).as("event_locked"),
			DSL.sum(DSL.when(EVENT.LOCKED.isFalse(), 1).otherwise(0)).coerce(Long.class).as("event_unlocked")
		)
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE.PK.equal(SCOPE_ANCESTOR.SCOPE_FK))
			.leftJoin(EVENT).on(EVENT.SCOPE_FK.equal(SCOPE_ANCESTOR.SCOPE_FK))
			.where(SCOPE_ANCESTOR.ANCESTOR_FK.in(childScopePks))
			.and(SCOPE_ANCESTOR.DEFAULT.isTrue())
			.and(SCOPE_ANCESTOR.START_DATE.lessThan(now))
			.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))
			.and(SCOPE.SCOPE_MODEL_ID.eq(leafScopeModel.getId()))
			.and(SCOPE.DELETED.isFalse())
			//remember that the scope may not have any event
			.and(EVENT.DELETED.isNull().or(EVENT.DELETED.isFalse()))
			.groupBy(SCOPE_ANCESTOR.ANCESTOR_FK, SCOPE.PK).asTable("x");

		final var query = create.select(
			scopeSubquery.field("ancestor_fk"),
			DSL.sum(DSL.when(scopeSubquery.field("scope_locked").isTrue(), 1).otherwise(0)).coerce(Long.class).as("scope_locked"),
			DSL.sum(DSL.when(scopeSubquery.field("scope_locked").isFalse(), 1).otherwise(0)).coerce(Long.class).as("scope_unlocked"),
			DSL.sum(scopeSubquery.field("event_locked", Long.class)).coerce(Long.class).as("event_locked"),
			DSL.sum(scopeSubquery.field("event_unlocked", Long.class)).coerce(Long.class).as("event_unlocked")
		)
			.from(scopeSubquery)
			.groupBy(scopeSubquery.field("ancestor_fk"));

		for(final var record : query.fetch()) {
			final var status = Map.of(
				EntityLockStatus.SCOPES_LOCKED.name(), record.component2(),
				EntityLockStatus.SCOPES_UNLOCKED.name(), record.component3(),
				EntityLockStatus.EVENTS_LOCKED.name(), record.component4(),
				EntityLockStatus.EVENTS_UNLOCKED.name(), record.component5()
			);
			summaryByScopePk.put(record.get(SCOPE_ANCESTOR.ANCESTOR_FK), status);
			for(final var entityLockStatus : EntityLockStatus.values()) {
				rootScopeValues.compute(entityLockStatus.name(), (k, v) -> v + status.get(k));
			}
		}

		//add missing child scopes
		for(final var childScope : childScopes) {
			if(!summaryByScopePk.containsKey(childScope.getPk())) {
				summaryByScopePk.put(childScope.getPk(), DEFAULT_VALUES);
			}
		}

		final var rows = summaryByScopePk.entrySet().stream()
			.map(entry -> {
				final var scopeDTO = new ScopeTinyDTO(scopesByPk.get(entry.getKey()));
				return new SummaryRowDTO(scopeDTO, entry.getValue());
			})
			.toList();

		final var columns = Stream.of(EntityLockStatus.values())
			.map(s -> new SummaryColumnDTO(s.name(), s.getLabel(leafScopeModel), false, false))
			.toList();

		return new SummaryDTO(
			"LOCK_SUMMARY",
			Map.of("en", "Lock summary"),
			leafScopeModel.getId(),
			columns,
			rows
		);
	}

	public void getScopeExport(final OutputStream out, final Scope scope, final ScopeModel leafScopeModel, final String[] languages) throws IOException {
		final List<ScopeModel> scopeModels = studyService.getStudy().getLineageOfScopeModels(leafScopeModel);
		final var scopeModelIds = Displayable.getIdsFromDisplayables(scopeModels);

		final var ancestorRelationsTable = SCOPE_ANCESTOR.as("ancestor_relations");
		final var ancestorTable = SCOPE.as("ancestor");
		final var ancestorsField = DSL.multisetAgg(
			ancestorTable.PK,
			ancestorTable.SCOPE_MODEL_ID,
			ancestorTable.CODE,
			ancestorTable.SHORTNAME,
			ancestorTable.LONGNAME
		).as("ancestors").convertFrom(r -> r.map(ra -> new ScopeTinyDTO(ra.value1(), ra.value2(), ra.value3(), ra.value4(), ra.value5())));
		final var lockField = SCOPE.LOCKED.cast(Boolean.class);
		final var query = create.select(ancestorsField, SCOPE.PK, SCOPE.SCOPE_MODEL_ID, SCOPE.CODE, SCOPE.SHORTNAME, SCOPE.LONGNAME, lockField)
			.from(SCOPE)
			//join first on scope ancestor to be able to check rights
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE.PK.equal(SCOPE_ANCESTOR.SCOPE_FK))
			//join twice on scope ancestor to retrieve all scope ancestors
			.innerJoin(ancestorRelationsTable).on(SCOPE.PK.equal(ancestorRelationsTable.SCOPE_FK))
			.innerJoin(ancestorTable).on(ancestorTable.PK.equal(ancestorRelationsTable.ANCESTOR_FK))
			.where(SCOPE_ANCESTOR.ANCESTOR_FK.eq(scope.getPk()))
			.and(ancestorTable.VIRTUAL.isFalse())
			.and(ancestorTable.SCOPE_MODEL_ID.in(scopeModelIds))
			.and(SCOPE.SCOPE_MODEL_ID.eq(leafScopeModel.getId()))
			.and(SCOPE.DELETED.isFalse())
			.groupBy(SCOPE.PK);

		try(var writer = new CSVWriter(new OutputStreamWriter(out))) {

			//header
			final var header = new ArrayList<>();
			for(final var scopeModel : scopeModels) {
				final var scopeModelLabel = scopeModel.getLocalizedShortname(languages);
				header.add(String.format("%s ID", scopeModelLabel));
				header.add(scopeModelLabel);
			}
			header.add("Status");
			writer.writeNext(header.toArray(new String[0]));

			//content
			final var results = query.fetch();
			for(final var record : results) {
				final var line = new ArrayList<String>(header.size());

				//find scope lineage
				final List<ScopeTinyDTO> ancestors = record.get(ancestorsField);
				final var currentScope = new ScopeTinyDTO(
					record.getValue(SCOPE.PK),
					record.getValue(SCOPE.SCOPE_MODEL_ID),
					record.getValue(SCOPE.CODE),
					record.getValue(SCOPE.SHORTNAME),
					record.getValue(SCOPE.LONGNAME)
				);
				ancestors.add(currentScope);

				//column of ancestors' scopes
				for(final var scopeModel : scopeModels) {
					final var ancestor = ancestors.stream().filter(a -> a.modelId().equals(scopeModel.getId())).findAny();
					line.add(ancestor.map(ScopeTinyDTO::pk).map(p -> Long.toString(p)).orElse(""));
					line.add(ancestor.map(ScopeTinyDTO::code).orElse(""));
				}

				line.add(record.getValue(lockField) ? "Locked" : "Unlocked");

				writer.writeNext(line.toArray(new String[0]));
			}
		}
	}

	public void getEventExport(final OutputStream out, final Scope scope, final ScopeModel leafScopeModel, final String[] languages) throws IOException {
		final List<ScopeModel> scopeModels = studyService.getStudy().getLineageOfScopeModels(leafScopeModel);
		final var scopeModelIds = Displayable.getIdsFromDisplayables(scopeModels);

		final var ancestorRelationsTable = SCOPE_ANCESTOR.as("ancestor_relations");
		final var ancestorTable = SCOPE.as("ancestor");
		final var ancestorsField = DSL.multisetAgg(
			ancestorTable.PK,
			ancestorTable.SCOPE_MODEL_ID,
			ancestorTable.CODE,
			ancestorTable.SHORTNAME,
			ancestorTable.LONGNAME
		).as("ancestors").convertFrom(r -> r.map(ra -> new ScopeTinyDTO(ra.value1(), ra.value2(), ra.value3(), ra.value4(), ra.value5())));
		final var lockField = EVENT.LOCKED.cast(Boolean.class);
		final var query = create.select(ancestorsField, SCOPE.PK, SCOPE.SCOPE_MODEL_ID, SCOPE.CODE, SCOPE.SHORTNAME, SCOPE.LONGNAME, EVENT.PK, EVENT.EVENT_MODEL_ID, EVENT.DATE, lockField)
			.from(SCOPE)
			.innerJoin(EVENT).on(EVENT.SCOPE_FK.eq(SCOPE.PK))
			//join first on scope ancestor to be able to check rights
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE.PK.equal(SCOPE_ANCESTOR.SCOPE_FK))
			//join twice on scope ancestor to retrieve all scope ancestors
			.innerJoin(ancestorRelationsTable).on(SCOPE.PK.equal(ancestorRelationsTable.SCOPE_FK))
			.innerJoin(ancestorTable).on(ancestorTable.PK.equal(ancestorRelationsTable.ANCESTOR_FK))
			.where(SCOPE_ANCESTOR.ANCESTOR_FK.eq(scope.getPk()))
			.and(ancestorTable.VIRTUAL.isFalse())
			.and(ancestorTable.SCOPE_MODEL_ID.in(scopeModelIds))
			.and(SCOPE.SCOPE_MODEL_ID.eq(leafScopeModel.getId()))
			.and(SCOPE.DELETED.isFalse())
			.and(EVENT.DELETED.isFalse())
			.groupBy(EVENT.PK);

		try(var writer = new CSVWriter(new OutputStreamWriter(out))) {

			//header
			final var header = new ArrayList<>();
			for(final var scopeModel : scopeModels) {
				final var scopeModelLabel = scopeModel.getLocalizedShortname(languages);
				header.add(String.format("%s ID", scopeModelLabel));
				header.add(scopeModelLabel);
			}
			header.add("Event ID");
			header.add("Event");
			header.add("Event date");
			header.add("Status");
			writer.writeNext(header.toArray(new String[0]));

			//content
			final var results = query.fetch();
			for(final var record : results) {
				final var line = new ArrayList<String>(header.size());

				//find scope lineage
				final List<ScopeTinyDTO> ancestors = record.get(ancestorsField);
				final var currentScope = new ScopeTinyDTO(
					record.getValue(SCOPE.PK),
					record.getValue(SCOPE.SCOPE_MODEL_ID),
					record.getValue(SCOPE.CODE),
					record.getValue(SCOPE.SHORTNAME),
					record.getValue(SCOPE.LONGNAME)
				);
				ancestors.add(currentScope);

				//column of ancestors' scopes
				for(final var scopeModel : scopeModels) {
					final var ancestor = ancestors.stream().filter(a -> a.modelId().equals(scopeModel.getId())).findAny();
					line.add(ancestor.map(ScopeTinyDTO::pk).map(p -> Long.toString(p)).orElse(""));
					line.add(ancestor.map(ScopeTinyDTO::code).orElse(""));
				}
				line.add(Long.toString(record.getValue(EVENT.PK)));
				final var eventModel = leafScopeModel.getEventModel(record.getValue(EVENT.EVENT_MODEL_ID));
				line.add(eventModel.getLocalizedShortname(languages));
				final var eventDate = record.getValue(EVENT.DATE);
				line.add(eventDate != null ? UtilsService.HUMAN_READABLE_DATE_TIME.format(eventDate) : "");

				line.add(record.getValue(lockField) ? "Locked" : "Unlocked");

				writer.writeNext(line.toArray(new String[0]));
			}
		}
	}

}
