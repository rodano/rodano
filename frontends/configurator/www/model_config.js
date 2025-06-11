import {create_config} from './model/config/entities_config.js';
import {create_config_helpers} from './model/config/config_helpers.js';

const Config = create_config();
const ConfigHelpers = create_config_helpers(Config);

export {Config, ConfigHelpers};
