/**
 * Rodano API
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


/**
 * Used for scope relation creation and transfer
 */
export interface ScopeRelationCreationDTO { 
    /**
     * The parent PK
     */
    parentPk: number;
    /**
     * Start date of the relation
     */
    startDate: Date;
    /**
     * End date of the relation (optional)
     */
    endDate?: Date;
}

