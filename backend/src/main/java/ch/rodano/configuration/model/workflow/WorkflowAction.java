package ch.rodano.configuration.model.workflow;

import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.language.LanguageStatic;

public enum WorkflowAction implements SuperDisplayable {
	CREATE_SCOPE {
		@Override
		public String toString() {
			return "Scope created";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Scope créé");
			return shortname;
		}
	},
	REMOVE_SCOPE {
		@Override
		public String toString() {
			return "Scope removed";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Scope supprimé");
			return shortname;
		}
	},
	RESTORE_SCOPE {
		@Override
		public String toString() {
			return "Scope restored";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Scope restauré");
			return shortname;
		}
	},
	CREATE_EVENT {
		@Override
		public String toString() {
			return "Visit created";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Visite créée");
			return shortname;
		}
	},
	REMOVE_EVENT {
		@Override
		public String toString() {
			return "Visit removed";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Visite supprimé");
			return shortname;
		}
	},
	RESTORE_EVENT {
		@Override
		public String toString() {
			return "Visit restored";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Visite restauré");
			return shortname;
		}
	},
	CREATE_DATASET {
		@Override
		public String toString() {
			return "Dataset created";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Dataset créé");
			return shortname;
		}
	},
	REMOVE_DATASET {
		@Override
		public String toString() {
			return "Dataset removed";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Dataset supprimé");
			return shortname;
		}
	},
	RESTORE_DATASET {
		@Override
		public String toString() {
			return "Dataset restored";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Dataset restauré");
			return shortname;
		}
	},
	CREATE_WORKFLOW_STATUS {
		@Override
		public String toString() {
			return "Workflow status created";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Statut de workflow créé");
			return shortname;
		}
	},
	UPDATE_VALUE {
		@Override
		public String toString() {
			return "Field updated";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Champ mis à jour");
			return shortname;
		}
	},
	SAVE_FORM {
		@Override
		public String toString() {
			return "Form saved";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Formulaire sauvé");
			return shortname;
		}
	},
	ROLE_CREATE {
		@Override
		public String toString() {
			return "Role created";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Rôle créé");
			return shortname;
		}
	},
	ROLE_ENABLE {
		@Override
		public String toString() {
			return "Role enabled";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Rôle activé");
			return shortname;
		}
	},
	ROLE_DISABLE {
		@Override
		public String toString() {
			return "Role disabled";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Rôle désactivé");
			return shortname;
		}
	},
	USER_LOGIN {
		@Override
		public String toString() {
			return "User logged in";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), toString());
			shortname.put(LanguageStatic.fr.getId(), "Utilisation connecté");
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
