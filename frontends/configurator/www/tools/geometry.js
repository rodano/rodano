export const Geometry = {
	Middle: function(x1, x2) {
		return Math.min(x1, x2) + Math.abs((x1 - x2) / 2);
	}
};
