import path from 'node:path';
import generate_pdf from './generate_pdf.js';

const [report_path = path.join('test-output', 'results.xml'), pdf_path] = process.argv.slice(2);

await generate_pdf(report_path, pdf_path ?? path.join(path.dirname(report_path), `${path.basename(report_path, '.xml')}.pdf`));
