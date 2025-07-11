/**
 * Rodano API
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { KVPair } from './kv-pair';
import { StatsInStateDTO } from './stats-in-state-dto';


export interface SchedulingSummaryDTO { 
    totalByEvent: { [key: string]: StatsInStateDTO; };
    eventsDue: { [key: string]: StatsInStateDTO; };
    eventsWorkflows: { [key: string]: { [key: string]: StatsInStateDTO; }; };
    rowsLabel: Array<KVPair>;
}

