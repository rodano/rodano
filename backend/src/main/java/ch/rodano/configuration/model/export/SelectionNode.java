package ch.rodano.configuration.model.export;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class SelectionNode implements Node {

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

	public static Optional<SelectionNode> getSelection(final List<SelectionNode> selections, final Entity nodeEntity, final String nodeId) {
		return selections.stream().filter(s -> s.getNodeEntity().equals(nodeEntity) && s.getNodeId().equals(nodeId)).findAny();
	}

	public Optional<SelectionNode> getSelection(final Entity entity, final String id) {
		return getSelection(selections, entity, id);
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
