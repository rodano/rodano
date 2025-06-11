package ch.rodano.configuration.model.feature;

import java.util.SortedMap;
import java.util.TreeMap;

import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.language.LanguageStatic;

public enum FeatureStatic implements SuperDisplayable {
	//basics
	ADMIN {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Administrator");
			shortname.put(LanguageStatic.fr.getId(), "Administrateur");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			final SortedMap<String, String> description = new TreeMap<>();
			description.put(LanguageStatic.en.getId(), "Gives global administration rights");
			description.put(LanguageStatic.fr.getId(), "Donne les droits d'administration");
			return description;
		}
	},
	//management
	MANAGE_CONFIGURATION {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Manage configuration");
			shortname.put(LanguageStatic.fr.getId(), "Gestion de la configuration");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			final SortedMap<String, String> description = new TreeMap<>();
			description.put(LanguageStatic.en.getId(), "Allows to edit the configuration");
			description.put(LanguageStatic.fr.getId(), "Permet l'edition de la configuration");
			return description;
		}
	},
	MANAGE_DELETED_DATA {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Manage deleted data");
			shortname.put(LanguageStatic.fr.getId(), "Gestion des données supprimées");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			final SortedMap<String, String> description = new TreeMap<>();
			description.put(LanguageStatic.en.getId(), "Allows to view and manage deleted data");
			description.put(LanguageStatic.fr.getId(), "Permet de voir et gérer les données supprimées");
			return description;
		}
	},
	MANAGE_MAILS {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Manage mails");
			shortname.put(LanguageStatic.fr.getId(), "Gestion des mails");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			final SortedMap<String, String> description = new TreeMap<>();
			description.put(LanguageStatic.en.getId(), "Allows to manage e-mails sent by system");
			description.put(LanguageStatic.fr.getId(), "Permet de gérer les e-mails envoyées par le système");
			return description;
		}
	},
	MANAGE_RESOURCE {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Manage resource");
			shortname.put(LanguageStatic.fr.getId(), "Gestion des ressources");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			final SortedMap<String, String> description = new TreeMap<>();
			description.put(LanguageStatic.en.getId(), "Allows to manage resources");
			description.put(LanguageStatic.fr.getId(), "Permet de gérer les ressources");
			return description;
		}
	},
	LOCK {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Lock scopes and visits");
			shortname.put(LanguageStatic.fr.getId(), "Verrouiller les scopes et les visites");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			final SortedMap<String, String> description = new TreeMap<>();
			description.put(LanguageStatic.en.getId(), "Allows to lock scopes and visits");
			description.put(LanguageStatic.fr.getId(), "Permet de verrouiller les scopes et les visites");
			return description;
		}
	},
	DOCUMENTATION {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Documentation");
			shortname.put(LanguageStatic.fr.getId(), "Documentation");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			final SortedMap<String, String> description = new TreeMap<>();
			description.put(LanguageStatic.en.getId(), "Allows to export documentation files");
			description.put(LanguageStatic.fr.getId(), "Permet d'exporter les fichiers de documentation");
			return description;
		}
	},
	EXPORT {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Export");
			shortname.put(LanguageStatic.fr.getId(), "Export");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			final SortedMap<String, String> description = new TreeMap<>();
			description.put(LanguageStatic.en.getId(), "Allows to export data");
			description.put(LanguageStatic.fr.getId(), "Permet d'exporter les données");
			return description;
		}
	},
	//notifications
	NOTIFY_RESOURCE_PUBLISHED {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Get notified when a resource is published");
			shortname.put(LanguageStatic.fr.getId(), "Etre notifié lorsque des ressources sont publiées");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			return getShortname();
		}

		@Override
		public boolean isOptional() {
			return true;
		}
	},
	//views
	VIEW_AUDIT_TRAIL {
		@Override
		public SortedMap<String, String> getShortname() {
			final SortedMap<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "View audit trails");
			shortname.put(LanguageStatic.fr.getId(), "Voir les audit trails");
			return shortname;
		}

		@Override
		public SortedMap<String, String> getDescription() {
			return getShortname();
		}
	};

	@Override
	public final String getId() {
		return name();
	}

	public boolean isOptional() {
		return false;
	}

	@Override
	public abstract SortedMap<String, String> getShortname();

	@Override
	public final SortedMap<String, String> getLongname() {
		return getShortname();
	}

	@Override
	public abstract SortedMap<String, String> getDescription();
}
