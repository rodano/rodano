package ch.rodano.configuration.model.export;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;

public class SelectionNode implements Node {

	@Serial
	private static final long serialVersionUID = -840638351472570819L;

	private Entity nodeEntity;
	private String nodeId;
	private List<SelectionNode> selections;

	public Entity getNodeEntity() {
		return nodeEntity;
	}

	public void setNodeEntity(final Entity nodeEntity) {
		this.nodeEntity = nodeEntity;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(final String id) {
		this.nodeId = id;
	}

	public List<SelectionNode> getSelections() {
		return selections;
	}

	public void setSelections(final List<SelectionNode> selections) {
		this.selections = selections;
	}

	@JsonIgnore
	public UUID getNodeUuid() {
		if(nodeEntity == null || nodeId == null || nodeId.isBlank()) {
			return null;
		}
		return deterministic(null, nodeEntity.name(), nodeId);
	}

	public static Optional<SelectionNode> getSelection(final List<SelectionNode> selections, final Entity nodeEntity, final String nodeCode) {
		if(selections == null || nodeEntity == null || nodeCode == null) {
			return Optional.empty();
		}
		return selections.stream()
			.filter(s -> nodeEntity.equals(s.getNodeEntity()) &&
				nodeCode.equalsIgnoreCase(s.getNodeId()))
			.findAny();
	}

	public static Optional<SelectionNode> getSelection(final List<SelectionNode> selections, final Entity nodeEntity, final UUID nodeUuid) {
		if(selections == null || nodeEntity == null || nodeUuid == null) {
			return Optional.empty();
		}
		return selections.stream()
			.filter(s -> nodeEntity.equals(s.getNodeEntity()) &&
				nodeUuid.equals(s.getNodeUuid()))
			.findAny();
	}

	public Optional<SelectionNode> getSelection(final Entity entity, final String nodeCode) {
		return getSelection(selections, entity, nodeCode);
	}

	public Optional<SelectionNode> getSelection(final Entity entity, final UUID nodeUuid) {
		return getSelection(selections, entity, nodeUuid);
	}

	@Override
	public final Entity getEntity() {
		return Entity.SELECTION_NODE;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
