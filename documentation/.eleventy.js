import {EleventyHtmlBasePlugin} from '@11ty/eleventy';
import {readFileSync} from 'fs';
import {basename, join} from 'path';

const SOURCE_FOLDER = 'src';

function formatDatePart(number) {
	return number.toString().padStart(2, '0');
}

function generateDrawioAddress(filename) {
	const title = basename(filename, '.drawio');
	const path = `${process.cwd()}/${filename}`;
	const content = readFileSync(path, 'utf-8').toString();
	const encodedContent = encodeURIComponent(content);
	return `https://viewer.diagrams.net/?title=${title}#R${encodedContent}`;
}

//read the application version from the backend pom.xml so it is always in sync with the build
function readVersion() {
	const content = readFileSync(join(process.cwd(), '..', 'backend', 'pom.xml'), 'utf-8').toString();
	//match the first <version> element, which is the project version (it comes before the <parent> block)
	const match = content.match(/<version>([^<]+)<\/version>/);
	return match ? match[1] : 'unknown';
}

export default function(config) {
	config.addPlugin(EleventyHtmlBasePlugin);

	config.addPassthroughCopy(`${SOURCE_FOLDER}/css`);
	config.addPassthroughCopy(`${SOURCE_FOLDER}/fonts`);
	config.addPassthroughCopy(`${SOURCE_FOLDER}/images`);
	config.addPassthroughCopy(`${SOURCE_FOLDER}/js`);

	const fqdn = 'docs.rodano.ch';
	config.addGlobalData('fqdn', fqdn);
	config.addGlobalData('url', `https://${fqdn}`);
	config.addGlobalData('rodano', 'Rodano');
	config.addGlobalData('version', readVersion());

	config.addFilter('isoDate', date => {
		return `${date.getFullYear()}-${formatDatePart(date.getMonth() + 1)}-${formatDatePart(date.getDate())}`;
	});

	config.addShortcode('drawio', generateDrawioAddress);

	return {
		templateFormats: [
			'njk', 'html'
		],
		dir: {
			input: SOURCE_FOLDER
		}
	};
}
