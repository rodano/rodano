package ch.rodano.core.services.bll.export.report;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.utils.ExportableUtils;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.utils.UtilsService;

import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.SCOPE_RELATION;

@Profile({ "!migration & !database" })
@Service
public class ReportServiceImpl implements ReportService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DSLContext create;
	private final ScopeRelationService scopeRelationService;

	public ReportServiceImpl(
		final DSLContext create,
		final ScopeRelationService scopeRelationService
	) {
		this.create = create;
		this.scopeRelationService = scopeRelationService;
	}

	@Override
	public void getTransferReport(final OutputStream out, final Collection<Scope> rootScopes, final ScopeModel scopeModel) throws IOException {
		final var parentScopeModel = scopeModel.getDefaultParent();

		// Build csv
		try(var writer = new CSVWriter(new OutputStreamWriter(out))) {

			// Header
			final var header = new String[] {
				String.format("%s ID", scopeModel.getDefaultLocalizedShortname()),
				scopeModel.getDefaultLocalizedShortname(),
				String.format("%s ID", parentScopeModel.getDefaultLocalizedShortname()),
				parentScopeModel.getDefaultLocalizedShortname(),
				"Start date",
				"Stop date"
			};

			writer.writeNext(header);

			// Body
			final Collection<Scope> leafScopes = rootScopes
				.stream()
				.flatMap(rootScope -> scopeRelationService.getEnabledDescendants(rootScope, scopeModel).stream())
				.collect(Collectors.toCollection(TreeSet::new));

			for(final var leaf : leafScopes) {
				final var relations = scopeRelationService.getAllParentRelations(leaf);

				// Filter by parent model
				final var filteredRelations = relations.stream()
					.filter(relation -> scopeRelationService.getParent(relation).getScopeModelId().equals(parentScopeModel.getId()))
					.collect(Collectors.toCollection(ArrayList::new));

				if(filteredRelations.size() > 1) {
					for(final var relation : filteredRelations) {
						final var line = new String[header.length];
						var i = 0;
						line[i++] = leaf.getPk().toString();
						line[i++] = leaf.getCode();
						final var parent = scopeRelationService.getParent(relation);
						line[i++] = parent.getPk().toString();
						line[i++] = parent.getCode();
						line[i++] = relation.getStartDate().format(UtilsService.HUMAN_READABLE_DATE_TIME);

						if(relation.getEndDate() != null) {
							line[i] = relation.getEndDate().format(UtilsService.HUMAN_READABLE_DATE_TIME);
						}

						writer.writeNext(line);
					}
				}
			}
		}
	}

	@Override
	public void getVisitReport(final OutputStream out, final Collection<Scope> rootScopes, final ScopeModel scopeModel) throws IOException {
		final var parentScopeModel = scopeModel.getDefaultParent();
		final Collection<Long> rootScopePks = rootScopes.stream().map(Scope::getPk).toList();

		// Build csv
		try(var writer = new CSVWriter(new OutputStreamWriter(out))) {

			// Header
			final var header = new String[] {
				String.format("%s ID", parentScopeModel.getDefaultLocalizedShortname()),
				parentScopeModel.getDefaultLocalizedShortname(),
				String.format("%s ID", scopeModel.getDefaultLocalizedShortname()),
				scopeModel.getDefaultLocalizedShortname(),
				"Event ID",
				"Event",
				"Event expected date",
				"Event date",
				"Event end date",
				"Removed"
			};
			writer.writeNext(header);

			final var PARENT_SCOPE = SCOPE.as("parent");

			// Body
			final var query = create.select(PARENT_SCOPE.PK, PARENT_SCOPE.CODE, SCOPE.PK, SCOPE.CODE, EVENT.PK, EVENT.EVENT_MODEL_ID, EVENT.EXPECTED_DATE, EVENT.DATE, EVENT.END_DATE, EVENT.DELETED)
				.from(EVENT)
				.innerJoin(SCOPE).on(EVENT.SCOPE_FK.eq(SCOPE.PK))
				.innerJoin(SCOPE_RELATION).on(SCOPE.PK.eq(SCOPE_RELATION.SCOPE_FK))
				.innerJoin(PARENT_SCOPE).on(SCOPE_RELATION.PARENT_FK.eq(PARENT_SCOPE.PK).and(SCOPE_RELATION.DEFAULT.isTrue()))
				.innerJoin(SCOPE_ANCESTOR).on(SCOPE.PK.eq(SCOPE_ANCESTOR.SCOPE_FK))
				.where(SCOPE_ANCESTOR.ANCESTOR_FK.in(rootScopePks))
				.and(SCOPE.SCOPE_MODEL_ID.eq(scopeModel.getId()))
				.and(EVENT.EXPECTED_DATE.isNotNull());

			final var result = query.fetch();
			for(final Record record : result) {
				final var line = new String[header.length];
				var i = 0;
				line[i++] = record.get(PARENT_SCOPE.PK).toString();
				line[i++] = record.get(PARENT_SCOPE.CODE);
				line[i++] = record.get(SCOPE.PK).toString();
				line[i++] = record.get(SCOPE.CODE);
				line[i++] = record.get(EVENT.PK).toString();
				final var eventModelId = record.get(EVENT.EVENT_MODEL_ID);
				final var eventModel = scopeModel.getEventModel(eventModelId);
				//TODO this is a hack while some visits exist only to display scope information
				if(eventModel.getDatasetModels().stream().allMatch(DatasetModel::isScopeDocumentation)) {
					continue;
				}
				line[i++] = eventModel.getDefaultLocalizedShortname();
				final var expectedDate = record.get(EVENT.EXPECTED_DATE);
				line[i++] = expectedDate != null ? expectedDate.format(UtilsService.HUMAN_READABLE_DATE_TIME) : "";
				final var date = record.get(EVENT.DATE);
				line[i++] = date != null ? date.format(UtilsService.HUMAN_READABLE_DATE_TIME) : "";
				final var endDate = record.get(EVENT.END_DATE);
				line[i++] = endDate != null ? endDate.format(UtilsService.HUMAN_READABLE_DATE_TIME) : "";
				line[i++] = record.get(EVENT.DELETED).toString();
				writer.writeNext(line);
			}
		}
	}

	@Override
	public void getDataStructure(
		final OutputStream out,
		final DatasetModel datasetModel,
		final boolean withModificationDates,
		final String... languages
	) throws IOException {
		ExportableUtils.getDataStructure(out, Collections.singleton(datasetModel), withModificationDates, languages);
	}

	@Override
	public void zipDataStructures(
		final OutputStream out,
		final Collection<DatasetModel> datasetModels,
		final boolean withModificationDates,
		final String... languages
	) {
		// Open the zip output stream
		try(var zipStream = new ZipOutputStream(new BufferedOutputStream(out))) {
			for(final var datasetModel : datasetModels) {
				// Define the filename
				final var filename = getDataStructureFilename(datasetModel);

				// Put the new entry into the zip stream
				zipStream.putNextEntry(new ZipEntry(filename));

				// Write the bytes to the zip entry
				ExportableUtils.getDataStructure(zipStream, Collections.singletonList(datasetModel), withModificationDates, languages);

				// Close the zip entry
				zipStream.closeEntry();
			}
		}
		catch(final IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public String getDataStructureFilename(final DatasetModel datasetModel) {
		return String.format("%s_SPECIFICATIONS.csv", datasetModel.getId());
	}
}
