import * as path from 'path';
import * as fs from 'fs';
import {sync} from 'rimraf';

//custom node modules
import {Suite} from '../www/tools/suite.js';
import {Bundle} from '../www/tools/bundle.js';
import {Runner} from './runner.js';
import reporter from './reporter.js';

//check arguments
if(process.argv.length < 3) {
	console.log('Usage: test.js <directories|suite_files>');
	console.log('Usage: test.js --dependency=dependency --dependency=dependency --test=test --dom');
	process.exit();
}

//global variables
const TEST_OUTPUT = path.resolve('test-output');
const RESULT_FILENAME = path.resolve(TEST_OUTPUT, 'results.xml');
const SUITE_PATTERN = /(.*)\.suite\.json/i;

//retrieve current version
const VERSION = JSON.parse(fs.readFileSync(path.resolve('package.json'), {encoding: 'utf-8'})).version;

//prepare report folder
if(fs.existsSync(TEST_OUTPUT)) {
	sync(TEST_OUTPUT);
}
//prepare report folder
fs.mkdirSync(TEST_OUTPUT);

function normalize_path(file_path) {
	return file_path.startsWith('/') ? file_path : path.resolve(process.cwd(), file_path);
}

//analyse arguments
const debug_mode = process.argv.includes('--debug');
const no_headless_mode = process.argv.includes('--no-headless');
const single_test_mode = process.argv.some(arg => arg.startsWith('--test'));

//create runner with good parameters
const runner = new Runner(debug_mode, !no_headless_mode);

let suites;

if(single_test_mode) {
	//retrieve test
	const test = normalize_path(process.argv.find(arg => arg.startsWith('--test')).substring('--test'.length + 1));
	//retrieve website
	let website = process.argv.find(arg => arg.startsWith('--website'));
	if(website) {
		website = normalize_path(website.substring('--website'.length + 1));
	}
	//retrieve parameter that say if test needs the dom
	const dom = !!website || process.argv.includes('--dom');
	//build temporary suite
	const suite = new Suite('Temporary suite', undefined, [new Bundle(dom, website, test)]);
	suites = [suite];
}
else {
	//retrieve suites from arguments
	const arg_paths = process.argv.slice(2).filter(arg => !arg.startsWith('--'));
	//retrieve full path depending on if argument starts with a "/"
	const full_paths = arg_paths.map(normalize_path);

	//retrieve all suites files from paths
	const suite_files = full_paths.flatMap(full_path => {
		try {
			const stats = fs.statSync(full_path);
			if(stats.isDirectory()) {
				console.log(`Scanning directory ${full_path}`);
				return scan_directory(full_path);
			}
			return [full_path];
		}
		catch {
			console.error('\t\x1b[31m', `Path ${full_path} does not exist`, '\x1b[0m');
			process.exit(1);
			return undefined; //please ESLint
		}
	});

	//transform suites files in suites objects
	suites = suite_files.map(file => {
		try {
			const suite_definition = JSON.parse(fs.readFileSync(file, {encoding: 'utf-8'}));
			const base_path = `${path.resolve(path.dirname(file))}/`;
			return Suite.fromJSON(suite_definition, base_path);
		}
		catch {
			console.error('\t\x1b[31m', `Invalid suite file ${file}: suite file must be a JSON file. If you want to execute a single test, use --test=${file}`, '\x1b[0m');
			process.exit(1);
			return undefined; //please ESLint
		}
	});
}

//execute suites
suites.reduce(run_and_reduce_suite, Promise.resolve()).then(function() {
	const report = reporter('KVConfig tests', suites);
	//add version
	report.root().att({'version': VERSION});
	fs.writeFileSync(RESULT_FILENAME, report.end({prettyPrint: true}));
	//set error return code if test has failures
	if(suites.some(s => s.getFailsNumber() > 0)) {
		process.exit(1);
	}
}).catch(reason => {
	console.error('\t\x1b[31m', `Error while executing tests: ${reason}`, '\x1b[0m');
	console.log(reason);
	process.exit(1);
});

function run_and_reduce_suite(accumulator, suite) {
	return accumulator.then(function() {
		console.log('-\x1b[33m', `Run suite ${suite.name}`, '\x1b[0m');
		return suite.run(runner);
	});
}

function scan_directory(directory) {
	const name = directory.substring(directory.lastIndexOf('/') + 1);
	//skipping hidden folders
	if(name[0] === '.' && name[1] && name[1] !== '/') {
		console.log(`Skipping ${directory}`);
		return [];
	}
	//scan directory
	return fs.readdirSync(directory).flatMap(filename => {
		const deeper_path = path.join(directory, filename);
		const stats = fs.statSync(deeper_path);
		//go deeper
		if(stats.isDirectory()) {
			return scan_directory(deeper_path);
		}
		//test files matching pattern
		else if(SUITE_PATTERN.test(filename)) {
			return [deeper_path];
		}
		return [];
	});
}
