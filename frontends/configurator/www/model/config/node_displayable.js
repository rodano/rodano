import {IdentifiableNode} from './node_identifiable.js';
import {Report} from './report.js';
import {Utils} from './utils.js';

export class DisplayableNode extends IdentifiableNode {

	constructor() {
		super();
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
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
		const report = super.report(settings);
		Report.checkLocalizedLabel(report, this, 'shortname');
		Report.checkLocalizedLabel(report, this, 'longname');
		Report.checkLocalizedLabel(report, this, 'description');
		return report;
	}
}
