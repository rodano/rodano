import '../www/basic-tools/extension.js';

import * as path from 'path';
import * as fs from 'fs';
import * as vm from 'vm';
import * as http from 'http';
import finalhandler from 'finalhandler';
import serve_static from 'serve-static';
import puppeteer from 'puppeteer';
import {Assert} from '../www/tools/assert.js';

//global variables
const PORT = 1337;
const CODE_PATH = 'www';

function path_to_unix(path) {
	return path.replace(/\\/g, '/');
}

export class Runner {
	constructor(debug, headless) {
		this.debug = debug !== undefined ? debug : false;
		this.headless = headless !== undefined ? headless : true;
	}

	run(bundle) {
		//if module is imported more than once, be sure to provide the same instance
		const module_cache = {};
		function generate_linker(reference_path) {
			return function linker(specifier, reference_module) {
				const module_path = path.join(reference_path, specifier);
				if(!module_cache.hasOwnProperty(module_path)) {
					if(!fs.existsSync(module_path) || !fs.statSync(module_path).isFile()) {
						throw new Error(`Unable to resolve dependency: ${module_path}`);
					}
					const options = {
						identifier: specifier,
						context: reference_module.context
					};
					const module = new vm.SourceTextModule(fs.readFileSync(module_path, 'utf-8'), options);
					const linker = generate_linker(path.dirname(module_path));
					//cache the module before calling the link method
					//this prevent dependencies loop
					module_cache[module_path] = module;
					//link and evaluate module
					module.link(linker).then(() => module.evaluate()).then(() => module);
				}
				return module_cache[module_path];
			};
		}

		//eslint-disable-next-line no-async-promise-executor
		return new Promise(async (resolve, reject) => {
			//resolve paths
			let test = path.resolve(bundle.test);
			let website = bundle.website ? path.resolve(bundle.website) : undefined;
			//check that files exist
			const all_files = [test];
			if(website) {
				all_files.push(website);
			}
			const missing_files = all_files.filter(f => !fs.existsSync(f));
			if(!missing_files.isEmpty()) {
				throw new Error(`Invalid file paths: ${missing_files.join(', ')}`);
			}
			function on_feature(_, feature) {
				console.log('\t', feature.name);
			}
			function on_specification(_, feature, specification) {
				const prefix = specification.success ? '\x1b[32m✔\x1b[0m' : '\x1b[31m❌\x1b[0m';
				console.log('\t\t', prefix, '\x1b[90m', specification.message, '\x1b[0m');
				if(!specification.success) {
					console.log('\t\t\x1b[31m', specification.error, '\x1b[0m');
				}
			}

			//add listeners to bundle
			bundle.addEventListener('begin', () => {
				console.log('-\x1b[36m', `Executing test ${test}`, '\x1b[0m');
			});
			bundle.addEventListener('end', bundle => {
				let message = `Done in ${bundle.getDuration()}ms`;
				message += (` (${bundle.getSpecifications().length} tests, ${bundle.getFailsNumber()} fails)`);
				console.log(' \x1b[36m', message, '\x1b[0m');
				resolve(bundle);
			});
			bundle.addEventListener('feature', on_feature);
			bundle.addEventListener('specification', on_specification);
			//create assert in this context
			const assert = new Assert(this.debug);

			//if bundle requires DOM, run tests in a browser and retrieve report in XML
			//it would be nice to be able to provide a browser environment without launching a browser
			if(bundle.dom || bundle.website) {
				console.info(`Preparing server and browser for test ${test}`);
				//begin fake assert
				bundle.begin();
				//launch HTTP server
				const serve = serve_static(CODE_PATH);
				const server = http.createServer((request, response) => {
					serve(request, response, finalhandler(request, response));
				});
				server.listen(PORT);
				//create browser
				const base_url = `http://localhost:${PORT}`;
				const browser = await puppeteer.launch({
					headless: this.headless ? 'new' : false,
					//devtools: true,
					defaultViewport: null,
					args: [
						'--no-sandbox',
						'--window-size=1600,1200'
					]
				});
				//give some permissions
				const permissions = ['clipboard-read', 'clipboard-write'];
				await browser.defaultBrowserContext().overridePermissions(base_url, permissions);
				//create page
				const page = await browser.newPage();

				//customize download behavior
				//this avoids the popup "Download multiple files" that appears in Chrome when many files are downloaded from the same website in a short period of time
				//this occurs in some tests that check that reports can be downloaded properly
				await page._client().send('Page.setDownloadBehavior', {behavior: 'allow', downloadPath: path.resolve('/tmp')});

				//bring page to front so the current tab becomes active
				//this allows to put the focus in the page and allow clipboard management to work
				//clipboard operations cannot work if the page is not focuses (this is a security measure)
				//this makes sense only when browser is not in headless mode
				if(this.debug) {
					await page.bringToFront();
					console.info('The browser has the focus. Do not remove this focus by clicking outside the test window.');
				}
				//driver.manage().window().maximize();

				//create global functions that will be used by the browser runner to report the result of the assert
				await page.exposeFunction('onFeature', (_, feature) => {
					bundle.features.push(feature);
					on_feature(bundle, feature);
				});
				await page.exposeFunction('onSpecification', (_, feature, specification) => {
					const bundle_feature = bundle.features.find(f => f.id === feature.id);
					bundle_feature.specifications.push(specification);
					on_specification(bundle, feature, specification);
				});

				//manage path for test and website and create bundle
				test = path_to_unix(test.substring(test.indexOf(CODE_PATH) + CODE_PATH.length));
				const custom_bundle = {test: test};
				if(website) {
					website = path_to_unix(website.substring(website.indexOf(CODE_PATH) + CODE_PATH.length));
					custom_bundle.website = website;
				}
				//execute bundle in browser
				try {
					await page.goto(`${base_url}/tests/browser/browser_test.html#bundle=${JSON.stringify(custom_bundle)}`);
					await page.waitForSelector('#report_download', {visible: true, timeout: 60000});
				}
				catch(error) {
					console.trace(error);
					reject(error);
				}
				await browser.close();
				server.close();
				bundle.end();
				console.log(`End of test ${test}, stopping browser and server.`);
			}
			else {
				//create context
				const sandbox = {
					console: console,
					setTimeout: setTimeout
				};
				const context = vm.createContext(sandbox, {name: 'test_context'});
				//load test module
				const options = {
					identifier: test,
					context: context
				};
				const module = new vm.SourceTextModule(fs.readFileSync(test, 'utf-8'), options);
				await module.link(generate_linker(path.dirname(test)));
				await module.evaluate();
				try {
					module.namespace.default(bundle, assert);
				}
				catch(error) {
					console.trace(error);
					reject(error);
				}
			}
		});
	}
}
