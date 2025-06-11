import {Component, OnInit} from '@angular/core';
import {StudyDTO} from '@core/model/study-dto';
import {ConfigurationService} from '@core/services/configuration.service';

@Component({
	templateUrl: './support.component.html',
	styleUrls: ['./support.component.css']
})
export class SupportComponent implements OnInit {
	study?: StudyDTO;

	constructor(
		public configurationService: ConfigurationService
	) {}

	ngOnInit() {
		this.configurationService.getStudy().subscribe(study => this.study = study);
	}
}
