import * as path from 'path';
import * as fs from 'fs';
//@ts-ignore
import archiver from 'archiver';
import {ListFiles} from './list_files.js';

const ROOT_FOLDER = 'www';
const FILTERS = [/^experiments/, /^tests/, /\.test\.js$/, /\.demo\.html$/, /\.ts$/, /^\./];

//generate bundle name from version found in package.json
console.log('Retrieving version...');
const description = JSON.parse(fs.readFileSync('package.json', 'utf-8'));
const version = description.version;
console.log(`Found that version is ${version}`);
const bundle_name = `v${version}.zip`;

console.log('Generating file list...');
const filenames = ListFiles(ROOT_FOLDER, FILTERS).sort();
console.log(`Found ${filenames.length} files`);

console.log('Creating bundle...');
const result = fs.createWriteStream(bundle_name);
const archive = archiver('zip');
archive.pipe(result);

console.log('Adding files...');
filenames.forEach(filename => {
	const filepath = path.join(ROOT_FOLDER, filename);
	archive.append(fs.createReadStream(filepath), {name: filename});
});

archive.finalize();

console.log('Done!');
