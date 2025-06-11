import * as fs from 'fs';
import {ListFiles} from './list_files.js';

const ROOT_FOLDER = 'www';
const FILTERS = [/^experiments/, /^tests/, /\.test\.js$/, /\.demo\.html$/, /\.ts$/, /^\./];

const WORKER_CACHE = `${ROOT_FOLDER}/worker_cache.js`;
const BEGINNING_LOOKUP_TEXT = '//BEGINNING OF THE LIST';
const END_LOOKUP_TEXT = '//END OF THE LIST';

console.log('Generating file list...');
const filenames = ListFiles(ROOT_FOLDER, FILTERS).sort();
console.log(`Found ${filenames.length} files`);

//generate a readable file list
let text = JSON.stringify(filenames, undefined, '\t');
text = `const FILES = ${text}`;
text = text.replaceAll('"', '\''); //json use double quotes
text = text.replaceAll('\\\\', '/'); //on windows, separator is a double backslash
text += ';';

//read file as text and update text
let content = fs.readFileSync(WORKER_CACHE, {encoding: 'utf-8'});

const beginning_index = content.indexOf(BEGINNING_LOOKUP_TEXT);
const end_index = content.indexOf(END_LOOKUP_TEXT);

//remove previous list
content = content.slice(0, beginning_index + BEGINNING_LOOKUP_TEXT.length + 1) + content.slice(end_index);

//insert new list
content = content.replace(BEGINNING_LOOKUP_TEXT, `${BEGINNING_LOOKUP_TEXT}\n${text}`);

//patch file list in cache worker
console.log('Patching cache worker...');
fs.writeFileSync(WORKER_CACHE, content, {encoding: 'utf-8'});

console.log('Done!');
