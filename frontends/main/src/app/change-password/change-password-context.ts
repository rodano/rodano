export enum ChangePasswordContext {
	PASSWORD_RESET = 'passwordResetRequest', //User forgot its password, went through the recovery procedure
	USER_REQUEST = 'userChangeRequest', //User wants to change its password
	SYSTEM_REQUEST = 'systemChangeRequest' //System requests to change password
}
