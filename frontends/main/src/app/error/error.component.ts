import {Component, Input} from '@angular/core';
import {ErrorContext} from './error-context';

@Component({
	templateUrl: './error.component.html',
	styleUrls: ['./error.component.css']
})
export class ErrorComponent {
	ErrorContext = ErrorContext;
	@Input() context: ErrorContext;
}
