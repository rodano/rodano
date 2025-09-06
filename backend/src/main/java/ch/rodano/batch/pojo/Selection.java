package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Selection {

	private String nodeEntity;
	private String nodeId;

	private List<Selection> selections;

	public String getNodeEntity() {
		return nodeEntity;
	}

	public void setNodeEntity(final String nodeEntity) {
		this.nodeEntity = nodeEntity;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(final String nodeId) {
		this.nodeId = nodeId;
	}

	public List<Selection> getSelections() {
		return selections;
	}

	public void setSelections(final List<Selection> selections) {
		this.selections = selections;
	}

	@Override
	public String toString() {
		return "Selection{" +
			"nodeEntity='" + nodeEntity + '\'' +
			", nodeId='" + nodeId + '\'' +
			", selections=" + selections +
			'}';
	}
}
