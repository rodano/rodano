/**
 * Rodano API
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { ScopeDTO } from './scope-dto';
import { Paging } from './paging';


export interface PagedResultScopeDTO { 
    /**
     * Objects of the page
     */
    objects: Array<ScopeDTO>;
    /**
     * Paging information
     */
    paging: Paging;
}

