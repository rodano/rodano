package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MenuConfigDTO {

	@NotNull
	private UUID menuId;
	private UUID parentMenuId;
	@NotBlank
	private String id;

	@NotNull
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private int orderBy;
	private int sortOrder;
	private boolean isPublic;
	private boolean homePage;

	private MenuActionConfigDTO action;

	private List<UUID> submenuIds;

	public UUID getMenuId() {
		return menuId;
	}

	public void setMenuId(final UUID menuId) {
		this.menuId = menuId;
	}

	public UUID getParentMenuId() {
		return parentMenuId;
	}

	public void setParentMenuId(final UUID parentMenuId) {
		this.parentMenuId = parentMenuId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public int getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(final int orderBy) {
		this.orderBy = orderBy;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(final int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(final boolean aPublic) {
		isPublic = aPublic;
	}

	public boolean isHomePage() {
		return homePage;
	}

	public void setHomePage(final boolean homePage) {
		this.homePage = homePage;
	}

	public MenuActionConfigDTO getAction() {
		return action;
	}

	public void setAction(final MenuActionConfigDTO action) {
		this.action = action;
	}

	public List<UUID> getSubmenuIds() {
		return submenuIds;
	}

	public void setSubmenuIds(final List<UUID> submenuIds) {
		this.submenuIds = submenuIds;
	}
}
