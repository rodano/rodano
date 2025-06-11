package ch.rodano.configuration.model.language;

import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.common.SuperDisplayable;


public enum LanguageStatic implements SuperDisplayable {
	en {
		@Override
		public final String toString() {
			return "en";
		}

		@Override
		public final Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(en.getId(), "English");
			shortname.put(fr.getId(), "Anglais");
			return shortname;
		}
	},
	fr {
		@Override
		public final String toString() {
			return "fr";
		}

		@Override
		public final Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(en.getId(), "French");
			shortname.put(fr.getId(), "Français");
			return shortname;
		}
	};

	@Override
	public final String getId() {
		return name();
	}

	@Override
	public abstract Map<String, String> getShortname();

	@Override
	public final Map<String, String> getLongname() {
		return getShortname();
	}

	@Override
	public final Map<String, String> getDescription() {
		return getShortname();
	}
}
