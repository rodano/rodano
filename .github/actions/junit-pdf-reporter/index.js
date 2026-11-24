import path from 'path';
import generate_pdf from './generate_pdf.js';

const args = process.argv.slice(2);
if(args.length < 1) {
	console.error('Usage: node index.js [path/to/report] [path/to/pdf]');
}

const report_path = args.length > 0 ? args[0] : path.join(process.cwd(), 'test-output', 'results.xml');
const pdf_path = args.length > 1 ? args[1] : path.join(path.dirname(report_path), `${path.basename(report_path, '.xml')}.pdf`);

generate_pdf(report_path, pdf_path);
