import * as os from 'os';
import {create} from 'xmlbuilder2';

const sum_callback = (previous, current) => previous + current;

function contribute(report, suite) {
	const attributes = {
		name: suite.name,
		hostname: os.hostname(),
		failures: suite.getFailsNumber(),
		tests: suite.getSpecifications().length,
		time: (suite.getDuration() / 1000).toString(),
		timestamp: suite.beginTime.toISOString()
	};
	const test_suite = report.root().ele('testsuite', attributes);
	//add properties
	const properties = test_suite.ele('properties');
	properties.ele('property', {'name': 'platform', 'value': os.platform()});
	properties.ele('property', {'name': 'os.name', 'value': os.hostname()});
	//add test cases
	suite.bundles.flatMap(b => b.features).forEach(feature => {
		const prefix = feature.name;
		feature.specifications.forEach(specification => {
			const test_case = test_suite.ele('testcase', {'name': `${prefix}: ${specification.message}`});
			if(!specification.success) {
				test_case.ele('failure', {'message': specification.error});
			}
		});
	});
}

export default function(name, suites) {
	//generate report from bundle asserts
	const report = create({version: '1.0'});
	const attributes = {
		name: name,
		failures: suites.map(s => s.getFailsNumber()).reduce(sum_callback),
		tests: suites.map(s => s.getSpecifications().length).reduce(sum_callback),
		time: (suites.map(s => s.getDuration()).reduce(sum_callback) / 1000).toString()
	};
	report.ele('testsuites', attributes);
	suites.map(s => contribute(report, s));
	return report;
}
