import {Component, input} from '@angular/core';
import {ErrorContext} from './error-context';

@Component({
	templateUrl: './error.component.html',
	styleUrls: ['./error.component.css']
})
export class ErrorComponent {
	ErrorContext = ErrorContext;

	readonly context = input.required<ErrorContext>();
}
