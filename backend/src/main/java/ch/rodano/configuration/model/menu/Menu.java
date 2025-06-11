package ch.rodano.configuration.model.menu;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.cms.CMSAction;
import ch.rodano.configuration.model.cms.CMSLayout;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.study.Study;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Menu implements SuperDisplayable, Serializable, Assignable<Menu>, Node, Comparable<Menu> {
	private static final long serialVersionUID = 4081701801864249665L;

	private Study study;
	private Menu parent;

	private String id;
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private int orderBy;

	private boolean isPublic;
	private boolean homePage;

	private CMSLayout layout;
	private CMSAction action;

	private SortedSet<Menu> submenus;

	public Menu() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		submenus = new TreeSet<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference("study")
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference("study")
	public final Study getStudy() {
		return study;
	}

	@JsonBackReference("parent")
	public final void setParent(final Menu parent) {
		this.parent = parent;
	}

	@JsonBackReference("parent")
	public final Menu getParent() {
		return parent;
	}

	public final boolean isHomePage() {
		return homePage;
	}

	public final void setHomePage(final boolean homePage) {
		this.homePage = homePage;
	}

	public final boolean isPublic() {
		return isPublic;
	}

	public final void setPublic(final boolean value) {
		isPublic = value;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public final int getOrderBy() {
		return orderBy;
	}

	public final void setOrderBy(final int orderBy) {
		this.orderBy = orderBy;
	}

	@JsonManagedReference("parent")
	public final SortedSet<Menu> getSubmenus() {
		return submenus;
	}

	@JsonManagedReference("parent")
	public final void setSubmenus(final SortedSet<Menu> submenus) {
		this.submenus = submenus;
	}

	@JsonIgnore
	public Set<Menu> getPublicSubmenus() {
		return getSubmenus().stream().filter(Menu::isPublic).collect(Collectors.toCollection(TreeSet::new));
	}

	@JsonIgnore
	public Menu getSubmenu(final String submenuId) {
		return getSubmenus().stream()
			.filter(m -> m.getId().equalsIgnoreCase(submenuId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.MENU, submenuId));
	}

	@JsonIgnore
	public final boolean hasParent() {
		return getParent() != null;
	}

	@Override
	@JsonIgnore
	public final String getAssignableDescription() {
		if(getParent() != null) {
			return " + " + getLocalizedShortname(getStudy().getDefaultLanguage().getId());
		}
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	@Override
	public final Entity getEntity() {
		return Entity.MENU;
	}

	@Override
	@JsonIgnore
	public final int compareTo(final Menu menu) {
		if(getOrderBy() != menu.getOrderBy()) {
			return getOrderBy() - menu.getOrderBy();
		}
		return getId().compareTo(menu.getId());
	}

	@JsonIgnore
	public final boolean isSameFamily(final Menu menu) {
		if(hasParent()) {
			return getParent().equals(menu) || getParent().equals(menu.getParent());
		}
		return menu.hasParent() && menu.isSameFamily(this);
	}

	public final CMSLayout getLayout() {
		return layout;
	}

	public final void setLayout(final CMSLayout layout) {
		this.layout = layout;
	}

	public final CMSAction getAction() {
		return action;
	}

	public final void setAction(final CMSAction action) {
		this.action = action;
	}

	@Override
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case MENU:
				return Collections.unmodifiableSet(submenus);
			default:
				return Collections.emptyList();
		}
	}
}
