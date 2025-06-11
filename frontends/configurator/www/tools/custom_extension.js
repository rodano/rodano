String.prototype.idify = function() {
	//upper case, replace space, dot and slash by underscores, delete everything which is not number, "simple" letter or underscores and reduce underscores
	return this.toUpperCase().replace(/ |\.|\//g,'_').replace(/[^A-Za-z0-9_]/g,'').replace(/_+/g,'_');
};
