module.exports = function(config) {
	config.set({
		basePath: '',
		frameworks: ['jasmine', '@angular-devkit/build-angular'],
		plugins: [
			require('karma-jasmine'),
			require('karma-chrome-launcher'),
			require('karma-htmlfile-reporter'),
			require('@angular-devkit/build-angular/plugins/karma')
		],
		client: {
			clearContext: false // leave Jasmine Spec Runner output visible in browser
		},
		reporters: ['html'],
		port: 9876,
		colors: true,
		logLevel: config.LOG_ERROR,
		autoWatch: false,
		singleRun: true,
		browserNoActivityTimeout: 10000,
		htmlReporter: {
			outputFile: '../test-report.html',
			groupSuites: true,
			useCompactStyle: true,
			useLegacyStyle: true,
			showOnlyFailed: false
		},
		browsers: ['ChromeHeadlessCI'],
		customLaunchers: {
			ChromeHeadlessCI: {
				base: 'ChromeHeadless',
				flags: ['--no-sandbox']
			},
		}
	});
};
