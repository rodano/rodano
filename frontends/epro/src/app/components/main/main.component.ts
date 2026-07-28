import {Component} from '@angular/core';
import {IonIcon, IonLabel, IonTabBar, IonTabButton, IonTabs} from '@ionic/angular/standalone';

@Component({
	templateUrl: './main.component.html',
	styleUrls: ['./main.component.css'],
	standalone: true,
	imports: [
		IonIcon,
		IonLabel,
		IonTabBar,
		IonTabButton,
		IonTabs
	]
})
export class MainComponent {
}
