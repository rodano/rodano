package ch.rodano.configuration.model.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.utils.ExportableUtils;

public class Selection {

	public static org.w3c.dom.Document getExportForXml(final Study study, final String... languages) {
		final var studySelections = study.getSelections();
		final var doc = DocumentHelper.createDocument();

		final var studyElement = ExportableUtils.getExportForXml(doc, study, languages);
		doc.appendChild(studyElement);

		//add scope models
		final var smsElement = doc.createElement("scopeModels");
		studyElement.appendChild(smsElement);

		for(final var scopeModel : study.getScopeModels()) {
			final var scopeModelSelection = SelectionNode.getSelection(studySelections, Entity.SCOPE_MODEL, scopeModel.getId());
			if(scopeModelSelection.isEmpty()) {
				continue;
			}

			final var smElement = ExportableUtils.getExportForXml(doc, scopeModel, languages);
			smsElement.appendChild(smElement);

			//add event models
			final var emsElement = doc.createElement("eventModels");
			smElement.appendChild(emsElement);

			final List<EventModel> eventModels = new ArrayList<>(scopeModel.getEventModels());
			Collections.sort(eventModels);

			for(final var eventModel : eventModels) {
				final var eventModelSelection = scopeModelSelection.get().getSelection(Entity.EVENT_MODEL, eventModel.getId());
				if(eventModelSelection.isEmpty()) {
					continue;
				}

				final var emElement = ExportableUtils.getExportForXml(doc, eventModel, languages);
				emsElement.appendChild(emElement);

				//add form models
				final var fmsElement = doc.createElement("formModels");
				emElement.appendChild(fmsElement);

				for(final var formModel : eventModel.getFormModels()) {
					final var formModelSelection = eventModelSelection.get().getSelection(Entity.FORM_MODEL, formModel.getId());
					if(formModelSelection.isEmpty()) {
						continue;
					}

					final var fmElement = ExportableUtils.getExportForXml(doc, formModel, languages);
					fmsElement.appendChild(fmElement);

					//add layouts
					final var layoutsElement = doc.createElement("layouts");
					fmElement.appendChild(layoutsElement);

					for(final var layout : formModel.getLayouts()) {
						final var layoutSelection = formModelSelection.get().getSelection(Entity.LAYOUT, layout.getId());
						if(layoutSelection.isEmpty()) {
							continue;
						}

						for(final var fieldModel : layout.getFieldModels()) {
							final var fieldModelElement = ExportableUtils.getExportForXml(doc, fieldModel, languages);
							fmElement.appendChild(fieldModelElement);
						}

						final var layoutElement = ExportableUtils.getExportForXml(doc, layout, languages);
						layoutsElement.appendChild(layoutElement);
					}
				}
			}

			//retrieve and sort form models
			final var formModels = new ArrayList<FormModel>();
			scopeModel.getEventModels().forEach(event -> {
				final var eventSelection = scopeModelSelection.get().getSelection(Entity.EVENT_MODEL, event.getId());
				if(eventSelection.isPresent()) {

					event.getFormModels().forEach(formModel -> {
						final var formModelSelection = eventSelection.get().getSelection(Entity.FORM_MODEL, formModel.getId());
						if(formModelSelection.isPresent() && !formModels.contains(formModel)) {
							formModels.add(formModel);
						}
					});
				}
			});

			//add form models
			formModels.forEach(formModel -> {
				final var formModelElement = doc.createElement("page_matrix");
				formModelElement.setAttribute("id", formModel.getId());
				formModelElement.setAttribute("shortname", formModel.getLocalizedShortname(languages));
				formModelElement.setAttribute("longname", StringUtils.defaultIfBlank(formModel.getLocalizedLongname(languages), formModel.getLocalizedShortname(languages)));
				smElement.appendChild(formModelElement);
			});
		}

		doc.getDocumentElement().normalize();
		return doc;
	}
}
