import {enableProdMode, ErrorHandler, importProvidersFrom} from '@angular/core';
import {environment} from './environments/environment';
import {AppComponent} from './app/app.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {APP_ROUTES} from './app/app-routes';
import {provideAnimations} from '@angular/platform-browser/animations';
import {bootstrapApplication} from '@angular/platform-browser';
import {FileService} from '@core/services/file.service';
import {ReportService} from '@core/services/report.service';
import {ExtractService} from '@core/services/extract.service';
import {WidgetService} from '@core/services/widget.service';
import {EproService} from '@core/services/epro.service';
import {UserService} from '@core/services/user.service';
import {ActivationService} from '@core/services/activation.service';
import {FormService} from '@core/services/form.service';
import {EventService} from '@core/services/event.service';
import {ScopeService} from '@core/services/scope.service';
import {AuthService} from '@core/services/auth.service';
import {MeService} from '@core/services/me.service';
import {AdministrationService} from '@core/services/administration.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {SessionService} from '@core/services/session.service';
import {APIService} from '@core/services/api.service';
import {HttpParamsService} from '@core/services/http-params.service';
import {AppService} from './app/services/app.service';
import {MAT_TABS_CONFIG} from '@angular/material/tabs';
import {MatDialogModule} from '@angular/material/dialog';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS} from '@angular/material/form-field';
import {CustomDateAdapter} from './app/utils/custom-date-adapter';
import {DateAdapter, MatNativeDateModule} from '@angular/material/core';
import {AuthInterceptor} from './app/interceptors/auth.interceptor';
import {HTTP_INTERCEPTORS, withInterceptorsFromDi, provideHttpClient} from '@angular/common/http';
import {GlobalErrorHandler} from './app/services/error/global-error-handler.service';
import {provideRouter, withComponentInputBinding, withRouterConfig} from '@angular/router';
import {ActuatorService} from '@core/services/actuator.service';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {provideCharts, withDefaultRegisterables} from 'ng2-charts';
import {AuthStateService} from './app/services/auth-state.service';
import {MAT_ICON_DEFAULT_OPTIONS} from '@angular/material/icon';

if(environment.production) {
	enableProdMode();
}

bootstrapApplication(AppComponent, {
	providers: [
		//We need to import the providers from snackbar and dialog modules because they are used programmatically and must be injected into components and services.
		//We also need to import the datepicker and the native date module for our custom date management.
		importProvidersFrom(MatSnackBarModule, MatDialogModule, MatDatepickerModule, MatNativeDateModule),
		{provide: ErrorHandler, useClass: GlobalErrorHandler},
		{provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
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
		AppService,
		HttpParamsService,
		APIService,
		SessionService,
		ConfigurationService,
		AdministrationService,
		MeService,
		AuthStateService,
		AuthService,
		ScopeService,
		EventService,
		FormService,
		ActivationService,
		UserService,
		EproService,
		WidgetService,
		ExtractService,
		ReportService,
		FileService,
		ActuatorService,
		provideAnimations(),
		provideHttpClient(withInterceptorsFromDi()),
		provideRouter(APP_ROUTES, withComponentInputBinding(), withRouterConfig({paramsInheritanceStrategy: 'always'})), provideCharts(withDefaultRegisterables())
	]
}).catch(err => console.error(err));
