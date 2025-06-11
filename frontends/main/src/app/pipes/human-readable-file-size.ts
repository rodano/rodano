import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'humanReadableFileSize'
})
export class HumanReadableFileSizePipe implements PipeTransform {
	transform(size: number): string {
		const units = [
			'B',
			'kB',
			'MB',
			'GB',
			'TB'
		];
		let unitIndex = 0;
		let unitSize = size;
		while(unitSize > 1024 && unitIndex < units.length) {
			unitIndex++;
			unitSize = Math.round(unitSize / 1024);
		}
		return `${unitSize}${units[unitIndex]}`;
	}
}
