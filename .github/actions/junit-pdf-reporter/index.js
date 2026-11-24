import {execFileSync} from 'node:child_process';
import path from 'node:path';
import generate_pdf from './generate_pdf.js';

function git(...args) {
	return execFileSync('git', args, {encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore']}).trim();
}

//identify the tested code with its commit, preceded by its tag when the commit is tagged
function get_version() {
	try {
		const [tag] = git('tag', '--points-at', 'HEAD', '--sort=-v:refname').split('\n');
		return [tag, git('rev-parse', 'HEAD')].filter(Boolean).join(' ');
	}
	catch {
		return '';
	}
}

const [report_path = path.join('test-output', 'results.xml'), pdf_path] = process.argv.slice(2);

await generate_pdf(report_path, pdf_path ?? path.join(path.dirname(report_path), `${path.basename(report_path, '.xml')}.pdf`), get_version());
