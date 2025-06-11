import {UUID} from './basic-tools/uuid.js';
import {UI} from './tools/ui.js';
import {Effects} from './tools/effects.js';
import {Config} from './model_config.js';
import {bus} from './model/config/entities_hooks.js';
import {Languages} from './languages.js';
import {bus_ui} from './bus_ui.js';
import {Router} from './router.js';
import {NodeTools} from './node_tools.js';
import {FormHelpers} from './form_helpers.js';
import {CMSWidget} from './model/config/entities/cms_widget.js';
import {CMSSection} from './model/config/entities/cms_section.js';
import {MediaTypes} from './media_types.js';

let selected_layout;
let selected_section;

function update_widget_data() {
	document.querySelectorAll('#layout_widgets > div').forEach(function(widget_div) {
		const widget_global_id = widget_div.widget.getGlobalId();
		widget_div.dataset.nodeGlobalId = widget_global_id;
		widget_div.querySelector('h3 > a[title="Edit widget"]').setAttribute('href', `#node=${widget_global_id}`);
	});
}

function select_section(section) {
	if(selected_section !== section) {
		selected_section = section;
		//update sections ui
		const section_list = document.getElementById('layout_sections');
		section_list.childNodes.forEach(function(element) {
			element.classList.remove('selected');
			if(element.section === selected_section) {
				element.classList.add('selected');
			}
		});
		//update widgets ui
		const layout_widgets = document.getElementById('layout_widgets');
		layout_widgets.empty();
		selected_section.widgets.map(draw_widget).forEach(Node.prototype.appendChild, layout_widgets);
	}
}

function update_widget_from_container(widget, container) {
	//retrieve widget type
	const widget_type = Config.Enums.WidgetTypes[widget.type];

	if(widget_type.parameters) {
		for(let i = 0; i < widget_type.parameters.length; i++) {
			const parameter = widget_type.parameters[i];
			const parameter_container = container.children[i + 1];
			if(parameter.type === 'LIST') {
				//retrieve list of checkboxes
				const values = parameter_container.lastElementChild.querySelectorAll('input[type="checkbox"]');
				//transform list of checkboxes into values
				widget.parameters[parameter.id] = values.filter(v => v.checked).map(v => v.name);
			}
			else {
				//retrieve field
				const field = parameter_container.querySelector(`[name="${parameter.id}"]`);
				if(parameter.type === 'BOOLEAN') {
					widget.parameters[parameter.id] = field.checked;
				}
				else if(parameter.type === 'NUMBER') {
					widget.parameters[parameter.id] = parseInt(field.value);
				}
				else {
					widget.parameters[parameter.id] = field.value;
				}
			}
		}
	}
}

function draw_section(section) {
	const section_li = document.createFullElement('li', {title: section.id});
	section_li.section = section;
	section_li.node = section;
	section_li.addEventListener(
		'click',
		function() {
			select_section(this.section);
		}
	);
	NodeTools.MakeDroppable(section_li, section);
	section_li.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort', title: 'Sort section'}));
	section_li.appendChild(document.createFullElement('span', {}, section.getLocalizedLabel(Languages.GetLanguage())));

	const edit_section_button = document.createFullElement('a', {href: `#node=${section.getGlobalId()}`, title: 'Edit section'});
	edit_section_button.appendChild(document.createFullElement('img', {src: 'images/magnifier.png', alt: 'Edit'}));
	section_li.appendChild(edit_section_button);

	const delete_section_button = document.createFullElement('button', {type: 'button', title: 'Delete section', 'class': 'image'});
	delete_section_button.appendChild(document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete'}));
	delete_section_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			if(section.layout.sections.length > 1) {
				UI.Validate('Are you sure you want to delete this section?').then(confirmed => {
					if(confirmed) {
						section.layout.sections.removeElement(section);
						document.getElementById('layout_sections').removeChild(this.parentNode);
						select_section(section.layout.sections[0]);
					}
				});
			}
			else {
				UI.Notify('Unable to delete last section', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
			}
		}
	);
	section_li.appendChild(delete_section_button);

	return section_li;
}

function draw_widget(widget) {
	const widget_div = document.createFullElement('div');
	widget_div.widget = widget;
	widget_div.node = widget;
	NodeTools.MakeDraggable(widget_div, widget);

	const widget_type = Config.Enums.WidgetTypes[widget.type];

	//title
	const widget_title = document.createFullElement('h3');
	widget_div.appendChild(widget_title);

	widget_title.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort', title: 'Sort widget'}));
	widget_title.appendChild(document.createFullElement('span', {}, widget_type.label));

	const edit_widget_button = document.createFullElement('a', {href: `#node=${widget.getGlobalId()}`, title: 'Edit widget'});
	edit_widget_button.appendChild(document.createFullElement('img', {src: 'images/magnifier.png', alt: 'Edit'}));
	widget_title.appendChild(edit_widget_button);

	const delete_widget_button = document.createFullElement('button', {type: 'button', title: 'Delete widget', 'class': 'image'});
	delete_widget_button.appendChild(document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete'}));
	delete_widget_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			UI.Validate('Are you sure you want to delete this widget?').then(confirmed => {
				if(confirmed) {
					widget.delete();
				}
			});
		}
	);
	widget_title.appendChild(delete_widget_button);

	//parameters
	if(widget_type.parameters) {
		widget_type.parameters.forEach(function(parameter) {
			const paragraph = document.createFullElement('p');
			if(parameter.type === 'LIST') {
				paragraph.setAttribute('class', 'inline');
				paragraph.appendChild(document.createFullElement('span', {style: 'float: left; width: 130px;'}, parameter.label));
				const values_container = document.createFullElement('span', {style: 'float: left;'});
				paragraph.appendChild(values_container);
				if(parameter.values) {
					parameter.values.forEach(function(value) {
						const field_id = UUID.Generate();
						const input_properties = {id: field_id, type: 'checkbox', name: value, style: 'margin-right: 0.5rem;'};
						if(widget.parameters[parameter.id]?.includes(value)) {
							input_properties.checked = 'checked';
						}
						values_container.appendChild(document.createFullElement('input', input_properties));
						values_container.appendChild(document.createFullElement('label', {'for': field_id}, value));
						values_container.appendChild(document.createFullElement('br'));
					});
				}
				else if(parameter.entity) {
					const entities = widget.section.layout.layoutable.study.getChildren(parameter.getConfigurationEntity());
					entities.forEach(function(entity) {
						const field_id = UUID.Generate();
						const input_properties = {id: field_id, type: 'checkbox', name: entity.id, style: 'margin-right: 0.5rem;'};
						if(widget.parameters[parameter.id]?.includes(entity.id)) {
							input_properties.checked = 'checked';
						}
						values_container.appendChild(document.createFullElement('input', input_properties));
						values_container.appendChild(document.createFullElement('label', {'for': field_id}, entity.getLocalizedLabel(Languages.GetLanguage())));
						values_container.appendChild(document.createFullElement('br'));
					});
				}
			}
			else {
				const field_id = UUID.Generate();
				if(parameter.entity) {
					const entities = [];
					if(parameter.dependency) {
						//TODO improve this by displaying only nodes under dependency
						entities.push(widget_type.getParameter(parameter.dependency).getConfigurationEntity());
					}
					entities.push(parameter.getConfigurationEntity());
					//retrieve nodes
					let nodes = [widget.section.layout.layoutable.getStudy()];
					entities.forEach(function(entity) {
						nodes = Array.prototype.concat.apply([], nodes.map(n => n.getChildren(entity)));
					});
					paragraph.appendChild(document.createFullElement('label', {'for': field_id, style: 'margin-right: 0.5rem;'}, parameter.label));
					const select = document.createFullElement('select', {id: field_id, name: parameter.id});
					FormHelpers.FillSelect(select, nodes, true, widget.parameters[parameter.id], {label_property: 'getLocalizedLabel'});
					paragraph.appendChild(select);
				}
				else {
					paragraph.appendChild(document.createFullElement('label', {'for': field_id, style: 'margin-right: 0.5rem;'}, parameter.label));
					const field_properties = {id: field_id, name: parameter.id};
					//select
					if(parameter.options) {
						const select = document.createFullElement('select', field_properties);
						select.fill(parameter.options, false, widget.parameters[parameter.id]);
						paragraph.appendChild(select);
					}
					//input
					else {
						if(parameter.type === 'NUMBER') {
							field_properties.type = 'number';
							field_properties.value = widget.parameters[parameter.id] || '';
						}
						else if(parameter.type === 'BOOLEAN') {
							field_properties.type = 'checkbox';
							if(widget.parameters[parameter.id]) {
								field_properties.checked = 'checked';
							}
						}
						else {
							field_properties.value = widget.parameters[parameter.id] || '';
						}
						paragraph.appendChild(document.createFullElement('input', field_properties));
					}
				}
			}
			widget_div.appendChild(paragraph);
		});
	}

	return widget_div;
}

export const Layout = {
	Init: function() {
		const layout_widgets = document.getElementById('layout_widgets');
		const layout_sections = document.getElementById('layout_sections');

		//prepare widgets types
		function dragover(event) {
			if(event.dataTransfer.types.includes(MediaTypes.WIDGET_TYPE_ID)) {
				event.preventDefault();
				this.classList.add('dragover');
			}
		}

		function dragleave() {
			this.classList.remove('dragover');
		}

		function drop(event) {
			if(event.dataTransfer.types.includes(MediaTypes.WIDGET_TYPE_ID)) {
				event.preventDefault();
				this.classList.remove('dragover');
				const widget = new CMSWidget({
					type: event.dataTransfer.getData(MediaTypes.WIDGET_TYPE_ID),
					section: selected_section
				});
				selected_section.widgets.push(widget);
				layout_widgets.appendChild(draw_widget(widget));
			}
		}

		layout_widgets.addEventListener('dragover', dragover);
		layout_widgets.addEventListener('dragenter', dragover);
		layout_widgets.addEventListener('dragleave', dragleave);
		layout_widgets.addEventListener('drop', drop);

		//draw widget type list
		function dragstart(event) {
			this.style.opacity = 0.8;
			event.dataTransfer.effectAllowed = 'link';
			event.dataTransfer.setData('text/plain', this.dataset.widgetTypeId);
			event.dataTransfer.setData(MediaTypes.WIDGET_TYPE_ID, this.dataset.widgetTypeId);
		}

		function dragend() {
			this.style.opacity = 1;
		}

		function dblclick() {
			const widget = new CMSWidget({
				type: this.dataset.widgetTypeId,
				section: selected_section
			});
			selected_section.widgets.push(widget);
			layout_widgets.appendChild(draw_widget(widget));
		}

		function draw_widget_type(widget_type_id) {
			const widget_type = Config.Enums.WidgetTypes[widget_type_id];
			const widget_li = document.createFullElement('li', {draggable: 'true', 'data-widget-type-id': widget_type_id}, widget_type.label);
			widget_li.addEventListener('dragstart', dragstart);
			widget_li.addEventListener('dragend', dragend);
			widget_li.addEventListener('dblclick', dblclick);
			return widget_li;
		}

		const layout_widget_types = document.getElementById('layout_widgets_types');
		layout_widget_types.empty();
		Object.keys(Config.Enums.WidgetTypes)
			.sort((w1, w2) => Config.Enums.WidgetTypes[w1].label.compareTo(Config.Enums.WidgetTypes[w2].label))
			.map(draw_widget_type)
			.forEach(Node.prototype.appendChild, layout_widget_types);

		//prepare add section
		document.getElementById('layout_sections_add').addEventListener(
			'click',
			function() {
				const section = new CMSSection({id: (`SECTION_${selected_layout.sections.length + 1}`)});
				section.layout = selected_layout;
				selected_layout.sections.push(section);
				document.getElementById('layout_sections').insertBefore(draw_section(section), undefined);
				select_section(section);
			}
		);

		//manage sort
		Effects.Sortable(
			layout_widgets,
			function() {
				//update array
				selected_section.widgets = this.children.map(c => c.widget);
				//widgets don't have an id and are identified by their index so all widgets links must be updated
				update_widget_data();
			},
			'h3 > img:first-child'
		);

		Effects.Sortable(
			layout_sections,
			function() {
				selected_layout.sections = this.children.map(c => c.section);
			},
			'li > img:first-child'
		);

		//manage submission
		function exit_layout() {
			//retrieve layoutable parent
			const layoutable = selected_layout.layoutable;
			selected_layout = undefined;
			selected_section = undefined;
			//return to layoutable parent
			Router.SelectNode(layoutable);
		}

		document.getElementById('layout_composition').addEventListener(
			'submit',
			function(event) {
				event.stop();

				//save widgets
				const widgets = document.getElementById('layout_widgets').children;
				for(let i = selected_section.widgets.length - 1; i >= 0; i--) {
					update_widget_from_container(selected_section.widgets[i], widgets[i]);
				}

				exit_layout();
				UI.Notify('Layout saved successully', {tag: 'info', icon: 'images/notifications_icons/done.svg'});
			}
		);

		//manage cancel
		document.getElementById('layout_cancel').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				exit_layout();
			}
		);

		//cms layout
		function bus_register() {
			bus.register({
				onChangeCMSSection: function(event) {
					if(event.node.layout === selected_layout && ['id', 'labels'].includes(event.property)) {
						const section_ui = document.querySelectorAll('#layout_sections li').find(e => e.section === event.node);
						//update link
						section_ui.querySelector('a[title="Edit section"]').setAttribute('href', `#node=${event.node.getGlobalId()}`);
						//update title and text
						section_ui.setAttribute('title', event.node.id);
						section_ui.querySelector('span').textContent = event.node.getLocalizedLabel(Languages.GetLanguage());
					}
				},
				onMoveCMSWidget: function(event) {
					if(event.oldParent === selected_section) {
						layout_widgets.removeChild(layout_widgets.children.find(w => w.node === event.node));
						//widgets don't have an id and are identified by their index so all widgets links must be updated
						update_widget_data();
					}
				},
				onDeleteCMSWidget: function(event) {
					if(event.node.section === selected_section) {
						layout_widgets.removeChild(layout_widgets.children.find(w => w.node === event.node));
						//widgets don't have an id and are identified by their index so all widgets links must be updated
						update_widget_data();
					}
				}
			});
		}
		//register hook when a study is loaded because the bus will be reset at that time
		bus_ui.register({onLoadStudy: bus_register});
		bus_register();
	},
	Draw: function(layout) {
		selected_layout = layout;
		selected_section = undefined;

		const layout_sections = document.getElementById('layout_sections');
		layout_sections.empty();
		layout.sections.map(draw_section).forEach(Node.prototype.appendChild, layout_sections);

		select_section(layout.sections[0]);
	}
};
