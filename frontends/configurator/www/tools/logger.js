export class Logger {
	constructor(onlog) {
		this.debugging = false;
		this.logs = [];
		this.onlog = onlog;
	}
	clear() {
		this.logs.length = 0;
	}
	debug(debug) {
		this.debugging = debug;
	}
	log(level, message) {
		const log = {level: level, message: message, date: new Date()};
		this.logs.push(log);
		//manage callback
		if(this.onlog) {
			this.onlog.call(undefined, log);
		}
		//write to console in debug mode
		if(this.debugging) {
			if(level <= 3) {
				console.info(message);
			}
			else if(level <= 6) {
				console.warn(message);
			}
			else {
				console.error(message);
			}
		}
	}
	error(message) {
		this.log(8, message);
	}
	warning(message) {
		this.log(5, message);
	}
	info(message) {
		this.log(2, message);
	}
	getLogs(level) {
		return this.logs.filter(log => log.level >= level);
	}
}
