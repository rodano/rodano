import {readFile} from 'node:fs/promises';
import path from 'node:path';
import puppeteer from 'puppeteer';
import {Xslt, XmlParser} from 'xslt-processor';

const HEADER = '<p style="font-size: 8px; color: black; margin: auto; text-align: center;"><span class="title"></span> - <span class="date"></span></p>';
const FOOTER = '<footer style="font-size: 8px; color: black; margin: auto; text-align: center;"><span class="pageNumber"></span> of <span class="totalPages"></span></footer>';

export default async function generate_pdf(report_path, pdf_path, version = '') {
	console.log(`Generating PDF report from ${report_path}...`);
	const [report, stylesheet] = await Promise.all([
		readFile(report_path, 'utf8'),
		readFile(path.join(import.meta.dirname, 'junit.xsl'), 'utf8')
	]);

	console.log('Transforming report to HTML...');
	const parser = new XmlParser();
	const html = await new Xslt({parameters: [{name: 'version', value: version}]}).xsltProcess(parser.xmlParse(report), parser.xmlParse(stylesheet));

	console.log('Launching browser...');
	//the sandbox cannot start on runners that restrict unprivileged user namespaces
	const browser = await puppeteer.launch({args: ['--no-sandbox']});
	try {
		const page = await browser.newPage();
		//wait for the network to be idle to let custom fonts load
		await page.setContent(html, {waitUntil: 'networkidle0'});

		console.log(`Exporting PDF report ${pdf_path}...`);
		await page.pdf({
			path: pdf_path,
			displayHeaderFooter: true,
			format: 'A4',
			margin: {top: '1.5cm', bottom: '1.5cm', left: '0.5cm', right: '0.5cm'},
			headerTemplate: HEADER,
			footerTemplate: FOOTER
		});
	}
	finally {
		await browser.close();
	}
	console.log('Done.');
}
