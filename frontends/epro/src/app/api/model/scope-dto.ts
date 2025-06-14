/**
 * KV API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: DEV-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { EnrollmentTarget } from './enrollment-target';
import { WorkflowActionDTO } from './workflow-action-dto';
import { EnrollmentModel } from './enrollment-model';
import { ScopeModelDTO } from './scope-model-dto';
import { WorkflowStatusDTO } from './workflow-status-dto';
import { SubscriptionRestriction } from './subscription-restriction';


export interface ScopeDTO { 
    pk: number;
    id: string;
    code: string;
    shortname: string;
    longname?: string;
    modelId: string;
    virtual: boolean;
    startDate: Date;
    stopDate?: Date;
    model: ScopeModelDTO;
    root: boolean;
    creationTime: Date;
    lastUpdateTime: Date;
    removed: boolean;
    canWrite: boolean;
    canBeRemoved: boolean;
    description?: { [key: string]: string; };
    statusIcon?: string;
    expectedNumber?: number;
    maxNumber?: number;
    enrollmentModel?: EnrollmentModel;
    enrollmentTargets: Array<EnrollmentTarget>;
    locked: boolean;
    leaves: number;
    mainUserPk?: number;
    mainUserName?: string;
    subscriptionRestrictions: Array<SubscriptionRestriction>;
    defaultProfileId?: string;
    workflowStatus: Array<WorkflowStatusDTO>;
    workflowCreationActions: Array<WorkflowActionDTO>;
    label?: string;
}

