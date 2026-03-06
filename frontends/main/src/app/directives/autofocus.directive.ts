import {AfterViewInit, Directive, ElementRef} from '@angular/core';

@Directive({
	standalone: true,
	selector: '[appAutofocus]'
})
export class AutofocusDirective implements AfterViewInit {
	constructor(private readonly el: ElementRef<HTMLElement>) {}

	ngAfterViewInit(): void {
		this.el.nativeElement.focus();
	}
}
