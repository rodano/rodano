package ch.rodano.core.services.bll.export.views;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.services.bll.study.StudyService;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.SCOPE;

@Profile({ "!migration & !database" })
@Service
public class ExportViewServiceImpl implements ExportViewService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final List<String> BASE_COLUMNS = List.of(
		"scope_fk",
		"id",
		"code",
		"scope_model_id",
		"event_fk",
		"event_model_id",
		"event_group_number",
		"event_blocking",
		"event_expected_date",
		"event_date",
		"event_end_date",
		"last_update_time",
		"dataset_fk"
	);

	private final DSLContext create;
	private final StudyService studyService;

	public ExportViewServiceImpl(
		final DSLContext create,
		final StudyService studyService
	) {
		this.create = create;
		this.studyService = studyService;
	}

	@Override
	public void updateViews() {
		final var datasets = studyService.getStudy().getDatasetModels().stream()
			.filter(DatasetModel::isExportable)
			.toList();
		logger.info("Updating export views for datasets {}", datasets.stream().map(DatasetModel::getId).sorted().collect(Collectors.joining(",")));
		datasets.forEach(this::generateDatasetView);
	}

	public void generateDatasetView(final DatasetModel datasetModel) {
		if(datasetModel.isScopeDocumentation()) {
			generateDatasetOnScopeView(datasetModel);
		}
		else {
			generateDatasetOnEventView(datasetModel);
		}
	}

	private void generateDatasetOnScopeView(final DatasetModel datasetModel) {
		final List<String> columns = new ArrayList<>(BASE_COLUMNS);

		final List<SelectFieldOrAsterisk> fields = new ArrayList<>();
		fields.addAll(
			List.of(
				SCOPE.PK,
				SCOPE.ID,
				SCOPE.CODE,
				SCOPE.SCOPE_MODEL_ID,
				DSL.inline(null, SQLDataType.BIGINT),
				DSL.inline(null, SQLDataType.VARCHAR),
				DSL.inline(null, SQLDataType.INTEGER),
				DSL.inline(null, SQLDataType.BOOLEAN),
				DSL.inline(null, SQLDataType.DATE),
				DSL.inline(null, SQLDataType.DATE),
				DSL.inline(null, SQLDataType.DATE),
				DATASET.LAST_UPDATE_TIME,
				DATASET.PK
			)
		);

		for(final var column : generateFieldColumns(datasetModel)) {
			columns.add(column.getLeft());
			fields.add(column.getRight());
		}

		create.createOrReplaceView(
			datasetModel.getExportTableName(),
			columns.toArray(new String[columns.size()])
		).as(
			DSL.select(fields)
				.from(FIELD)
				.join(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
				.join(SCOPE).on(DATASET.SCOPE_FK.eq(SCOPE.PK))
				.where(
					DATASET.DATASET_MODEL_ID.eq(datasetModel.getId())
						.and(DATASET.DELETED.isFalse())
						.and(SCOPE.DELETED.isFalse())
				)
				.groupBy(DATASET.PK)
				.orderBy(DATASET.PK)
		).execute();
	}

	private void generateDatasetOnEventView(final DatasetModel datasetModel) {
		final List<String> columns = new ArrayList<>(BASE_COLUMNS);

		final List<SelectFieldOrAsterisk> fields = new ArrayList<>();
		fields.addAll(
			List.of(
				SCOPE.PK,
				SCOPE.ID,
				SCOPE.CODE,
				SCOPE.SCOPE_MODEL_ID,
				EVENT.PK,
				EVENT.EVENT_MODEL_ID,
				EVENT.EVENT_GROUP_NUMBER,
				EVENT.BLOCKING,
				EVENT.EXPECTED_DATE,
				EVENT.DATE,
				EVENT.END_DATE,
				DATASET.LAST_UPDATE_TIME,
				DATASET.PK
			)
		);

		for(final var column : generateFieldColumns(datasetModel)) {
			columns.add(column.getLeft());
			fields.add(column.getRight());
		}

		create.createOrReplaceView(
			datasetModel.getExportTableName(),
			columns.toArray(new String[columns.size()])
		).as(
			DSL.select(fields)
				.from(FIELD)
				.join(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
				.join(EVENT).on(DATASET.EVENT_FK.eq(EVENT.PK))
				.join(SCOPE).on(EVENT.SCOPE_FK.eq(SCOPE.PK))
				.where(
					DATASET.DATASET_MODEL_ID.eq(datasetModel.getId())
						.and(DATASET.DELETED.isFalse())
						.and(EVENT.DELETED.isFalse())
						.and(EVENT.DATE.isNotNull())
						.and(SCOPE.DELETED.isFalse())
				)
				.groupBy(DATASET.PK)
				.orderBy(EVENT.DATE, DATASET.PK)
		).execute();
	}

	private List<Pair<String, Field<?>>> generateFieldColumns(final DatasetModel datasetModel) {
		final List<Pair<String, Field<?>>> columns = new ArrayList<>();
		for(final var fieldModel : datasetModel.getFieldModelsExportables()) {
			columns.addAll(generateDatasetFieldColumns(fieldModel));
		}
		return columns;
	}

	private final List<Pair<String, Field<?>>> generateDatasetFieldColumns(final FieldModel fieldModel) {
		final var fielModelColumn = fieldModel.getId().toLowerCase();
		final Pair<String, Field<?>> rawColumn = Pair.of(
			String.format("%s_raw", fielModelColumn),
			DSL.anyValue(FIELD.VALUE).filterWhere(FIELD.FIELD_MODEL_ID.eq(fieldModel.getId()))
		);
		final Pair<String, Field<?>> typedColumn = Pair.of(
			fielModelColumn,
			typedColumn(fieldModel)
		);
		final Pair<String, Field<?>> mdColumn = Pair.of(
			String.format("%s_md", fielModelColumn),
			DSL.anyValue(FIELD.LAST_UPDATE_TIME).filterWhere(FIELD.FIELD_MODEL_ID.eq(fieldModel.getId()))
		);
		return List.of(rawColumn, typedColumn, mdColumn);
	}

	private Field<?> typedColumn(final FieldModel fieldModel) {
		final var rawValue = DSL.anyValue(FIELD.VALUE).filterWhere(FIELD.FIELD_MODEL_ID.eq(fieldModel.getId()));
		if(OperandType.NUMBER.equals(fieldModel.getDataType())) {
			return DSL.case_().when(rawValue.notEqual(""), rawValue.cast(SQLDataType.DECIMAL(18, 9)));
		}
		switch(fieldModel.getDataType()) {
			case DATE -> {
				final var datePrefix = new StringBuilder();
				if(!fieldModel.isWithMonths()) {
					datePrefix.append("01.");
				}
				if(!fieldModel.isWithDays()) {
					datePrefix.append("01.");
				}
				final Field<?> streamlinedValue = datePrefix.isEmpty() ? rawValue : DSL.concat(DSL.inline(datePrefix.toString()), rawValue);
				return DSL.function("str_to_date", SQLDataType.LOCALDATETIME, streamlinedValue, DSL.inline("%d.%m.%Y"));
			}
			case STRING -> {
				return rawValue.cast(SQLDataType.CHAR(255));
			}
			default -> {
				return rawValue;
			}
		}
	}
}
