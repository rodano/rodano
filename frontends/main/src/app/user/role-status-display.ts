import {RoleStatus} from '@core/model/role-status';

export {RoleStatus};

export interface RoleStatusDisplay {
	icon: string;
	status: string;
}

export const ROLE_STATUS_DISPLAY: Record<RoleStatus, RoleStatusDisplay> = {
	ENABLED: {
		icon: 'check_circle',
		status: 'Enabled'
	},
	DISABLED: {
		icon: 'no_accounts',
		status: 'Disabled'
	},
	PENDING: {
		icon: 'pending',
		status: 'Pending'
	}
};

//This function is used to get the display value for a given role status
//it ease the use of the RoleStatusDisplay object in the templates
export function getRoleStatusDisplay(status: RoleStatus): RoleStatusDisplay {
	return ROLE_STATUS_DISPLAY[status];
}
