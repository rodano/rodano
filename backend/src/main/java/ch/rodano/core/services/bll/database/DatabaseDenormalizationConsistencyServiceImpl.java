package ch.rodano.core.services.bll.database;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import static ch.rodano.core.model.jooq.tables.Dataset.DATASET;
import static ch.rodano.core.model.jooq.tables.DatasetAudit.DATASET_AUDIT;
import static ch.rodano.core.model.jooq.tables.Event.EVENT;
import static ch.rodano.core.model.jooq.tables.Field.FIELD;
import static ch.rodano.core.model.jooq.tables.Form.FORM;
import static ch.rodano.core.model.jooq.tables.FormAudit.FORM_AUDIT;
import static ch.rodano.core.model.jooq.tables.WorkflowStatus.WORKFLOW_STATUS;
import static ch.rodano.core.model.jooq.tables.WorkflowStatusAudit.WORKFLOW_STATUS_AUDIT;

@Service
public class DatabaseDenormalizationConsistencyServiceImpl implements DatabaseDenormalizationConsistencyService {

	private final DSLContext create;

	public DatabaseDenormalizationConsistencyServiceImpl(final DSLContext create) {
		this.create = create;
	}

	@Override
	public List<DenormalizationInconsistencyGroup> fixInconsistencies(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		issues.addAll(checkFormDenormalization(dryRun));
		issues.addAll(checkDatasetDenormalization(dryRun));
		issues.addAll(checkEventWorkflowStatusDenormalization(dryRun));
		issues.addAll(checkFormWorkflowStatusDenormalization(dryRun));
		issues.addAll(checkFieldWorkflowStatusDenormalization(dryRun));
		issues.addAll(checkFormAuditDenormalization(dryRun));
		issues.addAll(checkDatasetAuditDenormalization(dryRun));
		issues.addAll(checkEventWorkflowStatusAuditDenormalization(dryRun));
		issues.addAll(checkFormWorkflowStatusAuditDenormalization(dryRun));
		issues.addAll(checkFieldWorkflowStatusAuditDenormalization(dryRun));
		return groupIssues(issues);
	}

	private List<DenormalizationInconsistencyGroup> groupIssues(final List<DenormalizationInconsistency> issues) {
		final var groups = new ArrayList<DenormalizationInconsistencyGroup>();
		for(final var issue : issues) {
			var group = groups.stream()
				.filter(g -> g.entity().equals(issue.entity()) && g.modelId().equals(issue.modelId()) && g.status() == issue.status())
				.findFirst()
				.orElse(null);
			if(group == null) {
				group = new DenormalizationInconsistencyGroup(issue.entity(), issue.modelId(), new ArrayList<>(), issue.status());
				groups.add(group);
			}
			group.pks().add(issue.pk());
		}
		return groups;
	}

	private List<DenormalizationInconsistency> checkFormDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(FORM.PK, FORM.FORM_MODEL_ID, EVENT.SCOPE_FK)
			.from(FORM)
			.innerJoin(EVENT).on(FORM.EVENT_FK.eq(EVENT.PK))
			.where(FORM.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK));

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var formPk = r.get(FORM.PK);
				final var formModelId = r.get(FORM.FORM_MODEL_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				if(!dryRun) {
					create.update(FORM)
						.set(FORM.SCOPE_FK, expectedScopeFk)
						.where(FORM.PK.eq(formPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.FORM, formModelId, formPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkDatasetDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(DATASET.PK, DATASET.DATASET_MODEL_ID, EVENT.SCOPE_FK)
			.from(DATASET)
			.innerJoin(EVENT).on(DATASET.EVENT_FK.eq(EVENT.PK))
			.where(DATASET.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK));

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var datasetPk = r.get(DATASET.PK);
				final var datasetModelId = r.get(DATASET.DATASET_MODEL_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				if(!dryRun) {
					create.update(DATASET)
						.set(DATASET.SCOPE_FK, expectedScopeFk)
						.where(DATASET.PK.eq(datasetPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.DATASET, datasetModelId, datasetPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkEventWorkflowStatusDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(WORKFLOW_STATUS.PK, WORKFLOW_STATUS.WORKFLOW_ID, EVENT.SCOPE_FK)
			.from(WORKFLOW_STATUS)
			.innerJoin(EVENT).on(WORKFLOW_STATUS.EVENT_FK.eq(EVENT.PK))
			.where(WORKFLOW_STATUS.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK))
			.and(WORKFLOW_STATUS.FORM_FK.isNull())
			.and(WORKFLOW_STATUS.FIELD_FK.isNull());

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var wsPk = r.get(WORKFLOW_STATUS.PK);
				final var workflowId = r.get(WORKFLOW_STATUS.WORKFLOW_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				if(!dryRun) {
					create.update(WORKFLOW_STATUS)
						.set(WORKFLOW_STATUS.SCOPE_FK, expectedScopeFk)
						.where(WORKFLOW_STATUS.PK.eq(wsPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.WORKFLOW_STATUS, workflowId, wsPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkFormWorkflowStatusDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(WORKFLOW_STATUS.PK, WORKFLOW_STATUS.WORKFLOW_ID, EVENT.SCOPE_FK, FORM.EVENT_FK)
			.from(WORKFLOW_STATUS)
			.innerJoin(FORM).on(WORKFLOW_STATUS.FORM_FK.eq(FORM.PK))
			.innerJoin(EVENT).on(FORM.EVENT_FK.eq(EVENT.PK))
			.where(
				WORKFLOW_STATUS.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK)
					.or(WORKFLOW_STATUS.EVENT_FK.isDistinctFrom(FORM.EVENT_FK))
			)
			.and(WORKFLOW_STATUS.FIELD_FK.isNull());

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var wsPk = r.get(WORKFLOW_STATUS.PK);
				final var workflowId = r.get(WORKFLOW_STATUS.WORKFLOW_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				final var expectedEventFk = r.get(FORM.EVENT_FK);
				if(!dryRun) {
					create.update(WORKFLOW_STATUS)
						.set(WORKFLOW_STATUS.SCOPE_FK, expectedScopeFk)
						.set(WORKFLOW_STATUS.EVENT_FK, expectedEventFk)
						.where(WORKFLOW_STATUS.PK.eq(wsPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.WORKFLOW_STATUS, workflowId, wsPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkFieldWorkflowStatusDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(WORKFLOW_STATUS.PK, WORKFLOW_STATUS.WORKFLOW_ID, EVENT.SCOPE_FK, DATASET.EVENT_FK)
			.from(WORKFLOW_STATUS)
			.innerJoin(FIELD).on(WORKFLOW_STATUS.FIELD_FK.eq(FIELD.PK))
			.innerJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.innerJoin(EVENT).on(DATASET.EVENT_FK.eq(EVENT.PK))
			.where(
				WORKFLOW_STATUS.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK)
					.or(WORKFLOW_STATUS.EVENT_FK.isDistinctFrom(DATASET.EVENT_FK))
			);

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var wsPk = r.get(WORKFLOW_STATUS.PK);
				final var workflowId = r.get(WORKFLOW_STATUS.WORKFLOW_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				final var expectedEventFk = r.get(DATASET.EVENT_FK);
				if(!dryRun) {
					create.update(WORKFLOW_STATUS)
						.set(WORKFLOW_STATUS.SCOPE_FK, expectedScopeFk)
						.set(WORKFLOW_STATUS.EVENT_FK, expectedEventFk)
						.where(WORKFLOW_STATUS.PK.eq(wsPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.WORKFLOW_STATUS, workflowId, wsPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkFormAuditDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(FORM_AUDIT.PK, FORM_AUDIT.FORM_MODEL_ID, EVENT.SCOPE_FK)
			.from(FORM_AUDIT)
			.innerJoin(EVENT).on(FORM_AUDIT.EVENT_FK.eq(EVENT.PK))
			.where(FORM_AUDIT.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK));

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var auditPk = r.get(FORM_AUDIT.PK);
				final var formModelId = r.get(FORM_AUDIT.FORM_MODEL_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				if(!dryRun) {
					create.update(FORM_AUDIT)
						.set(FORM_AUDIT.SCOPE_FK, expectedScopeFk)
						.where(FORM_AUDIT.PK.eq(auditPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.FORM, formModelId, auditPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkDatasetAuditDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(DATASET_AUDIT.PK, DATASET_AUDIT.DATASET_MODEL_ID, EVENT.SCOPE_FK)
			.from(DATASET_AUDIT)
			.innerJoin(EVENT).on(DATASET_AUDIT.EVENT_FK.eq(EVENT.PK))
			.where(DATASET_AUDIT.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK));

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var auditPk = r.get(DATASET_AUDIT.PK);
				final var datasetModelId = r.get(DATASET_AUDIT.DATASET_MODEL_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				if(!dryRun) {
					create.update(DATASET_AUDIT)
						.set(DATASET_AUDIT.SCOPE_FK, expectedScopeFk)
						.where(DATASET_AUDIT.PK.eq(auditPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.DATASET, datasetModelId, auditPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkEventWorkflowStatusAuditDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(WORKFLOW_STATUS_AUDIT.PK, WORKFLOW_STATUS_AUDIT.WORKFLOW_ID, EVENT.SCOPE_FK)
			.from(WORKFLOW_STATUS_AUDIT)
			.innerJoin(EVENT).on(WORKFLOW_STATUS_AUDIT.EVENT_FK.eq(EVENT.PK))
			.where(WORKFLOW_STATUS_AUDIT.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK))
			.and(WORKFLOW_STATUS_AUDIT.FORM_FK.isNull())
			.and(WORKFLOW_STATUS_AUDIT.FIELD_FK.isNull());

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var auditPk = r.get(WORKFLOW_STATUS_AUDIT.PK);
				final var workflowId = r.get(WORKFLOW_STATUS_AUDIT.WORKFLOW_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				if(!dryRun) {
					create.update(WORKFLOW_STATUS_AUDIT)
						.set(WORKFLOW_STATUS_AUDIT.SCOPE_FK, expectedScopeFk)
						.where(WORKFLOW_STATUS_AUDIT.PK.eq(auditPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.WORKFLOW_STATUS, workflowId, auditPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkFormWorkflowStatusAuditDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(WORKFLOW_STATUS_AUDIT.PK, WORKFLOW_STATUS_AUDIT.WORKFLOW_ID, EVENT.SCOPE_FK, FORM.EVENT_FK)
			.from(WORKFLOW_STATUS_AUDIT)
			.innerJoin(FORM).on(WORKFLOW_STATUS_AUDIT.FORM_FK.eq(FORM.PK))
			.innerJoin(EVENT).on(FORM.EVENT_FK.eq(EVENT.PK))
			.where(
				WORKFLOW_STATUS_AUDIT.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK)
					.or(WORKFLOW_STATUS_AUDIT.EVENT_FK.isDistinctFrom(FORM.EVENT_FK))
			)
			.and(WORKFLOW_STATUS_AUDIT.FIELD_FK.isNull());

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var auditPk = r.get(WORKFLOW_STATUS_AUDIT.PK);
				final var workflowId = r.get(WORKFLOW_STATUS_AUDIT.WORKFLOW_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				final var expectedEventFk = r.get(FORM.EVENT_FK);
				if(!dryRun) {
					create.update(WORKFLOW_STATUS_AUDIT)
						.set(WORKFLOW_STATUS_AUDIT.SCOPE_FK, expectedScopeFk)
						.set(WORKFLOW_STATUS_AUDIT.EVENT_FK, expectedEventFk)
						.where(WORKFLOW_STATUS_AUDIT.PK.eq(auditPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.WORKFLOW_STATUS, workflowId, auditPk, status));
			}
		}
		return issues;
	}

	private List<DenormalizationInconsistency> checkFieldWorkflowStatusAuditDenormalization(final boolean dryRun) {
		final var issues = new ArrayList<DenormalizationInconsistency>();
		final var status = dryRun ? InconsistencyStatus.FIXABLE : InconsistencyStatus.FIXED;
		final var query = create
			.select(WORKFLOW_STATUS_AUDIT.PK, WORKFLOW_STATUS_AUDIT.WORKFLOW_ID, EVENT.SCOPE_FK, DATASET.EVENT_FK)
			.from(WORKFLOW_STATUS_AUDIT)
			.innerJoin(FIELD).on(WORKFLOW_STATUS_AUDIT.FIELD_FK.eq(FIELD.PK))
			.innerJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.innerJoin(EVENT).on(DATASET.EVENT_FK.eq(EVENT.PK))
			.where(
				WORKFLOW_STATUS_AUDIT.SCOPE_FK.isDistinctFrom(EVENT.SCOPE_FK)
					.or(WORKFLOW_STATUS_AUDIT.EVENT_FK.isDistinctFrom(DATASET.EVENT_FK))
			);

		try(var cursor = query.fetchLazy()) {
			while(cursor.hasNext()) {
				final var r = cursor.fetchNext();
				final var auditPk = r.get(WORKFLOW_STATUS_AUDIT.PK);
				final var workflowId = r.get(WORKFLOW_STATUS_AUDIT.WORKFLOW_ID);
				final var expectedScopeFk = r.get(EVENT.SCOPE_FK);
				final var expectedEventFk = r.get(DATASET.EVENT_FK);
				if(!dryRun) {
					create.update(WORKFLOW_STATUS_AUDIT)
						.set(WORKFLOW_STATUS_AUDIT.SCOPE_FK, expectedScopeFk)
						.set(WORKFLOW_STATUS_AUDIT.EVENT_FK, expectedEventFk)
						.where(WORKFLOW_STATUS_AUDIT.PK.eq(auditPk))
						.execute();
				}
				issues.add(new DenormalizationInconsistency(InconsistentEntity.WORKFLOW_STATUS, workflowId, auditPk, status));
			}
		}
		return issues;
	}

}
