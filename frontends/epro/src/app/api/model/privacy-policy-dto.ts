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


/**
 * Defines a privacy policy that is presented to users
 */
export interface PrivacyPolicyDTO { 
    /**
     * Privacy policy ID
     */
    id: string;
    shortname: { [key: string]: string; };
    longname: { [key: string]: string; };
    description?: { [key: string]: string; };
    /**
     * Privacy policy content
     */
    content: { [key: string]: string; };
    /**
     * Profiles affected by the privacy policy
     */
    profileIds: Set<string>;
}

