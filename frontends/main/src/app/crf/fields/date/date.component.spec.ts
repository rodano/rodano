import {beforeEach, describe, it, expect} from 'vitest';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DateComponent} from './date.component';
import {CRF_FIELD_DATE_OF_WITHDRAWAL} from 'src/test/stubs';
import {DateAdapter, MAT_DATE_FORMATS, MAT_NATIVE_DATE_FORMATS} from '@angular/material/core';
import {CustomDateAdapter} from 'src/app/utils/custom-date-adapter';

describe('DateComponent', () => {
	let component: DateComponent;
	let fixture: ComponentFixture<DateComponent>;

	beforeEach(async () => {
		TestBed.configureTestingModule({
			imports: [DateComponent],
			providers: [
				{provide: DateAdapter, useClass: CustomDateAdapter}, {provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS}
			]
		}).compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(DateComponent);
		component = fixture.componentInstance;

		//provide the field input
		fixture.componentRef.setInput('field', CRF_FIELD_DATE_OF_WITHDRAWAL);
		fixture.componentRef.setInput('disabled', false);
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
