import {Directive, HostBinding, Input} from '@angular/core';

@Directive({
	selector: '[appModified]',
	standalone: true
})
export class ModifiedDirective {
	@Input('appModified') isModified = false;

	@HostBinding('class.modified')
	get modifiedClass(): boolean {return this.isModified;}

	@HostBinding('style.border-top-color')
	get borderColor(): string {return this.isModified ? '#f59e0b' : '';}
}
