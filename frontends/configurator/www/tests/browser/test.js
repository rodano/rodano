import '../../basic-tools/dom_extension.js';

import {Suite} from '../../tools/suite.js';
import {Queue} from '../../basic-tools/queue.js';
import {Runner} from './runner.js';
import reporter from './reporter.js';

function collapse_all(root, event) {
	event.preventDefault();
	root.querySelectorAll('.bundle').forEach(function(bundle) {
		bundle.querySelector('h3').classList.remove('expanded');
		bundle.querySelector('h3').classList.add('collapsed');
		bundle.querySelector('ul').style.display = 'none';
	});
}

function expand_all(root, event) {
	event.preventDefault();
	root.querySelectorAll('.bundle').forEach(function(bundle) {
		bundle.querySelector('h3').classList.remove('collapsed');
		bundle.querySelector('h3').classList.add('expanded');
		bundle.querySelector('ul').style.display = 'block';
	});
}

function set_status_css_class(element, fail) {
	if(fail) {
		element.classList.remove('success');
		element.classList.add('fail');
	}
	else {
		element.classList.remove('fail');
		element.classList.add('success');
	}
}

export const Test = {
	Init: function() {
		document.getElementById('report_date').textContent = new Date().toFullDisplay();
		document.getElementById('report_collapse').addEventListener('click', collapse_all.bind(undefined, document.body));
		document.getElementById('report_expand').addEventListener('click', expand_all.bind(undefined, document.body));
	},
	RunSuites: function(config) {
		//transform configuration into an objects graph
		const suites = config.map(s => Suite.fromJSON(s));

		//update ui accordingly
		suites.forEach(suite => {

			//draw suite
			const suite_report = document.createFullElement('div', {'class': 'suite'});
			const results = document.getElementById('results');
			results.appendChild(suite_report);

			//suite header
			const suite_header = document.createFullElement('h2', {}, suite.name);
			suite_report.appendChild(suite_header);

			//suite infos
			suite_header.appendChild(document.createFullElement('span', {}, new Date().toFullDisplay()));

			const suite_status = document.createElement('span');
			suite_header.appendChild(suite_status);

			const suite_duration = document.createElement('span');
			suite_header.appendChild(suite_duration);

			const suite_collapse = document.createFullElement('a', {href: '#'}, 'Collapse all');
			suite_collapse.addEventListener('click', collapse_all.bind(undefined, suite_report));
			suite_header.appendChild(suite_collapse);

			const suite_expand = document.createFullElement('a', {href: '#'}, 'Expand all');
			suite_expand.addEventListener('click', expand_all.bind(undefined, suite_report));
			suite_header.appendChild(suite_expand);

			//content
			const suite_bundles = document.createElement('div');
			suite_report.appendChild(suite_bundles);

			//manage bundles
			suite.bundles.forEach(bundle => {
				//draw bundle
				const bundle_report = document.createFullElement('div', {class: 'bundle'});
				suite_bundles.appendChild(bundle_report);

				//bundle header
				const bundle_header = document.createFullElement('h3', {class: 'expanded'}, bundle.test,
					{
						'click': function(event) {
							event.preventDefault();
							if(this.classList.contains('expanded')) {
								this.classList.remove('expanded');
								this.classList.add('collapsed');
								bundle_results.style.display = 'none';
							}
							else {
								this.classList.remove('collapsed');
								this.classList.add('expanded');
								bundle_results.style.display = 'block';
							}
						}
					}
				);
				bundle_report.appendChild(bundle_header);

				//bundle infos
				const bundle_status = document.createElement('span');
				bundle_header.appendChild(bundle_status);

				const bundle_duration = document.createElement('span');
				bundle_header.appendChild(bundle_duration);

				const bundle_results = document.createElement('ul');
				bundle_report.appendChild(bundle_results);

				//write report in bundle header
				bundle.addEventListener('specification', (bundle, feature, specification) => {
					const element = document.createElement('li');
					element.setAttribute('class', specification.success ? 'success' : 'fail');
					element.appendChild(document.createTextNode(specification.message));
					if(!specification.success) {
						const error_element = document.createFullElement('p', {class: 'error'});
						error_element.appendChild(document.createTextNode(specification.error));
						element.appendChild(error_element);
					}
					bundle_results.appendChild(element);
					//update bundle results
					//var bundle_percentage = 100 * assert.getSuccessesNumber() / assert.getTotal();
					//var bundle_gradient = 'linear-gradient(to right, #D1EED1 ' + bundle_percentage + '%, #FDDFDE)';
					//bundle_header.style.background = bundle_gradient;
					const specifications = bundle.getSpecifications();
					set_status_css_class(bundle_header, bundle.getFailsNumber() > 0);
					bundle_status.textContent = `${bundle.getSuccessesNumber()}/${specifications.length} tests`;
					bundle_duration.textContent = `${new Date().getTime() - bundle.beginTime}ms`;
					//update suite results
					//var suite_percentage = 100 * suite.counters.successes / (suite.getTests());
					//var suite_gradient = 'linear-gradient(to right, #D1EED1 ' + suite_percentage + '%, #FDDFDE)';
					//suite_header.style.background = suite_gradient;
					set_status_css_class(suite_header, suite.getFailsNumber() > 0);
					suite_status.textContent = `${suite.getSuccessesNumber()}/${specifications.length} tests`;
					suite_duration.textContent = `${suite.getDuration()}ms`;
				});
			});
		});

		const times = {
			start: new Date().getTime()
		};
		const counters = {
			successes: 0,
			fails: 0
		};

		function update_results(suite) {
			counters.successes += suite.getSuccessesNumber();
			counters.failures += suite.getFailsNumber();
			times.stop = new Date().getTime();
			//update ui
			set_status_css_class(document.getElementById('report'), counters.failures > 0);
			document.getElementById('report_status').textContent = `${counters.successes}/${counters.successes + counters.fails} tests`;
			document.getElementById('report_duration').textContent = `${times.stop - times.start}ms`;
		}

		const queue = new Queue(update_results)
			.then(() => {
				document.getElementById('report_running').style.display = 'none';

				//manage reports
				const date = new Date().toISOString();

				//generate enhanced report
				const report_enhanced = reporter('KVConfig', suites);
				const content_enhanced = new XMLSerializer().serializeToString(report_enhanced);
				const blob_enhanced = new Blob([content_enhanced], {type: 'text/xml'});
				const blob_url_enhanced = window.URL.createObjectURL(blob_enhanced);

				//update and show download link
				const report_enhanced_download = document.getElementById('report_enhanced_download');
				report_enhanced_download.setAttribute('download', `report_${date}.xml`);
				report_enhanced_download.setAttribute('href', blob_url_enhanced);
				report_enhanced_download.style.display = 'inline';

				//generate normal report
				const report = reporter('KVConfig', suites);
				const content = new XMLSerializer().serializeToString(report);
				const blob = new Blob([content], {type: 'text/xml'});
				const blob_url = window.URL.createObjectURL(blob);

				//update and show download link
				const report_download = document.getElementById('report_download');
				report_download.setAttribute('download', `report_${date}.xml`);
				report_download.setAttribute('href', blob_url);
				report_download.style.display = 'inline';
			})
			.catch(exception => {
				console.error(exception);
			});

		//create runner and run the suites
		const runner = new Runner();
		queue.addAll(suites.map(s => Suite.prototype.run.bind(s, runner)));
	},
	RunBundle: function(bundle) {
		//create temporary suite
		const suite = {
			name: 'Tmp',
			path: '',
			bundles: [bundle]
		};
		Test.RunSuites([suite]);
	}
};
