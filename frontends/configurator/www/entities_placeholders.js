import {Loader} from './basic-tools/loader.js';
import {Entities} from './model/config/entities.js';

const available_entities = {
	[Entities.DatasetModel.name]: {html: true, js: true, css: false},
	[Entities.EventModel.name]: {html: true, js: true, css: true},
	[Entities.Menu.name]: {html: true, js: true, css: false},
	[Entities.Profile.name]: {html: true, js: true, css: false},
	[Entities.Workflow.name]: {html: true, js: true, css: true},
};

const PLACEHOLDERS_PATH = 'placeholders/';

const placeholders_loading = {};
const placeholders = {};
const loader = new Loader(document, {url: PLACEHOLDERS_PATH, nocache: false});

export const EntitiesPlaceholders = {
	HasForm: function(entity) {
		return available_entities.hasOwnProperty(entity.name);
	},
	IsFormLoading: function(entity) {
		return placeholders_loading.hasOwnProperty(entity.name);
	},
	IsFormLoaded: function(entity) {
		return placeholders.hasOwnProperty(entity.name);
	},
	Load: async function(entity) {
		const definition = available_entities[entity.name];
		placeholders_loading[entity.name] = true;
		placeholders[entity.name] = {};
		//load HTML template
		if(definition.html) {
			const placeholder_html = await loader.loadHTML(`${entity.id}.html`, document.querySelector('#nodes .content'));
			//keep a hook on placeholder html
			placeholders[entity.name].html = placeholder_html;
		}
		//load CSS
		if(definition.css) {
			await loader.loadCSS(`${entity.id}.css`);
		}
		//load script
		if(definition.js) {
			const placeholder_module = (await import(`./${PLACEHOLDERS_PATH}${entity.id}.js`)).default;
			//keep a hook on placeholder module
			placeholders[entity.name].module = placeholder_module;
			//initialize only forms loaded asynchronously
			if(placeholder_module.init) {
				placeholder_module.init();
			}
		}
		//form is no longer loading
		delete placeholders_loading[entity.name];
	},
	Open: async function(entity, node) {
		//if form has not been loaded yet, load it and re-open it
		//next time, it will be loaded
		if(!EntitiesPlaceholders.IsFormLoaded(entity)) {
			if(!EntitiesPlaceholders.IsFormLoading(entity)) {
				EntitiesPlaceholders.Load(entity).then(() => EntitiesPlaceholders.Open(entity, node));
			}
		}
		else {
			placeholders[entity.name].html.style.display = 'block';
			placeholders[entity.name].module.open(node);
		}
	},
	//for testing purpose, this method loads all forms
	LoadAll: function() {
		//retrieve available entities
		const entities = Object.values(Entities).filter(e => available_entities.hasOwnProperty(e.name));
		return Promise.all(entities.map(e => EntitiesPlaceholders.Load(e)));
	}
};
