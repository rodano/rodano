import {Node} from './node.js';
import {Report} from './report.js';
import {Utils} from './utils.js';

export class DisplayableNode extends Node {
	static ID_MAX_LENGTH = 62;
	static ID_REGEXP = /^[A-Z][A-Z0-9_]{0,61}$/;

	constructor() {
		super();
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
	}

	static checkId(id, check_format) {
		if(id) {
			if(id.length > DisplayableNode.ID_MAX_LENGTH) {
				return 'Too long';
			}
			if(check_format && !DisplayableNode.ID_REGEXP.test(id)) {
				return 'Wrong format';
			}
		}
		return undefined;
	}

	//prototype localization methods for displayable entities
	//shortname
	getLocalizedShortname(languages) {
		return Utils.getLocalizedField.call(this, 'shortname', languages);
	}
	//longname
	getLocalizedLongname(languages) {
		return Utils.getLocalizedField.call(this, 'longname', languages);
	}
	//description
	getLocalizedDescription(languages) {
		return Utils.getLocalizedField.call(this, 'description', languages);
	}
	//label
	getLocalizedLabel(languages) {
		return this.getLocalizedLongname(languages) || this.getLocalizedShortname(languages) || this.id;
	}
	//custom label
	getLocalizedLabelForType(label_type, languages) {
		if(label_type) {
			return this[label_type.method](languages);
		}
		return this.getLocalizedLabel(languages);
	}

	//get node full id
	getFullId() {
		if(!this.hasParent()) {
			return this.id;
		}
		return `${this.getParent().getFullId()}_${this.id}`;
	}
	//get full shortname
	getLocalizedFullShortname(languages) {
		const shortname = this.getLocalizedShortname(languages);
		if(!this.hasParent()) {
			return shortname;
		}
		return `${this.getParent().getLocalizedFullShortname(languages)} - ${shortname}`;
	}
	//get full longname
	getLocalizedFullLongname(languages) {
		const longname = this.getLocalizedLongname(languages);
		if(!this.hasParent()) {
			return longname;
		}
		return `${this.getParent().getLocalizedFullLongname(languages)} - ${longname}`;
	}
	//get full label
	getLocalizedFullLabel(languages) {
		const label = this.getLocalizedLabel(languages);
		if(!this.hasParent()) {
			return label;
		}
		return `${this.getParent().getLocalizedFullLabel(languages)} - ${label}`;
	}

	//prototype listeners for displayable entities
	onChangeLanguageId(event) {
		this.shortname[event.newValue] = this.shortname[event.oldValue];
		delete this.shortname[event.oldValue];
		this.longname[event.newValue] = this.longname[event.oldValue];
		delete this.longname[event.oldValue];
		this.description[event.newValue] = this.description[event.oldValue];
		delete this.description[event.oldValue];
	}
	onDeleteLanguage(event) {
		delete this.shortname[event.node.id];
		delete this.longname[event.node.id];
		delete this.description[event.node.id];
	}
	report(settings) {
		const report = new Report(this);
		Report.checkId(report, this, settings.id_check !== false);
		Report.checkLocalizedLabel(report, this, 'shortname');
		Report.checkLocalizedLabel(report, this, 'longname');
		Report.checkLocalizedLabel(report, this, 'description');
		return report;
	}
}
