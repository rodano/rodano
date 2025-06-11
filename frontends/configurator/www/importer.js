import {FieldModelType} from './model/config/field_model_type.js';
import {DataType} from './model/config/data_type.js';
import {FieldModel} from './model/config/entities/field_model.js';
import {DatasetModel} from './model/config/entities/dataset_model.js';
import {PossibleValue} from './model/config/entities/possible_value.js';
import {DisplayableNode} from './model/config/node_displayable.js';
import {StudyHandler} from './study_handler.js';
import {CSV} from './basic-tools/csv.js';
import {UI} from './tools/ui.js';
import {Router} from './router.js';

const INFORMATION_DROP = 'Drop a CSV file from your file system here';
const MANDATORY_COLUMNS = [
	'Dataset model ID',
	'Field model ID',
	'Field model shortname',
	'Type',
	'Data type',
];
const KNOWN_COLUMNS = [
	...MANDATORY_COLUMNS,
	'Dataset model shortname',
	'Field model longname',
	'Max integer digits',
	'Max decimal digits',
	'Collect years',
	'Collect months',
	'Collect days',
	'Collect hours',
	'Collect minutes',
	'Collect seconds',
	'Max length',
	'Size',
	'Help',
	'Possible values ID',
	'Possible values shortname',
	'Possible values'
];

let current_data = undefined;
let analysing = false;

function numberify(value, default_value) {
	if(value !== undefined && value !== '') {
		return parseInt(value);
	}
	return default_value;
}

function import_line(line, index, study, language, dry_run) {
	return new Promise(resolve => {
		setTimeout(() => {
			const report = {
				new_dataset_model_id: undefined,
				updated_dataset_model_id: undefined,
				new_field_model_id: undefined,
				updated_field_model_id: undefined,
				errors: []
			};
			//check that line contains the mandatory columns
			if(MANDATORY_COLUMNS.some(c => !line[c])) {
				report.errors.push(`Columns ${MANDATORY_COLUMNS.join(', ')} are required`);
			}
			//retrieve or create dataset model
			const dataset_model_id = line['Dataset model ID'];
			const dataset_model_shortname = line['Dataset model shortname'];
			const dataset_model_id_error = DisplayableNode.checkId(dataset_model_id, true);
			if(dataset_model_id_error) {
				report.errors.push(`Dataset model id [${dataset_model_id}] is not valid: ${dataset_model_id_error}`);
			}
			let dataset_model;
			try {
				dataset_model = study.getDatasetModel(dataset_model_id);
				report.updated_dataset_model_id = dataset_model_id;
				if(!dry_run && dataset_model_shortname) {
					dataset_model.shortname[language] = dataset_model_shortname;
				}
			}
			catch {
				report.new_dataset_model_id = dataset_model_id;
				const shortname = {};
				shortname[language] = dataset_model_shortname || dataset_model_id;
				dataset_model = new DatasetModel({id: dataset_model_id, shortname: shortname});
				if(!dry_run) {
					study.addChild(dataset_model);
				}
			}
			//retrieve or create field model
			const field_model_id = line['Field model ID'];
			const field_model_shortname = line['Field model shortname'];
			const field_model_longname = line['Field model longname'];
			const field_model_id_error = DisplayableNode.checkId(field_model_id, true);
			if(field_model_id_error) {
				report.errors.push(`Field model id [${field_model_id}] is not valid: ${field_model_id_error}`);
			}
			let field_model;
			try {
				field_model = dataset_model.getFieldModel(field_model_id);
				report.updated_field_model_id = field_model_id;
				if(!dry_run && field_model_shortname) {
					field_model.shortname[language] = field_model_shortname;
					field_model.longname[language] = field_model_longname;
				}
			}
			catch {
				report.new_field_model_id = `${dataset_model_id}_${field_model_id}`;
				const shortname = {};
				shortname[language] = field_model_shortname || field_model_id;
				const longname = {};
				if(field_model_longname) {
					longname[language] = field_model_longname;
				}
				field_model = new FieldModel({id: field_model_id, shortname: shortname, longname: longname});
				if(!dry_run) {
					dataset_model.addChild(field_model);
				}
			}
			//set field model export order based on line in CSV
			field_model.exportOrder = index;
			//set field model type
			const field_model_type = line['Type'];
			if(!FieldModelType[field_model_type]) {
				report.errors.push(`Undefined field model type ${field_model_type}`);
			}
			if(!dry_run) {
				field_model.type = field_model_type;
			}
			//set field model data type
			const field_model_data_type = line['Data type'];
			if(!DataType[field_model_data_type]) {
				report.errors.push(`Undefined field model data type ${field_model_data_type}`);
			}
			if(!dry_run) {
				field_model.dataType = field_model_data_type;
			}
			//set other properties
			if(FieldModelType.NUMBER.name === field_model_type && !line['Max integer digits']) {
				report.errors.push('Max integer digits is required for field model of type "NUMBER"');
			}
			if(!dry_run) {
				field_model.maxIntegerDigits = numberify(line['Max integer digits'], undefined);
				field_model.maxDecimalDigits = numberify(line['Max decimal digits'], field_model_type === FieldModelType.NUMBER.name ? 0 : undefined);
				field_model.withYears = !!line['Collect years'];
				field_model.withMonths = !!line['Collect months'];
				field_model.withDays = !!line['Collect days'];
				field_model.withHours = !!line['Collect hours'];
				field_model.withMinutes = !!line['Collect minutes'];
				field_model.withSeconds = !!line['Collect seconds'];
				field_model.size = numberify(line['Size'], undefined);
				field_model.maxLength = numberify(line['Max length'], undefined);
				field_model.forDisplay = line['Help'] || undefined;
			}
			//set possible values
			let possible_value_ids = undefined;
			let possible_value_shortnames = undefined;
			//there are 2 ways to set the possible values
			//the first way is a single cell containing all possible values, one per line, with the format "id: shortname"
			const possible_values = line['Possible values'];
			if(possible_values) {
				possible_value_ids = [];
				possible_value_shortnames = [];
				const values = possible_values.split('\n');
				values.forEach(value => {
					const [id, shortname] = value.split(':');
					possible_value_ids.push(id.trim());
					possible_value_shortnames.push(shortname.trim());
				});
			}
			//the second way uses 2 cells, one for ids and another for shortnames, both concatenated using the character "|"
			else {
				possible_value_ids = line['Possible values ID'] ? line['Possible values ID'].split('|') : undefined;
				possible_value_shortnames = line['Possible values shortname'] ? line['Possible values shortname'].split('|') : undefined;
			}
			if(!possible_value_ids && [FieldModelType.SELECT.name, FieldModelType.RADIO.name].includes(field_model_type)) {
				report.errors.push('Possible values are required for field model of type "SELECT" and "RADIO"');
			}
			else {
				//check that there are as many shortnames as ids or if no shortnames have been set (in this case, the ids will be used as shortnames)
				if(possible_value_shortnames && possible_value_ids.length !== possible_value_shortnames.length) {
					report.errors.push('Number of possible value ids and shortnames don\'t match');
				}
			}
			//reset possible values
			field_model.possibleValues = [];
			if(possible_value_ids) {
				possible_value_ids.forEach((id, index) => {
					const shortname = {};
					shortname[language] = possible_value_shortnames ? possible_value_shortnames[index] || id : id;
					field_model.addChild(new PossibleValue({id: id, shortname: shortname}));
				});
			}
			resolve(report);
		}, 0);
	});
}

async function import_data(lines, study, dry_run, progress) {
	const summary = {
		new_dataset_models: new Set(),
		updated_dataset_models: new Set(),
		new_field_models: new Set(),
		updated_field_models: new Set(),
		errors: []
	};
	//initialize progress
	if(progress) {
		progress.setAttribute('value', '0');
		progress.setAttribute('max', lines.length.toString());
	}
	const language = study.defaultLanguageId;
	for(const [index, line] of lines.entries()) {
		const report = await import_line(line, index, study, language, dry_run);
		if(report.new_dataset_model_id) {
			summary.new_dataset_models.add(report.new_dataset_model_id);
		}
		if(report.updated_dataset_model_id) {
			summary.updated_dataset_models.add(report.updated_dataset_model_id);
		}
		if(report.new_field_model_id) {
			summary.new_field_models.add(report.new_field_model_id);
		}
		if(report.updated_field_model_id) {
			summary.updated_field_models.add(report.updated_field_model_id);
		}
		if(!report.errors.isEmpty()) {
			summary.errors.push({line: index, errors: report.errors});
		}
		if(progress) {
			progress.setAttribute('value', (index + 1).toString());
		}
	}
	return summary;
}

function draw_report(report) {
	const report_li = document.createFullElement('li');
	//lines are 0 indexed and the header has been removed
	report_li.appendChild(document.createFullElement('span', {}, `Line ${report.line + 2}`));
	const report_ul = document.createFullElement('ul');
	report.errors.map(e => document.createFullElement('li', {}, e)).forEach(Node.prototype.appendChild, report_ul);
	report_li.appendChild(report_ul);
	return report_li;
}

function reset_state() {
	document.getElementById('importer_failure').style.display = 'none';
	document.getElementById('importer_success').style.display = 'none';
	document.getElementById('importer_progress').style.display = 'none';
	document.getElementById('importer_information').textContent = INFORMATION_DROP;
	document.getElementById('importer_warning').textContent = '';
}

export const Importer = {
	Init: function() {
		const importer_dialog = /**@type {HTMLDialogElement}*/ document.getElementById('importer');
		const importer_progress = document.getElementById('importer_progress');
		const importer_error = document.getElementById('importer_error');
		const importer_warning = document.getElementById('importer_warning');
		const importer_information = document.getElementById('importer_information');

		//hooks dialog is managed by the application URL
		importer_dialog.addEventListener('close', () => Router.CloseTool());

		importer_dialog.addEventListener(
			'dragover',
			event => {
				event.stop();
				if(!analysing) {
					importer_dialog.classList.add('dragover');
				}
			}
		);
		importer_dialog.addEventListener(
			'dragend',
			event => {
				event.stop();
				if(!analysing) {
					importer_dialog.classList.remove('dragover');
				}
			}
		);
		importer_dialog.addEventListener(
			'drop',
			event => {
				event.preventDefault();
				if(!analysing) {
					analysing = true;
					//update ui
					importer_dialog.classList.remove('dragover');
					importer_error.style.display = 'none';
					importer_warning.style.display = 'none';
					reset_state();
					//only one file can be managed at a time
					if(event.dataTransfer.files.length !== 1) {
						importer_error.textContent = `Drop one and only one file (${event.dataTransfer.files.length} files dropped)`;
						importer_error.style.display = 'block';
						analysing = false;
						return;
					}
					//only CSV files are allowed
					const file = event.dataTransfer.files[0];
					if(file.type !== '' && file.type !== CSV.MIME_TYPE) {
						importer_error.textContent = 'Only CSV files are supported';
						importer_error.style.display = 'block';
						analysing = false;
						return;
					}
					//read file
					importer_information.textContent = `Parsing and checking file ${file.name}`;
					const reader = new FileReader();
					reader.addEventListener(
						'loadstart',
						reader_event => {
							UI.StartLoading();
							importer_progress.removeAttribute('max');
							importer_progress.removeAttribute('value');
							importer_progress.style.display = 'block';
							if(reader_event.lengthComputable) {
								importer_progress.setAttribute('max', reader_event.total.toString());
							}
						}
					);
					reader.addEventListener(
						'error',
						() => {
							importer_error.textContent = `Error while loading ${file.name}`;
							importer_error.style.display = 'block';
						}
					);
					reader.addEventListener(
						'load',
						async () => {
							try {
								const text = /**@type {string}*/ (reader.result);
								if(text.trim().length === 0) {
									throw new Error('The file is empty');
								}
								//check header
								const header = CSV.parseHeader(text);
								const unknown_columns = header.filter(c => !KNOWN_COLUMNS.includes(c));
								if(!unknown_columns.isEmpty()) {
									importer_warning.textContent = `Found columns "${unknown_columns.join(', ')}" that are not supported by the importer. Valid columns are "${KNOWN_COLUMNS.join(', ')}".`;
									importer_warning.style.display = 'block';
								}
								current_data = CSV.parseToDictionary(text);
								//check data
								const summary = await import_data(current_data, StudyHandler.GetStudy(), true, importer_progress);
								if(!summary.errors.isEmpty()) {
									document.getElementById('importer_failure').style.display = 'block';
									summary.errors.map(draw_report).forEach(Node.prototype.appendChild, document.getElementById('importer_report_errors').empty());
								}
								else {
									document.getElementById('importer_success').style.display = 'block';
									document.getElementById('importer_report_new_dataset_models').textContent = summary.new_dataset_models.size.toString();
									document.getElementById('importer_report_updated_dataset_models').textContent = summary.updated_dataset_models.size.toString();
									document.getElementById('importer_report_new_field_models').textContent = summary.new_field_models.size.toString();
									document.getElementById('importer_report_updated_field_models').textContent = summary.updated_field_models.size.toString();
								}
								analysing = false;
							}
							catch(exception) {
								console.error(exception);
								importer_error.textContent = `Unable to parse file ${file.name}: ${exception.message}`;
								importer_error.style.display = 'block';
								analysing = false;
							}
						}
					);
					reader.addEventListener('loadend', () => UI.StopLoading());
					reader.addEventListener(
						'progress',
						reader_event => {
							if(reader_event.lengthComputable) {
								importer_progress.setAttribute('value', reader_event.loaded.toString());
							}
						}
					);
					reader.readAsText(file);
				}
			}
		);

		document.getElementById('importer_import').addEventListener(
			'click',
			async function() {
				if(current_data && !analysing) {
					analysing = true;
					this.setAttribute('disabled', 'disabled');
					document.getElementById('importer_information').textContent = 'Importing field models';
					const importer_progress = document.getElementById('importer_progress');
					importer_progress.style.display = 'block';
					await import_data(current_data, StudyHandler.GetStudy(), false, importer_progress);
					UI.Notify('Import successful', {tag: 'info', icon: 'images/notifications_icons/done.svg'});
					window.location.href = '#';
					this.removeAttribute('disabled');
					analysing = false;
				}
			}
		);
	},
	Open: function() {
		reset_state();
		/**@type {HTMLDialogElement}*/ (document.getElementById('importer')).showModal();
	}
};
