export class Loader {
	constructor(doc, parameters) {
		//DOMDocument: document where scripts must be loaded
		this.document = doc || document;
		//String: base url for scripts
		this.url = undefined;
		//Boolean: add a timestamp after each script to avoid browser cache
		this.nocache = true;
		//bind parameters
		for(const parameter in parameters) {
			this[parameter] = parameters[parameter];
		}
	}
	buildUrl(url) {
		let full_url = '';
		if(this.url) {
			full_url += this.url;
		}
		full_url += url;
		//append timestamp at the end of the url to avoid cache if required
		if(this.nocache) {
			full_url += `?${new Date().getTime()}`;
		}
		return full_url;
	}
	loadJavascript(js, type) {
		const js_url = this.buildUrl(js);
		const that = this;
		return new Promise(function(resolve, reject) {
			//check javascript has not already been included
			if(!that.document.head.querySelector(`script[type="${type}"][src^="${js_url}"]`)) {
				//create script element
				const script = that.document.createElement('script');
				script.setAttribute('type', type);
				script.setAttribute('src', js_url);
				script.addEventListener('load', resolve);
				script.addEventListener('error', reject);
				that.document.head.appendChild(script);
			}
			else {
				resolve();
			}
		});
	}
	loadLibrary(library) {
		return this.loadJavascript(library, 'text/javascript');
	}
	loadModule(mod) {
		return this.loadJavascript(mod, 'module');
	}
	loadQueuedLibraries(libraries) {
		return libraries.reduce((a, l) => a.then(this.loadLibrary.bind(this, l)), Promise.resolve());
	}
	loadConcurrentLibraries(libraries) {
		return Promise.all(libraries.map(l => this.loadLibrary(l)));
	}
	loadCSS(css) {
		const css_url = this.buildUrl(css);
		const that = this;
		return new Promise(function(resolve, reject) {
			//check library has not already been included
			if(!that.document.head.querySelector(`link[type="text/css"][href^="${css_url}"]`)) {
				//create link element
				const link = that.document.createElement('link');
				link.setAttribute('type', 'text/css');
				link.setAttribute('rel', 'stylesheet');
				link.setAttribute('href', css_url);
				link.addEventListener('load', resolve);
				link.addEventListener('error', reject);
				that.document.head.appendChild(link);
			}
			else {
				resolve();
			}
		});
	}
	loadHTML(html, container) {
		const html_url = this.buildUrl(html);
		const that = this;
		return new Promise(function(resolve, reject) {
			const xhr = new XMLHttpRequest();
			xhr.addEventListener(
				'load',
				function() {
					if(this.status === 200) {
						const node = that.document.importNode(this.response.body.firstElementChild, true);
						container.appendChild(node);
						resolve(node);
					}
					else {
						reject();
					}
				}
			);
			xhr.responseType = 'document';
			xhr.open('GET', html_url, true);
			xhr.send();
		});
	}
	//when loading a template, template node is put in the "head" element
	loadHTMLTemplate(html, container) {
		const html_url = this.buildUrl(html);
		const that = this;
		return new Promise(function(resolve, reject) {
			const xhr = new XMLHttpRequest();
			xhr.addEventListener(
				'load',
				function() {
					if(this.status === 200) {
						const node = that.document.importNode(this.response.head.firstElementChild, true);
						container.appendChild(node);
						resolve();
					}
					else {
						reject();
					}
				}
			);
			xhr.responseType = 'document';
			xhr.open('GET', html_url, true);
			xhr.send();
		});
	}
}
