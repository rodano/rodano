package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Selection;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.SelectionNode.SELECTION_NODE;

public class SelectionWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Selection> wrapped = (ProjectScoped<Selection>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Selection root = wrapped.getPayload();

				persistNode(tx, projectId, null, null, null, "", root, 0);
			}
		});
	}

	private void persistNode(final DSLContext tx,
							 final UUID projectId,
							 final UUID parentSelectionId,
							 final String scopeContext,
							 final String formContext,
							 final String pathKey,
							 final Selection node,
							 final int siblingOrder) {

		final String thisKey = node.getNodeEntity() + "|" + node.getNodeId();
		final String thisPath = pathKey == null || pathKey.isBlank() ? thisKey : pathKey + ">" + thisKey;

		final UUID selectionId = deterministic(projectId, "SELECTION", thisPath);
		final UUID targetNodeId = resolveTargetUuid(projectId, node.getNodeEntity(), node.getNodeId(), scopeContext, formContext);

		tx.insertInto(SELECTION_NODE)
			.set(SELECTION_NODE.PROJECT_ID, projectId)
			.set(SELECTION_NODE.SELECTION_ID, selectionId)
			.set(SELECTION_NODE.PARENT_SELECTION_ID, parentSelectionId)
			.set(SELECTION_NODE.NODE_ENTITY, node.getNodeEntity())
			.set(SELECTION_NODE.NODE_CODE, node.getNodeId())
			.set(SELECTION_NODE.NODE_ID, targetNodeId)
			.set(SELECTION_NODE.SORT_ORDER, siblingOrder)
			.onDuplicateKeyUpdate()
			.set(SELECTION_NODE.PARENT_SELECTION_ID, parentSelectionId)
			.set(SELECTION_NODE.NODE_ENTITY, node.getNodeEntity())
			.set(SELECTION_NODE.NODE_CODE, node.getNodeId())
			.set(SELECTION_NODE.NODE_ID, targetNodeId)
			.set(SELECTION_NODE.SORT_ORDER, siblingOrder)
			.execute();

		final String nextScopeContext = "SCOPE_MODEL".equals(node.getNodeEntity()) ? node.getNodeId() : scopeContext;
		final String nextFormContext = "FORM_MODEL".equals(node.getNodeEntity()) ? node.getNodeId() : formContext;

		if(node.getSelections() != null && !node.getSelections().isEmpty()) {
			int childOrder = 0;
			for(Selection child : node.getSelections()) {
				persistNode(tx, projectId, selectionId, nextScopeContext, nextFormContext, thisPath, child, childOrder++);
			}
		}
	}

	private UUID resolveTargetUuid(final UUID projectId,
								   final String entity,
								   final String code,
								   final String scopeContext,
								   final String formContext) {

		if(entity == null || code == null || code.isBlank()) {
			return null;
		}

		switch(entity) {
			case "SCOPE_MODEL":
				return deterministic(projectId, "SCOPE_MODEL", code);
			case "EVENT_MODEL": {
				final int pipe = code.indexOf('|');
				final int colon = code.indexOf(':');
				final boolean qualified = pipe >= 0 || colon >= 0;

				final String qualifiedCode = qualified
					? code
					: scopeContext != null && !scopeContext.isBlank() ? scopeContext + "|" + code : code;

				return deterministic(projectId, "EVENT_MODEL", qualifiedCode);
			}
			case "FORM_MODEL":
				return deterministic(projectId, "FORM_MODEL", code);
			case "LAYOUT": {
				final String qualifiedCode = formContext != null && !formContext.isBlank()
					? formContext + "|" + code
					: code;

				return deterministic(projectId, "FORM_LAYOUT", qualifiedCode);
			}
			default:
				return null;
		}
	}
}
