/**
 * Rodano API
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { Operator } from './operator';
import { VisibilityCriterionAction } from './visibility-criterion-action';


export interface VisibilityCriteriaDTO { 
    operator: Operator;
    values: Array<string>;
    action: VisibilityCriterionAction;
    targetLayoutIds: Array<string>;
    targetCellIds: Array<string>;
}
export namespace VisibilityCriteriaDTO {
}


