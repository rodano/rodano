import {Injectable} from '@angular/core';
import {environment} from 'src/environments/environment';

enum LogLevel {
	INFO = 'INFO',
	ERROR = 'ERROR'
}

@Injectable({
	providedIn: 'root'
})
export class LoggingService {
	info(...args: any[]): void {
		this.log(LogLevel.INFO, args);
	}

	error(...args: any[]): void {
		this.log(LogLevel.ERROR, args);
	}

	log(level: LogLevel, args: any []): void {
		if(!environment.production) {
			switch(level) {
				case LogLevel.INFO: {
					console.info(...args);
					break;
				}
				case LogLevel.ERROR: {
					console.error(...args);
					break;
				}
			}
		}
	}
}
