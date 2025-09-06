package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuLayout {

	private List<MenuLayoutSection> sections;

	public List<MenuLayoutSection> getSections() {
		return sections;
	}

	public void setSections(final List<MenuLayoutSection> sections) {
		this.sections = sections;
	}

	@Override
	public String toString() {
		return "MenuLayout{" +
			"sections=" + sections +
			'}';
	}
}
