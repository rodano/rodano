import {defineConfig} from 'vitest/config';

export default defineConfig({
	test: {
		globals: true,
		environment: 'jsdom',
		reporters: [
			'verbose',
			['junit', {suiteName: 'UI tests', outputFile: './test-output/results.xml'}]
		]
	}
});
