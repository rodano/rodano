import * as fs from 'fs';
import * as path from 'path';

/**
 *
 * @param {string} root_folder - The root folder used as a starting point for the listing
 * @param {RegExp[]} filters - A list of regular expressions used as filters on file paths (file paths relative to the root folder)
 * @returns {string[]} - The list of file paths in the root folder
 */
function ListFiles(root_folder, filters) {
	function list_files(folder) {
		return fs.readdirSync(folder).flatMap(file => {
			const filepath = path.join(folder, file);
			const stats = fs.statSync(filepath);
			if(stats.isDirectory()) {
				return list_files(filepath);
			}
			const relative_filepath = path.relative(root_folder, filepath);
			if(filters.some(r => r.test(relative_filepath))) {
				return [];
			}
			return [relative_filepath];
		});
	}
	return list_files(root_folder);
}

export {ListFiles};
