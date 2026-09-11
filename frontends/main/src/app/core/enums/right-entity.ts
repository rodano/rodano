/**
 * Entities that can be the target of a required right (matches the backend's RightAssignable implementations)
 */
export enum RightEntity {
	SCOPE_MODEL = 'SCOPE_MODEL',
	EVENT_MODEL = 'EVENT_MODEL',
	FORM_MODEL = 'FORM_MODEL',
	DATASET_MODEL = 'DATASET_MODEL',
	PROFILE = 'PROFILE'
}
