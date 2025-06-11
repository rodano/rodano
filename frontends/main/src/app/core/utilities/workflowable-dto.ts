import {EventDTO} from '../model/event-dto';
import {FieldDTO} from '../model/field-dto';
import {FormDTO} from '../model/form-dto';
import {ScopeDTO} from '../model/scope-dto';

export type WorkflowableDTO = ScopeDTO | EventDTO | FieldDTO | FormDTO;
