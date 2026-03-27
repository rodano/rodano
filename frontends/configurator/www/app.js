import './basic-tools/extension.js';

import {Driver} from './basic-tools/driver.js';
import {Loader} from './basic-tools/loader.js';
import {Logger} from './tools/logger.js';
import {Cron} from './tools/cron.js';
import {DOMAssert} from './tools/dom_assert.js';
import {UI} from './tools/ui.js';
import {Network} from './tools/network.js';
import {Settings} from './settings.js';
import {APITools} from './api_tools.js';
import {PWA} from './pwa.js';
import {Router} from './router.js';
import {NodeTools} from './node_tools.js';
import {Backups} from './backups.js';
import {Favorites} from './favorites.js';
import {ContextualMenu} from './contextual_menu.js';
import {Menu} from './menu.js';
import {Hooks} from './hooks.js';
import {Logs} from './logs.js';
import {Configuration} from './configuration.js';
import {Dashboards} from './dashboards.js';
import {Comparator} from './comparator.js';
import {RuleDefinitions} from './rule_definitions.js';
import {Keyboard} from './keyboard.js';
import {Shortcuts} from './shortcuts.js';
import {Themes} from './themes.js';
import {Welcome} from './welcome.js';
import {Templates} from './templates.js';
import {Changelogs} from './changelogs.js';
import {ConfigurationSerialized} from './configuration_serialized.js';
import {Search} from './search.js';
import {Rule} from './rule.js';
import {Layout} from './layout.js';
import {Wizards} from './wizards.js';
import {Transfer} from './transfer.js';
import {Statistics} from './statistics.js';
import {StudyLoader} from './study_loader.js';
import {StudyTree} from './study_tree.js';
import {EntitiesForms} from './entities_forms.js';
import {FormStaticActions} from './form_static_actions.js';
import {TemplateRepositories} from './template_repositories.js';
import {NodeClipboard} from './node_clipboard.js';
import {NodeContextualMenu} from './node_contextual_menu.js';
import {DocumentationSelection} from './documentation_selection.js';
import {Importer} from './importer.js';
import {FormulasHelp} from './formulas_help.js';
import {Bundle} from './tools/bundle.js';
import {Reports} from './reports.js';

//options
const APPLICATION_MODES = ['DEV', 'TEST', 'PROD'];
//---------------------------0-------1-------2
const APPLICATION_MODE = APPLICATION_MODES[2];

const DEBUG_CONFIG_URL = `${window.location.protocol}//${window.location.hostname}/configs/test.json`;

const TESTS_SCRIPTS = ['functional.test.js', 'integration.test.js'];
//------------------------------0-----------------------1
const TEST_SCRIPT = TESTS_SCRIPTS[1];

const SETTING_TREE_WIDTH = 'tree_width';

const available_custom_elements = [
	{id: 'palette', html: true, js: true},
	{id: 'constrained_input', html: true, js: true},
	{id: 'localized_input', html: true, js: true}
];

//create tools
const logger = new Logger();
const cron = new Cron();

//register service workers used to cache application
const https = window.location.protocol === 'https:';
//remove "index.html" from path if necessary
const path = window.location.pathname.replace(/index.html/, '');
//enable cache worker over HTTPS (the browser will not allow a service worker without HTTPS anyway) and in production mode
if(https) {
	if(APPLICATION_MODE === 'PROD') {
		navigator.serviceWorker.register(`${path}worker_cache.js`, {scope: path})
			.then(registration => {
				if(!registration.active) {
					console.info(`Cache service worker registered successfully with scope ${registration.scope}`);
				}
			}).catch(error => {
				console.error(`Cache service worker registration failed: ${error.message}`);
			});
	}
	//disable cache worker when in development mode
	else {
		navigator.serviceWorker.getRegistrations()
			.then(registrations => {
				return Promise.all(registrations.map(r => r.unregister()));
			})
			.then(results => {
				if(!results.isEmpty()) {
					console.log('Cache service worker unregistered for development. You should reload the page.');
				}
			});
	}
}

function init() {

	//load custom elements before anything else
	const loader = new Loader(document, {url: 'elements/', nocache: false});
	const custom_elements_container = document.getElementById('custom_elements');
	Promise.all(available_custom_elements.map(function(element) {
		let promise = Promise.resolve();
		if(element.html) {
			promise = promise.then(loader.loadHTMLTemplate.bind(loader, `${element.id}.html`, custom_elements_container));
		}
		promise = promise.then(loader.loadLibrary.bind(loader, `${element.id}.js`));
		return promise;
	})).then(function() {

		Router.Init();
		Configuration.Init();
		Backups.Init();
		TemplateRepositories.Init();
		Templates.Init();
		Welcome.Init();
		Hooks.Init();
		Reports.Init();
		Favorites.Init();
		Themes.Init();
		Menu.Init();
		Comparator.Init();
		RuleDefinitions.Init();
		Logs.Init();
		ConfigurationSerialized.Init();
		FormStaticActions.Init();
		StudyTree.Init();
		ContextualMenu.Init();
		NodeContextualMenu.Init();
		Dashboards.Init();
		Changelogs.Init();
		Importer.Init();
		DocumentationSelection.Init();
		Search.Init();
		Transfer.Init();
		EntitiesForms.Init();
		Rule.Init();
		Layout.Init();
		Wizards.Init();
		Shortcuts.Init();
		FormulasHelp.Init();
		Keyboard.Init();
		NodeClipboard.Init();
		Statistics.Init();
		NodeTools.Init();
		PWA.Init();

		//tabs
		document.querySelectorAll('ul.tabs:not(.manual)').forEach(UI.Tabify);

		//resize
		const aside = document.querySelector('aside');
		function resize_tree(width) {
			if(width > 140 && width < window.innerWidth - 800) {
				aside.style.width = `${width}px`;
			}
		}
		function resize_tree_listener(event) {
			event.stop();
			resize_tree(event.clientX);
		}
		function start_resize_tree(event) {
			if(event.clientX > this.offsetWidth - 5) {
				event.stop();
				document.body.classList.add('resizing');
				document.addEventListener('mousemove', resize_tree_listener);
				document.addEventListener('mouseup', stop_resize_tree);
			}
		}
		function stop_resize_tree(event) {
			document.body.classList.remove('resizing');
			document.removeEventListener('mousemove', resize_tree_listener);
			document.removeEventListener('mouseup', stop_resize_tree);
			Settings.Set(SETTING_TREE_WIDTH, event.clientX);
		}
		aside.addEventListener('mousedown', start_resize_tree);
		if(Settings.Exists(SETTING_TREE_WIDTH)) {
			resize_tree(Settings.Get(SETTING_TREE_WIDTH));
		}

		//manage application mode
		switch(APPLICATION_MODE) {
			case 'DEV':
				//load a configuration
				Configuration.PullFromURL(DEBUG_CONFIG_URL).then(function() {
					//do some stuff
					/*Backups.Get(new Date(1417190815458), function(backup) {
						var source = {config : study, name : 'Current configuration', processed : true, revived : true};
						var target = {config : backup.config, name : backup.name + ' - ' + backup.description + ' by ' + backup.user, processed : true, revived : false};
						Comparator.Compare(source, target);
					});*/
					/*Wizards.Open('form_model');
					setTimeout(() => {
						document.getElementById('wizard_start').click();
						document.getElementById('wizard_form_model_event_model_id').value = 'TELEPHONE_VISIT';
						document.getElementById('wizard_next').click();
					}, 500);*/
				});
				break;
			case 'TEST': {
				//close button
				document.getElementById('tests_close').addEventListener(
					'click',
					function() {
						this.parentElement.style.display = 'none';
					}
				);

				//display test results in a box
				const tests = document.getElementById('tests');
				tests.style.display = 'block';

				//asserts list
				const asserts = document.getElementById('tests_asserts');

				//prepare bundle
				const bundle = new Bundle();
				bundle.addEventListener('specification', (_, feature, specification) => {
					if(specification.success) {
						//update ui
						asserts.appendChild(document.createFullElement('li', {}, specification.message));
					}
					else {
						//stop tests
						console.trace();
						throw new Error(`Oups, this specification is wrong: ${specification.error}`);
					}
				});
				bundle.addEventListener('end', bundle => {
					const message = `${bundle.getSuccessesNumber()}/${bundle.getSpecifications().length} tests in ${bundle.getDuration()}ms`;
					console.log(message);
					asserts.appendChild(document.createFullElement('li', {}, message));
				});

				//prepare assert
				const assert = new DOMAssert(document);

				//prepare driver
				const driver = new Driver();

				//load test script
				import(`./tests/${TEST_SCRIPT}`).then(test => test.default(bundle, assert, driver));
				break;
			}
			default: {
				//try to retrieve API URL and session from url
				const url = new URL(window.location.href);
				const api_url = url.searchParams.get('api_url');

				if(api_url) {
					//check network
					UI.StartLoading();
					Network.Check(`${api_url}/config/public-study`)
						//initialize api tool and retrieve user and configuration if network is available
						.then(() => {
							//initialize API tools with URL parameters
							APITools.Init(api_url, url.searchParams.get('bearer_token'));
						})
						.then(APITools.RetrieveUser)
						.then(Configuration.PullFromServer)
						.catch(() => {
							UI.Notify('Unable to reach server', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
							Welcome.Open();
						})
						.finally(UI.StopLoading);
				}
				else {
					//display welcome screen if API URL has not been specified
					Welcome.Open();
				}
			}
		}
	});
}

function uninit() {
	StudyLoader.Unload();
}

window.addEventListener('load', init);
window.addEventListener('unload', uninit);

window.addEventListener(
	'beforeunload',
	function() {
		if(Settings.Get('prevent_exit')) {
			if(confirm('Make sure you saved your configuration before leaving or reloading this page.')) {
				history.go();
			}
			else {
				setTimeout(() => window.stop(), 1);
			}
		}
	}
);

export {DEBUG_CONFIG_URL, logger, cron};
