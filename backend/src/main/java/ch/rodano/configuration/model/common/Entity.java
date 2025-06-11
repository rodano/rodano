package ch.rodano.configuration.model.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.workflow.WorkflowableModel;

public enum Entity implements SuperDisplayable {
	STUDY {
		@Override
		public boolean isRoot() {
			return true;
		}

		@Override
		public List<Entity> getChildrenEmbedded() {
			return Arrays.asList(SCOPE_MODEL, EVENT_MODEL, FORM_MODEL, DATASET_MODEL, VALIDATOR, PROFILE, FEATURE, PAYMENT_PLAN, WORKFLOW, ACTION, WORKFLOW_WIDGET,
				RESOURCE_CATEGORY, PRIVACY_POLICY, REPORT, MENU, LANGUAGE);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Study");
			shortname.put(LanguageStatic.fr.getId(), "Etude");
			return shortname;
		}
	},
	LANGUAGE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Language");
			shortname.put(LanguageStatic.fr.getId(), "Langage");
			return shortname;
		}
	},
	SCOPE_MODEL {
		@Override
		public List<Entity> getChildren() {
			return Collections.singletonList(EVENT_GROUP);
		}

		@Override
		public boolean isWorkflowableModel() {
			return true;
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Scope model");
			shortname.put(LanguageStatic.fr.getId(), "Modèle de scope");
			return shortname;
		}
	},
	EVENT_GROUP {
		@Override
		public List<Entity> getChildren() {
			return Collections.singletonList(EVENT_MODEL);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Event group");
			shortname.put(LanguageStatic.fr.getId(), "Groupe d'évènements");
			return shortname;
		}
	},
	EVENT_MODEL {
		@Override
		public List<Entity> getChildren() {
			return Arrays.asList(DATASET_MODEL, FORM_MODEL, WORKFLOW);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Event model");
			shortname.put(LanguageStatic.fr.getId(), "Modèle d'évènement");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}

		@Override
		public boolean isWorkflowableModel() {
			return true;
		}
	},
	DATASET_MODEL {
		@Override
		public List<Entity> getChildrenEmbedded() {
			return Collections.singletonList(FIELD_MODEL);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Dataset model");
			shortname.put(LanguageStatic.fr.getId(), "Modèle de dataset");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}
	},
	FIELD_MODEL {
		@Override
		public List<Entity> getChildren() {
			return Collections.singletonList(VALIDATOR);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Field model");
			shortname.put(LanguageStatic.fr.getId(), "Modèle de champ");
			return shortname;
		}

		@Override
		public boolean isWorkflowableModel() {
			return true;
		}
	},
	POSSIBLE_VALUE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Possible value");
			shortname.put(LanguageStatic.fr.getId(), "Valeur possible");
			return shortname;
		}
	},
	VALIDATOR {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Validator");
			shortname.put(LanguageStatic.fr.getId(), "Validateur");
			return shortname;
		}
	},
	FORM_MODEL {
		@Override
		public List<Entity> getChildrenEmbedded() {
			return Collections.singletonList(LAYOUT);
		}

		@Override
		public List<Entity> getChildren() {
			return Collections.singletonList(WORKFLOW);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Form model");
			shortname.put(LanguageStatic.fr.getId(), "Modèle de formulaire");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}

		@Override
		public boolean isWorkflowableModel() {
			return true;
		}
	},
	LAYOUT {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Layout");
			shortname.put(LanguageStatic.fr.getId(), "Layout");
			return shortname;
		}
	},
	LINE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Line");
			shortname.put(LanguageStatic.fr.getId(), "Ligne");
			return shortname;
		}
	},
	COLUM {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Column");
			shortname.put(LanguageStatic.fr.getId(), "Colonnes");
			return shortname;
		}
	},
	CELL {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Cell");
			shortname.put(LanguageStatic.fr.getId(), "Cellule");
			return shortname;
		}
	},
	VISIBILITY_CRITERIA {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Visibility criteria");
			shortname.put(LanguageStatic.fr.getId(), "Critère de visibilité");
			return shortname;
		}
	},
	PROFILE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Profile");
			shortname.put(LanguageStatic.fr.getId(), "Profil");
			return shortname;
		}
	},
	RIGHT {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Right");
			shortname.put(LanguageStatic.fr.getId(), "Droit");
			return shortname;
		}
	},
	PROFILE_RIGHT {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Profile right");
			shortname.put(LanguageStatic.fr.getId(), "Droit de profile");
			return shortname;
		}
	},
	FEATURE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Feature");
			shortname.put(LanguageStatic.fr.getId(), "Droit");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}
	},
	WORKFLOW {
		@Override
		public List<Entity> getChildrenEmbedded() {
			return Collections.singletonList(WORKFLOW_STATE);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Workflow");
			shortname.put(LanguageStatic.fr.getId(), "Workflow");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}
	},
	WORKFLOW_STATE {
		@Override
		public List<Entity> getChildren() {
			return Collections.singletonList(ACTION);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "State");
			shortname.put(LanguageStatic.fr.getId(), "Etat");
			return shortname;
		}
	},
	WORKFLOW_STATE_SELECTOR {
		@Override
		public List<Entity> getChildren() {
			return Collections.singletonList(ACTION);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Workflow state selector");
			shortname.put(LanguageStatic.fr.getId(), "Sélecteur d'état de workflow");
			return shortname;
		}
	},
	ACTION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Action");
			shortname.put(LanguageStatic.fr.getId(), "Action");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}
	},
	RULE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Rule");
			shortname.put(LanguageStatic.fr.getId(), "Règle");
			return shortname;
		}
	},
	RULE_CONSTRAINT {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Rule constraint");
			shortname.put(LanguageStatic.fr.getId(), "Containte de règle");
			return shortname;
		}
	},
	RULE_CONDITION_LIST {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "List of conditions");
			shortname.put(LanguageStatic.fr.getId(), "List de conditions");
			return shortname;
		}
	},
	RULE_CONDITION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Condition");
			shortname.put(LanguageStatic.fr.getId(), "Condition");
			return shortname;
		}
	},
	RULE_CONDITION_CRITERION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Condition criterion");
			shortname.put(LanguageStatic.fr.getId(), "Critère de condition");
			return shortname;
		}
	},
	RULE_ACTION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Rule action");
			shortname.put(LanguageStatic.fr.getId(), "Action de règle");
			return shortname;
		}
	},
	RULE_ACTION_PARAMETER {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Rule action parameter");
			shortname.put(LanguageStatic.fr.getId(), "Paramètre d'action de règle");
			return shortname;
		}
	},
	WORKFLOW_WIDGET {
		@Override
		public List<Entity> getChildrenEmbedded() {
			return Collections.singletonList(WORKFLOW_WIDGET_COLUMN);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Workflow widget");
			shortname.put(LanguageStatic.fr.getId(), "Widget de workflow");
			return shortname;
		}
	},
	WORKFLOW_WIDGET_COLUMN {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Workflow widget column");
			shortname.put(LanguageStatic.fr.getId(), "Colonne de widget de workflow");
			return shortname;
		}
	},
	WORKFLOW_SUMMARY {
		@Override
		public List<Entity> getChildrenEmbedded() {
			return Collections.singletonList(WORKFLOW_SUMMARY_COLUMN);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Workflow summary");
			shortname.put(LanguageStatic.fr.getId(), "Rapport de workflow");
			return shortname;
		}
	},
	WORKFLOW_SUMMARY_COLUMN {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Workflow summary column");
			shortname.put(LanguageStatic.fr.getId(), "Colonne de rapport de widget");
			return shortname;
		}
	},
	RESOURCE_CATEGORY {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Category");
			shortname.put(LanguageStatic.fr.getId(), "Categorie");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}
	},
	PRIVACY_POLICY {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Privacy policy");
			shortname.put(LanguageStatic.fr.getId(), "Politique d'utilisation");
			return shortname;
		}
	},
	REPORT {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Report");
			shortname.put(LanguageStatic.fr.getId(), "Rapport");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}
	},
	MENU {
		@Override
		public List<Entity> getChildrenEmbedded() {
			return Collections.singletonList(MENU);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Menu");
			shortname.put(LanguageStatic.fr.getId(), "Menu");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}
	},
	CMS_LAYOUT {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "CMS layout");
			shortname.put(LanguageStatic.fr.getId(), "Layout du CMS");
			return shortname;
		}

		@Override
		public List<Entity> getChildrenEmbedded() {
			return Arrays.asList(CMS_SECTION);
		}
	},
	CMS_SECTION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "CMS section");
			shortname.put(LanguageStatic.fr.getId(), "Section du CMS");
			return shortname;
		}

		@Override
		public List<Entity> getChildrenEmbedded() {
			return Arrays.asList(CMS_WIDGET);
		}
	},
	CMS_WIDGET {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "CMS widget");
			shortname.put(LanguageStatic.fr.getId(), "Widget du CMS");
			return shortname;
		}
	},
	CMS_ACTION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "CMS action");
			shortname.put(LanguageStatic.fr.getId(), "Action du CMS");
			return shortname;
		}
	},
	TIMELINE_GRAPH {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Timeline graph");
			shortname.put(LanguageStatic.fr.getId(), "Graphique de ligne de vie");
			return shortname;
		}

		@Override
		public boolean isAssignableToProfile() {
			return true;
		}
	},
	TIMELINE_GRAPH_SECTION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Timeline graph section");
			shortname.put(LanguageStatic.fr.getId(), "Section de graphique de ligne de vie");
			return shortname;
		}
	},
	TIMELINE_GRAPH_SECTION_POSITION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Timeline graph section position");
			shortname.put(LanguageStatic.fr.getId(), "Position de section de graphique de ligne de vie");
			return shortname;
		}
	},
	TIMELINE_GRAPH_SECTION_REFERENCE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Timeline graph section reference");
			shortname.put(LanguageStatic.fr.getId(), "Référence de section de graphique de ligne de vie");
			return shortname;
		}
	},
	TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Timeline graph section reference entry");
			shortname.put(LanguageStatic.fr.getId(), "Entrée de référence de section de graphique de ligne de vie");
			return shortname;
		}
	},
	TIMELINE_GRAPH_SECTION_SCALE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Timeline graph section scale");
			shortname.put(LanguageStatic.fr.getId(), "Echelle de section de graphique de ligne de vie");
			return shortname;
		}
	},
	CHART {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Chart");
			shortname.put(LanguageStatic.fr.getId(), "Graphique");
			return shortname;
		}
	},
	CHART_RANGE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Chart range");
			shortname.put(LanguageStatic.fr.getId(), "Plage de graphique");
			return shortname;
		}
	},
	CHART_REQUEST {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Chart SQL request");
			shortname.put(LanguageStatic.fr.getId(), "Requête SQL de graphique");
			return shortname;
		}
	},
	PAYMENT_PLAN {
		@Override
		public List<Entity> getChildrenEmbedded() {
			return Collections.singletonList(PAYMENT_STEP);
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Payment plan");
			shortname.put(LanguageStatic.fr.getId(), "Plan de paiement");
			return shortname;
		}
	},
	PAYMENT_STEP {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Payment step");
			shortname.put(LanguageStatic.fr.getId(), "Etape de paiement");
			return shortname;
		}
	},
	PAYMENT_DISTRIBUTION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Payment distribution");
			shortname.put(LanguageStatic.fr.getId(), "Distribution d'un paiement");
			return shortname;
		}
	},
	CRON {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Cron");
			shortname.put(LanguageStatic.fr.getId(), "Cron");
			return shortname;
		}
	},
	SCOPE_CRITERION {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Scope criterion");
			shortname.put(LanguageStatic.fr.getId(), "Critère de scope");
			return shortname;
		}
	},
	SCOPE_CRITERION_RIGHT {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Scope criterion right");
			shortname.put(LanguageStatic.fr.getId(), "Droit sur un critère de scope");
			return shortname;
		}
	},
	SCOPE_SELECTOR {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Scope selector");
			shortname.put(LanguageStatic.fr.getId(), "Sélecteur de scope");
			return shortname;
		}
	},
	SELECTION_NODE {
		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Selection node");
			shortname.put(LanguageStatic.fr.getId(), "Noeud selection");
			return shortname;
		}
	};

	public boolean isRoot() {
		return false;
	}

	public boolean isAssignableToProfile() {
		return false;
	}

	public boolean isWorkflowableModel() {
		return false;
	}

	public List<Entity> getParents() {
		return Arrays.stream(values()).filter(entity -> entity.getChildren().contains(this)).toList();
	}

	public List<Entity> getChildren() {
		return Collections.emptyList();
	}

	public List<Entity> getChildrenEmbedded() {
		return Collections.emptyList();
	}

	@Override
	public abstract Map<String, String> getShortname();

	@Override
	public Map<String, String> getLongname() {
		return getShortname();
	}

	@Override
	public Map<String, String> getDescription() {
		return getShortname();
	}

	public boolean hasTypes() {
		return false;
	}

	public Enum<?>[] getTypes() {
		return new Enum[]{};
	}

	public static List<WorkflowableModel> filterWorklowableModelsOnEntity(final List<WorkflowableModel> workflowableModels, final Entity entity) {
		return workflowableModels.stream().filter(wm -> wm.getEntity().equals(entity)).toList();
	}

	@Override
	public final String getId() {
		return name();
	}
}
