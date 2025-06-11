export class Loader {
	constructor(HTMLDocument, any?);
	loadLibrary(library: string): Promise<void>;
	loadModule(mod: string): Promise<void>;
	loadCSS(css: string): Promise<void>;
	loadHTML(html: string, container: Node): Promise<void>;
	loadHTMLTemplate(html: string, container: Node): Promise<void>;
	loadQueuedLibraries(libraries: Array<string>): Promise<void>;
	loadConcurrentLibraries(libraries: Array<string>): Promise<Array<void>>;
}
