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
import { RoleDTO } from './role-dto';


/**
 * Application actor
 */
export interface ActorDTO { 
    /**
     * Unique ID
     */
    pk: number;
    /**
     * Creation time
     */
    creationTime: Date;
    /**
     * Last update time
     */
    lastUpdateTime: Date;
    /**
     * Has the actor been removed?
     */
    removed: boolean;
    /**
     * Name
     */
    name: string;
    /**
     * Roles
     */
    roles: Array<RoleDTO>;
}

