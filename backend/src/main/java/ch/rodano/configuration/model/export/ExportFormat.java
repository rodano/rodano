package ch.rodano.configuration.model.export;

public enum ExportFormat {
	EXCEL {
		@Override
		public String getMimeType() {
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		}

		@Override
		public String getExtension() {
			return "xlsx";
		}
	},

	CSV {
		@Override
		public String getMimeType() {
			return "text/csv";
		}

		@Override
		public String getExtension() {
			return "csv";
		}
	},

	XML {
		@Override
		public String getMimeType() {
			return "text/xml";
		}

		@Override
		public String getExtension() {
			return "xml";
		}
	},

	ZIP {
		@Override
		public String getMimeType() {
			return "application/zip";
		}

		@Override
		public String getExtension() {
			return "zip";
		}
	},

	JSON {
		@Override
		public String getMimeType() {
			return "application/json";
		}

		@Override
		public String getExtension() {
			return "json";
		}
	},

	SQL {
		@Override
		public String getMimeType() {
			return "application/octet-stream";
		}

		@Override
		public String getExtension() {
			return "sql";
		}
	},

	PDF {
		@Override
		public String getMimeType() {
			return "application/pdf";
		}

		@Override
		public String getExtension() {
			return "pdf";
		}
	};

	public abstract String getMimeType();

	public abstract String getExtension();

}
