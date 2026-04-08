import {ChangeDetectionStrategy, Component, OnInit, model, signal} from '@angular/core';
import {RouterLink, Router} from '@angular/router';
import {Robot} from '@core/model/robot';
import {Validators, ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {Profile} from '@core/model/profile';
import {Observable} from 'rxjs';
import {ConfigurationService} from '@core/services/configuration.service';
import {PagedResultScope} from '@core/model/paged-result-scope';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {RobotService} from '@core/services/robot.service';
import {NotificationService} from '../../services/notification.service';
import {RobotCreation} from '@core/model/robot-creation';
import {MatIcon} from '@angular/material/icon';
import {RobotUpdate} from '@core/model/robot-update';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {MatTooltip} from '@angular/material/tooltip';
import {ArraySortPipe} from '../../pipes/sort-array.pipe';
import {ScopeFinderComponent} from '../../scope-finder/scope-finder.component';
import {RoleCreation} from '@core/model/role-creation';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './robot.component.html',
	styleUrls: ['./robot.component.css'],
	imports: [
		RouterLink,
		ReactiveFormsModule,
		MatFormField,
		MatLabel,
		MatHint,
		MatInput,
		MatTooltip,
		MatSelect,
		MatOption,
		MatAutocompleteModule,
		MatButton,
		MatIcon,
		MatIconButton,
		LocalizeMapPipe,
		ArraySortPipe,
		AuditTrailButtonComponent,
		ScopeFinderComponent
	]
})
export class RobotComponent implements OnInit {
	readonly robot = model<Robot>();
	roleForm = new FormGroup({
		profileId: new FormControl('', [Validators.required]),
		scopePk: new FormControl<number | null>(null, [Validators.required])
	});

	robotForm = new FormGroup<{name: FormControl; key: FormControl; roleForm?: FormGroup}>({
		name: new FormControl('', [Validators.required]),
		key: new FormControl(''),
		roleForm: this.roleForm
	});

	readonly profiles = signal<Profile[]>([]);

	scopeResult$: Observable<PagedResultScope>;
	readonly error = signal<string | undefined>(undefined);

	constructor(
		private router: Router,
		private configurationService: ConfigurationService,
		private robotService: RobotService,
		private notificationService: NotificationService
	) {}

	ngOnInit() {
		this.configurationService.getProfiles().subscribe(p => this.profiles.set(p));
		if(this.robot()) {
			this.robotForm.removeControl('roleForm');
			this.robotForm.reset(this.robot());
		}
		else {
			this.robotForm.addControl('roleForm', this.roleForm);
			this.robotForm.reset();
		}
	}

	save() {
		if(this.robot()) {
			const robotUpdate = this.robotForm.value as RobotUpdate;
			this.robotService.save(this.robot()!.pk, robotUpdate).subscribe({
				next: robot => {
					this.robot.set(robot);
					this.notificationService.showSuccess('Robot saved');
				},
				error: e => this.error.set(e.error.message)
			});
		}
		else {
			const robotCreation = {
				name: this.robotForm.controls.name.value,
				key: this.robotForm.controls.key.value,
				role: this.roleForm.value as RoleCreation
			} as RobotCreation;

			this.robotService.create(robotCreation).subscribe({
				next: robot => {
					this.notificationService.showSuccess('Robot created');
					this.router.navigate(['/robots', robot.pk]);
				},
				error: e => this.error.set(e.error.message)
			});
		}
	}
}
