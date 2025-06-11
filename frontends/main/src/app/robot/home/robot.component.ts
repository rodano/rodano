import {Component, OnInit, Input} from '@angular/core';
import {RouterLink, Router} from '@angular/router';
import {RobotDTO} from '@core/model/robot-dto';
import {Validators, ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {ProfileDTO} from '@core/model/profile-dto';
import {Observable} from 'rxjs';
import {ConfigurationService} from '@core/services/configuration.service';
import {PagedResultScopeDTO} from '@core/model/paged-result-scope-dto';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {RobotService} from '@core/services/robot.service';
import {NotificationService} from 'src/app/services/notification.service';
import {RobotCreationDTO} from '@core/model/robot-creation-dto';
import {MatIcon} from '@angular/material/icon';
import {RobotUpdateDTO} from '@core/model/robot-update-dto';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {MatTooltip} from '@angular/material/tooltip';
import {ArraySortPipe} from 'src/app/pipes/sort-array.pipe';
import {ScopeFinderComponent} from 'src/app/scope-finder/scope-finder.component';
import {RoleCreationDTO} from '@core/model/role-creation-dto';

@Component({
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
	@Input() robot?: RobotDTO;
	roleForm = new FormGroup({
		profileId: new FormControl('', [Validators.required]),
		scopePk: new FormControl<number | null>(null, [Validators.required])
	});

	robotForm = new FormGroup<{name: FormControl; key: FormControl; roleForm?: FormGroup}>({
		name: new FormControl('', [Validators.required]),
		key: new FormControl(''),
		roleForm: this.roleForm
	});

	profiles: ProfileDTO[];

	scopeResult$: Observable<PagedResultScopeDTO>;
	errorText: string;

	constructor(
		private router: Router,
		private configurationService: ConfigurationService,
		private robotService: RobotService,
		private notificationService: NotificationService
	) {}

	ngOnInit() {
		this.configurationService.getProfiles().subscribe(p => this.profiles = p);
		if(this.robot) {
			this.robotForm.removeControl('roleForm');
			this.robotForm.reset(this.robot);
		}
		else {
			this.robotForm.addControl('roleForm', this.roleForm);
			this.robotForm.reset();
		}
	}

	save() {
		if(this.robot) {
			const robotUpdate = this.robotForm.value as RobotUpdateDTO;
			this.robotService.save(this.robot.pk, robotUpdate).subscribe({
				next: robot => {
					Object.assign(this.robot as RobotDTO, robot);
					this.notificationService.showSuccess('Robot saved');
				},
				error: e => this.errorText = e.error.message
			});
		}
		else {
			const robotCreation = {
				name: this.robotForm.controls.name.value,
				key: this.robotForm.controls.key.value,
				role: this.roleForm.value as RoleCreationDTO
			} as RobotCreationDTO;

			this.robotService.create(robotCreation).subscribe({
				next: r => {
					this.notificationService.showSuccess('Robot created');
					this.router.navigate(['/robots', r.pk]);
				},
				error: e => this.errorText = e.error.message
			});
		}
	}
}
