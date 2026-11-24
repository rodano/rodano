import fs from 'fs';
import path from 'path';
import http from 'http';
import {fileURLToPath} from 'url';
import puppeteer from 'puppeteer';

const PORT = 30000;
const STYLESHEET = '<?xml-stylesheet type="text/xsl" href="/report.xsl"?>';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default async function generate_pdf(report_path, pdf_path) {
	console.log(`Generating PDF report from ${report_path}...`);

	console.log('Loading XSL file in memory...');
	const xsl = fs.readFileSync(path.join(__dirname, 'junit.xsl'));

	console.log('Adding stylesheet to report in memory...');
	const report_content = fs.readFileSync(report_path, 'utf8');
	const report_content_lines = report_content.split('\n');
	report_content_lines.splice(1, 0, STYLESHEET);
	const updated_report_content = report_content_lines.join('\n');

	console.log('Launching HTTP server...');
	const server = http.createServer((request, response) => {
		response.writeHead(200);
		if(request.url === '/report.xsl') {
			response.end(xsl);
		}
		else {
			response.end(updated_report_content);
		}
	});
	server.listen(PORT);

	console.log('Launching browser...');
	const browser = await puppeteer.launch({
		headless: 'new',
		defaultViewport: null,
		args: [
			'--no-sandbox'
		]
	});

	const page = await browser.newPage();
	//it's important to wait for network to let custom fonts the time to be loaded
	await page.goto(`http://localhost:${PORT}`, {waitUntil: 'networkidle2'});

	console.log(`Exporting PDF report ${pdf_path}...`);
	const header = '<p style="font-size: 8px; color: black; margin: auto; text-align: center;"><span class="title"></span> - <span class="date"></span></p>';
	const footer = '<footer style="font-size: 8px; color: black; margin: auto; text-align: center;"><span class="pageNumber" ></span> of <span class="totalPages"></span><footer>';
	await page.pdf({
		path: pdf_path,
		displayHeaderFooter: true,
		format: 'A4',
		margin: {top: '1.5cm', bottom: '1.5cm', left: '0.5cm', right: '0.5cm'},
		headerTemplate: header,
		footerTemplate: footer
	});

	await browser.close();
	server.close();
	console.log('Done.');
}
