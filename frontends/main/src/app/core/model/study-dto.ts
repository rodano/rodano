/**
 * Rodano API
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { DatasetModelDTO } from './dataset-model-dto';
import { FormModelDTO } from './form-model-dto';
import { WorkflowDTO } from './workflow-dto';
import { LanguageDTO } from './language-dto';
import { ScopeModelDTO } from './scope-model-dto';
import { Environment } from './environment';
import { MenuDTO } from './menu-dto';
import { ProfileDTO } from './profile-dto';


export interface StudyDTO { 
    id: string;
    shortname: { [key: string]: string; };
    color: string;
    logo?: string;
    url: string;
    homePage: MenuDTO;
    eproEnabled: boolean;
    introductionText?: string;
    copyright?: string;
    activatedLanguages: Array<LanguageDTO>;
    defaultLanguage: LanguageDTO;
    environment: Environment;
    leafScopeModel: ScopeModelDTO;
    eproProfile?: ProfileDTO;
    email: string;
    clientEmail: string;
    welcomeText: string;
    configUser: string;
    configDate: Date;
    scopeModels: Array<ScopeModelDTO>;
    datasetModels: Array<DatasetModelDTO>;
    formModels: Array<FormModelDTO>;
    workflows: Array<WorkflowDTO>;
    profiles: Array<ProfileDTO>;
    menus: Array<MenuDTO>;
    rootScopeModelId: string;
    leafScopeId: string;
    leafScopeModelIds: Array<string>;
}
export namespace StudyDTO {
}


