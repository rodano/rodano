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
import { EnrollmentModel } from './enrollment-model';
import { SubscriptionRestriction } from './subscription-restriction';
import { EventConfigurationHook } from './event-configuration-hook';


export interface ScopeData { 
    description?: { [key: string]: string; };
    enrollmentModel?: EnrollmentModel;
    enrollmentStart?: Date;
    enrollmentStop?: Date;
    enrollmentTargets?: Array<EnrollmentTarget>;
    eventConfigurationHooks?: Array<EventConfigurationHook>;
    subscriptionRestrictions?: Array<SubscriptionRestriction>;
}

