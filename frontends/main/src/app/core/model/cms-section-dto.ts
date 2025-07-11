/**
 * Rodano API
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { CMSWidgetDTO } from './cms-widget-dto';


/**
 * A section contains labels and widgets
 */
export interface CMSSectionDTO { 
    /**
     * Unique ID of the section
     */
    id: string;
    /**
     * Section labels
     */
    labels: { [key: string]: string; };
    /**
     * Section widgets
     */
    widgets: Array<CMSWidgetDTO>;
}

