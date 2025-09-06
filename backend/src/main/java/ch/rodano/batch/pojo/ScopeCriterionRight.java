package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScopeCriterionRight {

	public String rightEntity;
	public String right;
	public String id;

	public String getRightEntity() {
		return rightEntity;
	}

	public void setRightEntity(final String rightEntity) {
		this.rightEntity = rightEntity;
	}

	public String getRight() {
		return right;
	}

	public void setRight(final String right) {
		this.right = right;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ScopeCriterionRight{" +
			"rightEntity='" + rightEntity + '\'' +
			", right='" + right + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
