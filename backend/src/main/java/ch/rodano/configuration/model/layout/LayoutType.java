package ch.rodano.configuration.model.layout;

public enum LayoutType {
	SINGLE {
		@Override
		public boolean isRepeatable() {
			return false;
		}

		@Override
		public boolean hasReferenceDataset() {
			return false;
		}
	},
	MULTIPLE {
		@Override
		public boolean isRepeatable() {
			return true;
		}

		@Override
		public boolean hasReferenceDataset() {
			return true;
		}
	};

	public abstract boolean isRepeatable();

	public abstract boolean hasReferenceDataset();
}
