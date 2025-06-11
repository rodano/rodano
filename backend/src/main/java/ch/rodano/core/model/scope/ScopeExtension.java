package ch.rodano.core.model.scope;

public enum ScopeExtension {
	NONE {
		@Override
		public boolean isGoingUp() {
			return false;
		}

		@Override
		public boolean isGoingDown() {
			return false;
		}
	},
	DESCENDANTS {
		@Override
		public boolean isGoingUp() {
			return false;
		}

		@Override
		public boolean isGoingDown() {
			return true;
		}
	},
	ANCESTORS {
		@Override
		public boolean isGoingUp() {
			return true;
		}

		@Override
		public boolean isGoingDown() {
			return false;
		}
	},
	BRANCH {
		@Override
		public boolean isGoingUp() {
			return true;
		}

		@Override
		public boolean isGoingDown() {
			return true;
		}
	};

	public abstract boolean isGoingUp();

	public abstract boolean isGoingDown();
}
