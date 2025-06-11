export function create_data_helpers(Data) {

	const DataHelpers = {};

	//some requirements to revive data
	DataHelpers.GetDataEntitiesConstructors = function(entity) {
		return Data.EntitiesProperties[entity];
	};

	DataHelpers.GetDataEntitiesProperties = function(entity) {
		return DataHelpers.GetConfigEntitiesConstructors(entity).getProperties();
	};

	return DataHelpers;
}
