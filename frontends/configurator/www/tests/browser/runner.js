import {DOMAssert} from '../../tools/dom_assert.js';
import {Driver} from '../../basic-tools/driver.js';

export class Runner {
	run(bundle) {
		return new Promise(resolve => {
			//create iframe to sandbox each test
			const iframe = document.createElement('iframe');
			iframe.setAttribute('sandbox', 'allow-same-origin allow-top-navigation allow-forms allow-scripts allow-modals allow-downloads');
			if(bundle.website) {
				//load website and show iframe
				iframe.setAttribute('src', bundle.website);
				iframe.setAttribute('width', '90%');
				iframe.setAttribute('height', '90%');
				iframe.setAttribute('style', 'position: fixed; top: 5%; left: 5%; display: none; z-index: 1000; border: 2px solid #666; background-color: white;');
			}
			else {
				const html = '<!DOCTYPE html><html><head><meta charset=\'UTF-8\' /></head><body></body></html>';
				iframe.srcdoc = html;
				iframe.setAttribute('width', '0');
				iframe.setAttribute('height', '0');
				iframe.setAttribute('style', 'display: none;');
			}
			//add listeners to bundle
			bundle.addEventListener('begin', bundle => {
				if(bundle.website) {
					iframe.style.display = 'block';
				}
			});
			bundle.addEventListener('end', bundle => {
				if(bundle.website) {
					iframe.style.display = 'none';
				}
				resolve(bundle);
			});
			bundle.addEventListener('feature', (bundle, feature) => {
				//if executed using puppeteer, the global function onFeature exists
				window.onFeature?.call(undefined, bundle, feature);
			});
			bundle.addEventListener('specification', (bundle, feature, specification) => {
				//if executed using puppeteer, the global function onSpecification exists
				window.onSpecification?.call(undefined, bundle, feature, specification);
			});
			iframe.addEventListener(
				'load',
				() => {
					//configure assert and driver
					//TODO remove this
					iframe.contentWindow.bundle = bundle;
					iframe.contentWindow.assert = new DOMAssert(iframe.contentDocument);
					iframe.contentWindow.driver = new Driver(iframe.contentWindow, iframe.contentDocument);
					//create testing script dynamically
					const launch_test_code =
					`
						import('${bundle.test}').then(t => {
							if(t.default) {
								t.default(bundle, assert, driver);
							}
							else {
								console.error('Test module must contain a default export function. Are you sure this is a test?');
							}
						});
					`;
					//import in a statement and not a function
					//it cannot be called in the context of the iframe without using eval
					iframe.contentWindow.eval(launch_test_code);
				}
			);
			document.body.appendChild(iframe);
		});
	}
}
