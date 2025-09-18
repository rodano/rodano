let token;

export const TokenManager = {
	SetToken: function(new_token) {
		token = new_token;
	},
	GetToken: function() {
		return token;
	}
};
