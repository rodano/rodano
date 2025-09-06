package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Menu {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private Integer orderBy;
	@JsonProperty("public")
	private Boolean isPublic;
	private Boolean homePage;

	private MenuAction action;
	private MenuLayout layout;
	private List<Menu> submenus;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Integer getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(final Integer orderBy) {
		this.orderBy = orderBy;
	}

	public Boolean getPublic() {
		return isPublic;
	}

	public void setPublic(final Boolean aPublic) {
		isPublic = aPublic;
	}

	public Boolean getHomePage() {
		return homePage;
	}

	public void setHomePage(final Boolean homePage) {
		this.homePage = homePage;
	}

	public MenuAction getAction() {
		return action;
	}

	public void setAction(final MenuAction action) {
		this.action = action;
	}

	public MenuLayout getLayout() {
		return layout;
	}

	public void setLayout(final MenuLayout layout) {
		this.layout = layout;
	}

	public List<Menu> getSubmenus() {
		return submenus;
	}

	public void setSubmenus(final List<Menu> submenus) {
		this.submenus = submenus;
	}

	@Override
	public String toString() {
		return "Menu{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", orderBy=" + orderBy +
			", isPublic=" + isPublic +
			", homePage=" + homePage +
			", action=" + action +
			", layout=" + layout +
			", submenus=" + submenus +
			'}';
	}
}
