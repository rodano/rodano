import {Event} from '../model/event';
import {Field} from '../model/field';
import {Form} from '../model/form';
import {Scope} from '../model/scope';

export type Workflowable = Scope | Event | Field | Form;
