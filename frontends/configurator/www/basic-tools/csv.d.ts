export class CSV {
	static MIME_TYPE: string;
	constructor(data: Array<Array<string>>);
	toString(): string;
	toBlob(): Blob;
	download(filename: string);
	static parse(string: string, limit: number): Array<Array<string>>;
	static parseHeader(string: string): Array<string>;
	static parseToDictionary(string: string): Array<{key: string, value: string}>;
}
