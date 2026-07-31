import {ErrorHandler, importProvidersFrom, provideZonelessChangeDetection} from '@angular/core';
import {AppComponent} from './app/app.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {bootstrapApplication} from '@angular/platform-browser';
import {MatDialogModule} from '@angular/material/dialog';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS} from '@angular/material/form-field';
import {CustomDateAdapter} from './app/utils/custom-date-adapter';
import {DateAdapter, MatNativeDateModule} from '@angular/material/core';
import {GlobalErrorHandler} from './app/error/global-error-handler';
import {provideRouter, withComponentInputBinding} from '@angular/router';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MAT_ICON_DEFAULT_OPTIONS} from '@angular/material/icon';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import appRoutes from './app/app-routes';
import {authInterceptor} from './app/auth.interceptor';

bootstrapApplication(AppComponent, {
	providers: [
		//the snackbar and the dialog are used programmatically, so their providers must be available for injection
		//the datepicker and the native date module are required by our custom date management
		importProvidersFrom(MatSnackBarModule, MatDialogModule, MatDatepickerModule, MatNativeDateModule),
		{provide: ErrorHandler, useClass: GlobalErrorHandler},
		//customization of Material Design components
		//only customize properties that are not related to styling here
		//customization of styling should be done using SCSS rules in the styles.scss file, in the :root section
		{provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {appearance: 'outline', floatLabel: 'auto', subscriptSizing: 'dynamic'}},
		//use the class defined in the styles.scss file for Material Symbols
		{provide: MAT_ICON_DEFAULT_OPTIONS, useValue: {fontSet: 'material-symbols'}},
		{provide: DateAdapter, useClass: CustomDateAdapter},
		provideZonelessChangeDetection(),
		provideHttpClient(withInterceptors([authInterceptor])),
		provideRouter(appRoutes, withComponentInputBinding())
	]
}).catch(err => console.error(err));
