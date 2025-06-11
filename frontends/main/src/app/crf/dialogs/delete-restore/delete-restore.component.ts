import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatError, MatFormField} from '@angular/material/form-field';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';

@Component({
	selector: 'app-delete-restore',
	templateUrl: './delete-restore.component.html',
	styleUrls: ['./delete-restore.component.css'],
	imports: [
		MatDialogModule,
		ReactiveFormsModule,
		MatFormField,
		MatError,
		MatInput,
		MatButton
	]
})
export class DeleteRestoreComponent {
	rationale = new FormControl<string>('', {nonNullable: true, validators: [Validators.required]});

	constructor(
		@Inject(MAT_DIALOG_DATA) public deletion: boolean
	) { }
}
