const sum_callback = (previous, current) => previous + current;

function contribute(report, suite) {
	const test_suite = report.createFullElement('testsuite', {
		name: suite.name,
		hostname: window.navigator.userAgent,
		failures: suite.getFailsNumber(),
		tests: suite.getSpecifications().length,
		time: suite.getDuration() / 1000,
		timestamp: suite.beginTime.toISOString()
	});
	report.documentElement.appendChild(test_suite);
	//add properties
	const properties = report.createElement('properties');
	test_suite.appendChild(properties);
	properties.appendChild(report.createFullElement('property', {name: 'platform', value: window.navigator.platform}));
	properties.appendChild(report.createFullElement('property', {name: 'os.name', value: window.navigator.userAgent}));
	//add test cases
	suite.getSpecifications().map(specification => {
		const test_case = report.createFullElement('testcase', {name: specification.message || ''});
		if(!specification.success) {
			test_case.appendChild(report.createFullElement('failure', {message: specification.error}));
		}
		return test_case;
	}).forEach(Node.prototype.appendChild, test_suite);
}

export default function(name, suites) {
	//generate report from bundle asserts
	const report = document.implementation.createDocument(null, 'testsuites', null);
	//add information
	report.documentElement.setAttribute('name', name);
	report.documentElement.setAttribute('failures', suites.map(s => s.getFailsNumber()).reduce(sum_callback));
	report.documentElement.setAttribute('tests', suites.map(s => s.getSpecifications().length).reduce(sum_callback));
	report.documentElement.setAttribute('time', (suites.map(s => s.getDuration()).reduce(sum_callback) / 1000).toString());
	suites.map(s => contribute(report, s));
	return report;
}
