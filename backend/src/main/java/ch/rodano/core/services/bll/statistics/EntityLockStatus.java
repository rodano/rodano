package ch.rodano.core.services.bll.statistics;

import java.util.Map;
import java.util.stream.Collectors;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.scope.ScopeModel;

enum EntityLockStatus {
	SCOPES_LOCKED(
		Map.of(
			LanguageStatic.en.name(), "Locked %s",
			LanguageStatic.fr.name(), "%s verrouillés"
		)
	), SCOPES_UNLOCKED(
		Map.of(
			LanguageStatic.en.name(), "Unlocked %s",
			LanguageStatic.fr.name(), "%s déverrouillés"
		)
	), EVENTS_LOCKED(
		Map.of(
			LanguageStatic.en.name(), "Locked %s events",
			LanguageStatic.fr.name(), "Evènements verrouillés"
		)
	), EVENTS_UNLOCKED(
		Map.of(
			LanguageStatic.en.name(), "Unlocked %s events",
			LanguageStatic.fr.name(), "Evènements déverrouillés"
		)
	);

	private final Map<String, String> label;

	EntityLockStatus(final Map<String, String> label) {
		this.label = label;
	}

	public Map<String, String> getLabel(final ScopeModel scopeModel) {
		return label.entrySet()
			.stream()
			.collect(
				Collectors.toMap(
					Map.Entry::getKey,
					e -> String.format(e.getValue(), scopeModel.getLocalizedPluralShortname(e.getKey()).toLowerCase())
				)
			);
	}

}
