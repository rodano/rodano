import '../../basic-tools/dom_extension.js';

import {Test} from './test.js';

//load suites
const ALL_SUITES = [
	'../suites/tools_unit_tests.suite.json',
	//'../suites/rest_api_sdk_tests.suite.json',
	'../suites/model_unit_tests.suite.json',
	'../suites/integration_tests.suite.json',
	'../suites/functional_tests.suite.json'
];

function load_suite(url) {
	return fetch(`${url}?${Date.now()}`).then(r => r.json());
}

window.addEventListener('load', function() {
	Test.Init();

	//run pre-defined test if hash contains auto
	if(location.hash === '#auto') {
		Promise.all(ALL_SUITES.map(load_suite)).then(Test.RunSuites).catch(console.error);
	}
	else if(location.hash.startsWith('#bundle')) {
		const bundle = JSON.parse(decodeURI(location.hash.substring(8)));
		Test.RunBundle(bundle);
	}

	//tmp suites
	//Test.RunBundle({test: '../../tools/partial_date.test.js'});
	//Test.RunBundle({test: '../../tools/custom_extension.test.js'});
	//Test.RunBundle({test: '../../tools/custom_dom_extension.test.js'});
	//Test.RunBundle({dom: true, website: '../../index.html', test: '../../tests/functional.test.js'});
	//Test.RunBundle({dom: true, website: '../../index.html', test: '../../tests/integration.test.js'});
	//Test.RunBundle({test: '../../model/config/entities_config_bus.test.js'});
	//Test.RunBundle({dom: true, test: '../../api.test.js'});
});
