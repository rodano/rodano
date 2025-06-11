import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {DateComponent} from './date.component';
import {CRF_FIELD_DATE_OF_WITHDRAWAL} from 'src/test/stubs';
import {DateAdapter, MAT_DATE_FORMATS, MAT_NATIVE_DATE_FORMATS} from '@angular/material/core';
import {CustomDateAdapter} from 'src/app/utils/custom-date-adapter';
import {provideNoopAnimations} from '@angular/platform-browser/animations';

describe('DateComponent', () => {
	let component: DateComponent;
	let fixture: ComponentFixture<DateComponent>;

	beforeEach(waitForAsync(() => {
		TestBed.configureTestingModule({
			imports: [DateComponent],
			providers: [
				provideNoopAnimations(),
				{provide: DateAdapter, useClass: CustomDateAdapter}, {provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS}
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(DateComponent);
		component = fixture.componentInstance;

		//provide the field input
		component.field = CRF_FIELD_DATE_OF_WITHDRAWAL;
		component.disabled = false;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
