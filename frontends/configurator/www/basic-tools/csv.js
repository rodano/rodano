class CSV {
	constructor(data) {
		this.data = data;
	}
	toString() {
		return this.data.map(generate_line).join(CSV.DELIMITER_LINE);
	}
	toBlob() {
		return new Blob([this.toString()], {type: CSV.MIME_TYPE});
	}
	download(name) {
		const filename = name || new Date().toFullDisplay();
		const blob = this.toBlob();
		const file = new File([blob], filename, {type: CSV.MIME_TYPE, lastModified: Date.now()});
		const url = URL.createObjectURL(file);
		//Chrome does not support to set location href
		if(/Chrome/.test(navigator.userAgent)) {
			const link = document.createFullElement('a', {href: url, download: filename});
			//add link in the current document to be able to test the download
			//if the link is not included in the document, there is no way to detect if it has been "used" (created and programmatically clicked) in tests
			document.body.appendChild(link); //this line is only for tests to be able to detect the click on the link
			const event = new MouseEvent('click', {bubbles: true, cancelable: true});
			link.dispatchEvent(event);
			//remove link because it is useless
			link.remove();
		}
		else {
			location.href = url;
		}
		//revoke url after event has been dispatched
		setTimeout(() => URL.revokeObjectURL(url), 0);
	}
	static parse(string, limit) {
		const lines = [];
		let columns = [];
		let column = '';
		//store if this is the beginning of a column
		let beginning = true;
		//store if the parser is currently reading the text of a column (in this mode, column and line delimiters are not considered)
		let reading_text = false;
		//store if the previous character was a quote to be able to detect double quotes that are used to quote text in a column
		let quote_before = false;
		for(let i = 0; i < string.length; i++) {
			const character = string[i];
			//manage quotes
			if(character === CSV.CHARACTER_QUOTER) {
				if(quote_before) {
					column += CSV.CHARACTER_QUOTER;
					quote_before = false;
					reading_text = true;
				}
				else {
					quote_before = true;
					reading_text = beginning;
				}
				continue;
			}
			quote_before = false;
			beginning = false;
			//manage column and line delimiters only if not reading text
			if(!reading_text) {
				if(character === CSV.DELIMITER_COLUMN || character === CSV.DELIMITER_LINE) {
					//end current column
					columns.push(column);
					column = '';
					beginning = true;
					if(character === CSV.DELIMITER_LINE) {
						//end current line
						lines.push(columns);
						//return if enough lines have been collected
						if(limit && lines.length >= limit) {
							return lines;
						}
						columns = [];
					}
					continue;
				}
				//do not accept some characters outside quotes
				if(CSV.INVALID_CHARACTERS.includes(character)) {
					continue;
				}
			}
			column += character;
		}
		//file may end without any delimiter
		if(column || columns.length > 0) {
			columns.push(column);
			lines.push(columns);
		}
		return lines;
	}
	static parseHeader(string) {
		return CSV.parse(string, 1)[0];
	}
	static parseToDictionary(string) {
		const data = CSV.parse(string);
		//remove header line
		const header = data.shift();
		return data.map(line => {
			return Object.fromEntries(header.map((header, index) => [header, line[index]]));
		});
	}
}

CSV.INVALID_CHARACTERS = ['\r'];
CSV.MIME_TYPE = 'text/csv';
CSV.DELIMITER_LINE = '\n';
CSV.DELIMITER_COLUMN = ',';
CSV.CHARACTER_QUOTER = '"';

const regexp = new RegExp(CSV.CHARACTER_QUOTER, 'g');

function generate_line(line) {
	return line
		.map(c => c || '')
		.map(c => c.replace(regexp, `${CSV.CHARACTER_QUOTER}${CSV.CHARACTER_QUOTER}`))
		.map(c => `${CSV.CHARACTER_QUOTER}${c}${CSV.CHARACTER_QUOTER}`)
		.join(CSV.DELIMITER_COLUMN);
}

export {CSV};
