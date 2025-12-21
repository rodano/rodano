import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
	selector: 'app-logo',
	standalone: true,
	imports: [CommonModule],
	template: `
		<div class="logo-container" [class.large]="size === 'large'">
      <span class="logo-icon">
        <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
          <path d="M100,83.92c-.43.05-.87.07-1.32.07-6.66,0-12.14-4.97-12.97-11.4v-2.95h0c0-.07,0-.14,0-.21,0-7.31-3.04-13.92-7.95-18.68,5.56-5.59,9.11-13.34,9.11-21.25C86.87,14.46,73.66.07,58.92,0h0s-58.92,0-58.92,0v17.42h58.82c6.89,0,10.61,6.99,10.61,12.31s-4.61,11.97-10.61,11.97h-31.44C12.96,42.39,1.12,53.89.01,68.18h-.01v31.82h17.44v-31.71c.71-4.95,4.89-8.76,9.94-9.19v.02h30.52c5.83.79,10.43,5.33,11.18,11.09v4.45h.04c.67,14.11,12.33,25.35,26.63,25.35,1.45,0,2.86-.12,4.25-.34v-15.73Z" fill="white"/>
        </svg>
      </span>
			<span class="logo-text">odano</span>
		</div>
	`,
	styles: [`
		.logo-container {
			display: flex;
			align-items: center;
			gap: 3px;
			font-weight: 500;
		}

		.logo-icon {
			display: inline-flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		.logo-icon svg {
			height: 30px;
			width: 30px;
			display: block;
		}

		.logo-text {
			font-size: 25px;
			color: white;
			font-weight: 500;
			letter-spacing: 1px;
		}

		.logo-container.large .logo-icon svg {
			height: 40px;
			width: 40px;
		}

		.logo-container.large .logo-text {
			font-size: 35px;
		}
	`]
})
export class LogoComponent {
	@Input() size: 'normal' | 'large' = 'normal';
}
