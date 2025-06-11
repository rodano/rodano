import { enableProdMode, ErrorHandler, importProvidersFrom } from '@angular/core';

import { environment } from './environments/environment';
import { AppComponent } from './app/app.component';
import { APP_ROUTES } from './app/app-routing.module';
import { BrowserModule, bootstrapApplication } from '@angular/platform-browser';
import { GlobalErrorHandler } from './app/error/global-error-handler';
import { IonicRouteStrategy, IonicModule } from '@ionic/angular';
import { provideRouter, RouteReuseStrategy } from '@angular/router';
import { AuthGuard } from './app/guards/authentication.guard';
import { FieldService } from './app/api/services/field.service';
import { DatasetService } from './app/api/services/dataset.service';
import { EventService } from './app/api/services/event.service';
import { ScopeService } from './app/api/services/scope.service';
import { ConfigurationService } from './app/api/services/configuration.service';
import { MeService } from './app/api/services/me.service';
import { AuthStateService } from './app/services/auth-state.service';
import { AuthService } from './app/api/services/auth.service';
import { APIService } from './app/api/services/api.service';
import { UntypedFormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ErrorService } from './app/services/error.service';
import { AppService } from './app/services/app.service';
import { AuthInterceptor } from './app/auth.interceptor';
import { HTTP_INTERCEPTORS, withInterceptorsFromDi, provideHttpClient } from '@angular/common/http';

if (environment.production) {
	enableProdMode();
}

bootstrapApplication(AppComponent, {
	providers: [
		importProvidersFrom(BrowserModule, FormsModule, ReactiveFormsModule, IonicModule.forRoot()),
		{ provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
		{ provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
		{ provide: ErrorHandler, useClass: GlobalErrorHandler },
		provideHttpClient(withInterceptorsFromDi()),
		provideRouter(APP_ROUTES),
		AppService,
		ErrorService,
		UntypedFormBuilder,
		APIService,
		AuthService,
		AuthStateService,
		MeService,
		ConfigurationService,
		ScopeService,
		EventService,
		DatasetService,
		FieldService,
		AuthGuard
	]
}).catch(err => console.log(err));
