import {Entities} from './entities.js';

//rights
const Assignables = [
	Entities.Feature,
	Entities.ResourceCategory,
	Entities.TimelineGraph,
	Entities.Menu,
	Entities.Report
];

const RightAssignables = [
	Entities.ScopeModel,
	Entities.EventModel,
	Entities.DatasetModel,
	Entities.FormModel,
	Entities.Profile,
	Entities.PaymentPlan
];

const Attributables = [
	Entities.Workflow,
	Entities.Action
];

const ProfileRightAssignables = [
	Entities.Action
];

//rules
const Rulables = [
	Entities.EventModel,
	Entities.FormModel,
	Entities.DatasetModel,
	Entities.FieldModel,
	Entities.Workflow,
	Entities.Action
];

const Constrainables = [
	Entities.EventModel,
	Entities.FormModel,
	Entities.Layout,
	Entities.Cell
];

const Conditionnables = [
	Entities.FieldModel,
	Entities.Validator,
	Entities.Rule,
	Entities.FormModel,
	Entities.Layout,
	Entities.Cell
];

//other
const Templatables = [
	Entities.Study,
	Entities.DatasetModel,
	Entities.FieldModel,
	Entities.Workflow,
	Entities.FormModel
];

export {Assignables, RightAssignables, Attributables, ProfileRightAssignables, Rulables, Constrainables, Conditionnables, Templatables};
