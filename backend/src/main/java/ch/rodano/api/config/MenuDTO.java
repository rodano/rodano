package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MenuDTO(
	@NotEmpty String id,
	@NotNull SortedMap<String, String> shortname,
	SortedMap<String, String> longname,
	SortedMap<String, String> description,

	@NotNull MenuActionDTO action,

	int orderBy,

	@NotNull boolean isPublic,
	@NotNull boolean homePage,

	@NotNull List<MenuDTO> submenus
) implements Comparable<MenuDTO> {

	@Override
	public int compareTo(final MenuDTO menu) {
		if(orderBy != menu.orderBy) {
			return orderBy - menu.orderBy;
		}
		return id.compareTo(menu.id);
	}
}
