import {Component, OnInit, signal} from '@angular/core';
import {Router} from '@angular/router';
import {Validators, ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {Profile} from '@core/model/profile';
import {ConfigurationService} from '@core/services/configuration.service';
import {RobotService} from '@core/services/robot.service';
import {RobotCreation} from '@core/model/robot-creation';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {ArraySortPipe} from '../../pipes/sort-array.pipe';
import {ScopeFinderComponent} from '../../scope-finder/scope-finder.component';
import {NotificationService} from '../../services/notification.service';

@Component({
	templateUrl: './robot-create.component.html',
	styleUrls: ['./robot-create.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatLabel,
		MatHint,
		MatInput,
		MatSelect,
		MatOption,
		MatButton,
		LocalizeMapPipe,
		ArraySortPipe,
		ScopeFinderComponent
	]
})
export class RobotCreateComponent implements OnInit {
	robotCreationForm = new FormGroup({
		name: new FormControl('', [Validators.required]),
		key: new FormControl(''),
		role: new FormGroup({
			profileId: new FormControl('', [Validators.required]),
			scopePk: new FormControl<number | null>(null, [Validators.required])
		})
	});

	readonly profiles = signal<Profile[]>([]);
	readonly error = signal<string | undefined>(undefined);

	constructor(
		private router: Router,
		private configurationService: ConfigurationService,
		private robotService: RobotService,
		private notificationService: NotificationService
	) {}

	ngOnInit() {
		this.configurationService.getProfiles().subscribe(p => this.profiles.set(p));
	}

	save() {
		const robotCreation = this.robotCreationForm.value as RobotCreation;

		this.robotService.create(robotCreation).subscribe({
			next: robot => {
				this.notificationService.showSuccess('Robot created');
				this.router.navigate(['/robots', robot.pk]);
			},
			error: e => this.error.set(e.error.message)
		});
	}
}
