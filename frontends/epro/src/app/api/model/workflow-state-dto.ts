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
import { WorkflowActionDTO } from './workflow-action-dto';


/**
 * Workflow state
 */
export interface WorkflowStateDTO { 
    id: string;
    shortname: { [key: string]: string; };
    longname: { [key: string]: string; };
    description: { [key: string]: string; };
    /**
     * ID of the associated workflow
     */
    workflowId: string;
    /**
     * Is the state hidden?
     */
    hidden: boolean;
    /**
     * Is the state important?
     */
    important: boolean;
    /**
     * Should the state message be displayed?
     */
    showMessage: boolean;
    /**
     * Possible actions relating to the state
     */
    possibleActions: Array<WorkflowActionDTO>;
    icon: string;
    color: string;
}

