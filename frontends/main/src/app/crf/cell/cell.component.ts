import {Component, Input, DestroyRef, OnInit, AfterViewInit} from '@angular/core';
import {CellDTO} from '@core/model/cell-dto';
import {VisibilityService} from '../services/visibility.service';
import {CellLoadingService} from '../services/cell-loading.service';
import {CRFField} from '../models/crf-field';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {FieldComponent} from '../fields/field/field.component';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {LoggingService} from '@core/services/logging.service';
import {FieldUpdateService} from '../services/field-update.service';
import {EmptyObjectCheck} from 'src/app/utils/empty-object-check';
import {merge} from 'rxjs';
import {SafeHtmlPipe} from 'src/app/pipes/safe-html.pipe';

@Component({
	selector: 'app-cell',
	templateUrl: './cell.component.html',
	styleUrls: ['./cell.component.scss'],
	imports: [
		LocalizeMapPipe,
		SafeHtmlPipe,
		FieldComponent
	]
})
export class CellComponent implements OnInit, AfterViewInit {
	@Input() cell: CellDTO;
	//layoutUid is the global identifier of the parent layout
	//it is required for cells that are in a multiple layout and do not contain a field
	//they must be identified properly to manage visibility criteria
	@Input() layoutUid: string;
	@Input() field?: CRFField;
	@Input() disabled: boolean;

	shown = true;

	constructor(
		private visibilityService: VisibilityService,
		private cellLoadingService: CellLoadingService,
		private fieldUpdateService: FieldUpdateService,
		private loggingService: LoggingService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		/*this.visibilityService.cellCriterionEvents$(this.cell.id, this.layoutUid).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(criterion => {
			this.loggingService.info(`Cell ${this.cell.id} receiving criterion`, criterion);
			const show = criterion.action.toLocaleLowerCase() === VisibilityCriteriaDTO.ActionEnum.SHOW.toLocaleLowerCase();
			this.shown = criterion.reverse ? !show : show;

			//mark the field
			if(this.field) {
				this.field.shown = this.shown;
			}

			//trigger nested visibility criterion to the cells inside the layout

			/*if(!this.shown) {
				//this.triggerHide();
			}
			else {
				if(this.field?.value) {
					this.triggerCriteria();
				}
			}*/
		//});

		this.visibilityService.cellVisibilityEvents$(this.cell.id, this.layoutUid).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(shown => {
			this.loggingService.info(`Cell ${this.cell.id} receiving visibility event containing ${shown}`);
			this.shown = shown;
			//mark the field
			if(this.field) {
				this.field.shown = this.shown;
			}
			//if the cell becomes visible, its visibility criteria must be re-triggered
			if(shown) {
				this.triggerCriteria();
			}
			//if the cell is hidden its targets must be hidden as well
			else {
				this.cell.visibilityCriteria.forEach(criterion => {
					criterion.targetCellIds.forEach(c => this.visibilityService.triggerCellVisibilityEvent(c, this.layoutUid, false));
					criterion.targetLayoutIds.forEach(l => this.visibilityService.triggerLayoutVisibilityEvent(l, false));
				});
			}
		});

		if(this.field) {
			//visibility criteria are triggered when all the cells are loaded or when the field is updated
			merge(
				this.fieldUpdateService.cellFieldUpdated$(this.field),
				this.cellLoadingService.allCellsLoaded$
			).pipe(
				takeUntilDestroyed(this.destroyRef)
			).subscribe(() => {
				this.triggerCriteria();
			});
		}
	}

	//consider cell to be loaded when this hook is triggered by Angular
	//do not run this in an ngOnChanges, because in that case the cell is already loaded and ready to receive visibility events
	ngAfterViewInit() {
		this.cellLoadingService.cellLoadingComplete(this.cell.id);
	}

	isEmptyObject(object: any): boolean {
		return EmptyObjectCheck.isEmptyObject(object);
	}

	triggerCriteria() {
		//only cell containing fields may trigger visibility criteria
		if(this.field) {
			this.visibilityService.triggerCriteria(this.cell, this.layoutUid, this.field);
		}
	}
}
