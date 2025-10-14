import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SearchOverallComponent} from './search-overall.component';

describe('SearchOverallComponent', () => {
	let component: SearchOverallComponent;
	let fixture: ComponentFixture<SearchOverallComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [SearchOverallComponent]
		})
			.compileComponents();

		fixture = TestBed.createComponent(SearchOverallComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
