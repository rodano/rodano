package ch.rodano.core.model.payment;

public enum PaymentBatchStatus {
	NOT_PAID {
		@Override
		public String toString() {
			return "Open";
		}
	},
	CLOSED {
		@Override
		public String toString() {
			return "Closed";
		}
	},
	PRINTED {
		@Override
		public String toString() {
			return "Printed";
		}
	},
	PAID {
		@Override
		public String toString() {
			return "Paid";
		}
	}
}
