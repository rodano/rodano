import {Route} from '@angular/router';

import {ChangePasswordContext} from './change-password/change-password-context';
import {CRF_PATH} from './crf/crf-routes';
import {ErrorContext} from './error/error-context';
import {AuthGuard} from './guards/authentication.guard';
import {EproEnabledGuard} from './guards/epro-enabled.guard';
import {LoginGuard} from './guards/login.guard';
import {CMSLayoutResolver} from './resolvers/cms-layout-resolver';
import {MeResolver} from './resolvers/me-resolver';

export default [
	{
		path: '',
		redirectTo: '/dashboard', pathMatch: 'full'
	},
	{
		path: 'login',
		canActivate: [LoginGuard],
		loadComponent: () => import('./login/login.component').then(m => m.LoginComponent)
	},
	{
		path: 'bootstrap',
		loadComponent: () => import('./bootstrap/bootstrap.component').then(m => m.BootstrapComponent)
	},
	{
		path: 'register/:registrationCode',
		loadComponent: () => import('./registration/registration.component').then(m => m.RegistrationComponent)
	},
	{
		path: 'dashboard',
		loadChildren: () => import('./dashboard/dashboard-routes')
	},
	{
		path: 'benchmark',
		loadChildren: () => import('./benchmark/benchmark-routes')
	},
	{
		path: 'scopes-search',
		canActivate: [AuthGuard],
		loadChildren: () => import('./search/scopes-search')
	},
	{
		path: CRF_PATH,
		canActivate: [AuthGuard],
		loadChildren: () => import('./crf/crf-routes')
	},
	{
		path: 'users',
		canActivate: [AuthGuard],
		loadChildren: () => import('./user/user-routes')
	},
	{
		path: 'scopes',
		canActivate: [AuthGuard],
		loadChildren: () => import('./scope/scope-routes')
	},
	{
		path: 'robots',
		canActivate: [AuthGuard],
		loadChildren: () => import('./robot/robot-routes')
	},
	{
		path: 'epro',
		canActivate: [AuthGuard, EproEnabledGuard],
		loadChildren: () => import('./epro/epro-routes')
	},
	{
		path: 'extracts',
		canActivate: [AuthGuard],
		loadComponent: () => import('./extract/extract.component').then(m => m.ExtractComponent)
	},
	{
		path: 'documentation',
		canActivate: [AuthGuard],
		loadComponent: () => import('./documentation/documentation.component').then(m => m.DocumentationComponent)
	},
	{
		path: 'resources',
		canActivate: [AuthGuard],
		loadChildren: () => import('./resource/resource-routes')
	},
	{
		path: 'mails',
		canActivate: [AuthGuard],
		loadComponent: () => import('./mails/mail-list/mail-list.component').then(m => m.MailListComponent)
	},
	{
		path: 'send-test-mail',
		canActivate: [AuthGuard],
		loadComponent: () => import('./send-test-mail/send-test-mail.component').then(m => m.SendTestMailComponent)
	},
	{
		path: 'widget/:menuId',
		canActivate: [AuthGuard],
		loadComponent: () => import('./widgets/widget-layout/widget-layout.component').then(m => m.WidgetLayoutComponent),
		resolve: {
			layout: CMSLayoutResolver
		}
	},
	{
		path: 'change-password/system',
		canActivate: [AuthGuard],
		loadComponent: () => import('./change-password/change-password.component').then(m => m.ChangePasswordComponent),
		data: {changeRequestContext: ChangePasswordContext.SYSTEM_REQUEST},
		resolve: {
			me: MeResolver
		}
	},
	{
		path: 'change-password/:recoveryCode',
		loadComponent: () => import('./change-password/change-password.component').then(m => m.ChangePasswordComponent),
		data: {changeRequestContext: ChangePasswordContext.PASSWORD_RESET}
	},
	{
		path: 'recover-password/:resetCode',
		loadComponent: () => import('./change-password/change-password.component').then(m => m.ChangePasswordComponent)
	},
	{
		path: 'recover-account/:recoveryCode',
		loadComponent: () => import('./account-recovery/account-recovery.component').then(m => m.AccountRecoveryComponent)
	},
	{
		path: 'email-verification/:verificationCode',
		loadComponent: () => import('./email-verification/email-verification.component').then(m => m.EmailVerificationComponent)
	},
	{
		path: 'help',
		canActivate: [AuthGuard],
		loadChildren: () => import('./help/help.component').then(m => m.HelpComponent.ROUTES)
	},
	{
		path: 'administration',
		canActivate: [AuthGuard],
		loadChildren: () => import('./administration/administration-routes')
	},
	{
		path: 'error',
		loadComponent: () => import('./error/error.component').then(m => m.ErrorComponent)
	},
	{
		path: '**',
		loadComponent: () => import('./error/error.component').then(m => m.ErrorComponent),
		data: {context: ErrorContext.NOT_FOUND}
	}
] satisfies Route[];
