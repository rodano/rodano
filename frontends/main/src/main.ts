import {ErrorHandler, importProvidersFrom, provideZonelessChangeDetection} from '@angular/core';
import {AppComponent} from './app/app.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {bootstrapApplication} from '@angular/platform-browser';
import {MAT_TABS_CONFIG} from '@angular/material/tabs';
import {MatDialogModule} from '@angular/material/dialog';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS} from '@angular/material/form-field';
import {CustomDateAdapter} from './app/utils/custom-date-adapter';
import {DateAdapter, MatNativeDateModule} from '@angular/material/core';
import {authInterceptor} from './app/interceptors/auth.interceptor';
import {withInterceptors, provideHttpClient} from '@angular/common/http';
import {GlobalErrorHandler} from './app/services/error/global-error-handler.service';
import {provideRouter, TitleStrategy, withComponentInputBinding} from '@angular/router';
import {StudyTitleStrategy} from './app/core/services/study-title-strategy';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {provideCharts, withDefaultRegisterables} from 'ng2-charts';
import {MAT_ICON_DEFAULT_OPTIONS} from '@angular/material/icon';
import appRoutes from './app/app-routes';

bootstrapApplication(AppComponent, {
	providers: [
		//We need to import the providers from snackbar and dialog modules because they are used programmatically and must be injected into components and services.
		//We also need to import the datepicker and the native date module for our custom date management.
		importProvidersFrom(MatSnackBarModule, MatDialogModule, MatDatepickerModule, MatNativeDateModule),
		{provide: ErrorHandler, useClass: GlobalErrorHandler},
		//customization of Material Design components
		//only customize properties that are not related to styling here
		//customization of styling should be done using SCSS rules in the styles.scss file, in the :root section
		{provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {appearance: 'outline', floatLabel: 'auto', subscriptSizing: 'dynamic'}},
		{provide: MAT_TABS_CONFIG, useValue: {animationDuration: '0'}},
		//use the class defined in the styles.scss file for Material Symbols
		{provide: MAT_ICON_DEFAULT_OPTIONS, useValue: {fontSet: 'material-symbols'}},
		//TODO a CustomDateAdapter may not be needed anymore at native adapters can be configured to use UTC
		//see here https://material.angular.io/components/datepicker/overview#customizing-the-parse-and-display-formats
		{provide: DateAdapter, useClass: CustomDateAdapter},
		provideZonelessChangeDetection(),
		provideHttpClient(withInterceptors([authInterceptor])),
		provideRouter(appRoutes, withComponentInputBinding()),
		{provide: TitleStrategy, useClass: StudyTitleStrategy},
		provideCharts(withDefaultRegisterables())
	]
}).catch(err => console.error(err));
