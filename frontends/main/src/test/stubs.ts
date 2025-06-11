import {CRFDataset} from 'src/app/crf/models/crf-dataset';
import {CRFField} from 'src/app/crf/models/crf-field';
import {DatasetModelDTO} from '@core/model/dataset-model-dto';
import {FieldModelDTO} from '@core/model/field-model-dto';
import {FieldDTO} from '@core/model/field-dto';
import {DatasetDTO} from '@core/model/dataset-dto';
import {FormDTO} from '@core/model/form-dto';
import {FormModelDTO} from '@core/model/form-model-dto';
import {UserDTO} from '@core/model/user-dto';

export const FIELD_MODEL_DATE_OF_WITHDRAWAL = {
	type: 'DATE',
	dataType: 'DATE',
	datasetModelId: 'VISIT_DOCUMENTATION',
	id: 'DATE_OF_WITHDRAWAL',
	shortname: {
		en: 'Date of withdrawal'
	},
	longname: {},
	description: {},
	inlineHelp: 'dd.mm.yyyy',
	advancedHelp: {},
	possibleValues: [],
	dictionary: '',
	readOnly: false,
	order: 20,
	searchable: false,
	required: true,
	maxLength: 10,
	withYears: true,
	withMonths: true,
	withDays: true,
	withHours: false,
	withMinutes: false,
	withSeconds: false,
	minValue: undefined,
	maxValue: undefined,
	allowDateInFuture: false,
	yearsMandatory: false,
	monthsMandatory: false,
	daysMandatory: false,
	hoursMandatory: false,
	minutesMandatory: false,
	secondsMandatory: false,
	minYear: undefined,
	maxYear: undefined,
	dynamic: false,
	exportable: false
} satisfies FieldModelDTO;

export const FIELD_DATE_OF_WITHDRAWAL = {
	scopePk: 8,
	scopeId: '75c432c5-e538-4252-9d97-4f95b6d7f494',
	scopeCodeAndShortname: 'AT-01-01',
	eventPk: 2,
	eventId: 'VISIT_6_MONTHS',
	eventShortname: 'EventDTO 6 months - 24.10.2020',
	datasetPk: 16,
	datasetId: 'f566b84f-892c-450e-84f0-98f946e52322',
	datasetModelId: 'VISIT_DOCUMENTATION',
	pk: 89,
	model: FIELD_MODEL_DATE_OF_WITHDRAWAL,
	modelId: FIELD_MODEL_DATE_OF_WITHDRAWAL.id,
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	possibleValues: [],
	value: '24.10.2020',
	valueLabel: '',
	newContent: false,
	workflowStatuses: [],
	possibleWorkflows: [],
	fileName: '',
	inRemoved: false,
	inLocked: false
} satisfies FieldDTO;

export const CRF_FIELD_DATE_OF_WITHDRAWAL = {
	...FIELD_DATE_OF_WITHDRAWAL,
	error: undefined,
	shown: true
} satisfies CRFField;

const FIELD_MODEL_DATE_OF_VISIT = {
	type: 'DATE',
	dataType: 'DATE',
	datasetModelId: 'VISIT_DOCUMENTATION',
	id: 'DATE_OF_VISIT',
	shortname: {
		en: 'Date of event-dto'
	},
	longname: {},
	description: {},
	inlineHelp: 'dd.mm.yyyy',
	advancedHelp: {},
	possibleValues: [],
	dictionary: '',
	readOnly: false,
	order: 0,
	searchable: false,
	required: true,
	maxLength: 10,
	withYears: true,
	withMonths: true,
	withDays: true,
	withHours: false,
	withMinutes: false,
	withSeconds: false,
	minValue: undefined,
	maxValue: undefined,
	allowDateInFuture: false,
	yearsMandatory: false,
	monthsMandatory: false,
	daysMandatory: false,
	hoursMandatory: false,
	minutesMandatory: false,
	secondsMandatory: false,
	minYear: undefined,
	maxYear: undefined,
	dynamic: false,
	exportable: false
} satisfies FieldModelDTO;

const FIELD_DATE_OF_VISIT = {
	scopePk: 8,
	scopeId: '75c432c5-e538-4252-9d97-4f95b6d7f494',
	scopeCodeAndShortname: 'AT-01-01',
	eventPk: 2,
	eventId: 'VISIT_6_MONTHS',
	eventShortname: 'EventDTO 6 months - 24.10.2020',
	datasetPk: 16,
	datasetId: 'f566b84f-892c-450e-84f0-98f946e52322',
	datasetModelId: 'VISIT_DOCUMENTATION',
	pk: 86,
	model: FIELD_MODEL_DATE_OF_VISIT,
	modelId: FIELD_MODEL_DATE_OF_VISIT.id,
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	possibleValues: [],
	value: undefined,
	valueLabel: '',
	newContent: false,
	workflowStatuses: [],
	possibleWorkflows: [],
	inRemoved: false,
	inLocked: false
} satisfies FieldDTO;

const CRF_FIELD_DATE_OF_VISIT = {
	...FIELD_DATE_OF_VISIT,
	error: undefined,
	shown: true
} satisfies CRFField;

const DATASET_MODEL_VISIT_DOCUMENTATION = {
	id: 'VISIT_DOCUMENTATION',
	shortname: {
		en: 'Event documentation'
	},
	fieldModels: [FIELD_MODEL_DATE_OF_VISIT, FIELD_MODEL_DATE_OF_WITHDRAWAL],
	multiple: false,
	exportable: true,
	scopeDocumentation: false,
	canWrite: true
} satisfies DatasetModelDTO;

export const DATASET_VISIT_DOCUMENTATION = {
	scopePk: 8,
	scopeId: '75c432c5-e538-4252-9d97-4f95b6d7f494',
	scopeCodeAndShortname: 'AT-01-01',
	eventPk: 2,
	eventShortname: 'Event 6 months - 24.10.2020',
	pk: 16,
	id: 'f566b84f-892c-450e-84f0-98f946e52322',
	model: DATASET_MODEL_VISIT_DOCUMENTATION,
	modelId: DATASET_MODEL_VISIT_DOCUMENTATION.id,
	removed: false,
	fields: [FIELD_DATE_OF_VISIT, FIELD_DATE_OF_WITHDRAWAL],
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	inRemoved: false,
	inLocked: false,
	canWrite: true,
	canBeRemoved: true
} satisfies DatasetDTO;

export const CRF_DATASET_VISIT_DOCUMENTATION = {
	...DATASET_VISIT_DOCUMENTATION,
	expanded: false,
	show: true,
	rationale: undefined,
	fields: [CRF_FIELD_DATE_OF_VISIT, CRF_FIELD_DATE_OF_WITHDRAWAL]
} satisfies CRFDataset;

const FIELD_MODEL_SELF_CARE = {
	type: 'RADIO',
	dataType: 'STRING',
	datasetModelId: 'EQ5D',
	id: 'SELF_CARE',
	shortname: {
		en: 'Self-care'
	},
	longname: {},
	description: {},
	inlineHelp: undefined,
	advancedHelp: {},
	possibleValues: [
		{
			id: 'NO_PROBLEM',
			shortname: {
				en: 'I have no problems washing or dressing myself'
			},
			specify: false
		},
		{
			id: 'SLIGHT_PROBLEMS',
			shortname: {
				en: 'I have slight problems washing or dressing myself'
			},
			specify: false
		},
		{
			id: 'MODERATE_PROBLEMS',
			shortname: {
				en: 'I have moderate problems washing or dressing myself'
			},
			specify: false
		},
		{
			id: 'SEVERE_PROBLEMS',
			shortname: {
				en: 'I have severe problems washing or dressing myself'
			},
			specify: false
		},
		{
			id: 'UNABLE',
			shortname: {
				en: 'I am unable to wash or dress myself'
			},
			specify: false
		}
	],
	readOnly: false,
	order: 5,
	searchable: false,
	required: false,
	maxLength: undefined,
	minValue: undefined,
	maxValue: undefined,
	withYears: true,
	withMonths: true,
	withDays: true,
	withHours: false,
	withMinutes: false,
	withSeconds: false,
	allowDateInFuture: false,
	yearsMandatory: false,
	monthsMandatory: false,
	daysMandatory: false,
	hoursMandatory: false,
	minutesMandatory: false,
	secondsMandatory: false,
	minYear: undefined,
	maxYear: undefined,
	dynamic: false,
	exportable: false
} satisfies FieldModelDTO;

const FIELD_MODEL_HEALTH = {
	type: 'NUMBER',
	dataType: 'NUMBER',
	datasetModelId: 'EQ5D',
	id: 'HEALTH',
	shortname: {
		en: 'We would like to know how good or bad your health is today from 0 to 100'
	},
	longname: {},
	description: {},
	inlineHelp: undefined,
	advancedHelp: {
		en: 'This scale is numbered from 0 to 100.\n100 means the best health you can imagine.\n0 means the worst health you can imagine.'
	},
	possibleValues: [],
	readOnly: false,
	order: 25,
	searchable: false,
	required: false,
	maxLength: 3,
	maxIntegerDigits: 3,
	maxDecimalDigits: 0,
	minValue: 0,
	maxValue: 100,
	withYears: true,
	withMonths: true,
	withDays: true,
	withHours: false,
	withMinutes: false,
	withSeconds: false,
	allowDateInFuture: false,
	yearsMandatory: false,
	monthsMandatory: false,
	daysMandatory: false,
	hoursMandatory: false,
	minutesMandatory: false,
	secondsMandatory: false,
	minYear: undefined,
	maxYear: undefined,
	dynamic: false,
	exportable: false
} satisfies FieldModelDTO;

const FIELD_HEALTH = {
	scopeId: 'AT-01',
	scopePk: 8,
	scopeCodeAndShortname: 'AT-01-01',
	eventPk: 2,
	eventId: 'VISIT_6_MONTHS',
	eventShortname: 'Event 6 months - 24.10.2020',
	datasetPk: 17,
	datasetId: 'f566b84f-892c-450e-84f0-98f946e52322',
	datasetModelId: 'EQ5D',
	pk: 110,
	model: FIELD_MODEL_HEALTH,
	modelId: FIELD_MODEL_HEALTH.id,
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	possibleValues: [],
	value: undefined,
	valueLabel: '',
	newContent: false,
	workflowStatuses: [],
	possibleWorkflows: [],
	inRemoved: false,
	inLocked: false
} satisfies FieldDTO;

const CRF_FIELD_HEALTH = {
	...FIELD_HEALTH,
	error: undefined,
	shown: true
} satisfies CRFField;

const FIELD_SELF_CARE = {
	scopeId: 'AT-01',
	scopePk: 8,
	scopeCodeAndShortname: 'AT-01-01',
	eventPk: 2,
	eventId: 'VISIT_6_MONTHS',
	eventShortname: 'Event 6 months - 24.10.2020',
	datasetPk: 17,
	datasetId: 'f566b84f-892c-450e-84f0-98f946e52322',
	datasetModelId: 'EQ5D',
	pk: 106,
	model: FIELD_MODEL_SELF_CARE,
	modelId: FIELD_MODEL_SELF_CARE.id,
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	possibleValues: [],
	value: undefined,
	valueLabel: '',
	newContent: false,
	workflowStatuses: [],
	possibleWorkflows: [],
	inRemoved: false,
	inLocked: false
} satisfies FieldDTO;

const CRF_FIELD_SELF_CARE = {
	...FIELD_SELF_CARE,
	error: undefined,
	shown: true
} satisfies CRFField;

const DATASET_MODEL_EQ5D = {
	id: 'EQ5D',
	shortname: {
		en: 'EQ5D'
	},
	fieldModels: [FIELD_MODEL_HEALTH, FIELD_MODEL_SELF_CARE],
	multiple: false,
	exportable: true,
	scopeDocumentation: false,
	expandedLabelPattern: '',
	collapsedLabelPattern: '',
	canWrite: true
} satisfies DatasetModelDTO;

export const DATASET_EQ5D = {
	scopePk: 8,
	scopeId: '75c432c5-e538-4252-9d97-4f95b6d7f494',
	scopeCodeAndShortname: 'AT-01-01',
	eventPk: 2,
	eventShortname: 'Event 6 months - 24.10.2020',
	pk: 17,
	id: 'f566b84f-892c-450e-84f0-98f946e52322',
	model: DATASET_MODEL_EQ5D,
	modelId: DATASET_MODEL_EQ5D.id,
	removed: false,
	fields: [FIELD_HEALTH, FIELD_SELF_CARE],
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	inRemoved: false,
	inLocked: false,
	canWrite: true,
	canBeRemoved: true
} satisfies DatasetDTO;

export const CRF_DATASET_EQ5D = {
	...DATASET_EQ5D,
	expanded: false,
	show: true,
	rationale: '',
	fields: [CRF_FIELD_HEALTH, CRF_FIELD_SELF_CARE]
} satisfies CRFDataset;

const DATASET_MODEL = {
	id: 'DATASET_MODEL_1',
	shortname: {en: 'Dataset model 1'},
	fieldModels: [],
	multiple: false,
	exportable: true,
	scopeDocumentation: false,
	canWrite: true
} satisfies DatasetModelDTO;

export const DATASET = {
	scopePk: 2,
	scopeId: '75c432c5-e538-4252-9d97-4f95b6d7f494',
	scopeCodeAndShortname: 'AT-01-01',
	eventPk: 2,
	eventShortname: 'Event 6 months - 24.10.2020',
	pk: 18,
	id: 'f05f433e-ce16-433d-8a28-7d194228b8fe',
	model: DATASET_MODEL,
	modelId: DATASET_MODEL.id,
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	fields: [],
	removed: false,
	inRemoved: false,
	inLocked: false,
	canWrite: true,
	canBeRemoved: true
} satisfies DatasetDTO;

export const CRF_DATASET = {
	...DATASET,
	expanded: false,
	show: true,
	rationale: undefined
} satisfies CRFDataset;

const FORM_MODEL = {
	id: 'FORM_MODEL_1',
	shortname: {en: 'Form model 1'}
} satisfies FormModelDTO;

export const FORM = {
	scopePk: 2,
	scopeId: '75c432c5-e538-4252-9d97-4f95b6d7f494',
	scopeCodeAndShortname: 'AT-01-01',
	eventPk: 2,
	eventShortname: 'Event 6 months - 24.10.2020',
	pk: 1,
	model: FORM_MODEL,
	modelId: FORM_MODEL.id,
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	removed: false,
	inRemoved: false,
	inLocked: false,
	printButtonLabel: undefined,
	canWrite: false,
	canBeRemoved: false,
	workflowStatuses: [],
	possibleWorkflows: [],
	printable: false
} satisfies FormDTO;

export const USER = {
	pk: 1,
	creationTime: new Date(),
	lastUpdateTime: new Date(),
	removed: false,
	name: 'Test user',
	email: 'testuser@rodano.ch',
	externallyManaged: false,
	activated: true,
	canWrite: false,
	languageId: 'en',
	hasPassword: true,
	roles: [],
	rights: {
		readProfilesIds: [],
		writeProfilesIds: [],
		readScopeModelIds: [],
		writeScopeModelIds: [],
		canCreateUser: true
	},
	blocked: false
} satisfies UserDTO;
