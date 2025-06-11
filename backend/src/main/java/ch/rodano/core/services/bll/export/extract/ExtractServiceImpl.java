package ch.rodano.core.services.bll.export.extract;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.opencsv.CSVWriter;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.study.StudyService;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.SCOPE_RELATION;

@Service
public class ExtractServiceImpl implements ExtractService {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX").withZone(ZoneOffset.UTC);

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DSLContext create;
	private final StudyService studyService;

	public ExtractServiceImpl(
		final StudyService studyService,
		final DSLContext create
	) {
		this.create = create;
		this.studyService = studyService;
	}

	@Override
	public void getDataExtract(
		final OutputStream out,
		final DatasetModel datasetModel,
		final String languageId,
		final Collection<Scope> scopes,
		final boolean withModificationDates
	) throws IOException {
		Assert.notEmpty(scopes, "A non empty list of scopes must be provided");
		final List<FieldModel> fieldModels = datasetModel.getFieldModelsExportables();

		//retrieve document scope model
		final var scopeModels = datasetModel.getScopeModels();
		final var scopeModel = !scopeModels.isEmpty() ? scopeModels.get(0) : datasetModel.getEventModels().get(0).getScopeModel();
		final Optional<ScopeModel> parentScopeModel = scopeModel.isRoot() ? Optional.empty() : Optional.of(scopeModel.getDefaultParent());

		final var parentTable = SCOPE.as("parent");

		final List<SelectFieldOrAsterisk> fields = new ArrayList<>();
		fields.addAll(
			List.of(
				SCOPE.PK,
				SCOPE.CODE,
				parentTable.PK,
				parentTable.CODE,
				EVENT.PK,
				EVENT.SCOPE_MODEL_ID,
				EVENT.EVENT_MODEL_ID,
				DATASET.PK
			)
		);

		for(final var fieldModel : datasetModel.getFieldModelsExportables()) {
			fields.add(DSL.anyValue(FIELD.VALUE).filterWhere(FIELD.FIELD_MODEL_ID.eq(fieldModel.getId())).as(fieldModel.getId()));
			fields.add(DSL.anyValue(FIELD.LAST_UPDATE_TIME).filterWhere(FIELD.FIELD_MODEL_ID.eq(fieldModel.getId())).as(String.format("%s_MD", fieldModel.getId())));
		}

		final List<Condition> conditions = new ArrayList<>();
		conditions.add(DATASET.DATASET_MODEL_ID.eq(datasetModel.getId()));
		conditions.add(DATASET.DELETED.isFalse());
		conditions.add(SCOPE.DELETED.isFalse());

		//add event conditions
		if(!datasetModel.isScopeDocumentation()) {
			conditions.add(EVENT.DELETED.isFalse());
			conditions.add(EVENT.DATE.isNotNull());
		}

		//add scope conditions
		final var now = ZonedDateTime.now();
		final List<Condition> scopeConditions = new ArrayList<>();
		for(final var scope : scopes) {
			scopeConditions.add(
				SCOPE.PK.eq(scope.getPk()).or(
					SCOPE_ANCESTOR.ANCESTOR_FK.eq(scope.getPk())
						.and(SCOPE_ANCESTOR.START_DATE.lessThan(now))
						.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))
				)
			);
		}
		conditions.add(DSL.or(scopeConditions));

		final SelectOnConditionStep<Record> query;
		if(datasetModel.isScopeDocumentation()) {
			query = create.select(fields)
				.from(FIELD)
				.join(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
				//this join allows to select columns of the event table even if they are all null
				.leftJoin(EVENT).on(DATASET.EVENT_FK.eq(EVENT.PK))
				.join(SCOPE).on(DATASET.SCOPE_FK.eq(SCOPE.PK));
		}
		else {
			query = create.select(fields)
				.from(FIELD)
				.join(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
				.join(EVENT).on(DATASET.EVENT_FK.eq(EVENT.PK))
				.join(SCOPE).on(EVENT.SCOPE_FK.eq(SCOPE.PK));
		}

		//build query
		query.leftJoin(SCOPE_ANCESTOR).on(SCOPE.PK.eq(SCOPE_ANCESTOR.SCOPE_FK))
			.leftJoin(SCOPE_RELATION).on(SCOPE.PK.eq(SCOPE_RELATION.SCOPE_FK).and(SCOPE_RELATION.DEFAULT.isTrue()))
			.leftJoin(parentTable).on(SCOPE_RELATION.PARENT_FK.eq(parentTable.PK))
			.where(DSL.and(conditions))
			.groupBy(DATASET.PK)
			.orderBy(SCOPE.CODE, EVENT.DATE, DATASET.PK);

		logger.debug(query.toString());

		final var study = studyService.getStudy();

		//choose event export column label
		final var eventColumnLabel = StringUtils.defaultIfBlank(study.getExportVisitsLabel(), "EVENT").toUpperCase();

		final var writer = new CSVWriter(new OutputStreamWriter(out));

		var columnNumber = 3;
		columnNumber += fieldModels.size();
		if(withModificationDates) {
			columnNumber += fieldModels.size();
		}
		if(parentScopeModel.isPresent()) {
			columnNumber += 2;
		}
		if(!datasetModel.isScopeDocumentation()) {
			columnNumber += 2;
			//restore to 4 when EVENT_DATE and EVENT_END_DATE will be re-added
			//columnNumber += 4;
		}

		//header
		short i = 0;
		final var csvHeader = new String[columnNumber];
		//static columns
		if(parentScopeModel.isPresent()) {
			csvHeader[i++] = String.format("%s_ID", parentScopeModel.get().getDefaultLocalizedShortname().toUpperCase());
			csvHeader[i++] = parentScopeModel.get().getId().toUpperCase();
		}

		csvHeader[i++] = String.format("%s_ID", scopeModel.getDefaultLocalizedShortname().toUpperCase());
		csvHeader[i++] = scopeModel.getId().toUpperCase();

		if(!datasetModel.isScopeDocumentation()) {
			csvHeader[i++] = "EVENT_ID";
			csvHeader[i++] = eventColumnLabel;
			//csvHeader[i++] = "EVENT_DATE";
			//csvHeader[i++] = "EVENT_END_DATE";
		}
		csvHeader[i++] = "DATASET_ID";

		//dynamic columns
		for(final var fieldModel : fieldModels) {
			final var fieldModelLabel = fieldModel.getExportColumnLabel().toUpperCase();
			csvHeader[i++] = fieldModelLabel;
			if(withModificationDates) {
				csvHeader[i++] = String.format("%s_MODIFICATION_DATE", fieldModelLabel);
			}
		}
		writer.writeNext(csvHeader);

		for(final org.jooq.Record row : query.fetch()) {
			i = 0;
			final var csvLine = new String[columnNumber];

			//static columns
			if(parentScopeModel.isPresent()) {
				csvLine[i++] = row.get(parentTable.PK).toString();
				csvLine[i++] = row.get(parentTable.CODE);
			}

			csvLine[i++] = row.get(SCOPE.PK).toString();
			csvLine[i++] = row.get(SCOPE.CODE);

			if(!datasetModel.isScopeDocumentation()) {
				csvLine[i++] = row.get(EVENT.PK).toString();
				final var eventModel = study.getScopeModel(row.get(EVENT.SCOPE_MODEL_ID)).getEventModel(row.get(EVENT.EVENT_MODEL_ID));
				csvLine[i++] = eventModel.getDefaultLocalizedShortname();
				/*final var date = TimeHelper.asZonedDateTime(results.getTimestamp("event_date"));
				csvLine[i++] = date != null ? date.format(DATE_FORMATTER) : "";
				final var endDate = TimeHelper.asZonedDateTime(results.getTimestamp("event_end_date"));
				csvLine[i++] = endDate != null ? endDate.format(DATE_FORMATTER) : "";*/
			}

			csvLine[i++] = row.get(DATASET.PK).toString();

			//dynamic columns
			for(final var fieldModel : fieldModels) {
				csvLine[i++] = formatField(fieldModel, row);
				if(withModificationDates) {
					csvLine[i++] = row.get(String.format("%s_MD", fieldModel.getId()), ZonedDateTime.class).format(DATE_FORMATTER);
				}
			}

			writer.writeNext(csvLine);
		}

		// Flush the writer to the OutputStream
		writer.flush();
	}

	@Override
	public void zipExtracts(
		final OutputStream out,
		final List<DatasetModel> datasetModels,
		final String languageId,
		final Collection<Scope> scopes,
		final boolean withModificationDates
	) {
		// Open the zip output stream
		try(var zipStream = new ZipOutputStream(new BufferedOutputStream(out))) {
			for(final var document : datasetModels) {
				// Define the filename
				final var filename = getCSVDocumentFilename(document);

				// Put the new entry into the zip stream
				zipStream.putNextEntry(new ZipEntry(filename));

				// Write to the zip entry
				getDataExtract(zipStream, document, languageId, scopes, withModificationDates);

				// Close the zip entry
				zipStream.closeEntry();
			}
		}
		catch(final IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public String getCSVDocumentFilename(final DatasetModel datasetModel) {
		return String.format("%s.csv", datasetModel.getId());
	}

	private String formatField(final FieldModel fieldModel, final org.jooq.Record row) {
		final String value = row.get(fieldModel.getId(), String.class);

		if(StringUtils.isNotBlank(value)) {
			return value.replaceAll("[\r\n]+", " ");
		}

		return "";
	}
}
