import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import {CellDTO} from '@core/model/cell-dto';
import {CRFField} from '../models/crf-field';
import {CRFService} from './crf.service';
import {CellVisibilityEvent} from './visibility-event-cell';
import {LayoutVisibilityEvent} from './visibility-event-layout';
import {Operator} from '@core/model/operator';
import {VisibilityCriterionAction} from '@core/model/visibility-criterion-action';

@Injectable({
	providedIn: 'root'
})
export class VisibilityService {
	constructor(
		private crfService: CRFService
	) { }

	//The subjects for the cell and layout visibility actions
	//private criterionStream$ = new Subject<CRFVisibilityCriterion>();

	//subject for the visibility of cells
	private cellVisibilityStream$ = new Subject<CellVisibilityEvent>();

	//subject for the visibility of layouts
	private layoutVisibilityStream$ = new Subject<LayoutVisibilityEvent>();

	//allows for subscribers to receive only the events that are of interest for them
	/*public cellCriterionEvents$(cellId: string, layoutUid?: string): Observable<CRFVisibilityCriterion> {
		return this.criterionStream$.pipe(
			filter(c => c.targetCellIds.includes(cellId) && c.layoutUid === layoutUid)
		);
	}*/

	//allows for subscribers to receive only the events that are of interest for them
	/*public layoutCriterionEvents$(
		layoutId: string
	): Observable<CRFVisibilityCriterion> {
		return this.criterionStream$.pipe(
			filter(c => c.targetLayoutIds.includes(layoutId))
		);
	}*/

	public triggerCriteria(cell: CellDTO, layoutUid: string, field: CRFField): void {
		cell.visibilityCriteria.forEach(criterion => {
			const fieldValues = this.crfService.parseFieldValue(field);
			const criterionValues = this.crfService.typeFieldValues(field.model, criterion.values);
			//check if current value match visibility criterion
			const reverse = !criterionValues.some(criterionValue => {
				return fieldValues.some(fieldValue => {
					//basic criterion implies the EQUALS_TO operator
					const operator = criterion.operator ?? Operator.EQUALS;
					switch(operator.toLowerCase()) {
						case Operator.EQUALS.toLowerCase():
							return fieldValue === criterionValue;
						case Operator.NOT_EQUALS.toLowerCase():
							return fieldValue !== criterionValue;
						case Operator.GREATER.toLowerCase():
							return fieldValue > criterionValue;
						case Operator.GREATER_EQUALS.toLowerCase():
							return fieldValue >= criterionValue;
						case Operator.LOWER.toLowerCase():
							return fieldValue < criterionValue;
						case Operator.LOWER_EQUALS.toLowerCase():
							return fieldValue <= criterionValue;
						default:
							throw new Error('Unknown visibility criterion operator');
					}
				});
			});
			let visible = criterion.action === VisibilityCriterionAction.SHOW;
			visible = reverse ? !visible : visible;

			//trigger visibility consequences to target cells
			criterion.targetCellIds.forEach(c => this.triggerCellVisibilityEvent(c, layoutUid, visible));

			//trigger visibility consequences to target layouts
			criterion.targetLayoutIds.forEach(l => this.triggerLayoutVisibilityEvent(l, visible));
		});
	}

	public triggerCellVisibilityEvent(cellId: string, layoutUid: string, shown: boolean) {
		const event = {
			layoutUid: layoutUid,
			cellId,
			shown
		} satisfies CellVisibilityEvent;
		this.cellVisibilityStream$.next(event);
	}

	public triggerCellsVisibilityEvent(cells: CellDTO[], layoutUid: string, visible: boolean): void {
		cells
			.map(c => c.id)
			.forEach(c => this.triggerCellVisibilityEvent(c, layoutUid, visible));
	}

	public triggerLayoutVisibilityEvent(layoutId: string, shown: boolean) {
		const event = {
			layoutId,
			shown
		} satisfies LayoutVisibilityEvent;
		this.layoutVisibilityStream$.next(event);
	}

	public cellVisibilityEvents$(cellId: string, layoutUid: string): Observable<boolean> {
		return this.cellVisibilityStream$.pipe(
			filter(e => e.cellId === cellId && e.layoutUid === layoutUid),
			map(e => e.shown)
		);
	}

	public layoutVisibilityEvents$(layoutId: string): Observable<boolean> {
		return this.layoutVisibilityStream$.pipe(
			filter(e => e.layoutId === layoutId),
			map(e => e.shown)
		);
	}
}
