import {ErrorHandler} from '@angular/core';
import {AppComponent} from './app/app.component';
import {bootstrapApplication} from '@angular/platform-browser';
import {GlobalErrorHandler} from './app/error/global-error-handler';
import {IonicRouteStrategy, provideIonicAngular} from '@ionic/angular/standalone';
import {provideRouter, RouteReuseStrategy} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {registerIcons} from './app/icons';
import appRoutes from './app/app-routes';
import {authInterceptor} from './app/auth.interceptor';

registerIcons();

bootstrapApplication(AppComponent, {
	providers: [
		provideIonicAngular(),
		{provide: RouteReuseStrategy, useClass: IonicRouteStrategy},
		{provide: ErrorHandler, useClass: GlobalErrorHandler},
		provideHttpClient(withInterceptors([authInterceptor])),
		provideRouter(appRoutes)
	]
}).catch(err => console.log(err));
